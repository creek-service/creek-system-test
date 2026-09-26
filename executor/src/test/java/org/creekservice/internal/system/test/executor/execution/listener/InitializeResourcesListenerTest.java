/*
 * Copyright 2022-2026 Creek Contributors (https://github.com/creek-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.creekservice.internal.system.test.executor.execution.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.platform.resource.ResourceInitializer;
import org.creekservice.api.service.extension.component.model.ResourceHandler;
import org.creekservice.api.system.test.extension.component.definition.AggregateDefinition;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.internal.system.test.executor.api.SystemTest;
import org.creekservice.internal.system.test.executor.execution.input.Inputters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InitializeResourcesListenerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SystemTest api;

    @Mock private ResourceInitializer initializer;
    @Mock private Inputters inputters;
    @Mock private CreekTestSuite suite;
    @Mock private ServiceDefinition def0;
    @Mock private ServiceDefinition def1;
    @Mock private ServiceDefinition def2;
    @Mock private AggregateDefinition def3;
    @Mock private ServiceDescriptor desc0;
    @Mock private ServiceDescriptor desc1;
    @Mock private ServiceDescriptor desc2;
    @Mock private AggregateDescriptor desc3;
    private InitializeResourcesListener listener;

    @BeforeEach
    void setUp() {
        listener = new InitializeResourcesListener(api, initializer, inputters);

        when(suite.services()).thenReturn(List.of("duplicate", "service-1", "duplicate"));
        doReturn(List.of()).when(suite).seedData();
        when(inputters.resourceIds(any())).thenReturn(Set.of());

        when(api.components().definitions().services().get("duplicate")).thenReturn(def0);
        when(api.components().definitions().services().get("service-1")).thenReturn(def1);
        when(api.components().definitions().stream()).thenAnswer(inv -> Stream.of(def0, def1));

        when(def0.name()).thenReturn("duplicate");
        doReturn(Optional.of(desc0)).when(def0).descriptor();
        when(def1.name()).thenReturn("service-1");
        doReturn(Optional.of(desc1)).when(def1).descriptor();
        when(def2.name()).thenReturn("service-2");
        doReturn(Optional.of(desc2)).when(def2).descriptor();
        when(def3.name()).thenReturn("agg-3");
        doReturn(Optional.of(desc3)).when(def3).descriptor();
    }

    @Test
    void shouldRunInitForServicesUnderTest() {
        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(initializer).init(argThat(inAnyOrder(desc0, desc1)));
    }

    @Test
    void shouldRunTestForServicesUnderTest() {
        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(initializer).test(argThat(inAnyOrder(desc0, desc1)), eq(List.of()), eq(Set.of()));
    }

    @Test
    void shouldExcludeServicesUnderTestWithoutDescriptors() {
        // Given:
        when(def0.descriptor()).thenReturn(Optional.empty());

        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(initializer).init(List.of(desc1));
        verify(initializer).test(List.of(desc1), List.of(), Set.of());
    }

    @Test
    void shouldThrowOnUnknownService() {
        // Given:
        final RuntimeException expected = new RuntimeException("unknown service");
        when(api.components().definitions().services().get("service-1")).thenThrow(expected);

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> listener.beforeSuite(suite));

        // Then:
        assertThat(e, is(sameInstance(expected)));
    }

    @Test
    void shouldPassOtherComponentDescriptors() {
        // Given:
        when(api.components().definitions().stream())
                .thenAnswer(inv -> Stream.of(def0, def1, def2, def3));

        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(initializer)
                .test(argThat(inAnyOrder(desc0, desc1)), eq(List.of(desc2, desc3)), eq(Set.of()));
    }

    @Test
    void shouldPassOtherAggregateComponentDescriptorWithSameNameAsServiceUnderTest() {
        // Given:
        when(def3.name()).thenReturn("duplicate");
        when(api.components().definitions().stream())
                .thenAnswer(inv -> Stream.of(def0, def1, def3));

        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(initializer)
                .test(argThat(inAnyOrder(desc0, desc1)), eq(List.of(desc3)), eq(Set.of()));
    }

    @Test
    void shouldPassSeedResourceIdsFromSeedDataToTest() {
        // Given:
        final List<Input> seedData = List.of();
        final Set<URI> seedResourceIds = Set.of(URI.create("kafka-topic://default/foo"));
        doReturn(seedData).when(suite).seedData();
        when(inputters.resourceIds(seedData)).thenReturn(seedResourceIds);

        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(initializer)
                .test(argThat(inAnyOrder(desc0, desc1)), eq(List.of()), eq(seedResourceIds));
    }

    @Test
    void shouldNotPrepareAnythingIfNothingEnsured() {
        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(api.extensions().model(), never()).resourceHandler(any());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldPrepareResourceEnsuredByInit() {
        // Given:
        final TestResource resource = new TestResource(URI.create("test://shared"));
        when(desc0.resources()).thenAnswer(inv -> Stream.of(resource));
        when(initializer.init(any())).thenReturn(Set.of(resource.id()));

        final ResourceHandler handler = mock(ResourceHandler.class);
        when(api.extensions().model().resourceHandler(TestResource.class)).thenReturn(handler);

        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(handler).prepare(List.of(resource));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldPrepareResourceEnsuredByTest() {
        // Given:
        final TestResource resource = new TestResource(URI.create("test://seed-target"));
        when(desc0.resources()).thenAnswer(inv -> Stream.of(resource));
        when(initializer.test(any(), any(), any())).thenReturn(Set.of(resource.id()));

        final ResourceHandler handler = mock(ResourceHandler.class);
        when(api.extensions().model().resourceHandler(TestResource.class)).thenReturn(handler);

        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(handler).prepare(List.of(resource));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldPrepareUnionOfResourcesEnsuredByInitAndTest() {
        // Given:
        final TestResource sharedResource = new TestResource(URI.create("test://shared"));
        final TestResource seedResource = new TestResource(URI.create("test://seed-target"));
        when(desc0.resources()).thenAnswer(inv -> Stream.of(sharedResource, seedResource));
        when(initializer.init(any())).thenReturn(Set.of(sharedResource.id()));
        when(initializer.test(any(), any(), any())).thenReturn(Set.of(seedResource.id()));

        final ResourceHandler handler = mock(ResourceHandler.class);
        when(api.extensions().model().resourceHandler(TestResource.class)).thenReturn(handler);

        final ArgumentCaptor<Collection<TestResource>> captor =
                ArgumentCaptor.forClass(Collection.class);

        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(handler).prepare(captor.capture());
        assertThat(captor.getValue(), containsInAnyOrder(sharedResource, seedResource));
    }

    private static ArgumentMatcher<List<ServiceDescriptor>> inAnyOrder(
            final ServiceDescriptor... expected) {
        final List<ServiceDescriptor> expectedList = List.of(expected);
        return actual ->
                actual != null
                        && actual.size() == expectedList.size()
                        && actual.containsAll(expectedList);
    }

    private static final class TestResource implements ResourceDescriptor {
        private final URI id;

        private TestResource(final URI id) {
            this.id = id;
        }

        @Override
        public URI id() {
            return id;
        }
    }
}

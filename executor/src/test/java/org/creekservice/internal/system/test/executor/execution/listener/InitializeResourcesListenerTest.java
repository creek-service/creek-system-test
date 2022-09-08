/*
 * Copyright 2022 Creek Contributors (https://github.com/creek-service)
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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.platform.resource.ResourceInitializer;
import org.creekservice.api.system.test.extension.component.definition.AggregateDefinition;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.internal.system.test.executor.api.SystemTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
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
        listener = new InitializeResourcesListener(api, initializer);

        when(suite.services()).thenReturn(List.of("duplicate", "service-1", "duplicate"));

        when(api.component().definitions().service().get("duplicate")).thenReturn(def0);
        when(api.component().definitions().service().get("service-1")).thenReturn(def1);
        when(api.component().definitions().stream()).thenAnswer(inv -> Stream.of(def0, def1));

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
        initializer.init(List.of(desc0, desc1));
    }

    @Test
    void shouldRunTestForServicesUnderTest() {
        // When:
        listener.beforeSuite(suite);

        // Then:
        initializer.test(List.of(desc0, desc1), List.of());
    }

    @Test
    void shouldExcludeServicesUnderTestWithoutDescriptors() {
        // Given:
        when(def0.descriptor()).thenReturn(Optional.empty());

        // When:
        listener.beforeSuite(suite);

        // Then:
        initializer.init(List.of(desc1));
        initializer.test(List.of(desc1), List.of());
    }

    @Test
    void shouldThrowOnUnknownService() {
        // Given:
        final RuntimeException expected = new RuntimeException("unknown service");
        when(api.component().definitions().service().get("service-1")).thenThrow(expected);

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> listener.beforeSuite(suite));

        // Then:
        assertThat(e, is(sameInstance(expected)));
    }

    @Test
    void shouldPassOtherComponentDescriptors() {
        // Given:
        when(api.component().definitions().stream())
                .thenAnswer(inv -> Stream.of(def0, def1, def2, def3));

        // When:
        listener.beforeSuite(suite);

        // Then:
        initializer.test(List.of(desc0, desc1), List.of(desc3, desc2));
    }

    @Test
    void shouldPassOtherAggregateComponentDescriptorWithSameNameAsServiceUnderTest() {
        // Given:
        when(def3.name()).thenReturn("duplicate");
        when(api.component().definitions().stream()).thenAnswer(inv -> Stream.of(def0, def1, def3));

        // When:
        listener.beforeSuite(suite);

        // Then:
        initializer.test(List.of(desc0, desc1), List.of(desc3));
    }
}

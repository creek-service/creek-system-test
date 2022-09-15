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

package org.creekservice.internal.system.test.executor.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.testing.NullPointerTester;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.CreekService;
import org.creekservice.api.service.extension.component.model.ComponentModelCollection;
import org.creekservice.api.system.test.extension.component.definition.AggregateDefinition;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.internal.service.api.component.model.ComponentModel;
import org.creekservice.internal.system.test.executor.api.component.definition.ComponentDefinitions;
import org.creekservice.internal.system.test.executor.api.test.env.TestEnv;
import org.creekservice.internal.system.test.executor.api.test.model.TestModel;
import org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemTestTest {

    @Mock private TestModel testModel;
    @Mock private ComponentModel componentModel;
    @Mock private TestEnv testEnv;
    @Mock private ComponentDefinitions<ServiceDefinition> services;
    @Mock private ComponentDefinitions<AggregateDefinition> aggregates;
    @Mock private CreekExtensionProvider<CreekExtension> provider0;
    @Mock private CreekExtensionProvider<CreekExtension> provider1;
    @Captor private ArgumentCaptor<CreekService> serviceApiCaptor;
    private SystemTest api;

    @BeforeEach
    void setUp() {
        api = new SystemTest(testModel, testEnv, services, aggregates, componentModel);
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester =
                new NullPointerTester().setDefault(ServiceDebugInfo.class, ServiceDebugInfo.none());
        tester.testAllPublicConstructors(SystemTest.class);
        tester.testAllPublicStaticMethods(SystemTest.class);
        tester.testAllPublicInstanceMethods(api);
    }

    @Test
    void shouldExposeModel() {
        assertThat(api.tests().model(), is(sameInstance(testModel)));
    }

    @Test
    void shouldExposeTestEnv() {
        assertThat(api.tests().env(), is(sameInstance(testEnv)));
    }

    @Test
    void shouldExposeServiceDefinitions() {
        assertThat(api.components().definitions().services(), is(sameInstance(services)));
    }

    @Test
    void shouldExposeAggregateDefinitions() {
        assertThat(api.components().definitions().aggregates(), is(sameInstance(aggregates)));
    }

    @Test
    void shouldInitializeServiceExtensionWithSharedComponentModel() {
        // Given:
        final ComponentModelCollection sharedModel = api.extensions().model();

        // When:
        api.extensions().initialize(provider0);
        api.extensions().initialize(provider1);

        // Then:
        verify(provider0).initialize(serviceApiCaptor.capture());
        verify(provider1).initialize(serviceApiCaptor.capture());
        serviceApiCaptor
                .getAllValues()
                .forEach(
                        serviceApi ->
                                assertThat(
                                        serviceApi.components().model(),
                                        is(sameInstance(sharedModel))));
    }

    @Test
    void shouldReturnInitializedProvider() {
        // Given:
        final CreekExtension ext = mock(CreekExtension.class);
        when(provider0.initialize(any())).thenReturn(ext);

        // When:
        final CreekExtension result = api.extensions().initialize(provider0);

        // Then:
        assertThat(result, is(ext));
    }
}

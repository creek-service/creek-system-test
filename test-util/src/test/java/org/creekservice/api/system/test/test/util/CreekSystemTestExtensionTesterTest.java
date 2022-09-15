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

package org.creekservice.api.system.test.test.util;

import static org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo.serviceDebugInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstance;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstanceContainer;
import org.creekservice.internal.system.test.executor.api.component.definition.ComponentDefinitions;
import org.creekservice.internal.system.test.executor.api.test.env.suite.service.DockerServiceContainer;
import org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreekSystemTestExtensionTesterTest {

    @Mock(strictness = LENIENT)
    private ServiceDefinition serviceDef;

    private CreekSystemTestExtensionTester tester;

    @BeforeEach
    void setUp() {
        tester = CreekSystemTestExtensionTester.extensionTester();

        when(serviceDef.name()).thenReturn("bob");
        when(serviceDef.dockerImage())
                .thenReturn("ghcr.io/creekservice/creek-system-test-test-service");
    }

    @Test
    void shouldLoadExtensions() {
        assertThat(tester.accessibleExtensions(), is(empty()));
    }

    @Test
    void shouldExposeServiceDefinitions() {
        assertThat(tester.serviceDefinitions(), is(instanceOf(ComponentDefinitions.class)));
        assertThat(tester.serviceDefinitions().get("test-service"), is(notNullValue()));
    }

    @Test
    void shouldExposeAggregateDefinitions() {
        assertThat(tester.aggregateDefinitions(), is(instanceOf(ComponentDefinitions.class)));
        assertThat(tester.aggregateDefinitions().get("test-agg"), is(notNullValue()));
    }

    @Test
    void shouldExposeDockerBasedServicesContainer() {
        assertThat(tester.dockerServicesContainer(), is(instanceOf(DockerServiceContainer.class)));
    }

    @Test
    void shouldNotReturnContainerIdIfNonRunning() {
        // Given:
        tester.dockerServicesContainer().add(serviceDef);

        // When:
        final Map<String, String> result = tester.runningContainerIds();

        // Then:
        assertThat(result.entrySet(), is(empty()));
    }

    @Test
    void shouldReturnRunningContainerIds() {
        // Given:
        final ServiceInstanceContainer services = tester.dockerServicesContainer();
        final ServiceInstance instance = services.add(serviceDef);
        instance.start();

        try {

            // When:
            final Map<String, String> result = tester.runningContainerIds();

            // Then:
            assertThat(result.entrySet(), hasSize(1));
            assertThat(result, hasKey(instance.name()));
            assertThat(result.get(instance.name()), is(not(blankOrNullString())));
        } finally {
            instance.stop();
        }
    }

    @Test
    void shouldClear() {
        // Given:
        final ServiceInstanceContainer services = tester.dockerServicesContainer();
        services.add(serviceDef);
        assertThat(services.iterator().hasNext(), is(true));

        // When:
        tester.clearServices();

        // Then:
        assertThat(services.iterator().hasNext(), is(false));
    }

    @Test
    void shouldDefaultToNoServicesBeingDebug() {
        assertThat(
                ((DockerServiceContainer) tester.dockerServicesContainer()).serviceDebugInfo(),
                is(ServiceDebugInfo.none()));
    }

    @Test
    void shouldSupportConfiguringServicesForDebugging() {
        // Given:
        final ServiceDebugInfo debugServiceInfo =
                serviceDebugInfo(123, 321, Set.of("a"), Set.of("b"));
        tester = tester.withDebugServices(debugServiceInfo);

        // Then:
        assertThat(
                ((DockerServiceContainer) tester.dockerServicesContainer()).serviceDebugInfo(),
                is(debugServiceInfo));
    }
}

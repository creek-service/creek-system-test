/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.env.suite.service.ConfigurableServiceInstance;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.internal.system.test.executor.api.SystemTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddServicesUnderTestListenerTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private SystemTest api;

    @Mock private CreekTestSuite suite;
    private AddServicesUnderTestListener listener;
    private final Map<String, ServiceDefinition> defs = new HashMap<>();
    private final Map<String, ConfigurableServiceInstance> instances = new HashMap<>();

    @BeforeEach
    void setUp() {
        defs.clear();
        instances.clear();

        listener = new AddServicesUnderTestListener(api);
    }

    @Test
    void shouldAddServicesBeforeSuite() {
        // Given:
        givenSuiteHasServices("a", "b", "c");

        // When:
        listener.beforeSuite(suite);

        // Then:
        final InOrder inOrder =
                inOrder(
                        api.components().definitions().services(),
                        api.tests().env().currentSuite().services());
        inOrder.verify(api.components().definitions().services()).get("a");
        inOrder.verify(api.tests().env().currentSuite().services()).add(defs.get("a"));
        inOrder.verify(api.components().definitions().services()).get("b");
        inOrder.verify(api.tests().env().currentSuite().services()).add(defs.get("b"));
        inOrder.verify(api.components().definitions().services()).get("c");
        inOrder.verify(api.tests().env().currentSuite().services()).add(defs.get("c"));
    }

    @Test
    void shouldConfigureStartUpLogMessage() {
        // Given:
        givenSuiteHasServices("a");

        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(instances.get("a:0"))
                .setStartupLogMessage(".*\\Qcreek.lifecycle.service.started\\E.*", 1);
    }

    @Test
    void shouldTrackAddedInstances() {
        // Given:
        givenSuiteHasServices("a", "b", "c");

        // When:
        listener.beforeSuite(suite);

        // Then:
        assertThat(
                listener.added(),
                contains(instances.get("a:0"), instances.get("b:0"), instances.get("c:0")));
    }

    @Test
    void shouldSupportMultipleServicesWithSameName() {
        // Given:
        givenSuiteHasServices("a", "a");

        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(api.tests().env().currentSuite().services(), times(2)).add(defs.get("a"));
        assertThat(listener.added(), contains(instances.get("a:0"), instances.get("a:1")));
    }

    @Test
    void shouldThrowOnUnknownService() {
        // Given:
        when(suite.services()).thenReturn(List.of("misspelled-service"));
        when(api.components().definitions().services().get(any()))
                .thenThrow(new RuntimeException("unknown service"));

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> listener.beforeSuite(suite));

        // Then:
        assertThat(e.getMessage(), is("unknown service"));
    }

    private void givenSuiteHasServices(final String... serviceNames) {
        when(suite.services()).thenReturn(List.of(serviceNames));

        Arrays.stream(serviceNames)
                .collect(Collectors.groupingBy(Function.identity()))
                .values()
                .forEach(this::setupServiceMocks);
    }

    private void setupServiceMocks(final List<String> serviceNames) {
        final String serviceName = serviceNames.get(0);
        final ServiceDefinition def = setUpDefMock(serviceName);

        ConfigurableServiceInstance first = null;
        final ConfigurableServiceInstance[] others =
                new ConfigurableServiceInstance[serviceNames.size() - 1];

        for (int i = 0; i != serviceNames.size(); ++i) {
            final String instanceName = serviceName + ":" + i;
            final ConfigurableServiceInstance instance =
                    mock(
                            ConfigurableServiceInstance.class,
                            withSettings().name(instanceName).defaultAnswer(RETURNS_DEEP_STUBS));

            instances.put(instanceName, instance);

            if (i == 0) {
                first = instance;
            } else {
                others[i - 1] = instance;
            }
        }

        when(api.tests().env().currentSuite().services().add(def)).thenReturn(first, others);
    }

    private ServiceDefinition setUpDefMock(final String serviceName) {
        return defs.computeIfAbsent(
                serviceName,
                name -> {
                    final ServiceDefinition def = mock(ServiceDefinition.class, serviceName);
                    when(api.components().definitions().services().get(name)).thenReturn(def);
                    return def;
                });
    }
}

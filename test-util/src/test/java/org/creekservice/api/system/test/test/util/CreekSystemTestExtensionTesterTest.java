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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.when;

import org.creekservice.api.system.test.extension.service.ServiceContainer;
import org.creekservice.api.system.test.extension.service.ServiceDefinition;
import org.creekservice.internal.system.test.executor.api.testsuite.service.DockerServiceContainer;
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
        when(serviceDef.dockerImage()).thenReturn("bob-service");
    }

    @Test
    void shouldLoadExtensions() {
        assertThat(tester.accessibleExtensions(), is(empty()));
    }

    @Test
    void shouldExposeDockerBasedServicesContainer() {
        assertThat(tester.dockerServicesContainer(), is(instanceOf(DockerServiceContainer.class)));
    }

    @Test
    void shouldClear() {
        // Given:
        final ServiceContainer services = tester.dockerServicesContainer();
        services.add(serviceDef);
        assertThat(services.iterator().hasNext(), is(true));

        // When:
        tester.clearServices();

        // Then:
        assertThat(services.iterator().hasNext(), is(false));
    }
}

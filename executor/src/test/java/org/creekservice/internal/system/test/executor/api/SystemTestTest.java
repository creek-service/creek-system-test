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
import static org.mockito.Mockito.verify;

import java.util.List;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemTestTest {

    @Mock private CreekTestExtension ext1;
    @Mock private CreekTestExtension ext2;
    @Mock private Model model;
    @Mock private TestSuiteEnv testEnv;
    @Mock private ServiceDefinitions services;
    private SystemTest api;
    @Captor private ArgumentCaptor<SystemTest> apiCapture;

    @BeforeEach
    void setUp() {
        api = new SystemTest(List.of(), model, testEnv, services);
    }

    @Test
    void shouldExposeModel() {
        assertThat(api.model(), is(sameInstance(model)));
    }

    @Test
    void shouldExposeModelToExtensions() {
        // When:
        api = new SystemTest(List.of(ext1, ext2), model, testEnv, services);

        // Then:
        verify(ext1).initialize(apiCapture.capture());
        assertThat(apiCapture.getValue().model(), is(sameInstance(model)));

        verify(ext2).initialize(apiCapture.capture());
        assertThat(apiCapture.getValue().model(), is(sameInstance(model)));
    }

    @Test
    void shouldExposeTestEnv() {
        assertThat(api.testSuite(), is(sameInstance(testEnv)));
    }

    @Test
    void shouldExposeTestEnvToExtensions() {
        // When:
        api = new SystemTest(List.of(ext1, ext2), model, testEnv, services);

        // Then:
        verify(ext1).initialize(apiCapture.capture());
        assertThat(apiCapture.getValue().testSuite(), is(sameInstance(testEnv)));

        verify(ext2).initialize(apiCapture.capture());
        assertThat(apiCapture.getValue().testSuite(), is(sameInstance(testEnv)));
    }

    @Test
    void shouldExposeServiceRegistry() {
        assertThat(api.services(), is(sameInstance(services)));
    }

    @Test
    void shouldExposeServiceRegistryToExtensions() {
        // When:
        api = new SystemTest(List.of(ext1, ext2), model, testEnv, services);

        // Then:
        verify(ext1).initialize(apiCapture.capture());
        assertThat(apiCapture.getValue().services(), is(sameInstance(services)));

        verify(ext2).initialize(apiCapture.capture());
        assertThat(apiCapture.getValue().services(), is(sameInstance(services)));
    }
}

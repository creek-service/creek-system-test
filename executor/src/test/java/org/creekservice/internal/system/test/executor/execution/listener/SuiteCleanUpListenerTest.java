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

import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;
import org.creekservice.api.system.test.extension.test.env.suite.service.ConfigurableServiceInstance;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstance;
import org.creekservice.internal.system.test.executor.api.SystemTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SuiteCleanUpListenerTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private SystemTest api;

    private SuiteCleanUpListener listener;
    @Mock private ConfigurableServiceInstance service;
    @Captor private ArgumentCaptor<Consumer<? super ServiceInstance>> actionCaptor;

    @BeforeEach
    void setUp() {
        listener = new SuiteCleanUpListener(api);
    }

    @Test
    void shouldClearTheServicesContainerBeforeSuite() {
        // When:
        listener.beforeSuite(null);

        // Then:
        verify(api.tests().env().currentSuite().services()).clear();
    }

    @Test
    void shouldStopAllServicesAfterSuite() {
        // When:
        listener.afterSuite(null);

        // Then:
        verify(api.tests().env().currentSuite().services()).forEach(actionCaptor.capture());
        actionCaptor.getValue().accept(service);
        verify(service).stop();
    }
}

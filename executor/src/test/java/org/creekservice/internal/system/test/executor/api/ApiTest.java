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

package org.creekservice.internal.system.test.executor.api;

import static org.creekservice.internal.system.test.executor.api.Api.initializeApi;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.internal.system.test.executor.api.test.env.suite.service.ContainerFactory;
import org.creekservice.internal.system.test.executor.execution.listener.AddServicesUnderTestListener;
import org.creekservice.internal.system.test.executor.execution.listener.InitializeResourcesListener;
import org.creekservice.internal.system.test.executor.execution.listener.PrepareResourcesListener;
import org.creekservice.internal.system.test.executor.execution.listener.StartServicesUnderTestListener;
import org.creekservice.internal.system.test.executor.execution.listener.SuiteCleanUpListener;
import org.creekservice.internal.system.test.executor.observation.LoggingTestEnvironmentListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SystemTest api;

    @Mock private ContainerFactory containerFactory;
    @Mock private CreekTestExtension ext0;
    @Mock private CreekTestExtension ext1;

    @Test
    void shouldAddLoggingTestLifecycleListener() {
        // When:
        initializeApi(api, containerFactory, List.of());

        // Then:
        verify(api.tests().env().listeners()).append(isA(LoggingTestEnvironmentListener.class));
    }

    @Test
    void shouldAddContainerFactoryListener() {
        // When:
        initializeApi(api, containerFactory, List.of());

        // Then:
        final InOrder inOrder = inOrder(api.tests().env().listeners());
        inOrder.verify(api.tests().env().listeners())
                .append(isA(LoggingTestEnvironmentListener.class));
        inOrder.verify(api.tests().env().listeners()).append(containerFactory);
        inOrder.verify(api.tests().env().listeners()).append(isA(SuiteCleanUpListener.class));
    }

    @Test
    void shouldAddSuiteCleanUpListener() {
        // When:
        initializeApi(api, containerFactory, List.of());

        // Then:
        final InOrder inOrder = inOrder(api.tests().env().listeners());
        inOrder.verify(api.tests().env().listeners()).append(containerFactory);
        inOrder.verify(api.tests().env().listeners()).append(isA(SuiteCleanUpListener.class));
        inOrder.verify(api.tests().env().listeners())
                .append(isA(AddServicesUnderTestListener.class));
    }

    @Test
    void shouldAddAddServicesUnderTestListener() {
        // When:
        initializeApi(api, containerFactory, List.of(ext0));

        // Then:
        final InOrder inOrder = inOrder(api.tests().env().listeners(), ext0);
        inOrder.verify(api.tests().env().listeners()).append(isA(SuiteCleanUpListener.class));
        inOrder.verify(api.tests().env().listeners())
                .append(isA(AddServicesUnderTestListener.class));
        inOrder.verify(ext0).initialize(api);
    }

    @Test
    void shouldAddInitializeResourcesListener() {
        // When:
        initializeApi(api, containerFactory, List.of(ext0));

        // Then:
        final InOrder inOrder = inOrder(api.tests().env().listeners(), ext0);
        inOrder.verify(ext0).initialize(api);
        inOrder.verify(api.tests().env().listeners())
                .append(isA(InitializeResourcesListener.class));
        inOrder.verify(api.tests().env().listeners()).append(isA(PrepareResourcesListener.class));
    }

    @Test
    void shouldAddPrepareResourcesListener() {
        // When:
        initializeApi(api, containerFactory, List.of(ext0));

        // Then:
        final InOrder inOrder = inOrder(api.tests().env().listeners(), ext0);
        inOrder.verify(ext0).initialize(api);
        inOrder.verify(api.tests().env().listeners()).append(isA(PrepareResourcesListener.class));
        inOrder.verify(api.tests().env().listeners())
                .append(isA(StartServicesUnderTestListener.class));
    }

    @Test
    void shouldInitializeTestExtensions() {
        // When:
        initializeApi(api, containerFactory, List.of(ext0, ext1));

        // Then:
        final InOrder inOrder = inOrder(api.tests().env().listeners(), ext0, ext1);
        inOrder.verify(api.tests().env().listeners())
                .append(isA(AddServicesUnderTestListener.class));
        inOrder.verify(ext0).initialize(api);
        inOrder.verify(ext1).initialize(api);
        inOrder.verify(api.tests().env().listeners())
                .append(isA(InitializeResourcesListener.class));
    }

    @Test
    void shouldAddStartServicesUnderTestListener() {
        // When:
        initializeApi(api, containerFactory, List.of(ext0));

        // Then:
        final InOrder inOrder = inOrder(api.tests().env().listeners(), ext0);
        inOrder.verify(ext0).initialize(api);
        inOrder.verify(api.tests().env().listeners())
                .append(isA(StartServicesUnderTestListener.class));
    }
}

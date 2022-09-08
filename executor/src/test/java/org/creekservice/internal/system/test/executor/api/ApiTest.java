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

import static org.creekservice.internal.system.test.executor.api.Api.initializeApi;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.internal.system.test.executor.execution.listener.AddServicesUnderTestListener;
import org.creekservice.internal.system.test.executor.execution.listener.InitializeResourcesListener;
import org.creekservice.internal.system.test.executor.execution.listener.StartServicesUnderTestListener;
import org.creekservice.internal.system.test.executor.execution.listener.SuiteCleanUpListener;
import org.creekservice.internal.system.test.executor.observation.LoggingTestLifecycleListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SystemTest api;

    @Mock private CreekTestExtension ext0;
    @Mock private CreekTestExtension ext1;

    @Test
    void shouldAddLoggingTestLifecycleListener() {
        // When:
        initializeApi(api, List.of());

        // Then:
        verify(api.test().suite().listener()).append(isA(LoggingTestLifecycleListener.class));
    }

    @Test
    void shouldAddSuiteCleanUpListener() {
        // When:
        initializeApi(api, List.of());

        // Then:
        final InOrder inOrder = inOrder(api.test().suite().listener());
        inOrder.verify(api.test().suite().listener())
                .append(isA(LoggingTestLifecycleListener.class));
        inOrder.verify(api.test().suite().listener()).append(isA(SuiteCleanUpListener.class));
        inOrder.verify(api.test().suite().listener())
                .append(isA(AddServicesUnderTestListener.class));
    }

    @Test
    void shouldAddAddServicesUnderTestListener() {
        // When:
        initializeApi(api, List.of());

        // Then:
        final InOrder inOrder = inOrder(api.test().suite().listener());
        inOrder.verify(api.test().suite().listener()).append(isA(SuiteCleanUpListener.class));
        inOrder.verify(api.test().suite().listener())
                .append(isA(AddServicesUnderTestListener.class));
        inOrder.verify(api.test().suite().listener())
                .append(isA(InitializeResourcesListener.class));
    }

    @Test
    void shouldAddInitializeResourcesListener() {
        // When:
        initializeApi(api, List.of(ext0));

        // Then:
        final InOrder inOrder = inOrder(api.test().suite().listener(), ext0);
        inOrder.verify(api.test().suite().listener())
                .append(isA(AddServicesUnderTestListener.class));
        inOrder.verify(api.test().suite().listener())
                .append(isA(InitializeResourcesListener.class));
        inOrder.verify(ext0).initialize(api);
    }

    @Test
    void shouldInitializeTestExtensions() {
        // When:
        initializeApi(api, List.of(ext0, ext1));

        // Then:
        final InOrder inOrder = inOrder(api.test().suite().listener(), ext0, ext1);
        inOrder.verify(api.test().suite().listener())
                .append(isA(InitializeResourcesListener.class));
        inOrder.verify(ext0).initialize(api);
        inOrder.verify(ext1).initialize(api);
        inOrder.verify(api.test().suite().listener())
                .append(isA(StartServicesUnderTestListener.class));
    }

    @Test
    void shouldPassInitializingExtensionsToApi() {
        // When:
        initializeApi(api, List.of(ext0, ext1));

        // Then:
        final InOrder inOrder = Mockito.inOrder(api.component().model());
        inOrder.verify(api.component().model()).initializing(Optional.of(ext0));
        inOrder.verify(api.component().model()).initializing(Optional.empty());
        inOrder.verify(api.component().model()).initializing(Optional.of(ext1));
        inOrder.verify(api.component().model()).initializing(Optional.empty());
    }

    @Test
    void shouldClearInitializingExtensionOnException() {
        // Given:
        final RuntimeException expected = new RuntimeException("boom");
        doThrow(expected).when(ext0).initialize(any());

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> initializeApi(api, List.of(ext0, ext1)));

        // Then:
        final InOrder inOrder = Mockito.inOrder(api.component().model());
        inOrder.verify(api.component().model()).initializing(Optional.of(ext0));
        inOrder.verify(api.component().model()).initializing(Optional.empty());
        assertThat(e, is(expected));
    }

    @Test
    void shouldAddStartServicesUnderTestListener() {
        // When:
        initializeApi(api, List.of(ext0));

        // Then:
        final InOrder inOrder = inOrder(api.test().suite().listener(), ext0);
        inOrder.verify(ext0).initialize(api);
        inOrder.verify(api.test().suite().listener())
                .append(isA(StartServicesUnderTestListener.class));
    }
}

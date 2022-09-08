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

package org.creekservice.internal.system.test.executor.execution;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;
import org.creekservice.api.system.test.extension.test.suite.TestLifecycleListener;
import org.creekservice.api.system.test.extension.test.suite.TestListenerCollection;
import org.creekservice.api.system.test.model.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestCaseExecutorTest {

    @Mock private TestListenerCollection listeners;
    @Mock private TestCase testCase;
    @Mock private TestLifecycleListener listener;
    @Captor private ArgumentCaptor<Consumer<TestLifecycleListener>> actionCaptor;
    private TestCaseExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new TestCaseExecutor(listeners);
    }

    @Test
    void shouldInvokeListenersBeforeTest() {
        // When:
        executor.executeTest(testCase);

        // Then:
        verify(listeners).forEach(actionCaptor.capture());
        actionCaptor.getValue().accept(listener);
        verify(listener).beforeTest(testCase);
    }

    @Test
    void shouldInvokeListenersAfterTest() {
        // When:
        executor.executeTest(testCase);

        // Then:
        verify(listeners).forEachReverse(actionCaptor.capture());
        actionCaptor.getValue().accept(listener);
        verify(listener).afterTest(testCase);
    }

    @Test
    void shouldInvokeAfterTestListenersOnBeforeTestException() {
        // Given:
        final RuntimeException expected = new RuntimeException("Boom");
        doThrow(expected).when(listeners).forEach(any());

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> executor.executeTest(testCase));

        // Then:
        verify(listeners).forEachReverse(any());
        assertThat(e, is(sameInstance(expected)));
    }

    @Test
    void shouldInvokeListenersBeforeAndAfter() {
        // When:
        executor.executeTest(testCase);

        // Then:
        final InOrder inOrder = inOrder(listeners);
        inOrder.verify(listeners).forEach(any());
        inOrder.verify(listeners).forEachReverse(any());
    }
}

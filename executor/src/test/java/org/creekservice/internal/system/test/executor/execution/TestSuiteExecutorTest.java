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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.env.listener.TestListenerCollection;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;
import org.creekservice.api.system.test.model.TestCase;
import org.creekservice.api.system.test.model.TestSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestSuiteExecutorTest {

    @Mock private TestListenerCollection listeners;
    @Mock private TestCaseExecutor testExecutor;
    @Mock private TestSuite testSuite;
    @Mock private TestCase testCase0;
    @Mock private TestCase testCase1;
    @Mock private TestEnvironmentListener listener;
    @Captor private ArgumentCaptor<Consumer<TestEnvironmentListener>> actionCaptor;
    private TestSuiteExecutor suiteExecutor;

    @BeforeEach
    void setUp() {
        suiteExecutor = new TestSuiteExecutor(listeners, testExecutor);

        when(testCase0.name()).thenReturn("test0");
        when(testCase0.suite()).thenReturn(testSuite);
        when(testCase1.name()).thenReturn("test1");
        when(testCase1.suite()).thenReturn(testSuite);
        when(testSuite.name()).thenReturn("suite");
    }

    @Test
    void shouldInvokeListenersBeforeSuite() {
        // When:
        suiteExecutor.executeSuite(testSuite);

        // Then:
        verify(listeners).forEach(actionCaptor.capture());
        actionCaptor.getValue().accept(listener);
        verify(listener).beforeSuite(testSuite);
    }

    @Test
    void shouldInvokeListenersAfterSuite() {
        // When:
        suiteExecutor.executeSuite(testSuite);

        // Then:
        verify(listeners).forEachReverse(actionCaptor.capture());
        actionCaptor.getValue().accept(listener);
        verify(listener).afterSuite(eq(testSuite), isA(TestSuiteResult.class));
    }

    @Test
    void shouldInvokeAfterSuiteListenersOnBeforeSuiteException() {
        // Given:
        final RuntimeException expected = new RuntimeException("Boom");
        doThrow(expected).when(listeners).forEach(any());

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> suiteExecutor.executeSuite(testSuite));

        // Then:
        verify(listeners).forEachReverse(any());
        assertThat(e, is(sameInstance(expected)));
    }

    @Test
    void shouldInvokeAfterSuiteListenersOnTestExecuteException() {
        // Given:
        final RuntimeException expected = new RuntimeException("Boom");
        doThrow(expected).when(testExecutor).executeTest(any());
        givenTestCase(testCase0);

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> suiteExecutor.executeSuite(testSuite));

        // Then:
        verify(listeners).forEachReverse(any());
        assertThat(e, is(sameInstance(expected)));
    }

    @Test
    void shouldInvokeListenersBeforeAndAfter() {
        // Given:
        givenTestCase(testCase0);

        // When:
        suiteExecutor.executeSuite(testSuite);

        // Then:
        final InOrder inOrder = inOrder(listeners, testExecutor);
        inOrder.verify(listeners).forEach(any());
        inOrder.verify(testExecutor).executeTest(testCase0);
        inOrder.verify(listeners).forEachReverse(any());
    }

    @Test
    void shouldInvokeTestExecutor() {
        // Given:
        givenTestCase(testCase0, testCase1);

        // When:
        suiteExecutor.executeSuite(testSuite);

        // Then:
        final InOrder inOrder = inOrder(testExecutor);
        inOrder.verify(testExecutor).executeTest(testCase0);
        inOrder.verify(testExecutor).executeTest(testCase1);
    }

    private void givenTestCase(final TestCase... tests) {
        when(testSuite.tests()).thenReturn(List.of(tests));
    }
}

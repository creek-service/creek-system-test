/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.env.listener.TestListenerCollection;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.api.system.test.model.TestCase;
import org.creekservice.api.system.test.model.TestSuite;
import org.creekservice.internal.system.test.executor.execution.input.Inputters;
import org.creekservice.internal.system.test.executor.result.CaseResult;
import org.creekservice.internal.system.test.executor.result.SuiteResult;
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
    @Mock private Inputters inputters;
    @Mock private TestCaseExecutor testExecutor;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private TestSuite testSuite;

    @Mock private TestCase testCase0;
    @Mock private TestCase testCase1;
    @Mock private TestEnvironmentListener listener;
    @Mock private CaseResult testResult;
    @Captor private ArgumentCaptor<Consumer<TestEnvironmentListener>> actionCaptor;
    private TestSuiteExecutor suiteExecutor;

    @BeforeEach
    void setUp() {
        suiteExecutor = new TestSuiteExecutor(listeners, inputters, testExecutor);

        when(testCase0.name()).thenReturn("test0");
        when(testCase0.suite()).thenReturn(testSuite);
        when(testCase1.name()).thenReturn("test1");
        when(testCase1.suite()).thenReturn(testSuite);
        when(testSuite.name()).thenReturn("Fred");

        when(testExecutor.executeTest(any())).thenReturn(testResult);
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
        final SuiteResult result = suiteExecutor.executeSuite(testSuite);

        // Then:
        assertAfterTestCalled(result);
    }

    @Test
    void shouldInvokeListenersBeforeAndAfter() {
        // Given:
        givenTestCase(testCase0);

        // When:
        suiteExecutor.executeSuite(testSuite);

        // Then:
        final InOrder inOrder = inOrder(listeners, inputters, testExecutor);
        inOrder.verify(listeners).forEach(any());
        inOrder.verify(inputters).input(any(), any());
        inOrder.verify(testExecutor).executeTest(testCase0);
        inOrder.verify(listeners).forEachReverse(any());
    }

    @Test
    void shouldSeed() {
        // Given:
        final Input seed = mock(Input.class);
        when(testSuite.pkg().seedData()).thenReturn(List.of(seed));

        // When:
        suiteExecutor.executeSuite(testSuite);

        // Then:
        verify(inputters).input(List.of(seed), testSuite);
    }

    @Test
    void shouldInvokeTestExecutor() {
        // Given:
        givenTestCase(testCase0, testCase1);

        // When:
        final SuiteResult result = suiteExecutor.executeSuite(testSuite);

        // Then:
        final InOrder inOrder = inOrder(testExecutor);
        inOrder.verify(testExecutor).executeTest(testCase0);
        inOrder.verify(testExecutor).executeTest(testCase1);
        assertAfterTestCalled(result);
    }

    @Test
    void shouldHandleBeforeSuiteListenersThrowing() {
        // Given:
        final RuntimeException cause = new RuntimeException("boom");
        doThrow(cause).doNothing().when(listeners).forEach(any());

        givenTestCase(testCase0, testCase1);
        when(testCase1.disabled()).thenReturn(true);

        // When:
        final SuiteResult result = suiteExecutor.executeSuite(testSuite);

        // Then:
        assertThat(result.errors(), is(2L));
        assertThat(result.testResults(), is(empty()));
        assertThat(
                result.error().map(Exception::getMessage),
                is(Optional.of("Suite setup failed for test suite: Fred, cause: boom")));

        assertAfterTestCalled(result);
    }

    @Test
    void shouldHandleSeedingThrowing() {
        // Given:
        final RuntimeException cause = new RuntimeException("boom");
        doThrow(cause).when(inputters).input(any(), any());

        givenTestCase(testCase0);

        // When:
        final SuiteResult result = suiteExecutor.executeSuite(testSuite);

        // Then:
        assertThat(result.errors(), is(1L));
        assertThat(result.testResults(), is(empty()));
        assertThat(
                result.error().map(Exception::getMessage),
                is(Optional.of("Suite setup failed for test suite: Fred, cause: boom")));
        assertThat(result.error().map(Exception::getCause), is(Optional.of(cause)));

        assertAfterTestCalled(result);
    }

    @Test
    void shouldThrowIfCaseExecutorThrows() {
        // Given:
        final RuntimeException cause = new RuntimeException("boom");
        doThrow(cause).when(testExecutor).executeTest(any());

        givenTestCase(testCase0, testCase1);

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> suiteExecutor.executeSuite(testSuite));

        // Then:
        assertThat(e.getMessage(), is("Suite execution failed for test suite: Fred, cause: boom"));
        assertThat(e.getCause(), is(cause));
    }

    @Test
    void shouldThrowIfAfterSuiteListenersThrow() {
        // Given:
        final RuntimeException cause = new RuntimeException("boom");
        doThrow(cause).when(listeners).forEachReverse(any());

        givenTestCase(testCase0, testCase1);

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> suiteExecutor.executeSuite(testSuite));

        // Then:
        assertThat(e.getMessage(), is("Suite teardown failed for test suite: Fred, cause: boom"));
        assertThat(e.getCause(), is(cause));
    }

    private void givenTestCase(final TestCase... tests) {
        when(testSuite.tests()).thenReturn(List.of(tests));
    }

    private void assertAfterTestCalled(final SuiteResult result) {
        verify(listeners).forEachReverse(actionCaptor.capture());
        actionCaptor.getValue().accept(listener);
        verify(listener).afterSuite(testSuite, result);
    }
}

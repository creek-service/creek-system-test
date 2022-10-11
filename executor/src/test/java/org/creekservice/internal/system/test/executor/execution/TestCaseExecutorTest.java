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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.env.listener.TestListenerCollection;
import org.creekservice.api.system.test.extension.test.model.Expectation;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler.Verifier;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.api.system.test.model.TestCase;
import org.creekservice.api.system.test.model.TestSuite;
import org.creekservice.internal.system.test.executor.execution.expectation.Verifiers;
import org.creekservice.internal.system.test.executor.execution.input.Inputters;
import org.creekservice.internal.system.test.executor.result.CaseResult;
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
class TestCaseExecutorTest {

    @Mock private TestListenerCollection listeners;
    @Mock private Inputters inputters;
    @Mock private Verifiers verifiers;
    @Mock private TestSuite testSuite;
    @Mock private TestCase testCase;
    @Mock private TestEnvironmentListener listener;
    @Mock private List<? extends Input> inputs;
    @Mock private List<? extends Expectation> expectations;
    @Mock private Verifier verifier;
    @Captor private ArgumentCaptor<Consumer<TestEnvironmentListener>> actionCaptor;
    private TestCaseExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new TestCaseExecutor(listeners, inputters, verifiers);

        when(testCase.name()).thenReturn("Fred");
        when(testCase.suite()).thenReturn(testSuite);
        doReturn(inputs).when(testCase).inputs();
        doReturn(expectations).when(testCase).expectations();
        doReturn(verifier).when(verifiers).prepare(expectations, testSuite);
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
        final CaseResult result = executor.executeTest(testCase);

        // Then:
        assertAfterTestCalled(result);
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

    @Test
    void shouldHandleDisabledTest() {
        // Given:
        when(testCase.disabled()).thenReturn(true);

        // When:
        final CaseResult result = executor.executeTest(testCase);

        // Then:
        assertThat(result.skipped(), is(true));
        assertAfterTestCalled(result);
    }

    @Test
    void shouldHandleListenersBeforeTestThrowing() {
        // Given:
        final RuntimeException cause = new RuntimeException("boom");
        doThrow(cause).when(listeners).forEach(any());

        // When:
        final CaseResult result = executor.executeTest(testCase);

        // Then:
        assertThat(
                result.error().map(Exception::getMessage),
                is(Optional.of("Test setup failed for test case: Fred, cause: boom")));
        assertThat(result.error().map(Exception::getCause), is(Optional.of(cause)));
        assertAfterTestCalled(result);
    }

    @Test
    void shouldHandleInputtersThrowing() {
        // Given:
        final RuntimeException cause = new RuntimeException("boom");
        doThrow(cause).when(inputters).input(any(), any());

        // When:
        final CaseResult result = executor.executeTest(testCase);

        // Then:
        assertThat(
                result.error().map(Exception::getMessage),
                is(Optional.of("Test run failed for test case: Fred, cause: boom")));
        assertThat(result.error().map(Exception::getCause), is(Optional.of(cause)));
        assertAfterTestCalled(result);
    }

    @Test
    void shouldHandleExpectationPrepareThrowing() {
        // Given:
        final RuntimeException cause = new RuntimeException("boom");
        doThrow(cause).when(verifiers).prepare(any(), any());

        // When:
        final CaseResult result = executor.executeTest(testCase);

        // Then:
        assertThat(
                result.error().map(Exception::getMessage),
                is(Optional.of("Test run failed for test case: Fred, cause: boom")));
        assertThat(result.error().map(Exception::getCause), is(Optional.of(cause)));
        assertAfterTestCalled(result);
    }

    @Test
    void shouldHandleExpectationFailures() {
        // Given:
        final AssertionError cause = new AssertionError("boom");
        doThrow(cause).when(verifier).verify();

        // When:
        final CaseResult result = executor.executeTest(testCase);

        // Then:
        assertThat(result.failure(), is(Optional.of(cause)));
        assertAfterTestCalled(result);
    }

    @Test
    void shouldHandleExpectationErrors() {
        // Given:
        final RuntimeException cause = new RuntimeException("boom");
        doThrow(cause).when(verifier).verify();

        // When:
        final CaseResult result = executor.executeTest(testCase);

        // Then:
        assertThat(
                result.error().map(Exception::getMessage),
                is(Optional.of("Test run failed for test case: Fred, cause: boom")));
        assertThat(result.error().map(Exception::getCause), is(Optional.of(cause)));
        assertAfterTestCalled(result);
    }

    @Test
    void shouldThrowIfAfterTestListenersThrow() {
        // Given:
        final RuntimeException cause = new RuntimeException("boom");
        doThrow(cause).when(listeners).forEachReverse(any());

        // When:
        final Exception e =
                assertThrows(RuntimeException.class, () -> executor.executeTest(testCase));

        // Then:
        assertThat(e.getMessage(), is("Test teardown failed for test case: Fred, cause: boom"));
        assertThat(e.getCause(), is(cause));
    }

    private void assertAfterTestCalled(final CaseResult result) {
        verify(listeners).forEachReverse(actionCaptor.capture());
        actionCaptor.getValue().accept(listener);
        verify(listener).afterTest(testCase, result);
    }
}

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

package org.creekservice.internal.system.test.executor.execution;

import static java.util.Objects.requireNonNull;
import static org.creekservice.internal.system.test.executor.result.CaseResult.testCaseResult;

import java.time.Duration;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.test.env.listener.TestListenerCollection;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler.Verifier;
import org.creekservice.api.system.test.model.TestCase;
import org.creekservice.internal.system.test.executor.api.SystemTest;
import org.creekservice.internal.system.test.executor.execution.expectation.Verifiers;
import org.creekservice.internal.system.test.executor.execution.input.Inputters;
import org.creekservice.internal.system.test.executor.result.CaseResult;

/** Executor of test cases. */
public final class TestCaseExecutor {

    private final Inputters inputters;
    private final Verifiers verifiers;
    private final TestListenerCollection listeners;

    /**
     * @param api the system test api.
     * @param verifierTimeout the default verifier timeout, i.e. how long to wait for expectations
     *     to be met.
     */
    public TestCaseExecutor(final SystemTest api, final Duration verifierTimeout) {
        this(
                api.tests().env().listeners(),
                new Inputters(api.tests().model()),
                new Verifiers(api.tests().model(), verifierTimeout));
    }

    @VisibleForTesting
    TestCaseExecutor(
            final TestListenerCollection listeners,
            final Inputters inputters,
            final Verifiers verifiers) {
        this.listeners = requireNonNull(listeners, "listeners");
        this.inputters = requireNonNull(inputters, "inputter");
        this.verifiers = requireNonNull(verifiers, "verifiers");
    }

    /**
     * Execute a test case.
     *
     * @param testCase the test case.
     * @return the test result.
     */
    public CaseResult executeTest(final TestCase testCase) {
        final CaseResult result = execute(testCase);

        try {
            afterTest(testCase, result);
        } catch (final Exception e) {
            throw new TestExecutionException("Test teardown", testCase, e);
        }

        return result;
    }

    private CaseResult execute(final TestCase testCase) {
        final CaseResult.Builder builder = testCaseResult(testCase);
        if (testCase.disabled()) {
            return builder.disabled();
        }

        try {
            beforeTest(testCase);
        } catch (final Exception e) {
            return builder.error(new TestExecutionException("Test setup", testCase, e));
        }

        return runTest(testCase, builder);
    }

    private void beforeTest(final TestCase testCase) {
        listeners.forEach(listener -> listener.beforeTest(testCase));
    }

    private CaseResult runTest(final TestCase testCase, final CaseResult.Builder builder) {
        try {
            final Verifier verifier = verifiers.prepare(testCase.expectations(), testCase);
            inputters.input(testCase.inputs(), testCase.suite());

            try {
                verifier.verify();
                return builder.success();
            } catch (final AssertionError e) {
                return builder.failure(e);
            }
        } catch (final Exception e) {
            return builder.error(new TestExecutionException("Test run", testCase, e));
        }
    }

    private void afterTest(final TestCase testCase, final CaseResult result) {
        listeners.forEachReverse(listener -> listener.afterTest(testCase, result));
    }

    private static final class TestExecutionException extends RuntimeException {
        TestExecutionException(final String msg, final TestCase test, final Throwable cause) {
            super(
                    msg
                            + " failed for test case: "
                            + test.name()
                            + ", cause: "
                            + cause.getMessage(),
                    cause);
        }
    }
}

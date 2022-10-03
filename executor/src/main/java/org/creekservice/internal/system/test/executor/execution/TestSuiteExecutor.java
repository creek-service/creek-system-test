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

import static java.util.Objects.requireNonNull;
import static org.creekservice.internal.system.test.executor.result.CaseResult.testCaseResult;
import static org.creekservice.internal.system.test.executor.result.SuiteResult.testSuiteResult;

import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.test.env.listener.TestListenerCollection;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;
import org.creekservice.api.system.test.model.TestSuite;
import org.creekservice.internal.system.test.executor.api.SystemTest;
import org.creekservice.internal.system.test.executor.execution.input.Inputters;
import org.creekservice.internal.system.test.executor.result.SuiteResult;

public final class TestSuiteExecutor {

    private final Inputters inputters;
    private final TestListenerCollection listeners;
    private final TestCaseExecutor testExecutor;

    public TestSuiteExecutor(final SystemTest api) {
        this(
                api.tests().env().listeners(),
                new Inputters(api.tests().model()),
                new TestCaseExecutor(api));
    }

    @VisibleForTesting
    TestSuiteExecutor(
            final TestListenerCollection listeners,
            final Inputters inputters,
            final TestCaseExecutor testExecutor) {
        this.listeners = requireNonNull(listeners, "listeners");
        this.inputters = requireNonNull(inputters, "inputter");
        this.testExecutor = requireNonNull(testExecutor, "testExecutor");
    }

    public SuiteResult executeSuite(final TestSuite testSuite) {
        final SuiteResult result = execute(testSuite);

        try {
            afterSuite(testSuite, result);
        } catch (final Exception e) {
            throw new SuiteExecutionFailedException("Suite teardown", testSuite, e);
        }

        return result;
    }

    private SuiteResult execute(final TestSuite testSuite) {
        final SuiteResult.Builder builder = testSuiteResult(testSuite);

        try {
            beforeSuite(testSuite);
        } catch (final Exception e) {
            final SuiteExecutionFailedException cause =
                    new SuiteExecutionFailedException("Suite setup", testSuite, e);

            testSuite.tests().stream()
                    .map(
                            test ->
                                    test.disabled()
                                            ? testCaseResult(test).disabled()
                                            : testCaseResult(test).error(cause))
                    .peek(
                            result ->
                                    listeners.forEach(
                                            listener ->
                                                    listener.afterTest(result.testCase(), result)))
                    .forEach(builder::add);

            return builder.build();
        }

        runSuite(testSuite, builder);

        return builder.build();
    }

    private void beforeSuite(final TestSuite testSuite) {
        listeners.forEach(listener -> listener.beforeSuite(testSuite));
        inputters.input(testSuite.pkg().seedData());
    }

    private void runSuite(final TestSuite testSuite, final SuiteResult.Builder builder) {
        try {
            testSuite.tests().stream().map(testExecutor::executeTest).forEach(builder::add);
        } catch (final Exception e) {
            throw new SuiteExecutionFailedException("Suite execution", testSuite, e);
        }
    }

    private void afterSuite(final TestSuite testSuite, final TestSuiteResult result) {
        listeners.forEachReverse(listener -> listener.afterSuite(testSuite, result));
    }

    private static final class SuiteExecutionFailedException extends RuntimeException {
        SuiteExecutionFailedException(
                final String msg, final TestSuite suite, final Throwable cause) {
            super(msg + " failed for test suite: " + suite.name(), cause);
        }
    }
}

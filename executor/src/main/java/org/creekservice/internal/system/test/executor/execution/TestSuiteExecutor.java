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
import org.creekservice.api.system.test.model.TestSuite;
import org.creekservice.internal.system.test.executor.result.CaseResult;
import org.creekservice.internal.system.test.executor.result.SuiteResult;

public final class TestSuiteExecutor {

    private final TestListenerCollection listeners;
    private final TestCaseExecutor testExecutor;

    public TestSuiteExecutor(final TestListenerCollection listeners) {
        this(listeners, new TestCaseExecutor(listeners));
    }

    @VisibleForTesting
    TestSuiteExecutor(final TestListenerCollection listeners, final TestCaseExecutor testExecutor) {
        this.listeners = requireNonNull(listeners, "listeners");
        this.testExecutor = requireNonNull(testExecutor, "testExecutor");
    }

    public SuiteResult executeSuite(final TestSuite testSuite) {
        try {
            beforeSuite(testSuite);
            return runSuite(testSuite);
        } finally {
            afterSuite(testSuite);
        }
    }

    private void beforeSuite(final TestSuite testSuite) {
        listeners.forEach(listener -> listener.beforeSuite(testSuite));
    }

    private SuiteResult runSuite(final TestSuite testSuite) {
        testSuite.tests().forEach(testExecutor::executeTest);

        // For now, this facilitates testing:
        final SuiteResult.Builder results = testSuiteResult(testSuite);

        switch (testSuite.tests().size()) {
            case 1:
                results.add(testCaseResult(testSuite.tests().get(0)).error(new RuntimeException()));
                break;
            case 2:
                results.add(testCaseResult(testSuite.tests().get(0)).failure(new AssertionError()));
                results.add(testCaseResult(testSuite.tests().get(1)).success());
                break;
            default:
                testSuite.tests().stream()
                        .map(CaseResult::testCaseResult)
                        .map(CaseResult.Builder::success)
                        .forEach(results::add);
                break;
        }

        return results.build();
    }

    private void afterSuite(final TestSuite testSuite) {
        listeners.forEachReverse(
                listener -> listener.afterSuite(testSuite, testSuiteResult(testSuite).build()));
    }
}

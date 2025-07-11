/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.internal.system.test.executor.result;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.system.test.extension.test.model.CreekTestCase;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.model.TestCaseResult;
import org.creekservice.api.system.test.extension.test.model.TestExecutionResult;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;

/** Util class for formatting execution results to a string. */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ResultLogFormatter {

    private ResultLogFormatter() {}

    /**
     * Format any errors found in the execution {@code result}.
     *
     * @param result the execution result to format
     * @return the String containing any errors in the result.
     */
    public static String formatIssues(final TestExecutionResult result) {
        return result.results().stream()
                .flatMap(ResultLogFormatter::suiteIssues)
                .filter(msg -> !msg.isBlank())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static Stream<String> suiteIssues(final TestSuiteResult suite) {
        return Stream.concat(
                suite.error().stream()
                        .map(e -> formatIssue(suite.testSuite(), Optional.empty(), e)),
                suite.testResults().stream().flatMap(ResultLogFormatter::caseIssue));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Stream<String> caseIssue(final TestCaseResult test) {
        if (test.failure().isEmpty() && test.error().isEmpty()) {
            return Stream.empty();
        }

        final Throwable issue =
                test.failure().map(Throwable.class::cast).orElseGet(() -> test.error().get());

        return Stream.of(formatIssue(test.testCase().suite(), Optional.of(test.testCase()), issue));
    }

    private static String formatIssue(
            final CreekTestSuite suite, final Optional<CreekTestCase> test, final Throwable issue) {
        return suite.name() + test.map(t -> ":" + t.name()).orElse("") + ": " + issue.getMessage();
    }
}

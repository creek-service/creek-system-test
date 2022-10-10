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

package org.creekservice.internal.system.test.executor.result;


import java.util.stream.Collectors;
import org.creekservice.api.system.test.extension.test.model.TestCaseResult;
import org.creekservice.api.system.test.extension.test.model.TestExecutionResult;

public final class ResultLogFormatter {

    private ResultLogFormatter() {}

    public static String formatIssues(final TestExecutionResult result) {
        return result.results().stream()
                .flatMap(suite -> suite.testCases().stream())
                .map(ResultLogFormatter::formatIssue)
                .filter(msg -> !msg.isBlank())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String formatIssue(final TestCaseResult test) {
        if (test.failure().isEmpty() && test.error().isEmpty()) {
            return "";
        }

        final String issue =
                test.failure().isPresent()
                        ? test.failure().get().getMessage()
                        : test.error().get().getMessage();

        return test.testCase().suite().name() + ":" + test.testCase().name() + ": " + issue;
    }
}

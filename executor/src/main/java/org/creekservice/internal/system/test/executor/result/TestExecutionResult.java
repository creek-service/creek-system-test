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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

public final class TestExecutionResult {

    private final List<TestSuiteResult> results;

    public TestExecutionResult(final List<TestSuiteResult> results) {
        this.results = List.copyOf(requireNonNull(results, "results"));
    }

    public TestExecutionResult combine(final TestExecutionResult with) {
        final List<TestSuiteResult> all = new ArrayList<>(results);
        all.addAll(with.results);
        return new TestExecutionResult(all);
    }

    public boolean isEmpty() {
        return results.isEmpty();
    }

    /** @return number of test cases that failed, i.e. assertions not met */
    public int failed() {
        return results.stream().mapToInt(TestSuiteResult::failed).sum();
    }

    /** @return number of test cases that failed to execute */
    public int errors() {
        return results.stream().mapToInt(TestSuiteResult::errors).sum();
    }

    /** @return {@code true} if there were no failures or errors. */
    public boolean passed() {
        return failed() == 0 && errors() == 0;
    }
}

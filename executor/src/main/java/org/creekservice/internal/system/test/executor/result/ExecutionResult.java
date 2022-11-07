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

import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.stream.Collectors;
import org.creekservice.api.system.test.extension.test.model.TestExecutionResult;

/** Implementation of {@link TestExecutionResult} */
public final class ExecutionResult implements TestExecutionResult {

    private final List<SuiteResult> results;

    /** @param results the list of suite results to include */
    public ExecutionResult(final List<SuiteResult> results) {
        this.results = List.copyOf(requireNonNull(results, "results"));
    }

    @Override
    public boolean isEmpty() {
        return results.isEmpty();
    }

    @Override
    public long failed() {
        return results.stream().mapToLong(SuiteResult::failures).sum();
    }

    @Override
    public long errors() {
        return results.stream().mapToLong(this::suiteErrors).sum();
    }

    @Override
    public boolean passed() {
        return failed() == 0 && errors() == 0;
    }

    @Override
    public List<SuiteResult> results() {
        return List.copyOf(results);
    }

    @Override
    public String toString() {
        return "ExecutionResult{results="
                + lineSeparator()
                + results.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining("," + lineSeparator()))
                + lineSeparator()
                + "}";
    }

    private long suiteErrors(final SuiteResult result) {
        return result.errors() + result.error().stream().mapToLong(e -> 1L).sum();
    }
}

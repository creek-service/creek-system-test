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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.system.test.extension.test.model.TestExecutionResult;
import org.creekservice.api.system.test.model.TestPackage;
import org.creekservice.api.system.test.parser.TestPackagesLoader;
import org.creekservice.internal.system.test.executor.result.ExecutionResult;
import org.creekservice.internal.system.test.executor.result.ResultsWriter;
import org.creekservice.internal.system.test.executor.result.SuiteResult;

/** Executor of test packages. */
public final class TestPackagesExecutor {

    private final TestPackagesLoader loader;
    private final TestSuiteExecutor suiteExecutor;
    private final ResultsWriter resultsWriter;

    /**
     * @param loader used to load all available test packages
     * @param suiteExecutor used to execute each suite.
     * @param resultsWriter used to write results.
     */
    public TestPackagesExecutor(
            final TestPackagesLoader loader,
            final TestSuiteExecutor suiteExecutor,
            final ResultsWriter resultsWriter) {
        this.loader = requireNonNull(loader, "loader");
        this.suiteExecutor = requireNonNull(suiteExecutor, "suiteExecutor");
        this.resultsWriter = requireNonNull(resultsWriter, "resultsWriter");
    }

    /**
     * Execute all packages.
     *
     * @return the result.
     */
    public TestExecutionResult execute() {
        final TestExecutionResult result = executePackages();
        if (!result.isEmpty()) {
            resultsWriter.write(result);
        }
        return result;
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private TestExecutionResult executePackages() {
        try (Stream<TestPackage> packages = loader.stream()) {
            final List<SuiteResult> result =
                    packages.map(TestPackage::suites)
                            .flatMap(List::stream)
                            .map(suiteExecutor::executeSuite)
                            .collect(Collectors.toList());
            return new ExecutionResult(result);
        }
    }
}

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

package org.creekservice.api.system.test.executor;

import static java.lang.System.lineSeparator;
import static org.creekservice.api.system.test.parser.TestPackageParsers.yamlParser;
import static org.creekservice.api.system.test.parser.TestPackagesLoader.testPackagesLoader;
import static org.creekservice.internal.system.test.executor.api.Api.initializeApi;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.creekservice.api.base.type.JarVersion;
import org.creekservice.api.system.test.extension.test.model.TestExecutionResult;
import org.creekservice.api.system.test.parser.TestPackageParser;
import org.creekservice.api.system.test.parser.TestPackagesLoader;
import org.creekservice.internal.system.test.executor.api.SystemTest;
import org.creekservice.internal.system.test.executor.cli.PicoCliParser;
import org.creekservice.internal.system.test.executor.execution.TestPackagesExecutor;
import org.creekservice.internal.system.test.executor.execution.TestSuiteExecutor;
import org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo;
import org.creekservice.internal.system.test.executor.observation.TestPackageParserObserver;
import org.creekservice.internal.system.test.executor.result.ExecutionResult;
import org.creekservice.internal.system.test.executor.result.ResultLogFormatter;
import org.creekservice.internal.system.test.executor.result.xml.XmlResultsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Entry point for running system tests */
public final class SystemTestExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestExecutor.class);
    private static final Duration DEFAULT_VERIFIER_TIMEOUT = Duration.ofMinutes(1);

    private SystemTestExecutor() {}

    /**
     * Run the system tests.
     *
     * <p>See {@link org.creekservice.internal.system.test.executor.cli.PicoCliParser} for details
     * of supported command line parameters.
     *
     * @param args the command line parameters.
     */
    public static void main(final String... args) {
        try {
            final boolean success =
                    PicoCliParser.parse(args)
                            .map(SystemTestExecutor::run)
                            .map(TestExecutionResult::passed)
                            .orElse(true);

            System.exit(success ? 0 : 1);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            System.exit(2);
        }
    }

    /**
     * Run the system tests.
     *
     * @param options the options used to customise the test run.
     * @return the test run results.
     */
    public static TestExecutionResult run(final ExecutorOptions options) {
        if (options.echoOnly()) {
            echo(options);
            return new ExecutionResult(List.of());
        }

        if (!Files.isDirectory(options.testDirectory())) {
            throw new TestExecutionFailedException(
                    "Not a directory: " + options.testDirectory().toUri());
        }

        final TestExecutionResult result = executor(options).execute();
        if (result.isEmpty()) {
            throw new TestExecutionFailedException(
                    "No tests found under: " + options.testDirectory().toUri());
        }

        if (!result.passed()) {
            LOGGER.error(
                    "There were failing tests. See the report at: "
                            + options.resultDirectory().toUri()
                            + lineSeparator()
                            + ResultLogFormatter.formatIssues(result));
        }

        return result;
    }

    private static void echo(final ExecutorOptions options) {
        LOGGER.info(
                "SystemTestExecutor: {}",
                JarVersion.jarVersion(SystemTestExecutor.class).orElse("unknown"));
        LOGGER.info(classPath());
        LOGGER.info(modulePath());
        LOGGER.info("{}", options);
    }

    private static String classPath() {
        final String classPath = ManagementFactory.getRuntimeMXBean().getClassPath();
        return classPath.isEmpty() ? "" : "--class-path=" + classPath;
    }

    private static String modulePath() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
                .filter(arg -> arg.startsWith("--module-path") || arg.startsWith("-p"))
                .collect(Collectors.joining(" "));
    }

    private static TestPackagesExecutor executor(final ExecutorOptions options) {

        final Supplier<SystemTest> apiSupplier =
                () ->
                        initializeApi(
                                options.serviceDebugInfo()
                                        .map(ServiceDebugInfo::copyOf)
                                        .orElse(ServiceDebugInfo.none()),
                                options.mountInfo(),
                                options.env());

        final TestPackagesLoader loader =
                testPackagesLoader(
                        options.testDirectory(), createParser(apiSupplier), options.suitesFilter());

        return new TestPackagesExecutor(
                loader,
                new TestSuiteExecutor(
                        apiSupplier, options.verifierTimeout().orElse(DEFAULT_VERIFIER_TIMEOUT)),
                new XmlResultsWriter(options.resultDirectory()));
    }

    private static TestPackageParser createParser(final Supplier<SystemTest> apiSupplier) {
        // Initialize API and test extensions once here to obtain the list of model extensions:
        final SystemTest api = apiSupplier.get();

        return yamlParser(api.tests().model().modelTypes(), new TestPackageParserObserver(LOGGER));
    }

    private static final class TestExecutionFailedException extends RuntimeException {
        TestExecutionFailedException(final String msg) {
            super(msg);
        }
    }
}

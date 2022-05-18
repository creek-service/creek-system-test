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

package org.creekservice.api.system.test.executor;

import static org.creekservice.api.system.test.parser.TestPackageParsers.yamlParser;
import static org.creekservice.api.system.test.parser.TestPackagesLoader.testPackagesLoader;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.creekservice.api.base.type.JarVersion;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.api.system.test.extension.CreekTestExtensions;
import org.creekservice.api.system.test.parser.TestPackagesLoader;
import org.creekservice.internal.system.test.executor.api.SystemTest;
import org.creekservice.internal.system.test.executor.cli.PicoCliParser;
import org.creekservice.internal.system.test.executor.execution.TestPackagesExecutor;
import org.creekservice.internal.system.test.executor.execution.TestSuiteExecutor;
import org.creekservice.internal.system.test.executor.observation.TestPackageParserObserver;
import org.creekservice.internal.system.test.executor.result.ResultsWriter;
import org.creekservice.internal.system.test.executor.result.TestExecutionResult;

/** Entry point for running system tests */
public final class SystemTestExecutor {

    private static final Logger LOGGER = LogManager.getLogger(SystemTestExecutor.class);

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
                    PicoCliParser.parse(args).map(SystemTestExecutor::run).orElse(true);

            System.exit(success ? 0 : 1);
        } catch (final Exception e) {
            LOGGER.fatal(e.getMessage(), e);
            System.exit(2);
        }
    }

    /**
     * Run the system tests.
     *
     * @param options the options used to customise the test run.
     */
    public static boolean run(final ExecutorOptions options) {
        if (options.echoOnly()) {
            echo(options);
            return true;
        }

        if (!Files.isDirectory(options.testDirectory())) {
            throw new TestExecutionFailedException(
                    "Not a directory: " + options.testDirectory().toUri());
        }

        final SystemTest api = initializeApi();

        final TestExecutionResult result = executor(options, api).execute();
        if (result.isEmpty()) {
            throw new TestExecutionFailedException(
                    "No tests found under: " + options.testDirectory().toUri());
        }

        final boolean allPassed = result.passed();
        if (!allPassed) {
            LOGGER.error(
                    "There were failing tests. See the report at: "
                            + options.resultDirectory().toUri());
        }

        return allPassed;
    }

    private static void echo(final ExecutorOptions options) {
        LOGGER.info(
                "SystemTestExecutor: "
                        + JarVersion.jarVersion(SystemTestExecutor.class).orElse("unknown"));
        LOGGER.info(classPath());
        LOGGER.info(modulePath());
        LOGGER.info(options);
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

    private static SystemTest initializeApi() {
        return new SystemTest(loadExtensions());
    }

    private static List<CreekTestExtension> loadExtensions() {
        final List<CreekTestExtension> extensions = CreekTestExtensions.load();
        extensions.forEach(ext -> LOGGER.debug("Loaded extension: " + ext.name()));
        return extensions;
    }

    private static TestPackagesExecutor executor(
            final ExecutorOptions options, final SystemTest api) {

        final TestPackagesLoader loader =
                testPackagesLoader(
                        options.testDirectory(),
                        yamlParser(api.modelTypes(), new TestPackageParserObserver(LOGGER)),
                        options.suitesFilter());

        return new TestPackagesExecutor(
                loader, new TestSuiteExecutor(), new ResultsWriter(options.resultDirectory()));
    }

    private static final class TestExecutionFailedException extends RuntimeException {
        TestExecutionFailedException(final String msg) {
            super(msg);
        }
    }
}

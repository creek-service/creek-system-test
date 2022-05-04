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

import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.creekservice.api.base.type.JarVersion;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.api.system.test.extension.CreekTestExtensions;
import org.creekservice.api.system.test.extension.model.ModelType;
import org.creekservice.api.system.test.parser.TestPackagesLoader;
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

    public static boolean run(final ExecutorOptions options) {
        if (options.echoOnly()) {
            LOGGER.info(
                    "SystemTestExecutor: "
                            + JarVersion.jarVersion(SystemTestExecutor.class).orElse("unknown"));
            LOGGER.info(options);
            return true;
        }

        if (!Files.isDirectory(options.testDirectory())) {
            throw new TestExecutionFailedException(
                    "Not a directory: " + options.testDirectory().toUri());
        }

        final List<CreekTestExtension> extensions = loadExtensions();

        final TestExecutionResult result = executor(options, extensions).execute();
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

    private static List<CreekTestExtension> loadExtensions() {
        final List<CreekTestExtension> extensions = CreekTestExtensions.load();
        extensions.forEach(ext -> LOGGER.debug("Loaded extension: " + ext.name()));
        return extensions;
    }

    private static TestPackagesExecutor executor(
            final ExecutorOptions options, final Collection<CreekTestExtension> extensions) {
        final Collection<ModelType<?>> modelExtensions =
                extensions.stream()
                        .map(CreekTestExtension::modelTypes)
                        .flatMap(Set::stream)
                        .collect(Collectors.toList());

        final TestPackagesLoader loader =
                testPackagesLoader(
                        options.testDirectory(),
                        yamlParser(modelExtensions, new TestPackageParserObserver(LOGGER)),
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

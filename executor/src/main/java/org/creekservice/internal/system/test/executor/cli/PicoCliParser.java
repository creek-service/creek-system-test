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

package org.creekservice.internal.system.test.executor.cli;

import static java.lang.System.lineSeparator;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.creekservice.api.base.type.JarVersion;
import org.creekservice.api.system.test.executor.ExecutorOptions;
import org.creekservice.api.system.test.executor.SystemTestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Option;

public final class PicoCliParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PicoCliParser.class);

    private PicoCliParser() {}

    public static Optional<ExecutorOptions> parse(final String... args) {
        final Options options = new Options();
        final CommandLine parser = new CommandLine(options);

        try {
            parser.parseArgs(args);

            if (parser.isUsageHelpRequested()) {
                LOGGER.info(parser.getUsageMessage());
                return Optional.empty();
            }

            if (parser.isVersionHelpRequested()) {
                LOGGER.info(
                        "SystemTestExecutor: "
                                + JarVersion.jarVersion(SystemTestExecutor.class)
                                        .orElse("unknown"));
                return Optional.empty();
            }

            return Optional.of(options);
        } catch (final Exception e) {
            throw new InvalidArgumentsException(parser.getUsageMessage(), e);
        }
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @CommandLine.Command(name = "SystemTestExecutor", mixinStandardHelpOptions = true)
    public static final class Options implements ExecutorOptions {
        @Option(
                names = {"-td", "--test-directory"},
                required = true,
                paramLabel = "PATH",
                description = "The root directory to search for test packages")
        private Path testDir;

        @Option(
                names = {"-rd", "--result-directory"},
                required = true,
                paramLabel = "PATH",
                description = "The directory result files will be written to")
        private Path resultDir;

        @Option(
                names = {"-vt", "--verifier-timeout-seconds"},
                paramLabel = "SECONDS",
                description = {
                    "Set an optional custom verifier timeout.",
                    "The verifier timeout is the maximum amount of time the system tests "
                            + "will wait for a defined expectation to be met. A longer timeout will mean "
                            + "tests have more time for expectations to be met, but may run slower as a consequence."
                })
        private Optional<Long> verifierTimeout;

        @Option(
                names = {"-is", "--include-suites"},
                paramLabel = "REGEX",
                description = {
                    "Set an optional regular expression pattern to limit the test suites to run.",
                    "Only test suites whose relative path matches the supplied pattern will be included."
                })
        private Optional<Pattern> suitePattern;

        @Option(
                names = {"-e", "--echo-only"},
                hidden = true,
                description =
                        "Hidden option used for testing. When set the running will echo its config to "
                                + "standard out and exit.")
        private boolean echoOnly;

        @Override
        public Path testDirectory() {
            return testDir;
        }

        @Override
        public Path resultDirectory() {
            return resultDir;
        }

        @Override
        public Optional<Duration> verifierTimeout() {
            return verifierTimeout.map(Duration::ofSeconds);
        }

        @Override
        public Predicate<Path> suitesFilter() {
            return suitePattern
                    .map(
                            regex ->
                                    (Predicate<Path>)
                                            path -> regex.matcher(path.toString()).matches())
                    .orElse(path -> true);
        }

        @Override
        public boolean echoOnly() {
            return echoOnly;
        }

        @Override
        public String toString() {
            return "--test-directory="
                    + testDir
                    + lineSeparator()
                    + "--result-directory="
                    + resultDir
                    + lineSeparator()
                    + "--verifier-timeout-seconds="
                    + verifierTimeout.map(String::valueOf).orElse("<Not Set>")
                    + lineSeparator()
                    + "--include-suites="
                    + suitePattern.map(Pattern::pattern).orElse("<Not Set>");
        }
    }

    private static class InvalidArgumentsException extends RuntimeException {
        InvalidArgumentsException(final String usageMessage, final Throwable cause) {
            super(cause.getMessage() + lineSeparator() + usageMessage, cause);
        }
    }
}

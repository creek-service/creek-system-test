/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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
import static org.creekservice.api.base.type.Preconditions.requireNonEmpty;
import static org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo.DEFAULT_BASE_DEBUG_PORT;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.creekservice.api.base.type.JarVersion;
import org.creekservice.api.base.type.Lists;
import org.creekservice.api.system.test.executor.ExecutorOptions;
import org.creekservice.api.system.test.executor.SystemTestExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

/** Cli arguments parser for the executor */
public final class PicoCliParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PicoCliParser.class);
    private static final String NOT_SET = "<Not Set>";

    private PicoCliParser() {}

    /**
     * Parse the command line
     *
     * @param args the command line args.
     * @return the parsed command line, or else {@code empty} if the args have already been handled.
     */
    public static Optional<ExecutorOptions> parse(final String... args) {
        final Options options = new Options();
        final CommandLine parser = new CommandLine(options).setTrimQuotes(true);

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

    /** The command line options the executor supports. */
    @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "unused", "FieldMayBeFinal"})
    @CommandLine.Command(name = "SystemTestExecutor", mixinStandardHelpOptions = true)
    public static final class Options implements ExecutorOptions {

        @Spec CommandSpec spec;

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
                    "The verifier timeout is the maximum amount of time the system tests will wait"
                        + " for a defined expectation to be met. A longer timeout will mean tests"
                        + " have more time for expectations to be met, but may run slower as a"
                        + " consequence."
                })
        private Optional<Long> verifierTimeout;

        @Option(
                names = {"-is", "--include-suites"},
                paramLabel = "REGEX",
                description = {
                    "Set an optional regular expression pattern to limit the test suites to run.",
                    "Only test suites whose relative path matches the supplied pattern will be"
                            + " included."
                })
        private Optional<Pattern> suitePattern;

        @Option(
                names = {"-eo", "--echo-only"},
                hidden = true,
                description =
                        "Hidden option used for testing. When set the running will echo its config"
                                + " to standard out and exit.")
        private boolean echoOnly;

        private int debugServicePort = DEFAULT_BASE_DEBUG_PORT;

        /**
         * Method to allow {@code debugServicePort} to ve validated.
         *
         * @param port the debug port.
         */
        @Option(
                names = {"-dsp", "--debug-service-port"},
                description =
                        "The port the first service being debugged will listen on for the debugger"
                            + " to attach. Subsequent services being debugged will use sequential"
                            + " port numbers.")
        public void setDebugServicePort(final int port) {
            if (port <= 0) {
                throw new ParameterException(
                        spec.commandLine(),
                        "Invalid value '"
                                + port
                                + "' for option '--debug-service-port': "
                                + "value must be positive.");
            }
            this.debugServicePort = port;
        }

        private Set<String> debugServices = Set.of();

        /**
         * Method to allow {@code debugServices} to ve validated.
         *
         * @param names the services to debug
         */
        @Option(
                names = {"-ds", "--debug-service"},
                split = ",",
                description = "Comma seperated list of services to debug.")
        public void setDebugServices(final String[] names) {
            this.debugServices = Set.copyOf(new HashSet<>(Arrays.asList(names)));
        }

        private Set<String> debugInstances = Set.of();

        /**
         * Method to allow {@code debugServiceInstances} to ve validated.
         *
         * @param names the service instances to debug
         */
        @Option(
                names = {"-dsi", "--debug-service-instance"},
                split = ",",
                description = "Comma seperated list of service instances to debug.")
        public void setDebugServiceInstances(final String[] names) {
            this.debugInstances = Set.copyOf(new HashSet<>(Arrays.asList(names)));
        }

        @Option(
                names = {"-e", "--env"},
                split = ",",
                description =
                        "Comma seperated list of key=value environment variables to set on each"
                                + " service-under-test.")
        private Map<String, String> env = Map.of();

        private List<MountInfo> readOnlyMounts = new ArrayList<>(2);

        /**
         * Method for adding read-only mount points.
         *
         * @param mounts the map of host -> container paths to mount.
         */
        @Option(
                names = {"-mr", "--mount-read-only"},
                split = ",",
                description =
                        "Comma seperated list of hostPath=containerPath read-only mount points to"
                                + " set on each service-under-test.")
        public void setReadOnlyMount(final Map<String, String> mounts) {
            validateMounts(mounts);
            readOnlyMounts =
                    mounts.entrySet().stream()
                            .map(e -> new Mount(e.getKey(), e.getValue(), true))
                            .collect(Collectors.toUnmodifiableList());
        }

        private List<MountInfo> writeableMounts = new ArrayList<>(1);

        /**
         * Method for adding writable mount points.
         *
         * @param mounts the map of host -> container paths to mount.
         */
        @Option(
                names = {"-mw", "--mount-writable"},
                split = ",",
                description =
                        "Comma seperated list of hostPath=containerPath writable mount points to"
                                + " set on each service-under-test.")
        public void setWritableMount(final Map<String, String> mounts) {
            validateMounts(mounts);
            writeableMounts =
                    mounts.entrySet().stream()
                            .map(e -> new Mount(e.getKey(), e.getValue(), false))
                            .collect(Collectors.toUnmodifiableList());
        }

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
        public Optional<ServiceDebugInfo> serviceDebugInfo() {
            if (debugServices.isEmpty() && debugInstances.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(
                    org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo
                            .serviceDebugInfo(debugServicePort, debugServices, debugInstances));
        }

        @Override
        public Collection<MountInfo> mountInfo() {
            return Lists.combineList(readOnlyMounts, writeableMounts);
        }

        @Override
        public Map<String, String> env() {
            return Map.copyOf(env);
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
                    + verifierTimeout.map(String::valueOf).orElse(NOT_SET)
                    + lineSeparator()
                    + "--include-suites="
                    + suitePattern.map(Pattern::pattern).orElse(NOT_SET)
                    + lineSeparator()
                    + "--debug-service-port="
                    + (debugServicePort == DEFAULT_BASE_DEBUG_PORT ? NOT_SET : debugServicePort)
                    + lineSeparator()
                    + "--debug-service="
                    + formatList(debugServices)
                    + lineSeparator()
                    + "--debug-service-instance="
                    + formatList(debugInstances)
                    + lineSeparator()
                    + "--env="
                    + formatMap(env)
                    + lineSeparator()
                    + "--mount-read-only="
                    + formatMounts(readOnlyMounts)
                    + lineSeparator()
                    + "--mount-writable="
                    + formatMounts(writeableMounts);
        }

        private String formatList(final Set<String> list) {
            return list.isEmpty()
                    ? NOT_SET
                    : list.stream().sorted().collect(Collectors.joining(","));
        }

        private String formatMap(final Map<String, String> map) {
            return map.isEmpty()
                    ? NOT_SET
                    : map.entrySet().stream()
                            .map(e -> e.getKey() + "=" + e.getValue())
                            .sorted()
                            .collect(Collectors.joining(","));
        }

        private static String formatMounts(final List<MountInfo> mounts) {
            return mounts.isEmpty()
                    ? NOT_SET
                    : mounts.stream()
                            .map(m -> m.hostPath() + "=" + m.containerPath())
                            .collect(Collectors.joining(","));
        }

        private static void validateMounts(final Map<String, String> mounts) {
            mounts.forEach(
                    (hostPath, containerPath) -> {
                        if (hostPath.trim().isEmpty()) {
                            throw new IllegalArgumentException("Host path can not be empty");
                        }
                        if (containerPath.trim().isEmpty()) {
                            throw new IllegalArgumentException("Container path can not be empty");
                        }
                    });
        }

        private static final class Mount implements MountInfo {
            private final Path hostPath;
            private final Path containerPath;
            private final boolean readOnly;

            @SuppressFBWarnings(value = "PATH_TRAVERSAL_IN", justification = "Trusted user input")
            Mount(final String hostPath, final String containerPath, final boolean readOnly) {
                this.hostPath = Paths.get(requireNonEmpty(hostPath, "hostPath"));
                this.containerPath = Paths.get(requireNonEmpty(containerPath, "containerPath"));
                this.readOnly = readOnly;
            }

            @Override
            public Path hostPath() {
                return hostPath;
            }

            @Override
            public Path containerPath() {
                return containerPath;
            }

            @Override
            public boolean readOnly() {
                return readOnly;
            }
        }
    }

    private static class InvalidArgumentsException extends RuntimeException {
        InvalidArgumentsException(final String usageMessage, final Throwable cause) {
            super(cause.getMessage() + lineSeparator() + usageMessage, cause);
        }
    }
}

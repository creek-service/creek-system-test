/*
 * Copyright 2022-2026 Creek Contributors (https://github.com/creek-service)
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
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.creekservice.api.system.test.test.services.SharedResources.SHARED;
import static org.creekservice.api.system.test.test.services.TestServiceDescriptor.OwnedOutput;
import static org.creekservice.api.system.test.test.services.TestServiceDescriptor.UnmanagedInternal;
import static org.creekservice.api.system.test.test.services.TestServiceDescriptor.UnownedInput1;
import static org.creekservice.api.test.util.TestPaths.ensureDirectories;
import static org.creekservice.api.test.util.coverage.CodeCoverage.codeCoverageCmdLineArg;
import static org.creekservice.api.test.util.debug.RemoteDebug.remoteDebugArguments;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.creekservice.api.base.type.Suppliers;
import org.creekservice.api.system.test.test.extension.TestCreekExtensionProvider;
import org.creekservice.api.test.util.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Tag("ContainerisedTest")
class SystemTestExecutorFunctionalTest {

    // Change this to true locally to debug using attach me plugin:
    private static final boolean DEBUG = false;

    private static final Path LIB_DIR =
            TestPaths.moduleRoot("executor").resolve("build/install/executor/lib").toAbsolutePath();

    private static final Path TEST_EXT_LIB_DIR =
            TestPaths.moduleRoot("test-system-test-extension")
                    .resolve("build/libs")
                    .toAbsolutePath();

    private static final Path SERVICE_EXT_LIB_DIR =
            TestPaths.moduleRoot("test-service-extension-metadata")
                    .resolve("build/libs")
                    .toAbsolutePath();

    private static final Path TEST_SERVICES_LIB_DIR =
            TestPaths.moduleRoot("test-services").resolve("build/libs").toAbsolutePath();

    private static final String SEPARATOR = File.pathSeparator;

    private static final Pattern VERSION_PATTERN =
            Pattern.compile(".*SystemTestExecutor: \\d+\\.\\d+\\.\\d+.*", Pattern.DOTALL);

    private static final String VALID_SEED =
            """
            ---
            !creek/test
            value: seed value\
            """;

    private static final String VALID_INPUT =
            """
            ---
            !creek/test
            value: input value\
            """;

    private static final String THROWING_INPUT =
            """
            ---
            !creek/test
            value: should throw\
            """;

    private static final String PASSING_EXPECTATION =
            """
            ---
            !creek/test
            value: output value\
            """;

    private static final String FAILING_EXPECTATION =
            """
            ---
            !creek/test
            value: should fail\
            """;

    private static final String THROWING_EXPECTATION =
            """
            ---
            !creek/test
            value: should throw\
            """;

    private static final String VALID_SUITE =
            """
            ---
            name: suite name
            services:
              - test-service
            tests:
              - name: test 0
                inputs:
                  - input-1
                expectations:
                  - expectation-1
            """;

    private static final String OWNED_SEED =
            """
            ---
            !creek/test
            value: owned seed
            resource: output\
            """;

    private static final String SHARED_SEED =
            """
            ---
            !creek/test
            value: shared seed
            resource: shared\
            """;

    private static final String UNOWNED_SEED =
            """
            ---
            !creek/test
            value: unowned seed
            resource: upstream\
            """;

    private static final String OWNED_INPUT =
            """
            ---
            !creek/test
            value: owned input
            resource: output\
            """;

    private static final String UNOWNED_INPUT =
            """
            ---
            !creek/test
            value: unowned input
            resource: upstream\
            """;

    private static final String SHARED_INPUT =
            """
            ---
            !creek/test
            value: shared input
            resource: shared\
            """;

    private static final String SUITE_WITH_INPUTS_ON_EACH_OWNERSHIP_KIND =
            """
            ---
            name: suite name
            services:
              - test-service
            tests:
              - name: test 0
                inputs:
                  - input-owned
                  - input-unowned
                  - input-shared
                expectations:
                  - expectation-owned
                  - expectation-unowned
                  - expectation-shared
            """;

    @TempDir private Path root;
    private Path testDir;
    private Path resultDir;
    private Supplier<String> stdErr;
    private Supplier<String> stdOut;
    private Map<String, String> env;

    @BeforeEach
    void setUp() {
        testDir = root.resolve("tests");
        resultDir = root.resolve("results");
        env = new HashMap<>();

        TestPaths.write(testDir.resolve("seed/seed-1.yml"), VALID_SEED);
        TestPaths.write(testDir.resolve("inputs/input-1.yml"), VALID_INPUT);
        TestPaths.write(testDir.resolve("expectations/expectation-1.yml"), PASSING_EXPECTATION);
        TestPaths.write(testDir.resolve("suite.yml"), VALID_SUITE);
    }

    @Test
    void shouldOutputHelp() {
        // Given:
        final String[] args = {"-h"};

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(stdErr.get(), is(""));
        assertThat(stdOut.get(), startsWith("Usage: SystemTestExecutor"));
        assertThat(
                stdOut.get(), containsString("-h, --help      Show this help message and exit."));
        assertThat(stdOut.get(), containsString("-td, --test-directory=PATH"));
        assertThat(exitCode, is(0));
    }

    @Test
    void shouldOutputVersion() {
        // Given:
        final String[] args = {"-V"};

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(stdErr.get(), is(""));
        assertThat(stdOut.get(), matchesPattern(VERSION_PATTERN));
        assertThat(exitCode, is(0));
    }

    @Test
    void shouldEchoArguments() {
        // Given:
        final String[] args = minimalArgs("--echo-only");

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(stdErr.get(), is(""));
        assertThat(stdOut.get(), matchesPattern(VERSION_PATTERN));
        assertThat(stdOut.get(), containsString("--test-directory=" + testDir));
        assertThat(stdOut.get(), containsString("--result-directory=" + resultDir));
        assertThat(stdOut.get(), containsString("--verifier-timeout-seconds=<Not Set>"));
        assertThat(exitCode, is(0));
    }

    @Test
    void shouldEchoClassPath() {
        // Given:
        final String[] javaArgs = {
            "-cp",
            LIB_DIR + "/*",
            org.creekservice.api.system.test.executor.SystemTestExecutor.class.getName()
        };

        // When:
        final int exitCode = runExecutor(javaArgs, minimalArgs("--echo-only"));

        // Then:
        assertThat(stdErr.get(), is(""));
        assertThat(stdOut.get(), matchesPattern(VERSION_PATTERN));
        assertThat(stdOut.get(), containsString("--class-path=" + LIB_DIR));
        assertThat(stdOut.get(), not(containsString("--module-path")));
        assertThat(exitCode, is(0));
    }

    @Test
    void shouldEchoModulePath() {
        // Given:
        final String[] javaArgs = {
            "-p",
            "/another/path",
            "--module-path",
            LIB_DIR.toString(),
            "--module=creek.system.test.executor/org.creekservice.api.system.test.executor.SystemTestExecutor"
        };

        // When:
        final int exitCode = runExecutor(javaArgs, minimalArgs("--echo-only"));

        // Then:
        assertThat(stdErr.get(), is(""));
        assertThat(stdOut.get(), containsString("--module-path=" + LIB_DIR));
        assertThat(stdOut.get(), containsString("--module-path=/another/path"));
        assertThat(stdOut.get(), not(containsString("--class-path")));
        assertThat(exitCode, is(0));
    }

    @Test
    void shouldReportIssuesWithArguments() {
        // Given:
        final String[] args = minimalArgs("--unknown");

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(stdErr.get(), startsWith("Unknown option: '--unknown'"));
        assertThat(stdErr.get(), containsString("Usage: SystemTestExecutor"));
        assertThat(stdOut.get(), is(""));
        assertThat(exitCode, is(2));
    }

    @Test
    void shouldFailIfTestDirectoryIsMissing() {
        // Given:
        final String[] args = minimalArgs();
        TestPaths.delete(testDir);

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(stdErr.get(), startsWith("Not a directory: " + testDir.toUri()));
        assertThat(exitCode, is(2));
    }

    @Test
    void shouldFailIfThereWereNoTestPackages() {
        // Given:
        final String[] args = minimalArgs();
        TestPaths.delete(testDir);
        ensureDirectories(testDir);

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(stdErr.get(), startsWith("No tests found under: " + testDir.toUri()));
        assertThat(exitCode, is(2));
    }

    @Test
    void shouldReportSeedErrors() {
        // Given:
        final String[] args = minimalArgs();
        TestPaths.write(testDir.resolve("seed/seed-1.yml"), THROWING_INPUT);

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(
                stdOut.get(),
                containsString("Caused by: java.lang.RuntimeException: Failed to process input"));
        assertThat(
                stdErr.get(),
                containsString(
                        "There were failing tests. See the report at: " + resultDir.toUri()));
        assertThat(exitCode, is(1));
    }

    @Test
    void shouldReportInputErrors() {
        // Given:
        final String[] args = minimalArgs();
        TestPaths.write(testDir.resolve("inputs/input-1.yml"), THROWING_INPUT);

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(
                stdOut.get(),
                containsString("Caused by: java.lang.RuntimeException: Failed to process input"));
        assertThat(
                stdErr.get(),
                containsString(
                        "There were failing tests. See the report at: " + resultDir.toUri()));
        assertThat(exitCode, is(1));
    }

    @Test
    void shouldReportExpectationErrors() {
        // Given:
        final String[] args = minimalArgs();
        TestPaths.write(testDir.resolve("expectations/expectation-1.yml"), THROWING_EXPECTATION);

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(
                stdOut.get(),
                containsString(
                        "Caused by: java.lang.RuntimeException: Failed to process expectation"));
        assertThat(
                stdErr.get(),
                containsString(
                        "There were failing tests. See the report at: " + resultDir.toUri()));
        assertThat(exitCode, is(1));
    }

    @Test
    void shouldReportTestFailures() {
        // Given:
        final String[] args = minimalArgs();
        TestPaths.write(testDir.resolve("expectations/expectation-1.yml"), FAILING_EXPECTATION);

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(
                stdErr.get(),
                is(
                        "There were failing tests. See the report at: "
                                + resultDir.toUri()
                                + lineSeparator()
                                + "suite name:test 0: Failed because it was meant to"));
        assertThat(
                stdOut.get(),
                containsString("Finished test 'test 0': FAILED: Failed because it was meant to"));
        assertThat(exitCode, is(1));
    }

    @Test
    void shouldReportSuccess() {
        // Given:
        final String[] args = minimalArgs();

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(stdErr.get(), is(""));
        assertThat(stdOut.get(), containsString("Finished test 'test 0': SUCCESS"));
        assertThat(exitCode, is(0));
    }

    @Test
    void shouldSkipDisabledTest() {
        // Given:
        final String disabled =
                """
                ---
                name: suite name
                services:
                  - test-service
                tests:
                  - name: test 0
                    disabled:
                      reason: for testing
                    inputs:
                      - input-1
                    expectations:
                      - expectation-1
                """;

        final String[] args = minimalArgs();
        TestPaths.write(testDir.resolve("suite.yml"), disabled);

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(stdErr.get(), is(""));
        assertThat(stdOut.get(), containsString("Finished test 'test 0': SKIPPED"));
        assertThat(exitCode, is(0));
    }

    @Test
    void shouldLogTestLifecycle() {
        // When:
        runExecutor(minimalArgs());

        // Then:
        assertThat(stdErr.get(), is(""));
        assertThat(stdOut.get(), containsString("Starting suite 'suite name'"));
        assertThat(stdOut.get(), containsString("Starting test 'test 0'"));
        assertThat(stdOut.get(), containsString("Finished test 'test 0'"));
        assertThat(stdOut.get(), containsString("Finished suite 'suite name'"));
    }

    @Test
    void shouldLogOnUnusedDependency() {
        // Given:
        final String[] args = minimalArgs();
        final Path unused = testDir.resolve("expectations/unused-expectation.yml");
        TestPaths.write(unused, PASSING_EXPECTATION);

        // When:
        final int exitCode = runExecutor(args);

        // Then:
        assertThat(stdErr.get(), is(""));
        assertThat(stdOut.get(), startsWith("Unused dependencies in test package"));
        assertThat(stdOut.get(), containsString(unused.toUri().toString()));
        assertThat(exitCode, is(0));
    }

    @Test
    void shouldInitialiseSharedResources() {
        // When:
        runExecutor(minimalArgs());

        // Then:
        assertThat(stdOut.get(), containsString("Ensuring resources: [test://shared]"));
    }

    @Test
    void shouldInitialiseUnownedResources() {
        // When:
        runExecutor(minimalArgs());

        // Then:
        assertThat(stdOut.get(), containsString("Ensuring resources: [test://upstream]"));
    }

    @Test
    void shouldNotInitialiseOwnedResources() {
        // When:
        runExecutor(minimalArgs());

        // Then:
        assertThat(stdOut.get(), not(containsString("Ensuring resources: [test://output]")));
    }

    @Test
    void shouldNotInitialiseUnmanagedResources() {
        // When:
        runExecutor(minimalArgs());

        // Then:
        assertThat(stdOut.get(), not(containsString("Ensuring resources: [test://internal]")));
    }

    @Test
    void shouldAllowExtensionsToPrepareForAllKnownResources() {
        // When:
        runExecutor(minimalArgs());

        // Then:
        assertThat(stdOut.get(), containsString("Preparing resources: [test://internal]"));
        assertThat(
                stdOut.get(),
                containsString("Preparing resources: [test://upstream, test://output]"));
        assertThat(stdOut.get(), containsString("Preparing resources: [test://shared]"));
    }

    @Test
    void shouldPrepareOwnedSeedResourceBeforeSeedingIt() {
        // Given:
        TestPaths.write(testDir.resolve("seed/seed-1.yml"), OWNED_SEED);

        // When:
        final int exitCode = runExecutor(minimalArgs());

        // Then:
        assertThat(exitCode, is(0));
        assertEnsuredThenPreparedThenSeeded(OwnedOutput.id(), "owned seed");
    }

    @Test
    void shouldPrepareSharedSeedResourceBeforeSeedingIt() {
        // Given:
        TestPaths.write(testDir.resolve("seed/seed-1.yml"), SHARED_SEED);

        // When:
        final int exitCode = runExecutor(minimalArgs());

        // Then:
        assertThat(exitCode, is(0));
        assertEnsuredThenPreparedThenSeeded(SHARED.id(), "shared seed");
    }

    @Test
    void shouldPrepareUnownedSeedResourceBeforeSeedingIt() {
        // Given:
        TestPaths.write(testDir.resolve("seed/seed-1.yml"), UNOWNED_SEED);

        // When:
        final int exitCode = runExecutor(minimalArgs());

        // Then:
        assertThat(exitCode, is(0));
        assertEnsuredThenPreparedThenSeeded(UnownedInput1.id(), "unowned seed");
    }

    @Test
    void shouldProcessInputsForEachOwnershipKind() {
        // Given:
        TestPaths.write(testDir.resolve("inputs/input-owned.yml"), OWNED_INPUT);
        TestPaths.write(testDir.resolve("inputs/input-unowned.yml"), UNOWNED_INPUT);
        TestPaths.write(testDir.resolve("inputs/input-shared.yml"), SHARED_INPUT);
        TestPaths.write(
                testDir.resolve("expectations/expectation-owned.yml"),
                "---\n!creek/test\nvalue: owned expectation");
        TestPaths.write(
                testDir.resolve("expectations/expectation-unowned.yml"),
                "---\n!creek/test\nvalue: unowned expectation");
        TestPaths.write(
                testDir.resolve("expectations/expectation-shared.yml"),
                "---\n!creek/test\nvalue: shared expectation");
        TestPaths.write(testDir.resolve("suite.yml"), SUITE_WITH_INPUTS_ON_EACH_OWNERSHIP_KIND);

        // When:
        final int exitCode = runExecutor(minimalArgs());

        // Then:
        assertThat(stdOut.get(), containsString("Piping input: owned input"));
        assertThat(stdOut.get(), containsString("Piping input: unowned input"));
        assertThat(stdOut.get(), containsString("Piping input: shared input"));
        assertThat(
                stdOut.get(),
                containsString(
                        "Verifying expectations: owned expectation,unowned expectation,shared"
                                + " expectation"));
        assertThat(stdOut.get(), containsString("Finished test 'test 0': SUCCESS"));
        assertThat(exitCode, is(0));
    }

    @Test
    void shouldCloseExtensions() {
        // When:
        runExecutor(minimalArgs());

        // Then:
        assertThat(stdOut.get(), containsString("Closing TestCreekExtension"));
    }

    @Test
    void shouldProcessSeedData() {
        // When:
        runExecutor(minimalArgs());

        // Then:
        assertThat(stdOut.get(), containsString("Piping input: seed value"));
    }

    @Test
    void shouldProcessInputs() {
        // When:
        runExecutor(minimalArgs());

        // Then:
        assertThat(stdOut.get(), containsString("Piping input: input value"));
    }

    @Test
    void shouldProcessExpectations() {
        // When:
        runExecutor(minimalArgs());

        // Then:
        assertThat(stdOut.get(), containsString("Verifying expectations: output value"));
    }

    @Test
    void shouldReportExtensionInitializationFailure() {
        // Given:
        givenEnv(TestCreekExtensionProvider.ENV_FAIL_INITIALIZE_RESOURCE_ID, OwnedOutput.id());

        // When:
        final int exitCode = runExecutor(minimalArgs());

        // Then:
        assertThat(stdErr.get(), containsString("Extension initialization failed"));
        assertThat(exitCode, is(2));
    }

    @Test
    void shouldReportResourceValidationFailure() {
        // Given:
        givenEnv(TestCreekExtensionProvider.ENV_FAIL_VALIDATE_RESOURCE_ID, UnmanagedInternal.id());

        // When:
        final int exitCode = runExecutor(minimalArgs());

        // Then:
        assertThat(
                stdErr.get(),
                containsString("Validation failed for resource group: " + UnmanagedInternal.id()));
        assertThat(
                stdErr.get(),
                containsString(
                        "There were failing tests. See the report at: " + resultDir.toUri()));
        assertThat(exitCode, is(1));
    }

    @Test
    void shouldReportEnsureResourceFailures() {
        // Given:
        givenEnv(TestCreekExtensionProvider.ENV_FAIL_ENSURE_RESOURCE_ID, UnownedInput1.id());

        // When:
        final int exitCode = runExecutor(minimalArgs());

        // Then:
        assertThat(
                stdOut.get(), containsString("Ensure failed for resource: " + UnownedInput1.id()));
        assertThat(
                stdErr.get(),
                containsString(
                        "There were failing tests. See the report at: " + resultDir.toUri()));
        assertThat(exitCode, is(1));
    }

    @Test
    void shouldReportPrepareResourceFailures() {
        // Given:
        givenEnv(TestCreekExtensionProvider.ENV_FAIL_PREPARE_RESOURCE_ID, UnownedInput1.id());

        // When:
        final int exitCode = runExecutor(minimalArgs());

        // Then:
        assertThat(
                stdOut.get(), containsString("Prepare failed for resource: " + UnownedInput1.id()));
        assertThat(
                stdErr.get(),
                containsString(
                        "There were failing tests. See the report at: " + resultDir.toUri()));
        assertThat(exitCode, is(1));
    }

    @Test
    void shouldNotCheckInWithDebuggingEnabled() {
        assertThat("Do not check in with debugging enabled", !DEBUG);
    }

    private void assertEnsuredThenPreparedThenSeeded(final URI resourceId, final String seedValue) {
        final int ensuredIdx = indexOfLogLine("Ensuring resources", resourceId);
        final int preparedIdx = indexOfLogLine("Preparing resources", resourceId);
        final int seededIdx = stdOut.get().indexOf("Piping input: " + seedValue);

        assertThat("seed data was piped", seededIdx, is(greaterThanOrEqualTo(0)));
        assertThat("resource ensured before prepared", ensuredIdx, is(lessThan(preparedIdx)));
        assertThat("resource prepared before it was seeded", preparedIdx, is(lessThan(seededIdx)));
    }

    private int indexOfLogLine(final String label, final URI resourceId) {
        final Matcher matcher =
                Pattern.compile(
                                label
                                        + ": \\[[^]]*"
                                        + Pattern.quote(resourceId.toString())
                                        + "[^]]*]")
                        .matcher(stdOut.get());
        if (!matcher.find()) {
            throw new AssertionError(
                    "No '" + label + "' line found for " + resourceId + " in:\n" + stdOut.get());
        }
        return matcher.start();
    }

    private int runExecutor(final String[] cmdArgs) {
        // Run from the classpath by default until test containers works from the module path:
        final String classPath =
                LIB_DIR
                        + "/*"
                        + SEPARATOR
                        + TEST_EXT_LIB_DIR
                        + "/*"
                        + SEPARATOR
                        + SERVICE_EXT_LIB_DIR
                        + "/*"
                        + SEPARATOR
                        + TEST_SERVICES_LIB_DIR
                        + "/*";

        final String[] javaArgs = {
            "-cp",
            classPath,
            org.creekservice.api.system.test.executor.SystemTestExecutor.class.getName()
        };
        return runExecutor(javaArgs, cmdArgs);
    }

    @SuppressFBWarnings(value = "COMMAND_INJECTION", justification = "Test code")
    private int runExecutor(final String[] javaArgs, final String[] cmdArgs) {
        final List<String> cmd = buildCommand(javaArgs, cmdArgs);

        try {
            final ProcessBuilder builder = new ProcessBuilder().command(cmd);
            builder.environment().putAll(env);
            final Process executor = builder.start();

            stdErr = Suppliers.memoize(() -> readAll(executor.getErrorStream()));
            stdOut = Suppliers.memoize(() -> readAll(executor.getInputStream()));
            executor.waitFor(1, TimeUnit.MINUTES);
            return executor.exitValue();
        } catch (final Exception e) {
            throw new AssertionError(
                    "Error executing: "
                            + cmd
                            + ", stdErr: "
                            + stdErr.get()
                            + ", stdOut: "
                            + stdOut.get(),
                    e);
        }
    }

    private List<String> buildCommand(final String[] javaArgs, final String[] cmdArgs) {
        final List<String> cmd = new ArrayList<>(List.of("java"));
        if (DEBUG) {
            cmd.addAll(remoteDebugArguments());
        }
        codeCoverageCmdLineArg().ifPresent(cmd::add);

        cmd.addAll(List.of(javaArgs));
        cmd.addAll(List.of(cmdArgs));
        return cmd;
    }

    private String[] minimalArgs(final String... additional) {
        final List<String> args =
                new ArrayList<>(
                        List.of("--test-directory=" + testDir, "--result-directory=" + resultDir));
        args.addAll(List.of(additional));
        return args.toArray(String[]::new);
    }

    private static String readAll(final InputStream stdErr) {
        return new BufferedReader(new InputStreamReader(stdErr, UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    private void givenEnv(final String envName, final URI id) {
        this.env.put(envName, id.toString());
    }
}

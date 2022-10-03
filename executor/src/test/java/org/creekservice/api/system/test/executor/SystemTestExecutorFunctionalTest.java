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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.creekservice.api.system.test.test.services.TestServiceDescriptor.OwnedOutput;
import static org.creekservice.api.test.util.TestPaths.ensureDirectories;
import static org.creekservice.api.test.util.debug.RemoteDebug.remoteDebugArguments;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.creekservice.api.base.type.Suppliers;
import org.creekservice.api.system.test.test.extension.TestCreekExtensionProvider;
import org.creekservice.api.system.test.test.services.TestServiceDescriptor;
import org.creekservice.api.test.util.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

    private static final String SEPARATOR = System.getProperty("path.separator");

    private static final Pattern VERSION_PATTERN =
            Pattern.compile(".*SystemTestExecutor: \\d+\\.\\d+\\.\\d+.*", Pattern.DOTALL);

    // formatting:off
    private static final String VALID_SEED = "---\n" +
            "!creek/test\n" +
            "value: seed value";

    private static final String VALID_INPUT = "---\n" +
            "!creek/test\n" +
            "value: input value";

    private static final String THROWING_INPUT = "---\n" +
            "!creek/test\n" +
            "value: should throw";

    private static final String PASSING_EXPECTATION = "---\n" +
            "!creek/test\n" +
            "value: output value";

    private static final String FAILING_EXPECTATION = "---\n" +
            "!creek/test\n" +
            "value: should fail";

    private static final String THROWING_EXPECTATION = "---\n" +
            "!creek/test\n" +
            "value: should throw";

    private static final String VALID_SUITE = "---\n"
                    + "name: suite name\n"
                    + "services:\n"
                    + "  - test-service\n"
                    + "tests:\n"
                    + "  - name: test 0\n"
                    + "    inputs:\n"
                    + "      - input-1\n"
                    + "    expectations:\n"
                    + "      - expectation-1\n";
    // formatting:on

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
                is("There were failing tests. See the report at: " + resultDir.toUri()));
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
                "---\n"
                        + "name: suite name\n"
                        + "services:\n"
                        + "  - test-service\n"
                        + "tests:\n"
                        + "  - name: test 0\n"
                        + "    disabled:\n"
                        + "      reason: for testing\n"
                        + "    inputs:\n"
                        + "      - input-1\n"
                        + "    expectations:\n"
                        + "      - expectation-1\n";

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
        // Given:
        final String[] args = minimalArgs();

        // When:
        runExecutor(args);

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
    void shouldReportEnsureResourceFailures() {
        // Given:
        givenEnv(
                TestCreekExtensionProvider.ENV_FAIL_ENSURE_RESOURCE_ID,
                TestServiceDescriptor.UnownedInput1.id());

        // When:
        final int exitCode = runExecutor(minimalArgs());

        // Then:
        assertThat(
                stdOut.get(),
                containsString(
                        "Ensure failed for resource: " + TestServiceDescriptor.UnownedInput1.id()));
        assertThat(
                stdErr.get(),
                containsString(
                        "There were failing tests. See the report at: " + resultDir.toUri()));
        assertThat(exitCode, is(1));
    }

    @Test
    void shouldReportResourceValidationFailure() {
        // Given:
        givenEnv(TestCreekExtensionProvider.ENV_FAIL_VALIDATE_RESOURCE_ID, OwnedOutput.id());

        // When:
        final int exitCode = runExecutor(minimalArgs());

        // Then:
        assertThat(
                stdErr.get(),
                containsString("Validation failed for resource group: " + OwnedOutput.id()));
        assertThat(exitCode, is(2));
    }

    @Test
    void shouldNotCheckInWithDebuggingEnabled() {
        assertThat("Do not check in with debugging enabled", !DEBUG);
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
            throw new AssertionError("Error executing: " + cmd, e);
        }
    }

    private List<String> buildCommand(final String[] javaArgs, final String[] cmdArgs) {
        final List<String> cmd = new ArrayList<>(List.of("java"));
        if (DEBUG) {
            cmd.addAll(remoteDebugArguments());
        }
        findConvergeAgentCmdLineArg().ifPresent(cmd::add);

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

    private static Optional<String> findConvergeAgentCmdLineArg() {
        final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return runtimeMXBean.getInputArguments().stream()
                .filter(arg -> arg.startsWith("-javaagent"))
                .filter(arg -> arg.contains("org.jacoco.agent"))
                .reduce((first, second) -> first);
    }
}

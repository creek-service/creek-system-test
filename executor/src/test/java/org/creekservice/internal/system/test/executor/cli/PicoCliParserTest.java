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
import static org.creekservice.internal.system.test.executor.cli.PicoCliParser.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.creekservice.api.system.test.executor.ExecutorOptions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class PicoCliParserTest {

    @Test
    void shouldReturnEmptyOnHelp() {
        // Given:
        final String[] args = {"--help"};

        // When:
        final Optional<?> result = parse(args);

        // Then:
        assertThat(result, is(Optional.empty()));
    }

    @Test
    void shouldReturnEmptyOnVersion() {
        // Given:
        final String[] args = {"--version"};

        // When:
        final Optional<?> result = parse(args);

        // Then:
        assertThat(result, is(Optional.empty()));
    }

    @Test
    void shouldThrowOnInvalidArgs() {
        // Given:
        final String[] args = minimalArgs("--unknown");

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> parse(args));

        // Then:
        assertThat(e.getMessage(), startsWith("Unknown option: '--unknown'"));
    }

    @Test
    void shouldParseMinimalSetWithDefaults() {
        // Given:
        final String[] args = minimalArgs();

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.map(ExecutorOptions::testDirectory).map(Path::toString),
                is(Optional.of("test/path")));
        assertThat(
                result.map(ExecutorOptions::resultDirectory).map(Path::toString),
                is(Optional.of("result/path")));
        assertThat(result.flatMap(ExecutorOptions::verifierTimeout), is(Optional.empty()));
        assertThat(
                result.map(ExecutorOptions::suitesFilter).map(f -> f.test(Paths.get("any"))),
                is(Optional.of(true)));
        assertThat(result.map(ExecutorOptions::echoOnly), is(Optional.of(false)));
    }

    @Test
    void shouldThrowIfTestPathNotProvided() {
        // Given:
        final String[] args = {"-rd", "result/path"};

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> parse(args));

        // Then:
        assertThat(
                e.getMessage(), containsString("Missing required option: '--test-directory=PATH'"));
        assertThat(e.getMessage(), containsString("Usage: SystemTestExecutor"));
    }

    @Test
    void shouldThrowIfResultPathNotProvided() {
        // Given:
        final String[] args = {"-td", "test/path"};

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> parse(args));

        // Then:
        assertThat(
                e.getMessage(),
                containsString("Missing required option: '--result-directory=PATH"));
        assertThat(e.getMessage(), containsString("Usage: SystemTestExecutor"));
    }

    @Test
    void shouldParseVerifierTimeout() {
        // Given:
        final String[] args = minimalArgs("-vt", "987");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.flatMap(ExecutorOptions::verifierTimeout),
                is(Optional.of(Duration.ofSeconds(987))));
    }

    @Test
    void shouldParseSuitePattern() {
        // Given:
        final String[] args = minimalArgs("-is", ".*include.*");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        final Predicate<Path> filter = result.get().suitesFilter();
        assertThat(filter.test(Path.of("/some/included/path")), is(true));
        assertThat(filter.test(Path.of("/some/excluded/path")), is(false));
    }

    @Test
    void shouldParseEchoOnly() {
        // Given:
        final String[] args = minimalArgs("--echo-only");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(result.map(ExecutorOptions::echoOnly), is(Optional.of(true)));
    }

    @Test
    void shouldDefaultToNoDebugInfo() {
        // Given:
        final String[] args = minimalArgs();

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.map(ExecutorOptions::serviceDebugInfo), is(Optional.of(Optional.empty())));
    }

    @Test
    void shouldHaveNoDebugInfoIfOnlyBasePortIsSet() {
        // Given:
        final String[] args = minimalArgs("--debug-service-port=4000");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.map(ExecutorOptions::serviceDebugInfo), is(Optional.of(Optional.empty())));
    }

    @Test
    void shouldParseDebugInfo() {
        // Given:
        final String[] args =
                minimalArgs(
                        "--debug-service-port=4000",
                        "--debug-service=a,b",
                        "--debug-service=c",
                        "--debug-service-instance=d-0,e-0",
                        "--debug-service-instance=f-0");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        final Optional<ExecutorOptions.ServiceDebugInfo> debugInfo =
                result.flatMap(ExecutorOptions::serviceDebugInfo);
        assertThat(
                debugInfo.map(ExecutorOptions.ServiceDebugInfo::baseServicePort),
                is(Optional.of(4000)));
        assertThat(
                debugInfo.map(ExecutorOptions.ServiceDebugInfo::serviceNames),
                is(Optional.of(Set.of("a", "b", "c"))));
        assertThat(
                debugInfo.map(ExecutorOptions.ServiceDebugInfo::serviceInstanceNames),
                is(Optional.of(Set.of("d-0", "e-0", "f-0"))));
    }

    @Test
    void shouldParseSingleEnv() {
        // Given:
        final String[] args = minimalArgs("--env=NAME=VALUE");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(result.map(ExecutorOptions::env), is(Optional.of(Map.of("NAME", "VALUE"))));
    }

    @Test
    void shouldParseSingleMultipleEnvInSingleParam() {
        // Given:
        final String[] args = minimalArgs("--env=NAME=VALUE,NAME2=V2");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.map(ExecutorOptions::env),
                is(Optional.of(Map.of("NAME", "VALUE", "NAME2", "V2"))));
    }

    @Test
    void shouldParseEnvWithSpaceInValue() {
        // Given:
        final String[] args = minimalArgs("--env=NAME=\"VAL UE\"");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(result.map(ExecutorOptions::env), is(Optional.of(Map.of("NAME", "VAL UE"))));
    }

    @Test
    void shouldParseSingleMultipleEnvInMultipleParams() {
        // Given:
        final String[] args = minimalArgs("--env=NAME=VALUE", "--env=NAME2=V2");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.map(ExecutorOptions::env),
                is(Optional.of(Map.of("NAME", "VALUE", "NAME2", "V2"))));
    }

    @Test
    void shouldParseSingleDebugEnv() {
        // Given:
        final String[] args = minimalArgs("--debug-env=NAME=VALUE", "--debug-service=a");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.flatMap(ExecutorOptions::serviceDebugInfo)
                        .map(ExecutorOptions.ServiceDebugInfo::env),
                is(Optional.of(Map.of("NAME", "VALUE"))));
    }

    @Test
    void shouldParseSingleMultipleDebugEnvInSingleParam() {
        // Given:
        final String[] args = minimalArgs("--debug-env=NAME=VALUE,NAME2=V2", "--debug-service=a");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.flatMap(ExecutorOptions::serviceDebugInfo)
                        .map(ExecutorOptions.ServiceDebugInfo::env),
                is(Optional.of(Map.of("NAME", "VALUE", "NAME2", "V2"))));
    }

    @Test
    void shouldParseDebugEnvWithSpaceInValue() {
        // Given:
        final String[] args = minimalArgs("--debug-env=NAME=\"VAL UE\"", "--debug-service=a");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.flatMap(ExecutorOptions::serviceDebugInfo)
                        .map(ExecutorOptions.ServiceDebugInfo::env),
                is(Optional.of(Map.of("NAME", "VAL UE"))));
    }

    @Test
    void shouldParseSingleMultipleDebugEnvInMultipleParams() {
        // Given:
        final String[] args =
                minimalArgs("--debug-env=NAME=VALUE", "-de=NAME2=V2", "--debug-service=a");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.flatMap(ExecutorOptions::serviceDebugInfo)
                        .map(ExecutorOptions.ServiceDebugInfo::env),
                is(Optional.of(Map.of("NAME", "VALUE", "NAME2", "V2"))));
    }

    @Test
    void shouldParseReadOnlyMount() {
        // Given:
        final String[] args = minimalArgs("--mount-read-only=/host/path=/container/path");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.map(ExecutorOptions::mountInfo).map(Collection::size), is(Optional.of(1)));

        final ExecutorOptions.MountInfo mount = result.get().mountInfo().iterator().next();
        assertThat(mount.hostPath(), is(Path.of("/host/path")));
        assertThat(mount.containerPath(), is(Path.of("/container/path")));
        assertThat(mount.readOnly(), is(true));
    }

    @Test
    void shouldParseWritableMount() {
        // Given:
        final String[] args = minimalArgs("--mount-writable=host/path=container/path");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.map(ExecutorOptions::mountInfo).map(Collection::size), is(Optional.of(1)));

        final ExecutorOptions.MountInfo mount = result.get().mountInfo().iterator().next();
        assertThat(mount.hostPath(), is(Path.of("host/path")));
        assertThat(mount.containerPath(), is(Path.of("container/path")));
        assertThat(mount.readOnly(), is(false));
    }

    @Test
    void shouldParseMultipleMounts() {
        // Given:
        final String[] args =
                minimalArgs(
                        "--mount-read-only=/host/path=/container/path",
                        "--mount-read-only=/host/path2=/container/path2",
                        "--mount-writable=host/path=container/path",
                        "--mount-writable=host/path2=container/path2");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.map(ExecutorOptions::mountInfo).map(Collection::size), is(Optional.of(4)));
    }

    @Test
    void shouldThrowOnReadOnlyMountWithEmptyHostPath() {
        // Given:
        final String[] args = minimalArgs("--mount-read-only==container/path");

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> parse(args));

        // Then:
        assertThat(
                e.getMessage(),
                containsString("java.lang.IllegalArgumentException: Host path can not be empty"));
    }

    @Test
    void shouldThrowOnReadOnlyMountWithEmptyContainerPath() {
        // Given:
        final String[] args = minimalArgs("--mount-read-only=host/path=");

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> parse(args));

        // Then:
        assertThat(
                e.getMessage(),
                containsString(
                        "java.lang.IllegalArgumentException: Container path can not be empty"));
    }

    @Test
    void shouldThrowOnWriteableMountWithEmptyHostPath() {
        // Given:
        final String[] args = minimalArgs("--mount-writable==container/path");

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> parse(args));

        // Then:
        assertThat(
                e.getMessage(),
                containsString("java.lang.IllegalArgumentException: Host path can not be empty"));
    }

    @Test
    void shouldThrowOnWriteableMountWithEmptyContainerPath() {
        // Given:
        final String[] args = minimalArgs("--mount-writable=host/path=");

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> parse(args));

        // Then:
        assertThat(
                e.getMessage(),
                containsString(
                        "java.lang.IllegalArgumentException: Container path can not be empty"));
    }

    @Test
    void shouldDefaultBaseServicePort() {
        // Given:
        final String[] args = minimalArgs("--debug-service=a");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.flatMap(ExecutorOptions::serviceDebugInfo)
                        .map(ExecutorOptions.ServiceDebugInfo::baseServicePort),
                is(Optional.of(8000)));
    }

    @Test
    void shouldThrowOnInvalidBaseServicePort() {
        // Given:
        final String[] args = minimalArgs("--debug-service-port=0", "--debug-service=a");

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> parse(args));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Invalid value '0' for option '--debug-service-port': value must be"
                                + " positive."));
    }

    @Test
    void shouldDeduplicateServiceNames() {
        // Given:
        final String[] args = minimalArgs("--debug-service=a,a");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.flatMap(ExecutorOptions::serviceDebugInfo)
                        .map(ExecutorOptions.ServiceDebugInfo::serviceNames),
                is(Optional.of(Set.of("a"))));
    }

    @Test
    void shouldDeduplicateInstanceNames() {
        // Given:
        final String[] args = minimalArgs("--debug-service-instance=a,a");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.flatMap(ExecutorOptions::serviceDebugInfo)
                        .map(ExecutorOptions.ServiceDebugInfo::serviceInstanceNames),
                is(Optional.of(Set.of("a"))));
    }

    @Test
    void shouldImplementToStringOnMinimalOptions() {
        // Given:
        final String[] args = minimalArgs();

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.map(Object::toString),
                is(
                        Optional.of(
                                "--test-directory=test/path"
                                        + lineSeparator()
                                        + "--result-directory=result/path"
                                        + lineSeparator()
                                        + "--verifier-timeout-seconds=<Not Set>"
                                        + lineSeparator()
                                        + "--include-suites=<Not Set>"
                                        + lineSeparator()
                                        + "--debug-service-port=<Not Set>"
                                        + lineSeparator()
                                        + "--debug-service=<Not Set>"
                                        + lineSeparator()
                                        + "--debug-service-instance=<Not Set>"
                                        + lineSeparator()
                                        + "--env=<Not Set>"
                                        + lineSeparator()
                                        + "--mount-read-only=<Not Set>"
                                        + lineSeparator()
                                        + "--mount-writable=<Not Set>")));
    }

    @Test
    void shouldImplementToStringOnFullOptions() {
        // Given:
        final String[] args =
                minimalArgs(
                        "--verifier-timeout-seconds=90",
                        "--include-suites=.*include.*",
                        "-dsp=12345",
                        "-ds=a,b",
                        "-dsi=a-0,b-1",
                        "-e=A=B,C=D",
                        "-mr=/a/b=/c,d/e=/f",
                        "-mw=/a=/b/c,/d=/f");

        // When:
        final Optional<ExecutorOptions> result = parse(args);

        // Then:
        assertThat(
                result.map(Object::toString),
                is(
                        Optional.of(
                                "--test-directory=test/path"
                                        + lineSeparator()
                                        + "--result-directory=result/path"
                                        + lineSeparator()
                                        + "--verifier-timeout-seconds=90"
                                        + lineSeparator()
                                        + "--include-suites=.*include.*"
                                        + lineSeparator()
                                        + "--debug-service-port=12345"
                                        + lineSeparator()
                                        + "--debug-service=a,b"
                                        + lineSeparator()
                                        + "--debug-service-instance=a-0,b-1"
                                        + lineSeparator()
                                        + "--env=A=B,C=D"
                                        + lineSeparator()
                                        + "--mount-read-only=/a/b=/c,d/e=/f"
                                        + lineSeparator()
                                        + "--mount-writable=/a=/b/c,/d=/f")));
    }

    private static String[] minimalArgs(final String... additional) {
        final List<String> args =
                new ArrayList<>(List.of("-td", "test/path", "-rd", "result/path"));
        args.addAll(List.of(additional));
        return args.toArray(String[]::new);
    }
}

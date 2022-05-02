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

package org.creekservice.internal.system.test.executor;

import static java.lang.System.lineSeparator;
import static org.creekservice.internal.system.test.executor.PicoCliParser.parse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
                                        + "--include-suites=<Not Set>")));
    }

    @Test
    void shouldImplementToStringOnFullOptions() {
        // Given:
        final String[] args =
                minimalArgs("--verifier-timeout-seconds=90", "--include-suites=.*include.*");

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
                                        + "--include-suites=.*include.*")));
    }

    private static String[] minimalArgs(final String... additional) {
        final List<String> args =
                new ArrayList<>(List.of("-td", "test/path", "-rd", "result/path"));
        args.addAll(List.of(additional));
        return args.toArray(String[]::new);
    }
}

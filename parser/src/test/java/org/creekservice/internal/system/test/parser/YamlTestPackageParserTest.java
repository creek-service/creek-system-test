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

package org.creekservice.internal.system.test.parser;

import static org.creekservice.api.system.test.extension.model.ModelType.expectation;
import static org.creekservice.api.system.test.extension.model.ModelType.input;
import static org.creekservice.api.system.test.extension.model.ModelType.seed;
import static org.creekservice.api.system.test.model.TestCase.testCase;
import static org.creekservice.api.system.test.model.TestPackage.testPackage;
import static org.creekservice.api.system.test.model.TestSuite.testSuite;
import static org.creekservice.api.test.util.TestPaths.ensureDirectories;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.creekservice.api.system.test.extension.model.Expectation;
import org.creekservice.api.system.test.extension.model.Input;
import org.creekservice.api.system.test.extension.model.ModelType;
import org.creekservice.api.system.test.extension.model.Seed;
import org.creekservice.api.system.test.model.TestCaseDef;
import org.creekservice.api.system.test.model.TestPackage;
import org.creekservice.api.system.test.model.TestSuiteDef;
import org.creekservice.api.system.test.parser.TestPackageParser;
import org.creekservice.api.system.test.parser.TestPackageParsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class YamlTestPackageParserTest {

    private static final String INVALID_YAML =
            "I would cause a parsing exception. " + "But I won't as I won't be parsed... right";
    private static final String VALID_SEED_YAML = "---\n'@type': test\nseed: seed stuff";
    private static final String VALID_INPUTS_YAML = "---\n'@type': test\ninput: input stuff";
    private static final String VALID_EXPECTATIONS_YAML =
            "---\n'@type': test\noutput: output stuff";

    private static final String VALID_TEST_YAML =
            "---\n"
                    + "name: suite name\n"
                    + "services:\n"
                    + "  - service_a\n"
                    + "tests:\n"
                    + "  - name: test 1\n"
                    + "    inputs:\n"
                    + "      - input-1\n"
                    + "    expectations:\n"
                    + "      - expectation-1";

    public static final List<ModelType<?>> EXTENSIONS =
            List.of(
                    seed(TestSeed.class),
                    input(TestInput.class),
                    expectation(TestExpectation.class));

    private static final ObjectMapper MAPPER = SystemTestMapper.create(EXTENSIONS);

    private static final Seed SEED = parse(VALID_SEED_YAML, Seed.class);
    private static final Input INPUT = parse(VALID_INPUTS_YAML, Input.class);
    private static final Expectation EXPECTATION =
            parse(VALID_EXPECTATIONS_YAML, Expectation.class);
    private static final TestSuiteDef TEST_SUITE_DEF = parse(VALID_TEST_YAML, TestSuiteDef.class);
    private static final TestCaseDef TEST_CASE_DEF = TEST_SUITE_DEF.tests().get(0);

    @TempDir private Path root;
    @Mock private TestPackageParser.Observer observer;
    @Mock private Predicate<Path> predicate;
    private TestPackageParser parser;
    private String locationPrefix;

    @BeforeEach
    void setUp() {
        parser = TestPackageParsers.yamlParser(EXTENSIONS, observer);
        locationPrefix = root.toAbsolutePath().toUri().toString();

        when(predicate.test(any())).thenReturn(true);
    }

    @Test
    void shouldReturnEmptyIfNoExpectationsDirectory() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        // not expectation directory
        givenFile(root.resolve("suite.yml"), VALID_TEST_YAML);

        // When:
        final Optional<TestPackage> result = parser.parse(root, predicate);

        // Then:
        assertThat(result, is(Optional.empty()));
    }

    @Test
    void shouldReturnEmptyIfNoTestSuites() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        // no test suites

        // When:
        final Optional<TestPackage> result = parser.parse(root, predicate);

        // Then:
        assertThat(result, is(Optional.empty()));
    }

    @Test
    void shouldLoadMinimalTestPackage() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        givenFile(root.resolve("suite.yml"), VALID_TEST_YAML);

        // When:
        final Optional<TestPackage> result = parser.parse(root, predicate);

        // Then:
        assertThat(
                result,
                is(
                        Optional.of(
                                testPackage(
                                        root,
                                        List.of(),
                                        List.of(
                                                testSuite(
                                                        List.of(
                                                                testCase(
                                                                        List.of(INPUT),
                                                                        List.of(EXPECTATION),
                                                                        TEST_CASE_DEF)),
                                                        TEST_SUITE_DEF))))));
    }

    @Test
    void shouldLoadSeedData() {
        // Given:
        givenFile(root.resolve("seed/seed-1.yml"), VALID_SEED_YAML);
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        givenFile(root.resolve("suite.yml"), VALID_TEST_YAML);

        // When:
        final Optional<TestPackage> result = parser.parse(root, predicate);

        // Then:
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().seedData(), contains(SEED));
    }

    @Test
    void shouldWorkWithYamlOrYmlExtensions() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yaml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        givenFile(root.resolve("suite.yaml"), VALID_TEST_YAML);

        // When:
        final Optional<TestPackage> result = parser.parse(root, predicate);

        // Then:
        assertThat(result.isPresent(), is(true));
    }

    @Test
    void shouldSetLocations() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        givenFile(root.resolve("suite.yml"), VALID_TEST_YAML);

        // When:
        final Optional<TestPackage> result = parser.parse(root, predicate);

        assertThat(result.isPresent(), is(true));
        final TestPackage pkg = result.get();

        assertThat(
                pkg.suites().get(0).def().location().toString(),
                is(locationPrefix + "suite.yml:2"));
        assertThat(
                pkg.suites().get(0).tests().get(0).def().location().toString(),
                is(locationPrefix + "suite.yml:6"));
        assertThat(
                pkg.suites().get(0).def().tests().get(0).location().toString(),
                is(locationPrefix + "suite.yml:6"));
    }

    @Test
    void shouldSetParentRefs() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        givenFile(root.resolve("suite.yml"), VALID_TEST_YAML);

        // When:
        final Optional<TestPackage> result = parser.parse(root, predicate);

        assertThat(result.isPresent(), is(true));
        final TestPackage pkg = result.get();

        assertThat(pkg.suites().get(0).pkg(), is(sameInstance(pkg)));
        assertThat(
                pkg.suites().get(0).tests().get(0).suite(), is(sameInstance(pkg.suites().get(0))));
    }

    @Test
    void shouldThrowOnMissingInput() {
        // Given:
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        givenFile(root.resolve("suite.yml"), VALID_TEST_YAML);

        // When:
        final Exception e =
                assertThrows(TestLoadFailedException.class, () -> parser.parse(root, predicate));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Error in suite 'suite name':'test 1': "
                                + "Missing dependency: input-1, "
                                + "referenced: "
                                + locationPrefix
                                + "suite.yml:8"));
    }

    @Test
    void shouldThrowOnMissingExpectation() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        ensureDirectories(root.resolve("expectations"));
        givenFile(root.resolve("suite.yml"), VALID_TEST_YAML);

        // When:
        final Exception e =
                assertThrows(TestLoadFailedException.class, () -> parser.parse(root, predicate));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Error in suite 'suite name':'test 1': "
                                + "Missing dependency: expectation-1, "
                                + "referenced: "
                                + locationPrefix
                                + "suite.yml:10"));
    }

    @Test
    void shouldInvokeObserveOnUnusedInput() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("inputs/unused-input.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        givenFile(root.resolve("suite.yml"), VALID_TEST_YAML);

        // When:
        parser.parse(root, predicate);

        // Then:
        verify(observer).unusedDependencies(root, List.of(root.resolve("inputs/unused-input.yml")));
    }

    @Test
    void shouldInvokeObserveOnUnusedExpectation() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        givenFile(root.resolve("expectations/unused-expectation.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("suite.yml"), VALID_TEST_YAML);

        // When:
        parser.parse(root, predicate);

        // Then:
        verify(observer)
                .unusedDependencies(
                        root, List.of(root.resolve("expectations/unused-expectation.yml")));
    }

    @Test
    void shouldLazyLoadInputs() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("inputs/invalid-input.yml"), INVALID_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        givenFile(root.resolve("suite.yml"), VALID_TEST_YAML);

        // When:
        parser.parse(root, predicate);

        // Then: did not throw.
    }

    @Test
    void shouldLazyLoadExpectations() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        givenFile(root.resolve("expectations/invalid-expectations.yml"), INVALID_YAML);
        givenFile(root.resolve("suite.yml"), VALID_TEST_YAML);

        // When:
        parser.parse(root, predicate);

        // Then: did not throw.
    }

    @Test
    void shouldIgnoreNonYamlFiles() {
        // Given:
        givenFile(root.resolve("seed/ignore.not-yml"), INVALID_YAML);
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("inputs/ignore.not-yml"), INVALID_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        givenFile(root.resolve("expectations/ignore.not-yaml"), INVALID_YAML);
        givenFile(root.resolve("suite.yml"), VALID_TEST_YAML);

        // When:
        parser.parse(root, predicate);

        // Then: did not throw.
    }

    @Test
    void shouldFilterOutSuitesBeforeLoad() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), VALID_EXPECTATIONS_YAML);
        givenFile(root.resolve("suite-1.yml"), INVALID_YAML);
        givenFile(root.resolve("suite-2.yml"), VALID_TEST_YAML);
        givenFile(root.resolve("suite-3.yml"), VALID_TEST_YAML);

        when(predicate.test(any()))
                .thenAnswer(inv -> inv.<Path>getArgument(0).endsWith("suite-2.yml"));

        // When:
        final Optional<TestPackage> result = parser.parse(root, predicate);

        // Then: did not throw.
        assertThat(result.isPresent(), is(true));
        assertThat(result.get().suites(), hasSize(1));
        assertThat(
                result.get().suites().get(0).def().location().toString(),
                containsString("suite-2.yml"));
    }

    @Test
    void shouldThrowOnUnParsableFile() {
        // Given:
        givenFile(root.resolve("inputs/input-1.yml"), VALID_INPUTS_YAML);
        givenFile(root.resolve("expectations/expectation-1.yml"), INVALID_YAML);
        givenFile(root.resolve("suite-1.yml"), VALID_TEST_YAML);

        // When:
        final Exception e =
                assertThrows(InvalidTestFileException.class, () -> parser.parse(root, predicate));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Error in suite 'suite name':'test 1': "
                                + "Failed to load Expectation from "
                                + locationPrefix
                                + "expectations/expectation-1.yml"
                                + System.lineSeparator()
                                + "Please check the file is valid."
                                + System.lineSeparator()
                                + "Could not resolve subtype of"));
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private void givenFile(final Path file, final String content) {
        try {
            final Path path = root.resolve(file);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        } catch (final IOException e) {
            throw new AssertionError("Failed to write test data to " + file, e);
        }
    }

    private static <T> T parse(final String yml, final Class<T> type) {
        try {
            return MAPPER.readValue(yml, type);
        } catch (final Exception e) {
            throw new AssertionError("Failed to parse yaml: " + yml, e);
        }
    }

    @SuppressWarnings({"unused", "checkstyle:RedundantModifier"})
    public static final class TestSeed implements Seed {

        private final String seed;

        public TestSeed(@JsonProperty("seed") final String seed) {
            this.seed = seed;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final TestSeed testSeed = (TestSeed) o;
            return Objects.equals(seed, testSeed.seed);
        }

        @Override
        public int hashCode() {
            return Objects.hash(seed);
        }
    }

    @SuppressWarnings({"unused", "checkstyle:RedundantModifier"})
    public static final class TestInput implements Input {

        private final String input;

        public TestInput(@JsonProperty("input") final String input) {
            this.input = input;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final TestInput testInput = (TestInput) o;
            return Objects.equals(input, testInput.input);
        }

        @Override
        public int hashCode() {
            return Objects.hash(input);
        }
    }

    @SuppressWarnings({"unused", "checkstyle:RedundantModifier"})
    public static final class TestExpectation implements Expectation {

        private final String output;

        public TestExpectation(@JsonProperty("output") final String output) {
            this.output = output;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final TestExpectation that = (TestExpectation) o;
            return Objects.equals(output, that.output);
        }

        @Override
        public int hashCode() {
            return Objects.hash(output);
        }
    }
}

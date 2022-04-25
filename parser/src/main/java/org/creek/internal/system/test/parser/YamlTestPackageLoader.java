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

package org.creek.internal.system.test.parser;

import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;
import static org.creek.api.system.test.model.TestPackage.testPackage;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creek.api.base.type.Suppliers;
import org.creek.api.system.test.extension.model.BaseRef;
import org.creek.api.system.test.extension.model.Expectation;
import org.creek.api.system.test.extension.model.Input;
import org.creek.api.system.test.extension.model.ModelType;
import org.creek.api.system.test.extension.model.Seed;
import org.creek.api.system.test.model.TestCase;
import org.creek.api.system.test.model.TestCaseDef;
import org.creek.api.system.test.model.TestPackage;
import org.creek.api.system.test.model.TestSuite;
import org.creek.api.system.test.model.TestSuiteDef;
import org.creek.api.system.test.parser.TestPackageLoader;

/**
 * Parse test suites from a directory structure of Yaml files.
 *
 * <p>Expected directory structure:
 *
 * <pre>
 * root
 *   |--seed
 *   |--inputs
 *   |--expectations
 *   |--tests
 * </pre>
 */
public final class YamlTestPackageLoader implements TestPackageLoader {

    private static final PathMatcher YAML_MATCHER =
            FileSystems.getDefault().getPathMatcher("regex:.*\\.yml|.*\\.yaml");

    private static final Path SEED = Paths.get("seed");
    private static final Path INPUTS = Paths.get("inputs");
    private static final Path EXPECTATIONS = Paths.get("expectations");
    private static final Path SUITES = Paths.get("tests");

    private final ObjectMapper mapper;
    private final Observer observer;

    public YamlTestPackageLoader(
            final Collection<ModelType<?>> modelExtensions, final Observer observer) {
        this.mapper = SystemTestMapper.create(modelExtensions);
        this.observer = requireNonNull(observer, "observer");
    }

    @Override
    public Optional<TestPackage> load(final Path path, final Predicate<Path> predicate) {
        final List<Seed> seedData =
                loadDir(path, SEED, Seed.class).map(LazyFile::content).collect(Collectors.toList());

        final Map<String, LazyFile<Input>> inputs =
                loadDir(path, INPUTS, Input.class)
                        .collect(Collectors.toMap(LazyFile::id, Function.identity()));

        final Map<String, LazyFile<Expectation>> expectations =
                loadDir(path, EXPECTATIONS, Expectation.class)
                        .collect(Collectors.toMap(LazyFile::id, Function.identity()));

        final List<TestSuite.Builder> suites =
                loadDir(path, SUITES, TestSuiteDef.class)
                        .filter(f -> predicate.test(f.path()))
                        .map(f -> testSuiteBuilder(f.content(), inputs, expectations))
                        .collect(Collectors.toUnmodifiableList());

        if (suites.isEmpty()) {
            return Optional.empty();
        }

        warnOnUnusedDependencies(path, inputs, expectations);

        return Optional.of(testPackage(path, seedData, suites));
    }

    private <T> Stream<LazyFile<T>> loadDir(final Path root, final Path dir, final Class<T> type) {
        return ymlFilesInDir(root.resolve(dir)).stream()
                .map(path -> new LazyFile<>(id(path), path, () -> parse(path, type)));
    }

    @SuppressFBWarnings(
            value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
            justification = "false negative")
    private List<Path> ymlFilesInDir(final Path dir) {
        if (!Files.exists(dir)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.walk(dir, 1)) {
            return stream.filter(Files::isRegularFile)
                    .filter(YAML_MATCHER::matches)
                    .collect(Collectors.toUnmodifiableList());
        } catch (final IOException e) {
            throw new TestLoadFailedException("Error accessing directory " + dir, e);
        }
    }

    private <T> T parse(final Path path, final Class<T> type) {
        try {
            return mapper.readValue(path.toFile(), type);
        } catch (final Exception e) {
            throw new InvalidTestFileException(
                    "Failed to load "
                            + type.getSimpleName()
                            + " from "
                            + path.toUri()
                            + lineSeparator()
                            + "Please check the file is valid."
                            + lineSeparator()
                            + e.getMessage(),
                    e);
        }
    }

    private static TestSuite.Builder testSuiteBuilder(
            final TestSuiteDef def,
            final Map<String, LazyFile<Input>> inputs,
            final Map<String, LazyFile<Expectation>> expectations) {
        try {
            final List<TestCase.Builder> testCases =
                    def.tests().stream()
                            .map(testCaseDef -> testCaseBuilder(testCaseDef, inputs, expectations))
                            .collect(Collectors.toList());

            return TestSuite.testSuite(testCases, def);
        } catch (final InvalidTestFileException | MissingDependencyException e) {
            throw new InvalidTestFileException(
                    "Error in suite '" + def.name() + "':" + e.getMessage(), e);
        }
    }

    private static TestCase.Builder testCaseBuilder(
            final TestCaseDef def,
            final Map<String, LazyFile<Input>> inputs,
            final Map<String, LazyFile<Expectation>> expectations) {
        try {
            final List<Input> testInputs =
                    def.inputs().stream()
                            .map(i -> findDependency(i, inputs))
                            .collect(Collectors.toList());

            final List<Expectation> testExpectations =
                    def.expectations().stream()
                            .map(e -> findDependency(e, expectations))
                            .collect(Collectors.toList());

            return TestCase.testCase(testInputs, testExpectations, def);
        } catch (final InvalidTestFileException | MissingDependencyException e) {
            throw new InvalidTestFileException("'" + def.name() + "': " + e.getMessage(), e);
        }
    }

    private void warnOnUnusedDependencies(
            final Path path,
            final Map<String, LazyFile<Input>> inputs,
            final Map<String, LazyFile<Expectation>> expectations) {
        final List<Path> unused =
                Stream.concat(inputs.values().stream(), expectations.values().stream())
                        .filter(LazyFile::unused)
                        .map(LazyFile::path)
                        .map(Path::toAbsolutePath)
                        .collect(Collectors.toUnmodifiableList());

        if (!unused.isEmpty()) {
            observer.unusedDependencies(path, unused);
        }
    }

    private static <T> T findDependency(final BaseRef ref, final Map<String, LazyFile<T>> known) {
        final LazyFile<T> dependency = known.get(ref.id());
        if (dependency == null) {
            throw new MissingDependencyException(ref);
        }
        return dependency.content();
    }

    @SuppressFBWarnings(
            value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "path always has at least one part and extension")
    private static String id(final Path path) {
        final String fileName = path.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    private static final class LazyFile<T> {

        private final String id;
        private final Path path;
        private final Supplier<T> content;
        private boolean unused = true;

        LazyFile(final String id, final Path path, final Supplier<T> content) {
            this.id = requireNonNull(id, "id");
            this.path = requireNonNull(path, "path");
            this.content = Suppliers.memoize(content);
        }

        String id() {
            return id;
        }

        Path path() {
            return path;
        }

        T content() {
            unused = false;
            return content.get();
        }

        boolean unused() {
            return unused;
        }
    }
}

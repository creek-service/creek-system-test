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

package org.creekservice.api.system.test.parser;

import static org.creekservice.api.system.test.parser.TestPackagesLoader.testPackagesLoader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.system.test.model.TestPackage;
import org.creekservice.api.system.test.parser.TestPackagesLoader.WalkerFactory;
import org.creekservice.api.test.util.TestPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@SuppressFBWarnings(
        value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE",
        justification = "false positive")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestPackagesLoaderTest {

    @TempDir private Path root;
    @Mock private TestPackageParser parser;
    @Mock private Predicate<Path> predicate;
    @Mock private WalkerFactory walkerFactory;
    @Mock private TestPackage package1;
    @Mock private TestPackage package2;
    @Mock private TestPackage package3;
    private TestPackagesLoader loader;

    @BeforeEach
    void setUp() throws Exception {
        loader = new TestPackagesLoader(root, parser, predicate, walkerFactory);

        when(predicate.test(any())).thenReturn(true);
        when(walkerFactory.walk(any())).thenAnswer(inv -> Files.walk(inv.getArgument(0)));
    }

    @Test
    void shouldThrowIfRootDirDoesNotExist() {
        // Given:
        final Path path = root.resolve("I don't exist");
        loader = testPackagesLoader(path, parser, predicate);

        // When:
        final Exception e = assertThrows(RuntimeException.class, loader::stream);

        // Then:
        assertThat(e.getMessage(), is("Failed to parse test packages under " + path));
        assertThat(e.getCause(), is(instanceOf(NoSuchFileException.class)));
    }

    @Test
    void shouldThrowIfRootDirIsNotADirectory() {
        // Given:
        final Path path = root.resolve("a_file");
        TestPaths.write(path, "");
        loader = testPackagesLoader(path, parser, predicate);

        // When:
        final Exception e = assertThrows(RuntimeException.class, loader::stream);

        // Then:
        assertThat(e.getMessage(), is("Failed to parse test packages under " + path));
        assertThat(e.getCause().getMessage(), is("Not a directory: " + path));
    }

    @Test
    void shouldReturnEmptyForEmptyDir() {
        // When:
        try (Stream<TestPackage> result = loader.stream()) {

            // Then:
            assertThat(result.collect(Collectors.toList()), is(empty()));
        }
    }

    @Test
    void shouldCloseWalkerOnClose() throws Exception {
        // Given:
        final Stream<Path> walker = spy(Stream.of());
        reset(walkerFactory);
        when(walkerFactory.walk(any())).thenReturn(walker);
        final Stream<TestPackage> result = loader.stream();

        // When:
        result.close();

        // Then:
        verify(walker).close();
    }

    @Test
    void shouldLoadFromRoot() {
        // Given:
        when(parser.parse(root, predicate)).thenReturn(Optional.of(package1));

        // When:
        try (Stream<TestPackage> result = loader.stream()) {

            // Then:
            assertThat(result.collect(Collectors.toList()), is(List.of(package1)));
        }
    }

    @Test
    void shouldRecursivelyLoad() {
        // Given:
        givenDirectory("sub");
        givenDirectory("sub/dir");

        when(parser.parse(root, predicate)).thenReturn(Optional.of(package1));
        when(parser.parse(root.resolve("sub"), predicate)).thenReturn(Optional.of(package2));
        when(parser.parse(root.resolve("sub/dir"), predicate)).thenReturn(Optional.of(package3));

        // When:
        try (Stream<TestPackage> result = loader.stream()) {

            // Then:
            assertThat(
                    result.collect(Collectors.toList()), is(List.of(package1, package2, package3)));
        }
    }

    @Test
    void shouldIgnoreDirectoriesThatDoNotContainPackage() {
        // Given:
        givenDirectory("sub");
        givenDirectory("sub/dir");

        when(parser.parse(root.resolve("sub/dir"), predicate)).thenReturn(Optional.of(package2));

        // When:
        try (Stream<TestPackage> result = loader.stream()) {

            // Then:
            assertThat(result.collect(Collectors.toList()), is(List.of(package2)));
        }
    }

    @SuppressWarnings("unused")
    @Test
    void shouldFilterPackages() {
        // When:
        try (Stream<TestPackage> result = loader.stream()) {

            final List<TestPackage> ignored = result.collect(Collectors.toList());

            // Then:
            verify(parser).parse(root, predicate);
        }
    }

    @Test
    void shouldLoadLazily() {
        // When:
        try (Stream<TestPackage> ignored = loader.stream()) {
            // Then:
            verify(parser, never()).parse(any(), any());
        }
    }

    private void givenDirectory(final String relativePath) {
        TestPaths.ensureDirectories(root.resolve(relativePath));
    }
}

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

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.model.TestPackage;

/** Walks directory structure looking for test packages to parse. */
public final class TestPackagesLoader {

    private final Path rootDir;
    private final Function<Path, Optional<TestPackage>> parser;
    private final WalkerFactory walkerFactory;

    /**
     * Create a test package loader.
     *
     * @param rootDir the root directory under which to look, recursively, for test packages.
     * @param parser the parser to use to parse test packages.
     * @param predicate the filter used to control which paths to look under for test packages.
     * @return the loader.
     */
    public static TestPackagesLoader testPackagesLoader(
            final Path rootDir, final TestPackageParser parser, final Predicate<Path> predicate) {
        return new TestPackagesLoader(rootDir, parser, predicate, Files::walk);
    }

    @VisibleForTesting
    TestPackagesLoader(
            final Path rootDir,
            final TestPackageParser parser,
            final Predicate<Path> predicate,
            final WalkerFactory walkerFactory) {
        this.rootDir = requireNonNull(rootDir, "rootDir");
        this.parser = path -> requireNonNull(parser, "parser").parse(path, predicate);
        this.walkerFactory = requireNonNull(walkerFactory, "walkerFactory");
    }

    /**
     * @return the lazily loaded stream of test packages found under the loaders root directory.
     *     Note: the returned stream must be closed to release filesystem resources. closed in main
     *     code
     */
    public Stream<TestPackage> stream() {
        try {
            if (Files.exists(rootDir) && !Files.isDirectory(rootDir)) {
                throw new IOException("Not a directory: " + rootDir);
            }

            final Stream<Path> walker = walkerFactory.walk(rootDir);

            return walker.onClose(walker::close)
                    .filter(Files::isDirectory)
                    .map(parser)
                    .flatMap(Optional::stream);
        } catch (final IOException e) {
            throw new SystemTestLoadFailedException(
                    "Failed to parse test packages under " + rootDir, e);
        }
    }

    @VisibleForTesting
    interface WalkerFactory {
        Stream<Path> walk(Path start) throws IOException;
    }

    private static final class SystemTestLoadFailedException extends RuntimeException {
        SystemTestLoadFailedException(final String msg, final Throwable cause) {
            super(msg, cause);
        }
    }
}

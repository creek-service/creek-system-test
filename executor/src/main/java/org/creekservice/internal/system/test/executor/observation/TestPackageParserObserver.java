/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.internal.system.test.executor.observation;

import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.creekservice.api.system.test.parser.TestPackageParser;
import org.slf4j.Logger;

/**
 * Implementation of {@link TestPackageParser.Observer} that logs a warning on unused test files.
 */
public final class TestPackageParserObserver implements TestPackageParser.Observer {

    private final Logger logger;

    /**
     * @param logger the logger to log to.
     */
    public TestPackageParserObserver(final Logger logger) {
        this.logger = requireNonNull(logger, "logger");
    }

    @Override
    public void unusedDependencies(final Path packagePath, final List<Path> unused) {
        logger.warn(
                "Unused dependencies in test package."
                        + lineSeparator()
                        + "package location: "
                        + packagePath
                        + lineSeparator()
                        + "used dependencies: "
                        + unused.stream()
                                .map(Path::toUri)
                                .map(URI::toString)
                                .collect(
                                        Collectors.joining(
                                                lineSeparator() + "\t",
                                                lineSeparator() + "\t",
                                                "")));
    }
}

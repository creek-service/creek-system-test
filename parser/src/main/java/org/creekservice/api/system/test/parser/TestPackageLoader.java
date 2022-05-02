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


import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import org.creekservice.api.system.test.model.TestPackage;

/**
 * Loader of test suites.
 *
 * <p>See {@link TestPackageLoaders} for factory methods to access a loader instance.
 */
public interface TestPackageLoader {

    /**
     * Load test suites for the supplied {@code path} path, filtered by the supplied {@code
     * predicate}.
     *
     * @param path the path to load the test suites from
     * @param predicate called for each test suite found under {@code path} to limit which suites
     *     are returned.
     * @return the test package if the {@code path} contained one.
     */
    Optional<TestPackage> load(Path path, Predicate<Path> predicate);

    interface Observer {

        /**
         * One or more inputs or expectations in the test package were not used.
         *
         * @param path the root path of the package.
         * @param unused the list of unused dependency files.
         */
        void unusedDependencies(Path path, List<Path> unused);
    }
}

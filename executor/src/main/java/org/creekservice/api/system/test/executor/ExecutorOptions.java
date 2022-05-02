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


import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Predicate;

/** Options to control the {@link SystemTestExecutor}. */
public interface ExecutorOptions {

    /** @return the root directory to search for test packages to run. */
    Path testDirectory();

    /** @return the directory to output test results to. */
    Path resultDirectory();

    /**
     * An optional custom verifier timeout.
     *
     * <p>The verifier timeout is the maximum amount of time the system tests will wait for a
     * defined expectation to be met. A longer timeout will mean tests have more time for
     * expectations to be met, but may run slower as a consequence.
     *
     * @return custom timeout.
     */
    default Optional<Duration> verifierTimeout() {
        return Optional.empty();
    }

    /** @return optional filter to limit which suites to run. */
    default Predicate<Path> suitesFilter() {
        return path -> true;
    }

    /**
     * @return If set, the generator will parse and echo its arguments and exit. Useful for testing.
     */
    default boolean echoOnly() {
        return false;
    }
}

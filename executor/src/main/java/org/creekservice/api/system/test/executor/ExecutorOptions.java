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
import java.util.Set;
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

    default Optional<ServiceDebugInfo> serviceDebugInfo() {
        return Optional.empty();
    }

    /**
     * Controls if any services, or service instances, should be configured to request the InteliJ
     * debugger to attach when they start up.
     *
     * @see <a
     *     href="https://github.com/creek-service/creek-system-test#debugging-system-tests">Service
     *     Debugging</a>
     */
    interface ServiceDebugInfo {

        int DEFAULT_ATTACH_ME_PORT = 7857;
        int DEFAULT_BASE_DEBUG_PORT = 8000;

        /**
         * The port on which the attachMe plugin is listening on.
         *
         * <p>This is the port the attachMe agent running within the microservice's process will
         * call out on to ask the debugger to attach.
         *
         * @return the port the attachMe plugin is listening on.
         */
        default int attachMePort() {
            return DEFAULT_ATTACH_ME_PORT;
        }

        /**
         * The base debug port.
         *
         * <p>The port the first service being debugged will listen on for the debugger to attach.
         * Subsequent services being debugged will use sequential port numbers.
         *
         * @return the base port number used for debugging.
         */
        default int baseServicePort() {
            return DEFAULT_BASE_DEBUG_PORT;
        }

        /**
         * The set of services to be debugged.
         *
         * @return set of services to debug.
         */
        Set<String> serviceNames();

        /**
         * The set of service instances to be debugged.
         *
         * <p>An instance name is the name of the service with a dash and the instance number
         * appended, e.g. {@code my-service-1}.
         *
         * @return set of service instances to debug.
         */
        Set<String> serviceInstanceNames();
    }
}

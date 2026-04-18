/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/** Options to control the {@link SystemTestExecutor}. */
public interface ExecutorOptions {

    /**
     * @return the root directory to search for test packages to run.
     */
    Path testDirectory();

    /**
     * @return the directory to output test results to.
     */
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

    /**
     * @return optional filter to limit which suites to run.
     */
    default Predicate<Path> suitesFilter() {
        return path -> true;
    }

    /**
     * @return If set, the generator will parse and echo its arguments and exit. Useful for testing.
     */
    default boolean echoOnly() {
        return false;
    }

    /**
     * Info about which services should be debugged.
     *
     * <p>In addition to providing this information, actually debugging a service requires the
     * caller to provide the actual debugging mechanism. This will generally involve providing a
     * {@link #transferables() directory} containing a Java agent to copy to the container, and
     * enabling the agent by setting {@code JAVA_TOOLS_OPTIONS} environment variable via {@link
     * ServiceDebugInfo#env()}.
     *
     * @return info about which services should be debugged.
     */
    default Optional<ServiceDebugInfo> serviceDebugInfo() {
        return Optional.empty();
    }

    /**
     * @return info about what to copy to or from the Docker containers running the
     *     services-under-test or service-being-debugged.
     */
    default Collection<DirectoryInfo> transferables() {
        return List.of();
    }

    /**
     * Provides an optional map of environment variables that will be set on each service-under-test
     * instance.
     *
     * <p>For example, this can be used to set the {@code JAVA_TOOLS_OPTIONS} required to enable
     * coverage metrics capture.
     *
     * @return map of environment variables to set on each service-under-test.
     */
    default Map<String, String> env() {
        return Map.of();
    }

    /**
     * Controls if any services, or service instances, should be configured to request the IntelliJ
     * debugger to attach when they start up.
     *
     * @see <a
     *     href="https://github.com/creek-service/creek-system-test#debugging-system-tests">Service
     *     Debugging</a>
     */
    interface ServiceDebugInfo {

        /**
         * The start of the default range of ports on the local machine that services will listen on
         * for the debugger.
         */
        int DEFAULT_BASE_DEBUG_PORT = 8000;

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

        /**
         * Provides an optional map of environment variables that will be set on each service being
         * debugged.
         *
         * <p>Note: This will overwrite any environment variables with matching names in {@link
         * ExecutorOptions#env()}. It is the callers responsibility to merge values, if required.
         *
         * <p>For example, this can be used to set the {@code JAVA_TOOLS_OPTIONS} required to enable
         * service debugging.
         *
         * <p>When debugging services, all instances of the text {@code ${SERVICE_DEBUG_PORT}} found
         * in the {@code JAVA_TOOLS_OPTIONS} value, if present, will be replaced with the port the
         * service is configured to listen for the debugger on.
         *
         * @return map of environment variables to set on each service being debugged.
         */
        default Map<String, String> env() {
            return Map.of();
        }
    }

    /** Controls the direction of file transfer. */
    enum CopyDirection {
        /** Copy host → container only (before start). */
        COPY_TO_CONTAINER(true, false),
        /** Copy container → host only (after stop). */
        COPY_FROM_CONTAINER(false, true),
        /** Copy host → container before start, and container → host after stop. */
        COPY_TO_AND_FROM_CONTAINER(true, true);

        private final boolean copyTo;
        private final boolean copyFrom;

        CopyDirection(final boolean copyTo, final boolean copyFrom) {
            this.copyTo = copyTo;
            this.copyFrom = copyFrom;
        }

        /**
         * @return {@code true} if files should be copied from the host to the container.
         */
        public boolean copyTo() {
            return copyTo;
        }

        /**
         * @return {@code true} if files should be copied from the container to the host.
         */
        public boolean copyFrom() {
            return copyFrom;
        }
    }

    /**
     * Information about a directory to copy to/from a container.
     *
     * @param hostPath the path on the host machine.
     * @param containerPath the path within the Docker container.
     * @param direction the direction of file transfer.
     */
    record DirectoryInfo(Path hostPath, Path containerPath, CopyDirection direction) {
        /**
         * Validates required parameters.
         *
         * @param hostPath the path on the host machine.
         * @param containerPath the path within the Docker container.
         * @param direction the direction of file transfer.
         */
        public DirectoryInfo {
            requireNonNull(hostPath, "hostPath");
            requireNonNull(containerPath, "containerPath");
            requireNonNull(direction, "direction");
        }

        @Override
        public String toString() {
            return "DirectoryInfo[hostPath="
                    + hostPath.toString().replace('\\', '/')
                    + ", containerPath="
                    + containerPath.toString().replace('\\', '/')
                    + ", direction="
                    + direction
                    + "]";
        }
    }
}

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

package org.creekservice.api.system.test.extension.service;


import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.creekservice.api.platform.metadata.ServiceDescriptor;

import static java.util.Objects.requireNonNull;

/** An instance of a {@link ServiceDefinition} */
public interface ServiceInstance {

    /** The unique name of the instance. */
    String name();

    /**
     * An optional service descriptor.
     *
     * <p>Any service that is being tested, i.e. defined in the {@code services} property of the
     * test suite, will have an associated service definition. 3rd-party services started by
     * extensions to facilitate testing will not.
     *
     * @return the service definition, if present.
     */
    Optional<ServiceDescriptor> descriptor();

    /** Start the instance. No-op if already started. */
    void start();

    /** @return {@code true} if the instance is running. */
    boolean running();

    /** Stop the instance. No-op if already stopped. */
    void stop();

    // Todo: Better to instead have a way for extentions to add endpoint info.
    //   - think in terms of ServiceInstance being an interface, with docker container being only
    // one possible impl
    /** Retrieve the actual port a service's port can be reached on via the local network */
    int mappedPort(int original);

    // Todo: Doc and test
    String externalHostName();
    // Todo: Doc and test
    String internalHostName();

    // Run a command inside a running container, as though using "docker exec".
    // Todo: Doc and test
    // Todo: Throw if container not running?
    ExecResult execInContainer(final String... cmd);

    /**
     * Configure the service instance.
     *
     * <p>**Note**: this method can not be called while the instance is running.
     *
     * @return type used to configure the instance.
     */
    Configure configure();

    interface Configure {

        /**
         * Set an environment variable on the instance.
         *
         * @param name the name of the environment variable.
         * @param value the value of the environment variable.
         * @return self, for method chaining.
         */
        Configure withEnv(String name, String value);

        /**
         * Set environment variables on the instance.
         *
         * @param env the map of environment variables to add.
         * @return self, for method chaining.
         */
        default Configure withEnv(Map<String, String> env) {
            env.forEach(this::withEnv);
            return this;
        }

        /**
         * Add exposed ports to the container instance.
         *
         * @param ports the ports to expose.
         * @return self, for method chaining.
         */
        Configure withExposedPorts(int... ports);

        /**
         * Set the command to be run
         *
         * @param cmdParts the parts of the command.
         * @return self, for method chaining.
         */
        Configure withCommand(String... cmdParts);

        /**
         * Set a log message to wait for before considering the instance available.
         *
         * <p>If not set, the instance is considered available once any mapped ports are open.
         *
         * @param regex  the regex pattern to check for
         * @param times  the number of times the pattern is expected
         * @return self, for method chaining.
         */
        Configure withStartupLogMessage(String regex, int times);

        /**
         * Set a startup timeout after which the instance will be considered failed.
         *
         * <p>Failed containers may result in another attempt, or cause the test suite to fail.
         *
         * @param timeout the timeout
         * @return self, for method chaining.
         */
        Configure withStartupTimeout(Duration timeout);

        /**
         * Set how many attempts should be made to start the instance.
         *
         * <p>If the max attempts is exceeded the test suite will fail.
         *
         * @param attempts the max attempts.
         * @return self, for method chaining.
         */
        Configure withStartupAttempts(int attempts);
    }

    /**
     * Stores the result of {@link #execInContainer}.
     */
    final class ExecResult {

        private final int exitCode;
        private final String stdout;
        private final String stderr;

        public static ExecResult execResult(final int exitCode, final String stdout, final String stderr) {
            return new ExecResult(exitCode, stdout, stderr);
        }

        private ExecResult(final int exitCode, final String stdout, final String stderr) {
            this.exitCode = exitCode;
            this.stdout = requireNonNull(stdout, "stdout");
            this.stderr = requireNonNull(stderr, "stderr");
        }

        // Todo: test
        public int exitCode() {
            return exitCode;
        }

        public String stdout() {
            return stdout;
        }

        public String stderr() {
            return stderr;
        }
    }
}

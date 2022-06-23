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

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.creekservice.api.platform.metadata.ServiceDescriptor;

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

    /**
     * Retrieve the port a service's port can be reached on from the host network.
     *
     * <p>This may be the same port, or a different port, depending on where the instance is
     * running.
     */
    int mappedPort(int original);

    /**
     * The host name the instance can be reached on from the host network.
     *
     * <p>The host network is the network accessible to the system test process itself. The external
     * host name is the host name the system test process can use to access the instance.
     *
     * <p>For example, if services are running in local docker containers, then the external host
     * name will be {@code localhost}, i.e. the current machine, where all the containers are
     * running.
     *
     * @return the host name the instance can be reached on from the host network.
     */
    String externalHostName();

    /**
     * The host name the instance can be reach on from the internal network.
     *
     * <p>The internal network is the network service instances are using to communicate. The
     * internal host name is the host name other services instances can use to access the instance.
     *
     * <p>For example, if services are running in local docker containers, then the internal host
     * name will be a docker-generated host name for the container the service is running on.
     *
     * @return the host name the instance can be reach on from the internal network.
     */
    String internalHostName();

    /**
     * Run a command on the instance host.
     *
     * <p>**Note**: this method can not be called while the instance is running.
     *
     * <p>If services are running in docker containers this is the same as using {@code docker
     * exec}.
     *
     * @param cmd the command to run.
     * @return the result of the execution.
     */
    ExecResult execOnInstance(String... cmd);

    /**
     * Configure the service instance.
     *
     * <p>**Note**: this method can not be called while the instance is running.
     *
     * @return type used to configure the instance.
     */
    // Todo: instead have a ConfigurableServiceInstance that is derived from ServiceInstance
    ConfigureInstance configure();

    interface ConfigureInstance {

        /**
         * Set an environment variable on the instance.
         *
         * @param name the name of the environment variable.
         * @param value the value of the environment variable.
         * @return self, for method chaining.
         */
        ConfigureInstance addEnv(String name, String value);

        /**
         * Set environment variables on the instance.
         *
         * @param env the map of environment variables to add.
         * @return self, for method chaining.
         */
        default ConfigureInstance addEnv(Map<String, String> env) {
            env.forEach(this::addEnv);
            return this;
        }

        /**
         * Add exposed ports to the container instance.
         *
         * @param ports the ports to expose.
         * @return self, for method chaining.
         */
        ConfigureInstance addExposedPorts(int... ports);

        /**
         * Set the command to be run
         *
         * @param cmdParts the parts of the command.
         * @return self, for method chaining.
         */
        ConfigureInstance setCommand(String... cmdParts);

        /**
         * Set a log message to wait for before considering the instance available.
         *
         * <p>If not set, the instance is considered available once any mapped ports are open.
         *
         * @param regex the regex pattern to check for
         * @param times the number of times the pattern is expected
         * @return self, for method chaining.
         */
        ConfigureInstance setStartupLogMessage(String regex, int times);

        /**
         * Set a startup timeout after which the instance will be considered failed.
         *
         * <p>Failed containers may result in another attempt, or cause the test suite to fail.
         *
         * @param timeout the timeout
         * @return self, for method chaining.
         */
        ConfigureInstance setStartupTimeout(Duration timeout);

        /**
         * Set how many attempts should be made to start the instance.
         *
         * <p>If the max attempts is exceeded the test suite will fail.
         *
         * @param attempts the max attempts.
         * @return self, for method chaining.
         */
        ConfigureInstance setStartupAttempts(int attempts);
    }

    /** Stores the result of {@link #execOnInstance}. */
    final class ExecResult {

        private final int exitCode;
        private final String stdout;
        private final String stderr;

        public static ExecResult execResult(
                final int exitCode, final String stdout, final String stderr) {
            return new ExecResult(exitCode, stdout, stderr);
        }

        private ExecResult(final int exitCode, final String stdout, final String stderr) {
            this.exitCode = exitCode;
            this.stdout = requireNonNull(stdout, "stdout");
            this.stderr = requireNonNull(stderr, "stderr");
        }

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

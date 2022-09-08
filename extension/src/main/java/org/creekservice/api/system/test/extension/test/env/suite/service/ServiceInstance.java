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

package org.creekservice.api.system.test.extension.test.env.suite.service;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.creekservice.api.platform.metadata.ServiceDescriptor;

/**
 * An instance of a {@link
 * org.creekservice.api.system.test.extension.component.definition.ServiceDefinition}
 */
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
    Optional<? extends ServiceDescriptor> descriptor();

    /** Start the instance. No-op if already started. */
    void start();

    /** @return {@code true} if the instance is running. */
    boolean running();

    /** Stop the instance. No-op if already stopped. */
    void stop();

    /**
     * Retrieve the port reachable from the test-network for a service's port.
     *
     * <p>This may be the same port, or a different port, depending on where the instance is
     * running.
     *
     * <p>For example, if services are running in local docker containers, then the test-network
     * port will be a locally mapped port.
     *
     * @param serviceNetworkPort the port exposed by the service.
     * @return the port the instance can be reached on from the test-network.
     */
    int testNetworkPort(int serviceNetworkPort);

    /**
     * The hostname the instance can be reached on from the test-network.
     *
     * <p>The test-network is the network accessible to the system test process itself. The
     * test-network hostname is the hostname the system test process can use to access the instance.
     *
     * <p>For example, if services are running in local docker containers, then the test-network
     * hostname will likely be {@code localhost}, i.e. the current machine, where all the containers
     * are running.
     *
     * @return the hostname the instance can be reached on from the test-network.
     */
    String testNetworkHostname();

    /**
     * The hostname the instance can be reach on from the service-network.
     *
     * <p>The service-network is the network the service instances are using to communicate with
     * each other. The service-network hostname is the hostname other services instances can use to
     * access this instance.
     *
     * <p>For example, if services are running in local docker containers, then the service-network
     * hostname will be a network alias derived from the service's definition's {@link
     * org.creekservice.api.system.test.extension.component.definition.ServiceDefinition#name()
     * name}. For example, {@code kafka-default-0}.
     *
     * @return the hostname the instance can be reach on from the service-network.
     */
    String serviceNetworkHostname();

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

        @Override
        public String toString() {
            return "ExecResult{"
                    + "exitCode="
                    + exitCode
                    + ", stderr='"
                    + stderr
                    + '\''
                    + ", stdout='"
                    + stdout
                    + '\''
                    + '}';
        }
    }
}

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

    // Todo: Better to instead have a way for extentions to add endpoint info.
    //   - think in terms of ServiceInstance being an interface, with docker container being only
    // one possible impl
    /** Retrieve the actual port a service's port can be reached on via the local network */
    int mappedPort(int original);

    /**
     * Amend the definition of the service instance.
     *
     * <p>**Note**: this method can not be called while the instance is running.
     *
     * @return type used to modify the instance.
     */
    Modifier modify();

    interface Modifier {

        /**
         * Set an environment variable on the instance.
         *
         * @param name the name of the environment variable.
         * @param value the value of the environment variable.
         * @return self, for method chaining.
         */
        Modifier withEnv(String name, String value);

        /**
         * Set environment variables on the instance.
         *
         * @param env the map of environment variables to add.
         * @return self, for method chaining.
         */
        default Modifier withEnv(Map<String, String> env) {
            env.forEach(this::withEnv);
            return this;
        }

        /**
         * Add exposed ports to the container instance.
         *
         * @param ports the ports to expose.
         * @return self, for method chaining.
         */
        Modifier withExposedPorts(int... ports);

        /**
         * Set the command to be run
         *
         * @param cmdParts the parts of the command.
         * @return self, for method chaining.
         */
        Modifier withCommand(final String... cmdParts);
    }
}

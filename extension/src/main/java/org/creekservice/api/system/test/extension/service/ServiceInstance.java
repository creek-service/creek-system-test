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

/** An instance of a {@link ServiceDefinition} */
public interface ServiceInstance {

    /** The unique name of the instance. */
    String name();

    /** Start the instance. No-op if already started. */
    void start();

    /** @return {@code true} if the instance is running. */
    boolean running();

    /** Stop the instance. No-op if already stopped. */
    void stop();

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
    }
}

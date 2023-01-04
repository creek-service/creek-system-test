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

import java.time.Duration;
import java.util.Map;

/**
 * A configurable instance of a {@link
 * org.creekservice.api.system.test.extension.component.definition.ServiceDefinition}.
 *
 * <p>Methods declared on this interface will throw {@link IllegalStateException} if the instance is
 * running.
 */
public interface ConfigurableServiceInstance extends ServiceInstance {

    /**
     * Set an environment variable on the instance.
     *
     * @param name the name of the environment variable.
     * @param value the value of the environment variable.
     * @return self, for method chaining.
     * @throws IllegalStateException if the instance is running.
     */
    ConfigurableServiceInstance addEnv(String name, String value);

    /**
     * Set environment variables on the instance.
     *
     * @param env the map of environment variables to add.
     * @return self, for method chaining.
     * @throws IllegalStateException if the instance is running.
     */
    default ConfigurableServiceInstance addEnv(Map<String, String> env) {
        env.forEach(this::addEnv);
        return this;
    }

    /**
     * Add exposed ports to the container instance.
     *
     * @param ports the ports to expose.
     * @return self, for method chaining.
     * @throws IllegalStateException if the instance is running.
     */
    ConfigurableServiceInstance addExposedPorts(int... ports);

    /**
     * Set the command to be run
     *
     * @param cmdParts the parts of the command.
     * @return self, for method chaining.
     * @throws IllegalStateException if the instance is running.
     */
    ConfigurableServiceInstance setCommand(String... cmdParts);

    /**
     * Set a log message to wait for before considering the instance available.
     *
     * <p>If not set, the instance is considered available once any mapped ports are open.
     *
     * @param regex the regex pattern to check for
     * @param times the number of times the pattern is expected
     * @return self, for method chaining.
     * @throws IllegalStateException if the instance is running.
     */
    ConfigurableServiceInstance setStartupLogMessage(String regex, int times);

    /**
     * Set how many attempts should be made to start the instance.
     *
     * <p>If the max attempts is exceeded the test suite will fail.
     *
     * @param attempts the max attempts.
     * @return self, for method chaining.
     * @throws IllegalStateException if the instance is running.
     */
    ConfigurableServiceInstance setStartupAttempts(int attempts);

    /**
     * Set a startup timeout, after which the instance will be considered failed.
     *
     * <p>Failed containers may result in another attempt, or cause the test suite to fail.
     *
     * @param timeout the timeout
     * @return self, for method chaining.
     * @throws IllegalStateException if the instance is running.
     */
    ConfigurableServiceInstance setStartupTimeout(Duration timeout);

    /**
     * Set a shutdown timeout, after which the instance will be killed.
     *
     * <p>Containers are first sent a SIGTERM signal, which should trigger a graceful shutdown. If
     * the container fails to stop within the configured timeout it will be sent a SIGKILL signal.
     *
     * @param timeout the timeout
     * @return self, for method chaining.
     * @throws IllegalStateException if the instance is running.
     */
    ConfigurableServiceInstance setShutdownTimeout(Duration timeout);
}

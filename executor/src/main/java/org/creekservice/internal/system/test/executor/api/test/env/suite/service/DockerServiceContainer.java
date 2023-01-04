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

package org.creekservice.internal.system.test.executor.api.test.env.suite.service;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.env.suite.service.ConfigurableServiceInstance;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstance;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstanceContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/** A local, docker based, implementation of {@link ServiceInstanceContainer}. */
public final class DockerServiceContainer implements ServiceInstanceContainer {

    // See https://github.com/creek-service/creek-system-test/issues/79 for making these
    // configurable:
    private static final int CONTAINER_START_UP_ATTEMPTS = 3;
    private static final Duration CONTAINER_START_UP_TIMEOUT = Duration.ofMinutes(1);

    private final long threadId;
    private final Map<String, ConfigurableServiceInstance> instances = new HashMap<>();
    private final InstanceNaming naming = new InstanceNaming();
    private final ContainerFactory containerFactory;

    /**
     * @param containerFactory factory for creating Docker containers
     */
    public DockerServiceContainer(final ContainerFactory containerFactory) {
        this(containerFactory, Thread.currentThread().getId());
    }

    private DockerServiceContainer(final ContainerFactory containerFactory, final long threadId) {
        this.containerFactory = requireNonNull(containerFactory, "containerFactory");
        this.threadId = threadId;
    }

    @Override
    public ConfigurableServiceInstance add(final ServiceDefinition def) {
        throwIfNotOnCorrectThread();

        final String instanceName = naming.instanceName(def.name());
        final DockerImageName imageName = DockerImageName.parse(def.dockerImage());

        final GenericContainer<?> container =
                containerFactory.create(
                        imageName, instanceName, def.name(), def.descriptor().isPresent());

        final ConfigurableServiceInstance instance =
                new ContainerInstance(
                                instanceName,
                                imageName,
                                container,
                                def.descriptor(),
                                def::instanceStarted)
                        .setStartupAttempts(CONTAINER_START_UP_ATTEMPTS)
                        .setStartupTimeout(CONTAINER_START_UP_TIMEOUT);

        def.configureInstance(instance);

        instances.put(instance.name(), instance);
        return instance;
    }

    @Override
    public ConfigurableServiceInstance get(final String name) {
        requireNonNull(name, "name");
        throwIfNotOnCorrectThread();
        final ConfigurableServiceInstance instance = instances.get(name);
        if (instance == null) {
            throw new IllegalArgumentException("No instance found with name: " + name);
        }
        return instance;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<ConfigurableServiceInstance> iterator() {
        throwIfNotOnCorrectThread();
        return instances.values().iterator();
    }

    /**
     * Clear all services.
     *
     * <p>All services must already have been stopped.
     *
     * <p>Clears the container, ready for the next test suite.
     */
    public void clear() {
        throwIfNotOnCorrectThread();
        throwOnRunningServices();
        instances.clear();
        naming.clear();
    }

    private void throwOnRunningServices() {
        final String running =
                instances.values().stream()
                        .filter(ServiceInstance::running)
                        .map(ServiceInstance::name)
                        .sorted()
                        .collect(Collectors.joining(", "));

        if (!running.isEmpty()) {
            throw new IllegalStateException("The following services are still running: " + running);
        }
    }

    private void throwIfNotOnCorrectThread() {
        if (Thread.currentThread().getId() != threadId) {
            throw new ConcurrentModificationException("Class is not thread safe");
        }
    }
}

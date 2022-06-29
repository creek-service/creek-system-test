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

package org.creekservice.internal.system.test.executor.api.testsuite.service;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.service.ConfigurableServiceInstance;
import org.creekservice.api.system.test.extension.service.ServiceContainer;
import org.creekservice.api.system.test.extension.service.ServiceDefinition;
import org.creekservice.api.system.test.extension.service.ServiceInstance;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

/** A local, docker based, implementation of {@link ServiceContainer}. */
public final class DockerServiceContainer implements ServiceContainer {

    // See https://github.com/creek-service/creek-system-test/issues/79 for making these
    // configurable:
    private static final int CONTAINER_START_UP_ATTEMPTS = 3;
    private static final Duration CONTAINER_START_UP_TIMEOUT = Duration.ofMinutes(1);

    private final long threadId;
    private final Function<DockerImageName, GenericContainer<?>> containerFactory;
    private final Network network = Network.newNetwork();
    private final Map<String, ConfigurableServiceInstance> instances = new HashMap<>();
    private final InstanceNaming naming = new InstanceNaming();

    public DockerServiceContainer() {
        this(Thread.currentThread().getId(), GenericContainer::new);
    }

    @VisibleForTesting
    DockerServiceContainer(
            final long threadId,
            final Function<DockerImageName, GenericContainer<?>> containerFactory) {
        this.threadId = threadId;
        this.containerFactory = requireNonNull(containerFactory, "containerFactory");
    }

    @Override
    public ConfigurableServiceInstance add(final ServiceDefinition def) {
        throwIfNotOnCorrectThread();

        final String instanceName = naming.instanceName(def.name());
        final DockerImageName imageName = DockerImageName.parse(def.dockerImage());

        final GenericContainer<?> container = createContainer(imageName, instanceName);
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

    public void clear() {
        throwIfNotOnCorrectThread();
        throwOnRunningServices();
        instances.clear();
        naming.clear();
    }

    private GenericContainer<?> createContainer(
            final DockerImageName imageName, final String instanceName) {
        final GenericContainer<?> container = containerFactory.apply(imageName);

        container
                .withNetwork(network)
                .withNetworkAliases(instanceName)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(instanceName)));

        return container;
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

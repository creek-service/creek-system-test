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


import java.time.Duration;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.service.ServiceContainer;
import org.creekservice.api.system.test.extension.service.ServiceInstance;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/** A local, docker based, implementation of {@link ServiceContainer}. */
public final class LocalServiceInstances implements ServiceContainer {

    private static final int CONTAINER_START_UP_ATTEMPTS = 3;
    private static final Duration CONTAINER_START_UP_TIMEOUT = Duration.ofSeconds(90);

    private final long threadId;
    private final Network network = Network.newNetwork();
    private final List<ServiceInstance> instances = new ArrayList<>();
    private final InstanceNaming naming = new InstanceNaming();

    public LocalServiceInstances() {
        this(Thread.currentThread().getId());
    }

    @VisibleForTesting
    LocalServiceInstances(final long threadId) {
        this.threadId = threadId;
    }

    @Override
    public ServiceInstance add(final String serviceName, final String dockerImageName) {
        throwIfNotOnCorrectThread();

        final String instanceName = naming.instanceName(serviceName);
        final DockerImageName imageName = DockerImageName.parse(dockerImageName);

        final GenericContainer<?> container = createContainer(imageName, instanceName);
        final ComponentInstance instance =
                new ComponentInstance(instanceName, imageName, container);
        instances.add(instance);
        return instance;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<ServiceInstance> iterator() {
        throwIfNotOnCorrectThread();
        return instances.iterator();
    }

    public void clear() {
        throwIfNotOnCorrectThread();
        throwOnRunningServices();
        instances.clear();
        naming.clear();
    }

    private GenericContainer<?> createContainer(
            final DockerImageName imageName, final String instanceName) {
        final GenericContainer<?> container = new GenericContainer<>(imageName);

        return container
                .withNetwork(network)
                .withNetworkAliases(instanceName)
                .withStartupAttempts(CONTAINER_START_UP_ATTEMPTS)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(instanceName)))
                .waitingFor(
                        Wait.forLogMessage(".*lifecycle.*started.*", 1)
                                .withStartupTimeout(CONTAINER_START_UP_TIMEOUT));
    }

    private void throwOnRunningServices() {
        final String running =
                instances.stream()
                        .filter(ServiceInstance::running)
                        .map(ServiceInstance::name)
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

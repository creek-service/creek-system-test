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

package org.creekservice.internal.system.test.executor.api;

import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.service.ServiceContainer;
import org.creekservice.api.system.test.extension.service.ServiceDefinition;
import org.creekservice.api.system.test.extension.service.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/** A local, docker based, implementation of {@link ServiceContainer}. */
public final class LocalServiceInstances implements ServiceContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalServiceInstances.class);

    private static final int CONTAINER_START_UP_ATTEMPTS = 3;
    private static final Duration CONTAINER_START_UP_TIMEOUT = Duration.ofSeconds(90);

    private final long threadId;
    private final Network network = Network.newNetwork();
    private final List<ServiceInstance> instances = new ArrayList<>();
    private final Map<String, AtomicInteger> names = new HashMap<>();

    public LocalServiceInstances() {
        this(Thread.currentThread().getId());
    }

    @VisibleForTesting
    LocalServiceInstances(final long threadId) {
        this.threadId = threadId;
    }

    @Override
    public ServiceInstance start(final ServiceDefinition def) {
        throwIfNotOnCorrectThread();

        final Instance instance = new Instance(def);
        instance.start();
        instances.add(instance);
        return instance;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<ServiceInstance> iterator() {
        throwIfNotOnCorrectThread();
        return instances.iterator();
    }

    private String instanceName(final String serviceName) {
        final AtomicInteger counter = names.computeIfAbsent(serviceName, k -> new AtomicInteger());
        return serviceName + "-" + counter.getAndIncrement();
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

    private void throwIfNotOnCorrectThread() {
        if (Thread.currentThread().getId() != threadId) {
            throw new ConcurrentModificationException("Class is not thread safe");
        }
    }

    private static DockerImageName dockerImageName(final ServiceDefinition def) {
        final String fullName = def.dockerImage() + ":latest";
        return DockerImageName.parse(fullName);
    }

    @VisibleForTesting
    final class Instance implements ServiceInstance {

        private final String name;
        private final DockerImageName image;
        private final ServiceDefinition def;
        private final GenericContainer<?> container;
        private String cachedContainerId;

        Instance(final ServiceDefinition def) {
            this.def = requireNonNull(def, "def");
            this.name = instanceName(def.name());
            this.image = dockerImageName(def);
            this.container = requireNonNull(createContainer(image, name), "container");
            this.cachedContainerId = container.getContainerId();
        }

        @Override
        public void start() {
            if (running()) {
                return;
            }

            LOGGER.info("Starting {} ({})", name, image);

            try {
                container.start();
                cachedContainerId = container.getContainerId();

                LOGGER.info("Started {} ({}) with container-id {}", name, image, cachedContainerId);
            } catch (final Exception e) {
                throw new FailedToStartServiceException(def, container, e);
            }
        }

        @Override
        public boolean running() {
            throwIfNotOnCorrectThread();
            return container.getContainerId() != null;
        }

        @Override
        public void stop() {
            if (!running()) {
                return;
            }

            LOGGER.info("Stopping {} ({}) with container-id {}", name, image, cachedContainerId);
            container.stop();
            LOGGER.info("Stopped {} ({})", name, image);
        }

        String cachedContainerId() {
            return cachedContainerId;
        }
    }

    private static final class FailedToStartServiceException extends RuntimeException {
        FailedToStartServiceException(
                final ServiceDefinition def,
                final GenericContainer<?> container,
                final Throwable cause) {
            super(
                    "Failed to start service: "
                            + def.name()
                            + ", image: "
                            + dockerImageName(def)
                            + lineSeparator()
                            + "Cause: "
                            + cause.getMessage()
                            + lineSeparator()
                            + "Logs: "
                            + container.getLogs(),
                    cause);
        }
    }
}

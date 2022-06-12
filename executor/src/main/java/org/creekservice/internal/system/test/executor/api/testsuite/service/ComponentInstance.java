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

import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;
import static org.creekservice.api.base.type.Preconditions.requireNonBlank;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import org.creekservice.api.system.test.extension.service.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/** An instance of a service running in a local docker container. */
final class ComponentInstance implements ServiceInstance, ServiceInstance.Modifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentInstance.class);

    private final long threadId;
    private final String name;
    private final DockerImageName imageName;
    private final GenericContainer<?> container;

    ComponentInstance(
            final String name,
            final DockerImageName imageName,
            final GenericContainer<?> container) {
        this(name, imageName, container, Thread.currentThread().getId());
    }

    ComponentInstance(
            final String name,
            final DockerImageName imageName,
            final GenericContainer<?> container,
            final long threadId) {
        this.threadId = threadId;
        this.name = requireNonBlank(name, "name");
        this.imageName = requireNonNull(imageName, "imageName");
        this.container = requireNonNull(container, "container");
    }

    @Override
    public String name() {
        throwIfNotOnCorrectThread();
        return name;
    }

    @Override
    public void start() {
        if (running()) {
            return;
        }

        LOGGER.info("Starting {} ({})", name, imageName);

        try {
            container.start();

            LOGGER.info(
                    "Started {} ({}) with container-id {}",
                    name,
                    imageName,
                    container.getContainerId());
        } catch (final Exception e) {
            throw new FailedToStartServiceException(name, imageName, container, e);
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

        LOGGER.info(
                "Stopping {} ({}) with container-id {}",
                name,
                imageName,
                container.getContainerId());
        container.stop();
        LOGGER.info("Stopped {} ({})", name, imageName);
    }

    @Override
    public Modifier modify() {
        throwIfNotOnCorrectThread();
        throwIfRunning();
        return this;
    }

    @Override
    public Modifier withEnv(final String name, final String value) {
        throwIfNotOnCorrectThread();
        container.withEnv(requireNonNull(name, "name"), requireNonNull(value, "value"));
        return this;
    }

    @Override
    public Modifier withExposedPorts(final int... ports) {
        throwIfNotOnCorrectThread();
        Arrays.stream(ports).forEach(container::withExposedPorts);
        return this;
    }

    String containerId() {
        return container.getContainerId();
    }

    String logs() {
        return container.getLogs();
    }

    private void throwIfRunning() {
        if (running()) {
            throw new IllegalStateException(
                    "A service can not be modified when running. service: "
                            + name
                            + " ("
                            + imageName
                            + ") with container-id "
                            + container.getContainerId());
        }
    }

    private void throwIfNotOnCorrectThread() {
        if (Thread.currentThread().getId() != threadId) {
            throw new ConcurrentModificationException("Class is not thread safe");
        }
    }

    private static final class FailedToStartServiceException extends RuntimeException {
        FailedToStartServiceException(
                final String name,
                final DockerImageName imageName,
                final GenericContainer<?> container,
                final Throwable cause) {
            super(
                    "Failed to start service: "
                            + name
                            + ", image: "
                            + imageName
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

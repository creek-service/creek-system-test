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

import java.util.ConcurrentModificationException;
import org.creekservice.api.system.test.extension.service.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/** An instance of one of the services under test. */
final class InstanceUnderTest implements ServiceInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceUnderTest.class);

    private final long threadId;
    private final String name;
    private final DockerImageName imageName;
    private final GenericContainer<?> container;
    private String cachedContainerId;

    InstanceUnderTest(
            final String name,
            final DockerImageName imageName,
            final GenericContainer<?> container) {
        this(name, imageName, container, Thread.currentThread().getId());
    }

    InstanceUnderTest(
            final String name,
            final DockerImageName imageName,
            final GenericContainer<?> container,
            final long threadId) {
        this.threadId = threadId;
        this.name = requireNonBlank(name, "name");
        this.imageName = requireNonNull(imageName, "imageName");
        this.container = requireNonNull(container, "container");
        this.cachedContainerId = container.getContainerId();
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
            cachedContainerId = container.getContainerId();

            LOGGER.info("Started {} ({}) with container-id {}", name, imageName, cachedContainerId);
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

        LOGGER.info("Stopping {} ({}) with container-id {}", name, imageName, cachedContainerId);
        container.stop();
        LOGGER.info("Stopped {} ({})", name, imageName);
    }

    @Override
    public Modifier modify() {
        throwIfNotOnCorrectThread();
        throwIfRunning();
        return null;
    }

    String cachedContainerId() {
        return cachedContainerId;
    }

    private void throwIfRunning() {
        if (running()) {
            throw new IllegalStateException(
                    "A service can not be modified when running. service: "
                            + name
                            + " ("
                            + imageName
                            + ") with container-id "
                            + cachedContainerId);
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

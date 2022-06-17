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
import static org.creekservice.api.system.test.extension.service.ServiceInstance.ExecResult.execResult;

import java.io.IOException;
import java.time.Duration;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.function.Consumer;

import com.github.dockerjava.api.command.InspectContainerResponse;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.system.test.extension.service.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/** An instance of a service running in a local docker container. */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ContainerInstance implements ServiceInstance, ServiceInstance.Configure {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerInstance.class);

    private final long threadId;
    private final String name;
    private final DockerImageName imageName;
    private final GenericContainer<?> container;
    private final Optional<ServiceDescriptor> descriptor;
    private final Consumer<ServiceInstance> startedCallback;
    private Duration startUpTimeOut = Duration.ofSeconds(90);

    public ContainerInstance(
            final String name,
            final DockerImageName imageName,
            final GenericContainer<?> container,
            final Optional<ServiceDescriptor> descriptor,
            final Consumer<ServiceInstance> startedCallback) {
        this(name, imageName, container, descriptor, startedCallback, Thread.currentThread().getId());
    }

    @VisibleForTesting
    ContainerInstance(
            final String name,
            final DockerImageName imageName,
            final GenericContainer<?> container,
            final Optional<ServiceDescriptor> descriptor,
            final Consumer<ServiceInstance> startedCallback,
            final long threadId) {
        this.threadId = threadId;
        this.name = requireNonBlank(name, "name");
        this.imageName = requireNonNull(imageName, "imageName");
        this.container = requireNonNull(container, "container");
        this.descriptor = requireNonNull(descriptor, "descriptor");
        this.startedCallback = requireNonNull(startedCallback, "startedCallback");
    }

    @Override
    public String name() {
        throwIfNotOnCorrectThread();
        return name;
    }

    @Override
    public Optional<ServiceDescriptor> descriptor() {
        throwIfNotOnCorrectThread();
        return descriptor;
    }

    @Override
    public void start() {
        if (running()) {
            return;
        }

        LOGGER.info("Starting {} ({})", name, imageName);

        try {
            container.start();

            startedCallback.accept(this); // Todo: test.

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
        // Todo: death detection.
        return container.getContainerId() != null;
    }

    @Override
    public String externalHostName() {
        return container.getHost();
    }

    @Override
    public String internalHostName() {
        final InspectContainerResponse containerInfo = container.getContainerInfo();
        if (containerInfo == null) {
            throw new IllegalStateException("Container not running");
        }
        return containerInfo.getConfig().getHostName();
    }

    @Override
    public ExecResult execInContainer(final String... cmd) {
        final Container.ExecResult result;
        try {
            result = container.execInContainer(cmd);
            return execResult(result.getExitCode(), result.getStdout(), result.getStderr());
        } catch (IOException e) {
            throw new RuntimeException(e); // Todo: custom exception type.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Todo: test... throws.
            return null;
        }
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
    public int mappedPort(final int original) {
        throwIfNotOnCorrectThread();
        return container.getMappedPort(original);
    }

    @Override
    public Configure configure() {
        throwIfNotOnCorrectThread();
        throwIfRunning();
        return this;
    }

    @Override
    public Configure withEnv(final String name, final String value) {
        throwIfNotOnCorrectThread();
        container.withEnv(requireNonNull(name, "name"), requireNonNull(value, "value"));
        return this;
    }

    @Override
    public Configure withExposedPorts(final int... ports) {
        throwIfNotOnCorrectThread();
        container.addExposedPorts(requireNonNull(ports, "ports"));
        return this;
    }

    @Override
    public Configure withCommand(final String... cmdParts) {
        throwIfNotOnCorrectThread();
        container.withCommand(requireNonNull(cmdParts, "cmdParts"));
        return this;
    }

    // Todo: test
    @Override
    public Configure withStartupLogMessage(final String regex, final int times) {
        throwIfNotOnCorrectThread();
        container.setWaitStrategy(Wait.forLogMessage(regex, times)
                .withStartupTimeout(startUpTimeOut));
        return this;
    }

    // Todo: test
    @Override
    public Configure withStartupTimeout(final Duration timeout) {
        throwIfNotOnCorrectThread();
        startUpTimeOut = requireNonNull(timeout, "timeout");
        container.withStartupTimeout(startUpTimeOut);
        return this;
    }

    // Todo: test
    @Override
    public Configure withStartupAttempts(final int attempts) {
        throwIfNotOnCorrectThread();
        container.withStartupAttempts(attempts);
        return this;
    }

    String containerId() {
        return container.getContainerId();
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

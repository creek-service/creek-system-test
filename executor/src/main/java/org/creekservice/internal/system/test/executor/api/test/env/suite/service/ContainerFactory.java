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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.executor.ExecutorOptions.MountInfo;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

/** Factory that creates the Docker containers used to run services */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ContainerFactory implements TestEnvironmentListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerFactory.class);

    private final ServiceDebugInfo serviceDebugInfo;
    private final List<MountInfo> mountInfo;
    private final Map<String, String> env;
    private final RegularContainerFactory regularFactory;
    private final DebugContainerFactory debugFactory;
    private final Supplier<Network> networkSupplier;
    private final AtomicInteger nextDebugServicePort = new AtomicInteger();
    private final AtomicReference<Network> network = new AtomicReference<>();

    /**
     * Create factory instance.
     *
     * @param serviceDebugInfo info on what services to debug.
     * @param mountInfo info on what mounts to add to containers.
     * @param env environment vars to set on services-under-test.
     */
    public ContainerFactory(
            final ServiceDebugInfo serviceDebugInfo,
            final Collection<MountInfo> mountInfo,
            final Map<String, String> env) {
        this(
                serviceDebugInfo,
                mountInfo,
                env,
                new RegularContainerFactory(),
                new DebugContainerFactory(),
                Network::newNetwork);
    }

    @VisibleForTesting
    ContainerFactory(
            final ServiceDebugInfo serviceDebugInfo,
            final Collection<MountInfo> mountInfo,
            final Map<String, String> env,
            final RegularContainerFactory regularFactory,
            final DebugContainerFactory debugFactory,
            final Supplier<Network> networkSupplier) {
        this.serviceDebugInfo = requireNonNull(serviceDebugInfo, "serviceDebugInfo");
        this.mountInfo = List.copyOf(requireNonNull(mountInfo, "mountInfo"));
        this.env = Map.copyOf(requireNonNull(env, "env"));
        this.regularFactory = requireNonNull(regularFactory, "regularFactory");
        this.debugFactory = requireNonNull(debugFactory, "debugFactory");
        this.networkSupplier = requireNonNull(networkSupplier, "networkSupplier");
        reset();
    }

    /**
     * Create a Docker container.
     *
     * @param imageName the name of the Docker image
     * @param instanceName the name of instance being created
     * @param serviceName the name of the service the instance will run
     * @param serviceUnderTest {@code true} if the service is under test.
     * @return the created container.
     */
    public GenericContainer<?> create(
            final DockerImageName imageName,
            final String instanceName,
            final String serviceName,
            final boolean serviceUnderTest) {

        final Optional<Integer> serviceDebugPort = debugPort(instanceName, serviceName);

        final GenericContainer<?> container =
                serviceDebugPort.isPresent()
                        ? debugFactory.create(imageName, serviceDebugPort.get())
                        : regularFactory.create(imageName);

        if (serviceUnderTest || serviceDebugPort.isPresent()) {
            configureServiceUnderTest(container, serviceDebugPort);
        }

        container
                .withNetwork(network.get())
                .withNetworkAliases(instanceName)
                .withLogConsumer(
                        new Slf4jLogConsumer(LoggerFactory.getLogger(instanceName))
                                .withPrefix(instanceName));

        return container;
    }

    @Override
    public void beforeSuite(final CreekTestSuite suite) {
        reset();
    }

    private void reset() {
        network.updateAndGet(
                existing -> {
                    if (existing != null) {
                        existing.close();
                    }
                    return networkSupplier.get();
                });

        nextDebugServicePort.set(serviceDebugInfo.baseServicePort());
    }

    private Optional<Integer> debugPort(final String instanceName, final String serviceName) {
        return serviceDebugInfo.shouldDebug(serviceName, instanceName)
                ? Optional.of(nextDebugServicePort.getAndIncrement())
                : Optional.empty();
    }

    private void configureServiceUnderTest(
            final GenericContainer<?> container, final Optional<Integer> serviceDebugPort) {
        final Map<String, String> configuredEnv = configuredEnv(serviceDebugPort);
        if (!configuredEnv.isEmpty()) {
            LOGGER.debug("Setting container env. env: " + configuredEnv);
            container.withEnv(configuredEnv);
        }

        mountInfo.forEach(
                mount -> {
                    LOGGER.debug(
                            "Adding container mount. hostPath "
                                    + mount.hostPath()
                                    + ", containerPath: "
                                    + mount.containerPath()
                                    + ", read-only: "
                                    + mount.readOnly());

                    container.withFileSystemBind(
                            mount.hostPath().toString(),
                            mount.containerPath().toString(),
                            mount.readOnly() ? BindMode.READ_ONLY : BindMode.READ_WRITE);
                });
    }

    private Map<String, String> configuredEnv(final Optional<Integer> serviceDebugPort) {
        if (serviceDebugPort.isEmpty()) {
            return env;
        }

        final Map<String, String> configured = new HashMap<>(env);
        configured.computeIfPresent(
                "JAVA_TOOL_OPTIONS",
                (k, v) ->
                        v.replaceAll(
                                "\\$\\{SERVICE_DEBUG_PORT}",
                                String.valueOf(serviceDebugPort.get())));
        return Map.copyOf(configured);
    }
}

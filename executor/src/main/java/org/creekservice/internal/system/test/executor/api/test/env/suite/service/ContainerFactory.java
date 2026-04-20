/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.executor.ExecutorOptions.DirectoryInfo;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;
import org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/** Factory that creates the Docker containers used to run services */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ContainerFactory implements TestEnvironmentListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerFactory.class);

    private final ServiceDebugInfo serviceDebugInfo;
    private final List<DirectoryInfo> transferables;
    private final Map<String, String> env;
    private final RegularContainerFactory regularFactory;
    private final DebugContainerFactory debugFactory;
    private final Supplier<Network> networkSupplier;
    private final AtomicInteger nextDebugServicePort = new AtomicInteger();
    private final AtomicReference<Network> network = new AtomicReference<>();

    /**
     * Create a factory instance.
     *
     * @param serviceDebugInfo info on what services to debug.
     * @param transferables info on what to copy to/from containers.
     * @param env environment vars to set on services-under-test.
     */
    public ContainerFactory(
            final ServiceDebugInfo serviceDebugInfo,
            final Collection<DirectoryInfo> transferables,
            final Map<String, String> env) {
        this(
                serviceDebugInfo,
                transferables,
                env,
                new RegularContainerFactory(),
                new DebugContainerFactory(),
                Network::newNetwork);
    }

    @VisibleForTesting
    ContainerFactory(
            final ServiceDebugInfo serviceDebugInfo,
            final Collection<DirectoryInfo> transferables,
            final Map<String, String> env,
            final RegularContainerFactory regularFactory,
            final DebugContainerFactory debugFactory,
            final Supplier<Network> networkSupplier) {
        this.serviceDebugInfo = requireNonNull(serviceDebugInfo, "serviceDebugInfo");
        this.transferables = List.copyOf(requireNonNull(transferables, "transferables"));
        this.env = Map.copyOf(requireNonNull(env, "env"));
        this.regularFactory = requireNonNull(regularFactory, "regularFactory");
        this.debugFactory = requireNonNull(debugFactory, "debugFactory");
        this.networkSupplier = requireNonNull(networkSupplier, "networkSupplier");
        afterSuite(null, null);
    }

    /**
     * Create a Docker container.
     *
     * @param imageName the name of the Docker image
     * @param instanceName the name of instance being created
     * @param serviceName the name of the service the instance will run
     * @param serviceUnderTest {@code true} if the service is under test.
     * @param startingHook a hook invoked from {@code containerIsStarting} — after the container
     *     process starts but before the wait strategy completes, so mapped ports are available.
     * @return the created container along with any transferables to copy from the container after
     *     it stops.
     */
    public CreatedContainer create(
            final DockerImageName imageName,
            final String instanceName,
            final String serviceName,
            final boolean serviceUnderTest,
            final Runnable startingHook) {

        final Optional<Integer> serviceDebugPort = debugPort(instanceName, serviceName);

        final GenericContainer<?> container =
                serviceDebugPort.isPresent()
                        ? debugFactory.create(imageName, serviceDebugPort.get(), startingHook)
                        : regularFactory.create(imageName, startingHook);

        setEnv(instanceName, container, serviceUnderTest, serviceDebugPort);

        final boolean transfer = serviceUnderTest || serviceDebugPort.isPresent();
        if (transfer) {
            copyTransferablesToContainer(instanceName, container);
        }

        container
                .withNetwork(ensureNetwork())
                .withNetworkAliases(instanceName)
                .withLogConsumer(
                        new Slf4jLogConsumer(LoggerFactory.getLogger(instanceName))
                                .withPrefix(instanceName));

        final List<DirectoryInfo> writableCopies =
                transfer
                        ? transferables.stream().filter(t -> t.direction().copyFrom()).toList()
                        : List.of();

        return new CreatedContainer(container, writableCopies);
    }

    /**
     * Holds the result of {@link #create}, bundling the container with any transferables to copy
     * when the container closes.
     *
     * @param container the created container.
     * @param transferables transferables to transfer when the container closes.
     */
    public record CreatedContainer(
            GenericContainer<?> container, List<DirectoryInfo> transferables) {

        /**
         * Create an instance
         *
         * @param container the created container.
         * @param transferables any transferables to copy from the container when it stops.
         */
        @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "intentional exposure")
        public CreatedContainer {
            requireNonNull(container, "container");
            transferables = List.copyOf(requireNonNull(transferables, "writableCopies"));
        }

        /**
         * @return the created container.
         */
        @Override
        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
        public GenericContainer<?> container() {
            return container;
        }
    }

    @SuppressWarnings("resource")
    @Override
    public void afterSuite(final CreekTestSuite suite, final TestSuiteResult result) {
        network.updateAndGet(
                existing -> {
                    if (existing != null) {
                        existing.close();
                    }
                    return null;
                });

        nextDebugServicePort.set(serviceDebugInfo.baseServicePort());
    }

    private Network ensureNetwork() {
        return network.updateAndGet(
                existing -> existing != null ? existing : networkSupplier.get());
    }

    private Optional<Integer> debugPort(final String instanceName, final String serviceName) {
        return serviceDebugInfo.shouldDebug(serviceName, instanceName)
                ? Optional.of(nextDebugServicePort.getAndIncrement())
                : Optional.empty();
    }

    private Map<String, String> buildEnv(
            final boolean serviceUnderTest, final Optional<Integer> serviceDebugPort) {
        final Map<String, String> baseEnv = serviceUnderTest ? env : Map.of();
        if (serviceDebugPort.isEmpty()) {
            return baseEnv;
        }

        final Map<String, String> configured = new HashMap<>(baseEnv);
        configured.putAll(serviceDebugInfo.env());
        configured.computeIfPresent(
                "JAVA_TOOL_OPTIONS",
                (k, v) ->
                        v.replaceAll(
                                "\\$\\{SERVICE_DEBUG_PORT}",
                                String.valueOf(serviceDebugPort.get())));
        return Map.copyOf(configured);
    }

    private void setEnv(
            final String instanceName,
            final GenericContainer<?> container,
            final boolean serviceUnderTest,
            final Optional<Integer> serviceDebugPort) {
        final Map<String, String> env = buildEnv(serviceUnderTest, serviceDebugPort);
        if (!env.isEmpty()) {
            LOGGER.info("Setting container env. instance: " + instanceName + ", env : " + env);
            container.withEnv(env);
        }
    }

    private void copyTransferablesToContainer(
            final String instanceName, final GenericContainer<?> container) {
        transferables.stream()
                .filter(t -> t.direction().copyTo())
                .forEach(
                        transferable -> {
                            LOGGER.info(
                                    "Copying to container. instance: {}, hostPath: {},"
                                            + " containerPath: {}",
                                    instanceName,
                                    transferable.hostPath(),
                                    transferable.containerPath());

                            if (!Files.exists(transferable.hostPath())) {
                                throw new IllegalArgumentException(
                                        "Host path does not exist for transferable: "
                                                + transferable);
                            }

                            container.withCopyFileToContainer(
                                    MountableFile.forHostPath(transferable.hostPath()),
                                    transferable.containerPath().toString() + "/");
                        });
    }
}

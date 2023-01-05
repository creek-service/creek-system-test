/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.system.test.test.util;

import static java.util.Objects.requireNonNull;
import static org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo.DEFAULT_BASE_DEBUG_PORT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptors;
import org.creekservice.api.system.test.executor.ExecutorOptions;
import org.creekservice.api.system.test.executor.ExecutorOptions.MountInfo;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.api.system.test.extension.CreekTestExtensions;
import org.creekservice.api.system.test.extension.component.definition.AggregateDefinition;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinitionCollection;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstance;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstanceContainer;
import org.creekservice.api.system.test.extension.test.model.TestModelContainer;
import org.creekservice.internal.system.test.executor.api.component.definition.ComponentDefinitions;
import org.creekservice.internal.system.test.executor.api.test.env.suite.service.ContainerFactory;
import org.creekservice.internal.system.test.executor.api.test.env.suite.service.ContainerInstance;
import org.creekservice.internal.system.test.executor.api.test.env.suite.service.DockerServiceContainer;
import org.creekservice.internal.system.test.executor.api.test.model.TestModel;
import org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo;
import org.creekservice.internal.system.test.parser.SystemTestMapper;

/** A test helper for testing Creek system test extensions. */
public final class CreekSystemTestExtensionTester {

    private final DockerServiceContainer services;
    private final ComponentDefinitions<ServiceDefinition> serviceDefinitions;
    private final ComponentDefinitions<AggregateDefinition> aggregateDefinitions;
    private final ServiceDebugInfo serviceDebugInfo;
    private final List<MountInfo> mountInfo;
    private final Map<String, String> env;

    /**
     * @return a builder for creating a tester instance.
     */
    public static TesterBuilder tester() {
        return new TesterBuilder(ServiceDebugInfo.none(), List.of(), Map.of());
    }

    /**
     * Define a {@link MountInfo mount}.
     *
     * @param hostPath the path to the directory to mount on the host machine.
     * @param containerPath the path within the container the mount will be accessible.
     * @param readOnly indicates if the mount should be read-only or mountable.
     * @return the mount info.
     */
    public static MountInfo mount(
            final Path hostPath, final Path containerPath, final boolean readOnly) {
        return new Mount(hostPath, containerPath, readOnly);
    }

    /**
     * Returns a builder that can be used to customise the system test model and then build a YAML
     * deserializer.
     *
     * <p>The returned builder exposes the same {@link TestModelContainer} type that test extensions
     * use to register their model types. After registering types, the created deserializer can be
     * used to test deserialization of an extensions test model types.
     *
     * @return the builder.
     */
    public static YamlParserBuilder yamlParser() {
        return new YamlParserBuilder();
    }

    private CreekSystemTestExtensionTester(
            final ComponentDefinitions<ServiceDefinition> serviceDefinitions,
            final ComponentDefinitions<AggregateDefinition> aggregateDefinitions,
            final ServiceDebugInfo serviceDebugInfo,
            final Collection<MountInfo> mountInfo,
            final Map<String, String> env) {
        this.serviceDefinitions = requireNonNull(serviceDefinitions, "serviceDefinitions");
        this.aggregateDefinitions = requireNonNull(aggregateDefinitions, "aggregateDefinitions");
        this.serviceDebugInfo = requireNonNull(serviceDebugInfo, "serviceDebugInfo");
        this.mountInfo = List.copyOf(requireNonNull(mountInfo, "mountInfo"));
        this.env = Map.copyOf(requireNonNull(env, "env"));

        this.services =
                new DockerServiceContainer(
                        new ContainerFactory(this.serviceDebugInfo, this.mountInfo, this.env));
    }

    /**
     * @return created instance of the test helper
     * @deprecated use {@link #tester()} builder.
     */
    @Deprecated
    public static CreekSystemTestExtensionTester extensionTester() {
        final List<ComponentDescriptor> components = ComponentDescriptors.load();
        final ComponentDefinitions<ServiceDefinition> serviceDefinitions =
                ComponentDefinitions.serviceDefinitions(components);
        final ComponentDefinitions<AggregateDefinition> aggregateDefinitions =
                ComponentDefinitions.aggregateDefinitions(components);
        return new CreekSystemTestExtensionTester(
                serviceDefinitions,
                aggregateDefinitions,
                ServiceDebugInfo.none(),
                List.of(),
                Map.of());
    }

    /**
     * Loads accessible test extensions from the module and class paths.
     *
     * <p>Extension implementations can call this method to ensure the extension is correctly
     * registered and accessible to Creek.
     *
     * <p>Ideally, extensions should be registered both in the {@code module-info.java} and under
     * {@code META-INF/services}. This will ensure the extension is accessible when running within
     * JPMS and without.
     *
     * <p>Ideally, the accessibility of extension should be tested both within JPMS and without.
     *
     * @return the accessible extensions.
     */
    public List<CreekTestExtension> accessibleExtensions() {
        return CreekTestExtensions.load();
    }

    /**
     * Collection of all service definitions that could be loaded from the class and module paths.
     *
     * <p>The returned collection can be queried to get {@link
     * org.creekservice.api.system.test.extension.component.definition.ServiceDefinition service
     * definitions} that can be used to create service instances.
     *
     * @return service definition collection.
     */
    public ComponentDefinitionCollection<ServiceDefinition> serviceDefinitions() {
        return serviceDefinitions;
    }

    /**
     * Collection of all aggregate definitions that could be loaded from the class and module paths.
     *
     * @return aggregate definition collection.
     */
    public ComponentDefinitionCollection<AggregateDefinition> aggregateDefinitions() {
        return aggregateDefinitions;
    }

    /**
     * Get an implementation of the `ServiceContainer` that will run containers in local docker
     * containers.
     *
     * <p>Extension implementations can use this to perform functional testing of the extension,
     * i.e. ensuring any required 3rd party docker containers are correctly brought up and
     * accessible from the system test framework and other containers, as required.
     *
     * @return a docker based service container.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    public ServiceInstanceContainer dockerServicesContainer() {
        return services;
    }

    /**
     * @return the ids of running containers, keyed on the instance name.
     */
    @SuppressWarnings("deprecation")
    public Map<String, String> runningContainerIds() {
        return services.stream()
                .filter(ServiceInstance::running)
                .collect(
                        Collectors.toMap(
                                ServiceInstance::name, i -> ((ContainerInstance) i).containerId()));
    }

    /**
     * Stop and remove all services from the service container exposed from {@link
     * #dockerServicesContainer()}.
     */
    public void clearServices() {
        services.forEach(ServiceInstance::stop);
        services.clear();
    }

    ServiceDebugInfo serviceDebugInfo() {
        return serviceDebugInfo;
    }

    List<MountInfo> mountInfo() {
        return mountInfo;
    }

    Map<String, String> env() {
        return env;
    }

    /** Tester builder */
    public static final class TesterBuilder {

        private final ServiceDebugInfo serviceDebugInfo;
        private final List<MountInfo> mountInfo;
        private final Map<String, String> env;

        private TesterBuilder(
                final ServiceDebugInfo serviceDebugInfo,
                final List<MountInfo> mountInfo,
                final Map<String, String> env) {
            this.serviceDebugInfo = requireNonNull(serviceDebugInfo, "serviceDebugInfo");
            this.mountInfo = List.copyOf(requireNonNull(mountInfo, "mountInfo"));
            this.env = Map.copyOf(requireNonNull(env, "env"));
        }

        /**
         * Initialize the extension tester information on what services to debug.
         *
         * @param serviceNames the list of service names to debug.
         * @return amended test helper with debug info set.
         * @see <a
         *     href="https://github.com/creek-service/creek-system-test#debugging-system-tests">Service
         *     Debugging</a>
         */
        public TesterBuilder withDebugServices(final String... serviceNames) {
            return withDebugServices(
                    ServiceDebugInfo.serviceDebugInfo(
                            DEFAULT_BASE_DEBUG_PORT, Set.of(serviceNames), Set.of()));
        }

        /**
         * Initialize the extension tester information on what services to debug.
         *
         * @param debugInfo the info about the services to debug.
         * @return amended test helper with debug info set.
         * @see <a
         *     href="https://github.com/creek-service/creek-system-test#debugging-system-tests">Service
         *     Debugging</a>
         */
        public TesterBuilder withDebugServices(final ExecutorOptions.ServiceDebugInfo debugInfo) {
            return new TesterBuilder(ServiceDebugInfo.copyOf(debugInfo), mountInfo, env);
        }

        /**
         * Add a read-only mount.
         *
         * @param hostPath the path to the directory to mount on the host machine.
         * @param containerPath the path within the container the mount will be accessible.
         * @return self
         */
        public TesterBuilder withReadOnlyMount(final Path hostPath, final Path containerPath) {
            return withMount(mount(hostPath, containerPath, true));
        }

        /**
         * Add a writable mount.
         *
         * @param hostPath the path to the directory to mount on the host machine.
         * @param containerPath the path within the container the mount will be accessible.
         * @return self
         */
        public TesterBuilder withWritableMount(final Path hostPath, final Path containerPath) {
            return withMount(mount(hostPath, containerPath, false));
        }

        /**
         * Add a mount.
         *
         * @param mount details of the mount to add. See {@link #mount(Path, Path, boolean)}.
         * @return self
         */
        public TesterBuilder withMount(final MountInfo mount) {
            return withMounts(List.of(mount));
        }

        /**
         * Add mounts.
         *
         * @param mounts details of the mounts to add. See {@link #mount(Path, Path, boolean)}.
         * @return self
         */
        public TesterBuilder withMounts(final Collection<? extends MountInfo> mounts) {
            final List<MountInfo> all = new ArrayList<>(mountInfo);
            all.addAll(mounts);
            return new TesterBuilder(serviceDebugInfo, all, env);
        }

        /**
         * Add an environment variable to set on services-under-test.
         *
         * @param key the name of the environment variable.
         * @param value the value of the environment variable.
         * @return self.
         */
        public TesterBuilder withEnv(final String key, final String value) {
            return withEnv(Map.of(key, value));
        }

        /**
         * Add environment variables to set on services-under-test.
         *
         * @param env the variables to set.
         * @return self.
         */
        public TesterBuilder withEnv(final Map<String, String> env) {
            final Map<String, String> all = new HashMap<>(this.env);
            all.putAll(env);
            return new TesterBuilder(serviceDebugInfo, mountInfo, all);
        }

        /**
         * Build the tester.
         *
         * @return the tester
         */
        public CreekSystemTestExtensionTester build() {
            final List<ComponentDescriptor> components = ComponentDescriptors.load();
            final ComponentDefinitions<ServiceDefinition> serviceDefinitions =
                    ComponentDefinitions.serviceDefinitions(components);
            final ComponentDefinitions<AggregateDefinition> aggregateDefinitions =
                    ComponentDefinitions.aggregateDefinitions(components);
            return new CreekSystemTestExtensionTester(
                    serviceDefinitions, aggregateDefinitions, serviceDebugInfo, mountInfo, env);
        }
    }

    /** Parser builder. */
    public static final class YamlParserBuilder {

        private final TestModel model;

        private YamlParserBuilder() {
            this.model = new TestModel();
        }

        /**
         * The test model container.
         *
         * <p>Tests should add the same ref, input and expectation types that the extension adds.
         *
         * @return the test model container.
         */
        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
        public TestModelContainer model() {
            return model;
        }

        /**
         * @return the model parser.
         */
        public ModelParser build() {
            final ObjectMapper mapper = SystemTestMapper.create(model.modelTypes());

            return new ModelParser() {
                @Override
                public <B, S extends B> S parseOther(
                        final String text, final Class<B> baseType, final Class<S> subType)
                        throws JsonProcessingException {
                    return subType.cast(mapper.readValue(text, baseType));
                }
            };
        }
    }

    private static final class Mount implements MountInfo {
        private final Path hostPath;
        private final Path containerPath;
        private final boolean readOnly;

        private Mount(final Path hostPath, final Path containerPath, final boolean readOnly) {
            this.hostPath = requireNonNull(hostPath, "hostPath");
            this.containerPath = requireNonNull(containerPath, "containerPath");
            this.readOnly = readOnly;
        }

        @Override
        public Path hostPath() {
            return hostPath;
        }

        @Override
        public Path containerPath() {
            return containerPath;
        }

        @Override
        public boolean readOnly() {
            return readOnly;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Mount mount = (Mount) o;
            return readOnly == mount.readOnly
                    && Objects.equals(hostPath, mount.hostPath)
                    && Objects.equals(containerPath, mount.containerPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hostPath, containerPath, readOnly);
        }

        @Override
        public String toString() {
            return "Mount{"
                    + "hostPath="
                    + hostPath
                    + ", containerPath="
                    + containerPath
                    + ", readOnly="
                    + readOnly
                    + '}';
        }
    }
}

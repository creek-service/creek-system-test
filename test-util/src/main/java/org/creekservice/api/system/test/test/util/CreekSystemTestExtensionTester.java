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

package org.creekservice.api.system.test.test.util;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptors;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.api.system.test.extension.CreekTestExtensions;
import org.creekservice.api.system.test.extension.component.definition.AggregateDefinition;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinitionCollection;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.suite.service.ServiceInstance;
import org.creekservice.api.system.test.extension.test.suite.service.ServiceInstanceContainer;
import org.creekservice.internal.system.test.executor.api.component.definition.ComponentDefinitions;
import org.creekservice.internal.system.test.executor.api.test.suite.service.ContainerInstance;
import org.creekservice.internal.system.test.executor.api.test.suite.service.DockerServiceContainer;

/** A test helper for testing Creek system test extensions. */
public final class CreekSystemTestExtensionTester {

    private final DockerServiceContainer services;
    private final ComponentDefinitions<ServiceDefinition> serviceDefinitions;
    private final ComponentDefinitions<AggregateDefinition> aggregateDefinitions;

    private CreekSystemTestExtensionTester() {
        this.services = new DockerServiceContainer();
        final List<ComponentDescriptor> components = ComponentDescriptors.load();
        this.serviceDefinitions = ComponentDefinitions.serviceDefinitions(components);
        this.aggregateDefinitions = ComponentDefinitions.aggregateDefinitions(components);
    }

    public static CreekSystemTestExtensionTester extensionTester() {
        return new CreekSystemTestExtensionTester();
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

    /** @return the ids of running containers, keyed on the instance name. */
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
}

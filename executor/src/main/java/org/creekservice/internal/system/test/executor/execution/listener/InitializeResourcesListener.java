/*
 * Copyright 2022-2026 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.internal.system.test.executor.execution.listener;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.CreatableResource;
import org.creekservice.api.platform.metadata.ResourceCollection;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.platform.resource.ResourceInitializer;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinition;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinitionCollection;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.internal.system.test.executor.api.SystemTest;
import org.creekservice.internal.system.test.executor.execution.input.Inputters;

/**
 * Test listener that initialises any shared or unowned resources required by the services under
 * test, plus any resources targeted by seed data regardless of ownership.
 *
 * <p>Shared resources are not owned by any one service. In production, they would be initialised
 * before services were deployed. Hence, the test framework needs to also ensure shared resources
 * are initialised, before it runs any tests.
 *
 * <p>Unowned resources are resources from services not under test, which the services under test
 * are interacting with. For example, consuming an output topic that another service owns. The test
 * framework needs to ensure such edge resources are initialised, before it runs any tests.
 *
 * <p>Also prepares exactly the resources it ensures, so seed data can be injected before
 * services-under-test start. See {@link PrepareResourcesListener} for the latter, full preparation
 * of all known resources, once services-under-test have started.
 */
public final class InitializeResourcesListener implements TestEnvironmentListener {

    private final SystemTest api;
    private final ResourceInitializer initializer;
    private final Inputters inputters;

    /**
     * @param api system test api.
     */
    public InitializeResourcesListener(final SystemTest api) {
        this(
                api,
                ResourceInitializer.resourceInitializer(
                        new ResourceInitializer.Callbacks() {
                            @Override
                            public <T extends ResourceDescriptor> void validate(
                                    final Class<T> type, final Collection<T> resources) {
                                api.extensions().model().resourceHandler(type).validate(resources);
                            }

                            @Override
                            public <T extends CreatableResource> void ensure(
                                    final Class<T> type, final Collection<T> creatableResources) {
                                api.extensions()
                                        .model()
                                        .resourceHandler(type)
                                        .ensure(creatableResources);
                            }
                        }),
                new Inputters(api.tests().model()));
    }

    @VisibleForTesting
    InitializeResourcesListener(
            final SystemTest api,
            final ResourceInitializer initializer,
            final Inputters inputters) {
        this.api = requireNonNull(api, "api");
        this.initializer = requireNonNull(initializer, "initializer");
        this.inputters = requireNonNull(inputters, "inputters");
    }

    @Override
    public void beforeSuite(final CreekTestSuite suite) {
        final Set<String> serviceNames = Set.copyOf(suite.services());
        final List<ServiceDescriptor> underTest = servicesUnderTest(serviceNames);
        final List<ComponentDescriptor> other = otherComponents(serviceNames);

        final Set<URI> ensuredIds = new HashSet<>(initializer.init(underTest));
        ensuredIds.addAll(
                initializer.test(underTest, other, inputters.resourceIds(suite.seedData())));

        prepareEnsuredResources(underTest, other, ensuredIds);
    }

    private void prepareEnsuredResources(
            final List<ServiceDescriptor> underTest,
            final List<ComponentDescriptor> other,
            final Set<URI> ensuredIds) {
        if (ensuredIds.isEmpty()) {
            return;
        }

        final Map<URI, ResourceDescriptor> byId =
                Stream.of(underTest, other)
                        .flatMap(Collection::stream)
                        .flatMap(ResourceCollection::collectResources)
                        .filter(r -> ensuredIds.contains(r.id()))
                        .collect(
                                groupingBy(
                                        ResourceDescriptor::id,
                                        collectingAndThen(toList(), l -> l.get(0))));

        ResourcePreparer.prepare(api, byId.values());
    }

    private List<ServiceDescriptor> servicesUnderTest(final Set<String> servicesUnderTest) {
        final ComponentDefinitionCollection<ServiceDefinition> definitions =
                api.components().definitions().services();
        return servicesUnderTest.stream()
                .map(definitions::get)
                .map(ServiceDefinition::descriptor)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private List<ComponentDescriptor> otherComponents(final Set<String> servicesUnderTest) {
        final Predicate<ComponentDefinition> notServiceUnderTest =
                def -> {
                    final boolean service =
                            def.descriptor()
                                    .map(desc -> desc instanceof ServiceDescriptor)
                                    .orElse(false);
                    return !service || !servicesUnderTest.contains(def.name());
                };

        return api.components().definitions().stream()
                .filter(notServiceUnderTest)
                .map(ComponentDefinition::descriptor)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}

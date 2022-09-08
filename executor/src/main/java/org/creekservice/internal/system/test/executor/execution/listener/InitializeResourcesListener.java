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

package org.creekservice.internal.system.test.executor.execution.listener;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.platform.resource.ResourceInitializer;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinition;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinitionCollection;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.internal.system.test.executor.api.SystemTest;

public final class InitializeResourcesListener implements TestEnvironmentListener {

    private final SystemTest api;
    private final ResourceInitializer initializer;

    public InitializeResourcesListener(final SystemTest api) {
        this(
                api,
                ResourceInitializer.resourceInitializer(api.component().model()::resourceHandler));
    }

    @VisibleForTesting
    InitializeResourcesListener(final SystemTest api, final ResourceInitializer initializer) {
        this.api = requireNonNull(api, "api");
        this.initializer = requireNonNull(initializer, "initializer");
    }

    @Override
    public void beforeSuite(final CreekTestSuite suite) {
        final Set<String> serviceNames = Set.copyOf(suite.services());
        final List<ServiceDescriptor> underTest = servicesUnderTest(serviceNames);
        final List<ComponentDescriptor> other = otherComponents(serviceNames);

        initializer.init(underTest);
        initializer.test(underTest, other);
    }

    private List<ServiceDescriptor> servicesUnderTest(final Set<String> servicesUnderTest) {
        final ComponentDefinitionCollection<ServiceDefinition> definitions =
                api.component().definitions().service();
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

        return api.component().definitions().stream()
                .filter(notServiceUnderTest)
                .map(ComponentDefinition::descriptor)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}

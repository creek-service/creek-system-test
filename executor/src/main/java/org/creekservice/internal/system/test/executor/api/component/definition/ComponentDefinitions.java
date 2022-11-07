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

package org.creekservice.internal.system.test.executor.api.component.definition;

import static java.lang.System.lineSeparator;
import static org.creekservice.api.base.type.Preconditions.requireNonBlank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.base.type.CodeLocation;
import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.system.test.extension.component.definition.AggregateDefinition;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinition;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinitionCollection;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;

/**
 * Implementation of {@link ComponentDefinitionCollection}.
 *
 * @param <Def> the type of definition.
 */
public final class ComponentDefinitions<Def extends ComponentDefinition>
        implements ComponentDefinitionCollection<Def> {

    private final long threadId;
    private final String componentType;
    private final Map<String, Def> components;

    /**
     * Create aggregate definition collection.
     *
     * @param components list of components.
     * @return the aggregate definition collection.
     */
    public static ComponentDefinitions<AggregateDefinition> aggregateDefinitions(
            final Collection<? extends ComponentDescriptor> components) {
        return new ComponentDefinitions<>(
                components,
                AggregateDescriptor.class,
                AggregateDescriptorBasedDefinition::new,
                "aggregate",
                Thread.currentThread().getId());
    }

    /**
     * Create service definition collection.
     *
     * @param components list of components.
     * @return the service definition collection.
     */
    public static ComponentDefinitions<ServiceDefinition> serviceDefinitions(
            final Collection<? extends ComponentDescriptor> components) {
        return serviceDefinitions(components, Thread.currentThread().getId());
    }

    @VisibleForTesting
    static ComponentDefinitions<ServiceDefinition> serviceDefinitions(
            final Collection<? extends ComponentDescriptor> components, final long threadId) {
        return new ComponentDefinitions<>(
                components,
                ServiceDescriptor.class,
                ServiceDescriptorBasedDefinition::new,
                "service",
                threadId);
    }

    private <Desc extends ComponentDescriptor> ComponentDefinitions(
            final Collection<? extends ComponentDescriptor> components,
            final Class<Desc> descriptorType,
            final Function<Desc, Def> factory,
            final String componentType,
            final long threadId) {
        this.threadId = threadId;
        this.componentType = requireNonBlank(componentType, "componentType");
        this.components = toDefinitions(components, descriptorType, factory, componentType);
    }

    @Override
    public Def get(final String componentName) {
        throwIfNotOnCorrectThread();
        final Def def = components.get(componentName);
        if (def == null) {
            throw new UnknownComponentDefinitionException(
                    componentName, components.keySet(), componentType);
        }
        return def;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<Def> iterator() {
        throwIfNotOnCorrectThread();
        return components.values().iterator();
    }

    private static <Def extends ComponentDefinition, Desc extends ComponentDescriptor>
            Map<String, Def> toDefinitions(
                    final Collection<? extends ComponentDescriptor> components,
                    final Class<Desc> descriptorType,
                    final Function<Desc, Def> factory,
                    final String componentType) {
        return components.stream()
                .filter(descriptorType::isInstance)
                .map(descriptorType::cast)
                .map(factory)
                .collect(
                        Collectors.toUnmodifiableMap(
                                ComponentDefinition::name,
                                Function.identity(),
                                throwOnNameClash(componentType)));
    }

    private static <Def extends ComponentDefinition> BinaryOperator<Def> throwOnNameClash(
            final String componentType) {
        return (def0, def1) -> {
            throw new ComponentDescriptorNameClashException(def0, def1, componentType);
        };
    }

    private void throwIfNotOnCorrectThread() {
        if (Thread.currentThread().getId() != threadId) {
            throw new ConcurrentModificationException("Class is not thread safe");
        }
    }

    private static final class ComponentDescriptorNameClashException
            extends IllegalArgumentException {

        ComponentDescriptorNameClashException(
                final ComponentDefinition def0,
                final ComponentDefinition def1,
                final String componentType) {
            super(
                    "Two or more "
                            + componentType
                            + " descriptors where found with the same name. Names must be unique."
                            + lineSeparator()
                            + "name: "
                            + def0.name()
                            + lineSeparator()
                            + "descriptor_locations: ["
                            + lineSeparator()
                            + CodeLocation.codeLocation(def0.descriptor().orElseThrow())
                            + lineSeparator()
                            + CodeLocation.codeLocation(def1.descriptor().orElseThrow())
                            + lineSeparator()
                            + "]");
        }
    }

    private static final class UnknownComponentDefinitionException
            extends IllegalArgumentException {
        UnknownComponentDefinitionException(
                final String serviceName,
                final Set<String> knownServices,
                final String componentType) {
            super(
                    "Unknown "
                            + componentType
                            + ": "
                            + serviceName
                            + ". Known "
                            + componentType
                            + "s are: "
                            + sorted(knownServices));
        }

        private static List<String> sorted(final Collection<String> names) {
            final List<String> sorted = new ArrayList<>(names);
            sorted.sort(Comparator.naturalOrder());
            return sorted;
        }
    }
}

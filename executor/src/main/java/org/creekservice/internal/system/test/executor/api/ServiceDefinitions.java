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

package org.creekservice.internal.system.test.executor.api;

import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;

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
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.system.test.extension.service.ServiceDefinition;
import org.creekservice.api.system.test.extension.service.ServiceDefinitionCollection;

public final class ServiceDefinitions implements ServiceDefinitionCollection {

    private final long threadId;
    private final Map<String, ServiceDefinition> services;

    public ServiceDefinitions(final Collection<? extends ComponentDescriptor> components) {
        this(components, Thread.currentThread().getId());
    }

    @VisibleForTesting
    ServiceDefinitions(
            final Collection<? extends ComponentDescriptor> components, final long threadId) {
        this.threadId = threadId;
        this.services = toDefinitions(components);
    }

    @Override
    public ServiceDefinition get(final String serviceName) {
        throwIfNotOnCorrectThread();
        final ServiceDefinition def = services.get(serviceName);
        if (def == null) {
            throw new UnknownServiceDefinitionException(serviceName, services.keySet());
        }
        return def;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<ServiceDefinition> iterator() {
        throwIfNotOnCorrectThread();
        return services.values().iterator();
    }

    private static Map<String, ServiceDefinition> toDefinitions(
            final Collection<? extends ComponentDescriptor> components) {
        return components.stream()
                .filter(ServiceDescriptor.class::isInstance)
                .map(ServiceDescriptor.class::cast)
                .map(ServiceDefinitions::toDefinition)
                .collect(
                        Collectors.toUnmodifiableMap(
                                ServiceDefinition::name, Function.identity(), throwOnNameClash()));
    }

    private static ServiceDefinition toDefinition(final ServiceDescriptor descriptor) {
        return new ServiceDescriptorBasedDefinition(descriptor);
    }

    private static BinaryOperator<ServiceDefinition> throwOnNameClash() {
        return (def0, def1) -> {
            throw new ServiceDescriptorNameClashException(
                    (ServiceDescriptorBasedDefinition) def0,
                    (ServiceDescriptorBasedDefinition) def1);
        };
    }

    private void throwIfNotOnCorrectThread() {
        if (Thread.currentThread().getId() != threadId) {
            throw new ConcurrentModificationException("Class is not thread safe");
        }
    }

    private static final class ServiceDescriptorNameClashException
            extends IllegalArgumentException {

        ServiceDescriptorNameClashException(
                final ServiceDescriptorBasedDefinition def0,
                final ServiceDescriptorBasedDefinition def1) {
            super(
                    "Two or more ServiceDescriptors where found with the same name. Names must be unique."
                            + lineSeparator()
                            + "service_name: "
                            + def0.name()
                            + lineSeparator()
                            + "descriptor_locations: ["
                            + lineSeparator()
                            + CodeLocation.codeLocation(def0.descriptor)
                            + lineSeparator()
                            + CodeLocation.codeLocation(def1.descriptor)
                            + lineSeparator()
                            + "]");
        }
    }

    private static final class UnknownServiceDefinitionException extends IllegalArgumentException {
        UnknownServiceDefinitionException(
                final String serviceName, final Set<String> knownServices) {
            super(
                    "Unknown service: "
                            + serviceName
                            + ". Known services are: "
                            + sorted(knownServices));
        }

        private static List<String> sorted(final Collection<String> serviceNames) {
            final List<String> sorted = new ArrayList<>(serviceNames);
            sorted.sort(Comparator.naturalOrder());
            return sorted;
        }
    }

    private static final class ServiceDescriptorBasedDefinition implements ServiceDefinition {

        private final ServiceDescriptor descriptor;

        ServiceDescriptorBasedDefinition(final ServiceDescriptor descriptor) {
            this.descriptor = requireNonNull(descriptor, "descriptor");
        }

        @Override
        public String name() {
            return descriptor.name();
        }

        @Override
        public String dockerImage() {
            return descriptor.dockerImage();
        }
    }
}

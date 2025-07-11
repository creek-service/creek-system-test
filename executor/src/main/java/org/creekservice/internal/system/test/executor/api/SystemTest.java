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

package org.creekservice.internal.system.test.executor.api;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionOptions;
import org.creekservice.api.service.extension.CreekExtensionProvider;
import org.creekservice.api.service.extension.component.model.ComponentModelCollection;
import org.creekservice.api.system.test.extension.CreekSystemTest;
import org.creekservice.api.system.test.extension.component.definition.AggregateDefinition;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinition;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.internal.service.api.Creek;
import org.creekservice.internal.system.test.executor.api.component.definition.ComponentDefinitions;
import org.creekservice.internal.system.test.executor.api.test.env.TestEnv;
import org.creekservice.internal.system.test.executor.api.test.env.suite.service.ContainerFactory;
import org.creekservice.internal.system.test.executor.api.test.model.TestModel;

/** Implementation of {@link CreekSystemTest}. */
public final class SystemTest implements CreekSystemTest {

    private final Tests tests;
    private final Components components;
    private final Extensions extensions;

    /**
     * @param components all known components.
     * @param containerFactory factory for creating Docker containers
     */
    public SystemTest(
            final Collection<? extends ComponentDescriptor> components,
            final ContainerFactory containerFactory) {
        this(
                new TestModel(),
                new TestEnv(containerFactory),
                ComponentDefinitions.serviceDefinitions(components),
                ComponentDefinitions.aggregateDefinitions(components),
                c -> new Creek(c.descriptors()));
    }

    @VisibleForTesting
    SystemTest(
            final TestModel testModel,
            final TestEnv testEnv,
            final ComponentDefinitions<ServiceDefinition> serviceDefinitions,
            final ComponentDefinitions<AggregateDefinition> aggregateDefinitions,
            final Function<Components, Creek> api) {
        this.tests = new Tests(testModel, testEnv);
        this.components = new Components(serviceDefinitions, aggregateDefinitions);
        this.extensions = new Extensions(api.apply(components));
    }

    @Override
    public Tests tests() {
        return tests;
    }

    @Override
    public Components components() {
        return components;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    @Override
    public Extensions extensions() {
        return extensions;
    }

    /** Implementation of {@link CreekSystemTest.TestAccessor}. */
    public static final class Tests implements CreekSystemTest.TestAccessor {

        private final TestModel testModel;
        private final TestEnv testEnv;

        Tests(final TestModel testModel, final TestEnv testEnv) {
            this.testModel = requireNonNull(testModel, "testModel");
            this.testEnv = requireNonNull(testEnv, "testEnv");
        }

        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
        @Override
        public TestModel model() {
            return testModel;
        }

        @Override
        public TestEnv env() {
            return testEnv;
        }
    }

    /** Implementation of {@link ComponentAccessor}. */
    public static final class Components implements ComponentAccessor {

        private final Definitions definitions;

        Components(
                final ComponentDefinitions<ServiceDefinition> serviceDefinitions,
                final ComponentDefinitions<AggregateDefinition> aggregateDefinitions) {
            this.definitions = new Definitions(serviceDefinitions, aggregateDefinitions);
        }

        @Override
        public Definitions definitions() {
            return definitions;
        }

        List<? extends ComponentDescriptor> descriptors() {
            return definitions.stream()
                    .map(ComponentDefinition::descriptor)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
        }
    }

    /** Implementation of {@link ComponentDefinitionAccessor} */
    public static final class Definitions implements ComponentDefinitionAccessor {

        private final ComponentDefinitions<ServiceDefinition> serviceDefinitions;
        private final ComponentDefinitions<AggregateDefinition> aggregateDefinitions;

        Definitions(
                final ComponentDefinitions<ServiceDefinition> serviceDefinitions,
                final ComponentDefinitions<AggregateDefinition> aggregateDefinitions) {
            this.serviceDefinitions = requireNonNull(serviceDefinitions, "serviceDefinitions");
            this.aggregateDefinitions =
                    requireNonNull(aggregateDefinitions, "aggregateDefinitions");
        }

        @Override
        public ComponentDefinitions<AggregateDefinition> aggregates() {
            return aggregateDefinitions;
        }

        @Override
        public ComponentDefinitions<ServiceDefinition> services() {
            return serviceDefinitions;
        }
    }

    /** Implementation of {@link ExtensionAccessor}. */
    public static final class Extensions implements ExtensionAccessor {

        final Creek api;

        Extensions(final Creek api) {
            this.api = requireNonNull(api, "api");
        }

        @Override
        public void addOption(final CreekExtensionOptions option) {
            api.options().add(option);
        }

        @Override
        public <T extends CreekExtension> T ensureExtension(
                final Class<? extends CreekExtensionProvider<T>> provider) {
            return api.extensions().ensureExtension(provider);
        }

        /**
         * @return the component model, e.g. supported resource descriptor types, etc.
         */
        public ComponentModelCollection model() {
            return api.components().model();
        }

        /** Close all extensions. */
        public void close() {
            api.extensions().close();
        }
    }
}

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

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.system.test.extension.CreekSystemTest;
import org.creekservice.api.system.test.extension.component.definition.AggregateDefinition;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.internal.service.api.ComponentModel;
import org.creekservice.internal.system.test.executor.api.component.definition.ComponentDefinitions;
import org.creekservice.internal.system.test.executor.api.test.env.TestEnv;
import org.creekservice.internal.system.test.executor.api.test.model.TestModel;

public final class SystemTest implements CreekSystemTest {

    private final Test test;
    private final Components components;

    public SystemTest(final Collection<? extends ComponentDescriptor> components) {
        this(
                new TestModel(),
                new ComponentModel(),
                new TestEnv(),
                ComponentDefinitions.serviceDefinitions(components),
                ComponentDefinitions.aggregateDefinitions(components));
    }

    @VisibleForTesting
    SystemTest(
            final TestModel testModel,
            final ComponentModel componentModel,
            final TestEnv testEnv,
            final ComponentDefinitions<ServiceDefinition> serviceDefinitions,
            final ComponentDefinitions<AggregateDefinition> aggregateDefinitions) {
        this.test = new Test(testModel, testEnv);
        this.components = new Components(componentModel, serviceDefinitions, aggregateDefinitions);
    }

    @Override
    public Test test() {
        return test;
    }

    @Override
    public Components component() {
        return components;
    }

    public static final class Test implements CreekSystemTest.TestAccessor {

        private final TestModel testModel;
        private final TestEnv testEnv;

        Test(final TestModel testModel, final TestEnv testEnv) {
            this.testModel = requireNonNull(testModel, "testModel");
            this.testEnv = requireNonNull(testEnv, "testEnv");
        }

        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
        @Override
        public TestModel model() {
            return testModel;
        }

        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
        @Override
        public TestEnv env() {
            return testEnv;
        }
    }

    public static final class Components implements ComponentAccessor {

        private final ComponentModel componentModel;
        private final Definitions definitions;

        Components(
                final ComponentModel componentModel,
                final ComponentDefinitions<ServiceDefinition> serviceDefinitions,
                final ComponentDefinitions<AggregateDefinition> aggregateDefinitions) {
            this.componentModel = requireNonNull(componentModel, "componentModel");
            this.definitions = new Definitions(serviceDefinitions, aggregateDefinitions);
        }

        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
        @Override
        public ComponentModel model() {
            return componentModel;
        }

        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
        @Override
        public Definitions definitions() {
            return definitions;
        }
    }

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

        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
        @Override
        public ComponentDefinitions<AggregateDefinition> aggregate() {
            return aggregateDefinitions;
        }

        @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
        @Override
        public ComponentDefinitions<ServiceDefinition> service() {
            return serviceDefinitions;
        }
    }
}

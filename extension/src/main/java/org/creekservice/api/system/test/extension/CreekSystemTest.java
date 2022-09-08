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

package org.creekservice.api.system.test.extension;


import java.util.stream.Stream;
import org.creekservice.api.service.extension.model.ComponentModelContainer;
import org.creekservice.api.system.test.extension.component.definition.AggregateDefinition;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinition;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinitionCollection;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.env.TestEnvironment;
import org.creekservice.api.system.test.extension.test.model.TestModelContainer;

/** API to the system tests exposed to extensions */
public interface CreekSystemTest {

    /**
     * Information about tests and their data model, e.g. inputs, outputs, test suites, etc.
     *
     * @return accessor of information about tests.
     */
    TestAccessor test();

    /**
     * Information about components, i.e. services and aggregates, and their data model, i.e.
     * resource types, etc.
     *
     * @return accessor of information about system components.
     */
    ComponentAccessor component();

    interface TestAccessor {
        /**
         * The data model of the system tests.
         *
         * <p>This is the model used when deserializing system tests files.
         *
         * <p>Extensions can extend this model.
         *
         * @return the model.
         */
        TestModelContainer model();

        /**
         * The environment in which tests execute.
         *
         * @return the test environment.
         */
        TestEnvironment env();
    }

    interface ComponentAccessor {
        /**
         * The component data model, i.e. the resource types used by services and aggregates, etc.
         *
         * <p>Extensions can extend this model.
         *
         * @return the component model.
         */
        ComponentModelContainer model();

        /**
         * The definitions of the known components, i.e. services and aggregates.
         *
         * @return the accessor to the component definitions.
         */
        ComponentDefinitionAccessor definitions();
    }

    interface ComponentDefinitionAccessor {

        /**
         * Aggregate component definitions.
         *
         * <p>These are the aggregates discovered on the class or module path.
         *
         * @return a collection of aggregate definitions.
         */
        ComponentDefinitionCollection<AggregateDefinition> aggregate();

        /**
         * Service component definitions.
         *
         * <p>These are the services discovered on the class or module path.
         *
         * @return a collection of service definitions.
         */
        ComponentDefinitionCollection<ServiceDefinition> service();

        /** @return stream of all the known component definitions. */
        default Stream<ComponentDefinition> stream() {
            return Stream.concat(aggregate().stream(), service().stream());
        }
    }
}

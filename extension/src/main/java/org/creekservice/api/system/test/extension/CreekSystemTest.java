/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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
import org.creekservice.api.service.extension.CreekExtension;
import org.creekservice.api.service.extension.CreekExtensionOptions;
import org.creekservice.api.service.extension.CreekExtensionProvider;
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
    TestAccessor tests();

    /**
     * Information about components, i.e. services and aggregates, and their data model, i.e.
     * resource types, etc.
     *
     * @return accessor of information about system components.
     */
    ComponentAccessor components();

    /**
     * Used to register Creek service extensions.
     *
     * <p>System test extensions will generally want to register their associated service extension,
     * (https://github.com/creek-service/creek-service/tree/main/extension), as they register
     * necessary component model extensions and provide functionality test extensions need.
     *
     * @return extensions.
     */
    ExtensionAccessor extensions();

    /** Provides access to system test extensions. */
    interface ExtensionAccessor {

        /**
         * Register an extensions option set.
         *
         * <p>Allows customisation of service extensions added via {@link #ensureExtension}
         *
         * @param option the options to add.
         */
        void addOption(CreekExtensionOptions option);

        /**
         * Ensure an extension has been initialized.
         *
         * <p>Initializes the extension from the supplied {@code providerType} if it has not already
         * been initialized. Does nothing if the extension has already been applied.
         *
         * @param provider the extension provider type.
         * @param <T> the type of the extension
         * @return the initialized extension.
         */
        <T extends CreekExtension> T ensureExtension(
                Class<? extends CreekExtensionProvider<T>> provider);
    }

    /** Provides access to information about the tests */
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

    /** Provides access to the known components. */
    interface ComponentAccessor {

        /**
         * The definitions of the known components, i.e. services and aggregates.
         *
         * @return the accessor to the component definitions.
         */
        ComponentDefinitionAccessor definitions();
    }

    /** Provides access to the known component definitions. */
    interface ComponentDefinitionAccessor {

        /**
         * Aggregate component definitions.
         *
         * <p>These are the aggregates discovered on the class or module path.
         *
         * @return a collection of aggregate definitions.
         */
        ComponentDefinitionCollection<AggregateDefinition> aggregates();

        /**
         * Service component definitions.
         *
         * <p>These are the services discovered on the class or module path.
         *
         * @return a collection of service definitions.
         */
        ComponentDefinitionCollection<ServiceDefinition> services();

        /**
         * @return stream of all the known component definitions.
         */
        default Stream<ComponentDefinition> stream() {
            return Stream.concat(aggregates().stream(), services().stream());
        }
    }
}

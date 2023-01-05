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

package org.creekservice.api.system.test.extension.test.model;

/** A mutable container of extensions to the Creek Service model. */
public interface TestModelContainer extends TestModelCollection {

    /**
     * Register a generic {@link Ref} model extension.
     *
     * @param type the model extension.
     * @param <T> the model extension.
     * @return a builder that can be used to customize the model.
     */
    <T extends InputRef & ExpectationRef> NameBuilder addRef(Class<T> type);

    /**
     * Register an {@link InputRef} model extension.
     *
     * @param type the model extension.
     * @param <T> the model extension.
     * @return a builder that can be used to customize the model.
     */
    <T extends InputRef> NameBuilder addInputRef(Class<T> type);

    /**
     * Register an {@link ExpectationRef} model extension.
     *
     * @param type the model extension.
     * @param <T> the model extension.
     * @return a builder that can be used to customize the model.
     */
    <T extends ExpectationRef> NameBuilder addExpectationRef(Class<T> type);

    /**
     * Register an {@link Input} model extension and its handler.
     *
     * @param type the model extension.
     * @param handler the handler called to handle instances of this type.
     * @param <T> the model extension.
     * @return a builder that can be used to customize the model.
     */
    <T extends Input> NameBuilder addInput(Class<T> type, InputHandler<? super T> handler);

    /**
     * Register an {@link Expectation} model extension and its handler.
     *
     * @param type the model extension.
     * @param handler the handler called to handle instances of this type.
     * @param <T> the model extension.
     * @return a builder that can be used to customize the model.
     */
    <T extends Expectation> NameBuilder addExpectation(
            Class<T> type, ExpectationHandler<? super T> handler);

    /**
     * Register a test option extension.
     *
     * <p>Users can define options within a test suite that configure a test extension's
     * functionality.
     *
     * @param type the model extension.
     * @param <T> the model extension.
     * @return a builder that can be used to customize the model.
     */
    <T extends Option> NameBuilder addOption(Class<T> type);

    /** Allows customisation of the model extension. */
    interface NameBuilder {
        /**
         * Sets a custom name for the model.
         *
         * <p>This name if the name used within the YAML files in the {@code type} property to
         * inform the deserializer to deserialize the object as your custom type.
         *
         * <p>The default name of the subtype will be derived from the {@code type} name. See {@code
         * SubTypeNaming.subTypeName()} in {@code creek-base-schema} module for more info details.
         *
         * @param name the custom name, as used in the YAML files.
         */
        void withName(String name);
    }
}

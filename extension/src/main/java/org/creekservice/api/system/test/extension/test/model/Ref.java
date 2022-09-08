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

package org.creekservice.api.system.test.extension.test.model;

/**
 * A reference to a model file.
 *
 * <p>Creek test extensions can define additional reference types. Those reference types should not
 * implement this interface directly: they should either implement {@link InputRef} or {@link
 * ExpectationRef} or both.
 */
public interface Ref {
    /**
     * The id of the file being referenced
     *
     * <p>The name of the file within the type's directory, e.g. for an {@link InputRef input ref}
     * the path is relative to the {@code inputs} directory, and for an {@link ExpectationRef} it is
     * relative to the {@code expectations} directory.
     *
     * <p>The {@code .yml} or {@code .yaml} file extension is optional
     *
     * <p>For example, a {@link Input} file called {@code some_input.yml} within the test package's
     * {@code input} directory can be referenced with the id {@code "some_input"} or {@code
     * "some_input.yml"}.
     *
     * @return the id of the model file.
     */
    String id();
}

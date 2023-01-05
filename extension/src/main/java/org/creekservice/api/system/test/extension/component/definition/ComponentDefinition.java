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

package org.creekservice.api.system.test.extension.component.definition;

import java.util.Optional;
import org.creekservice.api.platform.metadata.ComponentDescriptor;

/** Information known about a component. */
public interface ComponentDefinition {
    /**
     * The name of the component.
     *
     * <p>This name must be unique within the system.
     *
     * @return the name of the component.
     */
    String name();

    /**
     * An optional component descriptor.
     *
     * <p>Any component loaded from the module or class path will have an associated {@link
     * ComponentDescriptor}.
     *
     * @return the component definition, if present.
     */
    default Optional<? extends ComponentDescriptor> descriptor() {
        return Optional.empty();
    }
}

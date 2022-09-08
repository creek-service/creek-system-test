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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.creekservice.api.system.test.extension.component.definition.AggregateDefinition;

final class AggregateDescriptorBasedDefinition implements AggregateDefinition {

    private final AggregateDescriptor descriptor;

    AggregateDescriptorBasedDefinition(final AggregateDescriptor descriptor) {
        this.descriptor = requireNonNull(descriptor, "descriptor");
    }

    @Override
    public String name() {
        return descriptor.name();
    }

    @Override
    public Optional<AggregateDescriptor> descriptor() {
        return Optional.of(descriptor);
    }
}

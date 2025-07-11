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

package org.creekservice.internal.system.test.executor.api.component.definition;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;

final class ServiceDescriptorBasedDefinition implements ServiceDefinition {

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
        // Default to latest for now. See
        // https://github.com/creek-service/creek-system-test/issues/78.
        return descriptor.dockerImage() + ":latest";
    }

    @Override
    public Optional<ServiceDescriptor> descriptor() {
        return Optional.of(descriptor);
    }
}

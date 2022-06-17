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

package org.creekservice.api.system.test.extension.service;


import java.util.Optional;
import org.creekservice.api.platform.metadata.ServiceDescriptor;

/** Information required by Creek System Test to start a service */
public interface ServiceDefinition {
    /**
     * The name of the service.
     *
     * <p>This name must be unique within the system.
     *
     * @return the name of the service.
     */
    String name();

    /** @return the docker image name, without version info. */
    String dockerImage();

    /**
     * An optional service descriptor.
     *
     * <p>Any service that is being tested, i.e. defined in the {@code services} property of the
     * test suite, will have an associated service definition. 3rd-party services started by
     * extensions to facilitate testing will not.
     *
     * @return the service definition, if present.
     */
    Optional<ServiceDescriptor> descriptor(); // Todo: test
}

// Todo: I think ALL definitions will have a descriptor

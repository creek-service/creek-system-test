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

package org.creekservice.api.system.test.extension.test.env.suite.service;


import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;

/** A container of services */
public interface ServiceInstanceContainer extends ServiceInstanceCollection {

    /**
     * Add an instance of the service defined by the supplied {@code def}.
     *
     * <p>Adding the same service def multiple times will result in multiple service instances, for
     * example {@code finance-service-0}, {@code finance-service-1}, etc.
     *
     * <p>The newly added instance is not automatically started. Call {@link ServiceInstance#start()
     * start} on the returned instance to start the service.
     *
     * @param def the def of the service to start.
     * @return the configurable service instance that was added.
     */
    ConfigurableServiceInstance add(ServiceDefinition def);
}

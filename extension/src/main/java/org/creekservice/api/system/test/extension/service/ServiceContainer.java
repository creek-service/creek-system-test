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

/** A container of services */
public interface ServiceContainer extends ServiceCollection {

    /**
     * Add an instance of the service defined by the supplied {@code def}.
     *
     * <p>Adding the same service def multiple times will result in multiple service instances.
     *
     * <p>The newly added instance is not automatically started. Call {@link ServiceInstance#start()
     * start} on the returned instance to start the service.
     *
     * @param def the def of the service to start.
     * @return the service instance that was added.
     */
    default ServiceInstance add(ServiceDefinition def) {
        return add(def.name(), def.dockerImage() + ":latest");
    }

    /**
     * Add a service instance to the container.
     *
     * <p>The name of the instance will be the supplied {@code serviceName} with a instance number
     * suffix. For example, adding a service named {@code finance-service} will result in an
     * instance named {@code finance-service-0}.
     *
     * <p>Adding the same service name multiple times will result in multiple service instances, for
     * example {@code finance-service-0}, {@code finance-service-1}, etc.
     *
     * <p>The newly added instance is not automatically started. Call {@link ServiceInstance#start()
     * start} on the returned instance to start the service.
     *
     * @param serviceName the name of the service, e.g. {@code finance-service}.
     * @param dockerImageName the full docker image name of the service.
     * @return the service instance that was added.
     */
    ServiceInstance add(String serviceName, String dockerImageName);
}
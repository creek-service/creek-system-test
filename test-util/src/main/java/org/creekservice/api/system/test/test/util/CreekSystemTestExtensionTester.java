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

package org.creekservice.api.system.test.test.util;


import java.util.List;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.api.system.test.extension.CreekTestExtensions;
import org.creekservice.api.system.test.extension.service.ServiceContainer;
import org.creekservice.internal.system.test.executor.api.testsuite.service.DockerServiceContainer;

/** A test helper for testing Creek system test extensions. */
public final class CreekSystemTestExtensionTester {

    private DockerServiceContainer services;

    private CreekSystemTestExtensionTester() {
        this.services = new DockerServiceContainer();
    }

    public static CreekSystemTestExtensionTester extensionTester() {
        return new CreekSystemTestExtensionTester();
    }

    /**
     * Loads accessible extensions from the module and class paths.
     *
     * <p>Extension implementations can call this method to ensure the extension is correctly
     * registered and accessible to Creek.
     *
     * <p>Ideally, extensions should be registered both in the {@code module-info.java} and under
     * {@code META-INF/services}. This will ensure the extension is accessible when running within
     * JPMS and without.
     *
     * <p>Ideally, the accessibility of extension should be tested both within JPMS and without.
     *
     * @return the accessible extensions.
     */
    public List<CreekTestExtension> accessibleExtensions() {
        return CreekTestExtensions.load();
    }

    /**
     * Get an implementation of the `ServiceContainer` that will run containers in local docker
     * containers.
     *
     * <p>Extension implementations can use this to perform functional testing of the extension,
     * i.e. ensuring any required 3rd party docker containers are correctly brought up and
     * accessible from the system test framework and other containers, as required.
     *
     * @return a docker based service container.
     */
    public ServiceContainer dockerServicesContainer() {
        return services;
    }

    /**
     * Remove all services from the service container exposed from {@link #dockerServicesContainer()}.
     */
    public void clearServices() {
        services.clear(); /// Todo: test
    }
}

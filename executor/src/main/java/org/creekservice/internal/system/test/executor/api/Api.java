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

package org.creekservice.internal.system.test.executor.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptors;
import org.creekservice.api.system.test.executor.ExecutorOptions.MountInfo;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.api.system.test.extension.CreekTestExtensions;
import org.creekservice.internal.system.test.executor.api.test.env.suite.service.ContainerFactory;
import org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo;
import org.creekservice.internal.system.test.executor.execution.listener.AddServicesUnderTestListener;
import org.creekservice.internal.system.test.executor.execution.listener.InitializeResourcesListener;
import org.creekservice.internal.system.test.executor.execution.listener.PrepareResourcesListener;
import org.creekservice.internal.system.test.executor.execution.listener.StartServicesUnderTestListener;
import org.creekservice.internal.system.test.executor.execution.listener.SuiteCleanUpListener;
import org.creekservice.internal.system.test.executor.observation.LoggingTestEnvironmentListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Util class for initialising the system test api. */
public final class Api {

    private static final Logger LOGGER = LoggerFactory.getLogger(Api.class);

    private Api() {}

    /**
     * Initialise the test api
     *
     * @param serviceDebugInfo info about which services should be debugged.
     * @param mountInfo info about container mounts.
     * @param env environment vars to set on services under test.
     * @return the initialised test api.
     */
    public static SystemTest initializeApi(
            final ServiceDebugInfo serviceDebugInfo,
            final Collection<MountInfo> mountInfo,
            final Map<String, String> env) {

        final ContainerFactory containerFactory =
                new ContainerFactory(serviceDebugInfo, mountInfo, env);

        return initializeApi(
                new SystemTest(loadComponents(), containerFactory),
                containerFactory,
                loadTestExtensions());
    }

    @VisibleForTesting
    static SystemTest initializeApi(
            final SystemTest api,
            final ContainerFactory containerFactory,
            final List<CreekTestExtension> creekTestExtensions) {
        api.tests().env().listeners().append(new LoggingTestEnvironmentListener());
        api.tests().env().listeners().append(containerFactory);
        api.tests().env().listeners().append(new SuiteCleanUpListener(api));
        final AddServicesUnderTestListener addServicesListener =
                new AddServicesUnderTestListener(api);
        api.tests().env().listeners().append(addServicesListener);
        creekTestExtensions.forEach(ext -> ext.initialize(api));
        api.tests().env().listeners().append(new InitializeResourcesListener(api));
        api.tests().env().listeners().append(new PrepareResourcesListener(api));
        api.tests()
                .env()
                .listeners()
                .append(new StartServicesUnderTestListener(addServicesListener::added));
        return api;
    }

    private static List<ComponentDescriptor> loadComponents() {
        final List<ComponentDescriptor> components = ComponentDescriptors.load();
        components.forEach(comp -> LOGGER.debug("Loaded components: " + comp.name()));
        return components;
    }

    private static List<CreekTestExtension> loadTestExtensions() {
        final List<CreekTestExtension> extensions = CreekTestExtensions.load();
        extensions.forEach(ext -> LOGGER.debug("Loaded extension: " + ext.name()));
        return extensions;
    }
}

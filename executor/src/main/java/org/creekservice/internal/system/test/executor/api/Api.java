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

package org.creekservice.internal.system.test.executor.api;


import java.util.List;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptors;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.api.system.test.extension.CreekTestExtensions;
import org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo;
import org.creekservice.internal.system.test.executor.execution.listener.AddServicesUnderTestListener;
import org.creekservice.internal.system.test.executor.execution.listener.InitializeResourcesListener;
import org.creekservice.internal.system.test.executor.execution.listener.StartServicesUnderTestListener;
import org.creekservice.internal.system.test.executor.execution.listener.SuiteCleanUpListener;
import org.creekservice.internal.system.test.executor.observation.LoggingTestEnvironmentListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Api {

    private static final Logger LOGGER = LoggerFactory.getLogger(Api.class);

    private Api() {}

    public static SystemTest initializeApi(final ServiceDebugInfo serviceDebugInfo) {
        return initializeApi(
                new SystemTest(loadComponents(), serviceDebugInfo), loadTestExtensions());
    }

    @VisibleForTesting
    static SystemTest initializeApi(
            final SystemTest api, final List<CreekTestExtension> creekTestExtensions) {
        api.tests().env().listeners().append(new LoggingTestEnvironmentListener());
        api.tests().env().listeners().append(new SuiteCleanUpListener(api));
        final AddServicesUnderTestListener addServicesListener =
                new AddServicesUnderTestListener(api);
        api.tests().env().listeners().append(addServicesListener);
        api.tests().env().listeners().append(new InitializeResourcesListener(api));
        creekTestExtensions.forEach(ext -> ext.initialize(api));
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

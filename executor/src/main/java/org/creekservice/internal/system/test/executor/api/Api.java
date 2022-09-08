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
import java.util.Optional;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptors;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.api.system.test.extension.CreekTestExtensions;
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

    public static SystemTest initializeApi() {
        return initializeApi(new SystemTest(loadComponents()), loadTestExtensions());
    }

    @VisibleForTesting
    static SystemTest initializeApi(
            final SystemTest api, final List<CreekTestExtension> creekTestExtensions) {
        api.test().env().listener().append(new LoggingTestEnvironmentListener());
        api.test().env().listener().append(new SuiteCleanUpListener(api));
        final AddServicesUnderTestListener addServicesListener =
                new AddServicesUnderTestListener(api);
        api.test().env().listener().append(addServicesListener);
        api.test().env().listener().append(new InitializeResourcesListener(api));
        creekTestExtensions.forEach(ext -> initializeExt(api, ext));
        api.test()
                .env()
                .listener()
                .append(new StartServicesUnderTestListener(addServicesListener::added));
        return api;
    }

    private static void initializeExt(final SystemTest api, final CreekTestExtension ext) {
        api.component().model().initializing(Optional.of(ext));
        try {
            ext.initialize(api);
        } finally {
            api.component().model().initializing(Optional.empty());
        }
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

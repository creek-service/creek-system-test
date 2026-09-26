/*
 * Copyright 2023-2026 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.internal.system.test.executor.execution.listener;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.creekservice.api.platform.metadata.ResourceCollection;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinition;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.internal.system.test.executor.api.SystemTest;

/**
 * Test listener that calls back into each client extension to allow it to initialise any internal
 * state required to service requests on the resources it handles.
 *
 * <p>Runs after services-under-test have started, ensuring owned resources' schemas etc., which
 * only the owning service registers on start-up, already exist. See {@link
 * InitializeResourcesListener} for the earlier, narrower preparation of resources needed to seed
 * data.
 */
public final class PrepareResourcesListener implements TestEnvironmentListener {

    private final SystemTest api;

    /**
     * @param api the system test API.
     */
    public PrepareResourcesListener(final SystemTest api) {
        this.api = requireNonNull(api, "api");
    }

    @Override
    public void beforeSuite(final CreekTestSuite suite) {
        final Map<URI, ResourceDescriptor> byId =
                api.components().definitions().stream()
                        .map(ComponentDefinition::descriptor)
                        .flatMap(Optional::stream)
                        .flatMap(ResourceCollection::collectResources)
                        .collect(
                                groupingBy(
                                        ResourceDescriptor::id,
                                        Collectors.collectingAndThen(toList(), l -> l.get(0))));

        ResourcePreparer.prepare(api, byId.values());
    }
}

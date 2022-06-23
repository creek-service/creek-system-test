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

package org.creekservice.internal.system.test.executor.execution.listener;

import static java.util.Objects.requireNonNull;
import static org.creekservice.api.observability.lifecycle.LoggableLifecycle.SERVICE_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.creekservice.api.observability.lifecycle.BasicLifecycle;
import org.creekservice.api.system.test.extension.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.service.ServiceDefinition;
import org.creekservice.api.system.test.extension.service.ServiceInstance;
import org.creekservice.api.system.test.extension.testsuite.TestLifecycleListener;
import org.creekservice.internal.system.test.executor.api.SystemTest;

/**
 * A test lifecycle listener that is responsible for registering {@link ServiceInstance service
 * instances} for each service under test.
 *
 * <p>Before each test suite is executed the listener will add an instance of each service listed
 * under the test suites {@code services} property, in the order they are defined, to the test
 * suite.
 */
public final class AddServicesUnderTestListener implements TestLifecycleListener {

    private static final String STARTED_LOG_LINE_PATTERN =
            ".*" + Pattern.quote(BasicLifecycle.started.logMessage(SERVICE_TYPE)) + ".*";

    private final SystemTest api;
    private final List<ServiceInstance> added = new ArrayList<>();

    public AddServicesUnderTestListener(final SystemTest api) {
        this.api = requireNonNull(api, "api");
    }

    @Override
    public void beforeSuite(final CreekTestSuite suite) {
        added.clear();
        suite.services().stream().map(this::addServiceUnderTest).forEachOrdered(added::add);
    }

    public List<ServiceInstance> added() {
        return List.copyOf(added);
    }

    private ServiceInstance addServiceUnderTest(final String serviceName) {
        final ServiceDefinition def = api.services().get(serviceName);
        final ServiceInstance instance = api.testSuite().services().add(def);

        instance.configure().setStartupLogMessage(STARTED_LOG_LINE_PATTERN, 1);

        return instance;
    }
}

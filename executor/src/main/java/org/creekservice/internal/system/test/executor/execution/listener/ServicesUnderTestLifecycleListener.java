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
import static org.creekservice.api.base.type.Iterators.reverseIterator;

import java.util.ArrayList;
import java.util.List;
import org.creekservice.api.system.test.extension.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.service.ServiceDefinition;
import org.creekservice.api.system.test.extension.service.ServiceInstance;
import org.creekservice.api.system.test.extension.test.TestLifecycleListener;
import org.creekservice.internal.system.test.executor.api.SystemTest;

/**
 * A test lifecycle listener that is responsible for starting and stopping the services under test.
 *
 * <p>Before each test suite is executed the listener will start an instance of each service listed
 * under the test suites {@code services} property, in the order they are defined.
 *
 * <p>After each test suite, the listener will stop the instances it started, in reverse order.
 */
public final class ServicesUnderTestLifecycleListener implements TestLifecycleListener {

    private final SystemTest api;
    private final List<ServiceInstance> started = new ArrayList<>();

    public ServicesUnderTestLifecycleListener(final SystemTest api) {
        this.api = requireNonNull(api, "api");
    }

    @Override
    public void beforeSuite(final CreekTestSuite suite) {
        started.clear();
        suite.services().stream().map(this::startServicesUnderTest).forEachOrdered(started::add);
    }

    @Override
    public void afterSuite(final CreekTestSuite suite) {
        reverseIterator(started).forEachRemaining(ServiceInstance::stop);
    }

    private ServiceInstance startServicesUnderTest(final String serviceName) {
        final ServiceDefinition def = api.services().get(serviceName);
        return api.testSuite().services().start(def);
    }
}

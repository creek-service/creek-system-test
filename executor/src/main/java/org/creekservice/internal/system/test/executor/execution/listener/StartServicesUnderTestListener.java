/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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
import java.util.function.Supplier;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstance;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;

/**
 * A test lifecycle listener that is responsible for starting and stopping the services under test.
 *
 * <p>Before each test suite is executed the listener will start an instance of each service listed
 * under the test suites {@code services} property, in the order they are defined.
 *
 * <p>After each test suite, the listener will stop the instances it started, in reverse order.
 */
public final class StartServicesUnderTestListener implements TestEnvironmentListener {

    private final Supplier<List<ServiceInstance>> servicesSupplier;
    private final List<ServiceInstance> started = new ArrayList<>();

    /**
     * @param servicesSupplier a supplier that can be called during test suite execution to get the
     *     list services to start.
     */
    public StartServicesUnderTestListener(final Supplier<List<ServiceInstance>> servicesSupplier) {
        this.servicesSupplier = requireNonNull(servicesSupplier, "servicesSupplier");
    }

    @Override
    public void beforeSuite(final CreekTestSuite suite) {
        started.clear();
        final List<ServiceInstance> services = servicesSupplier.get();
        for (ServiceInstance service : services) {
            service.start();
            started.add(service);
        }
    }

    @Override
    public void afterSuite(final CreekTestSuite suite, final TestSuiteResult result) {
        reverseIterator(started).forEachRemaining(ServiceInstance::stop);
    }
}

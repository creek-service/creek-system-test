/*
 * Copyright 2022-2026 Creek Contributors (https://github.com/creek-service)
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

import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.internal.system.test.executor.api.SystemTest;
import org.creekservice.internal.system.test.executor.execution.input.Inputters;

/**
 * Test listener that injects a test suite's seed data.
 *
 * <p>Must run after {@link InitializeResourcesListener} and before {@link
 * StartServicesUnderTestListener}.
 */
public final class SeedingListener implements TestEnvironmentListener {

    private final Inputters inputters;

    /**
     * @param api the system test api.
     */
    public SeedingListener(final SystemTest api) {
        this(new Inputters(api.tests().model()));
    }

    @VisibleForTesting
    SeedingListener(final Inputters inputters) {
        this.inputters = requireNonNull(inputters, "inputters");
    }

    @Override
    public void beforeSuite(final CreekTestSuite suite) {
        inputters.input(suite.seedData(), suite);
    }
}

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

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.test.TestSuiteEnvironment;

public final class TestSuiteEnv implements TestSuiteEnvironment {

    private final TestListeners listeners;
    private final LocalServiceInstances services;

    public TestSuiteEnv() {
        this(new TestListeners(), new LocalServiceInstances());
    }

    @VisibleForTesting
    TestSuiteEnv(final TestListeners listeners, final LocalServiceInstances services) {
        this.listeners = requireNonNull(listeners, "listeners");
        this.services = requireNonNull(services, "services");
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    @Override
    public TestListeners listener() {
        return listeners;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    @Override
    public LocalServiceInstances services() {
        return services;
    }
}

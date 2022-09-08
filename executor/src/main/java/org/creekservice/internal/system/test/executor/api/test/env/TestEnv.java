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

package org.creekservice.internal.system.test.executor.api.test.env;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.test.env.TestEnvironment;
import org.creekservice.internal.system.test.executor.api.test.env.listener.TestListeners;
import org.creekservice.internal.system.test.executor.api.test.env.suite.TestSuiteEnv;

public final class TestEnv implements TestEnvironment {

    private final TestListeners listeners;
    private final TestSuiteEnv suite;

    public TestEnv() {
        this(new TestListeners(), new TestSuiteEnv());
    }

    @VisibleForTesting
    TestEnv(final TestListeners listeners, final TestSuiteEnv suite) {
        this.listeners = requireNonNull(listeners, "listeners");
        this.suite = requireNonNull(suite, "suite");
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    @Override
    public TestSuiteEnv currentSuite() {
        return suite;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    @Override
    public TestListeners listener() {
        return listeners;
    }
}

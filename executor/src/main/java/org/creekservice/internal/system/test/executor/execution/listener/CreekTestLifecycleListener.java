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

import org.creekservice.api.system.test.extension.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.TestLifecycleListener;
import org.creekservice.internal.system.test.executor.api.SystemTest;

public final class CreekTestLifecycleListener implements TestLifecycleListener {

    private final SystemTest api;

    public CreekTestLifecycleListener(final SystemTest api) {
        this.api = requireNonNull(api, "api");
    }

    @Override
    public void beforeSuite(final CreekTestSuite suite) {
        // Coming soon...
    }

    @Override
    public void afterSuite(final CreekTestSuite suite) {
        // Coming soon...
    }
}

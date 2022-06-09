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

package org.creekservice.internal.system.test.executor.execution;

import static java.util.Objects.requireNonNull;

import org.creekservice.api.system.test.extension.model.TestListenerCollection;
import org.creekservice.api.system.test.model.TestCase;

public final class TestCaseExecutor {

    private final TestListenerCollection listeners;

    public TestCaseExecutor(final TestListenerCollection listeners) {
        this.listeners = requireNonNull(listeners, "listeners");
    }

    public void executeTest(final TestCase testCase) {
        try {
            beforeTest(testCase);
            runTest(testCase);
        } finally {
            afterTest(testCase);
        }
    }

    private void beforeTest(final TestCase testCase) {
        listeners.forEach(listener -> listener.beforeTest(testCase.def().name()));
    }

    private void runTest(final TestCase testCase) {
        // Coming soon...
    }

    private void afterTest(final TestCase testCase) {
        listeners.forEachReverse(listener -> listener.afterTest(testCase.def().name()));
    }
}

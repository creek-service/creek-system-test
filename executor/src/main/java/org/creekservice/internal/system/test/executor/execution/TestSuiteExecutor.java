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


import org.creekservice.api.system.test.model.TestSuite;
import org.creekservice.internal.system.test.executor.result.TestSuiteResult;

public final class TestSuiteExecutor {

    public TestSuiteResult executeSuite(final TestSuite testSuite) {
        // Coming soon...
        // For now, this facilitates testing:
        switch (testSuite.tests().size()) {
            case 1:
                return new TestSuiteResult(0, 1);
            case 2:
                return new TestSuiteResult(1, 0);
            default:
                return new TestSuiteResult(0, 0);
        }
    }
}

/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.system.test.extension.test.env;

import org.creekservice.api.system.test.extension.test.env.listener.TestListenerContainer;
import org.creekservice.api.system.test.extension.test.env.suite.TestSuiteEnvironment;

/** The environment in which tests execute. */
public interface TestEnvironment {

    /**
     * The test suite being executed.
     *
     * @return the currently running suite.
     */
    TestSuiteEnvironment currentSuite();

    /**
     * Test listeners.
     *
     * <p>Listeners are invoked on test lifecycle events.
     *
     * @return test listeners
     */
    TestListenerContainer listeners();
}

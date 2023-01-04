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

package org.creekservice.api.system.test.extension.test.env.suite;

import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstanceContainer;

/** The environment in which test suites execute. */
public interface TestSuiteEnvironment {

    /**
     * Services within the test environment.
     *
     * @return service container.
     */
    ServiceInstanceContainer services();
}

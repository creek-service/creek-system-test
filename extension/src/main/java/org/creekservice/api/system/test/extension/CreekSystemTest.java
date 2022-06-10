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

package org.creekservice.api.system.test.extension;


import org.creekservice.api.system.test.extension.model.ModelContainer;
import org.creekservice.api.system.test.extension.service.ServiceDefinitionCollection;
import org.creekservice.api.system.test.extension.test.TestSuiteEnvironment;

/** API to the system tests exposed to extensions */
public interface CreekSystemTest {

    /**
     * The data model of the system tests.
     *
     * <p>This is the model used when deserializing system tests.
     *
     * @return the model.
     */
    ModelContainer model();

    /**
     * The test suite being executed.
     *
     * @return the test container.
     */
    TestSuiteEnvironment testSuite();

    /**
     * The services known to Creek.
     *
     * <p>These are the services discovered on the class or module path.
     *
     * @return a collection of services.
     */
    ServiceDefinitionCollection services();
}

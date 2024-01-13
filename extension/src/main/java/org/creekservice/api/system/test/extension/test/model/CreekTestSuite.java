/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.system.test.extension.test.model;

import java.util.List;

/** Public API of a test suite. */
public interface CreekTestSuite extends Locatable {

    /**
     * @return the name of the suite
     */
    String name();

    /**
     * @return the list of services under test
     */
    List<String> services();

    /**
     * @param type the option type to look up.
     * @param <T> the option type to look up.
     * @return the list of test options or the requested type.
     */
    <T extends Option> List<T> options(Class<T> type);

    /**
     * @return the test cases in the suite.
     */
    List<? extends CreekTestCase> tests();
}

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

package org.creekservice.api.system.test.extension.test.model;


import java.util.List;

/** Public API of a test case. */
public interface CreekTestCase extends Locatable {

    /** @return the name of the test case */
    String name();

    /** @return the suite the test case belongs too. */
    CreekTestSuite suite();

    /** @return the inputs to be fed in at the start of the test case. */
    List<Input> inputs();

    /** @return the expectations to be asserted at the end of the test case. */
    List<Expectation> expectations();
}

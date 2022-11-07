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

/** The result of a test run. */
public interface TestExecutionResult {

    /** @return {@code true} if the result contains no test suites. */
    boolean isEmpty();

    /** @return number of test cases that failed, i.e. assertions not met */
    long failed();

    /** @return number of test cases that failed to execute */
    long errors();

    /** @return {@code true} if there were no failures or errors. */
    boolean passed();

    /** @return the suite results */
    List<? extends TestSuiteResult> results();
}

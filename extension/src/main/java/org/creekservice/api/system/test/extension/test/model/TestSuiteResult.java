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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** The result of a {@link CreekTestSuite} execution */
public interface TestSuiteResult {

    /**
     * @return the test suite.
     */
    CreekTestSuite testSuite();

    /**
     * @return the number of skipped tests.
     */
    long skipped();

    /**
     * @return the number of failed tests, i.e. tests where an expectation was not met
     */
    long failures();

    /**
     * @return the number of tests that failed to execute.
     */
    long errors();

    /**
     * @return the start time of the test suite.
     */
    Instant start();

    /**
     * @return how long the test suite took to run.
     */
    Duration duration();

    /**
     * Indicates the test suite did not execute.
     *
     * @return any exception thrown trying to set up the suite.
     */
    Optional<Exception> error();

    /**
     * @return the results for the tests within the suite.
     */
    List<? extends TestCaseResult> testResults();
}

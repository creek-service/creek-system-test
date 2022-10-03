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

package org.creekservice.api.system.test.extension.test.env.listener;


import org.creekservice.api.system.test.extension.test.model.CreekTestCase;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.model.TestCaseResult;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;

/**
 * Listener of lifecycle events for tests.
 *
 * <p>Once registered, the listener will be invoked as each test suite is executed.
 *
 * <p>Throwing an exception from a callback will result in test executing terminating.
 */
public interface TestEnvironmentListener {

    /**
     * Called before the supplied {@code suite} is executed.
     *
     * @param suite the suite about to be executed.
     * @throws RuntimeException Throwing an exception indicates that the suite setup failed. The
     *     tests within the suite will not be executed.
     */
    default void beforeSuite(CreekTestSuite suite) {}

    /**
     * Called after the supplied {@code suite} has executed.
     *
     * <p>Implementations should <i>not</i> throw exceptions. Any exception thrown will terminate
     * the test run.
     *
     * @param suite the suite that was executed.
     * @param result the result of the suite.
     */
    default void afterSuite(CreekTestSuite suite, final TestSuiteResult result) {}

    /**
     * Called before an individual {@code test} case is executed.
     *
     * @param test the test about to be executed
     * @throws RuntimeException Throwing an exception indicates that the test setup failed. The
     *     tests will not be executed.
     */
    default void beforeTest(CreekTestCase test) {}

    /**
     * Called after an individual {@code test} case has executed.
     *
     * <p>Implementations should <i>not</i> throw exceptions. Any exception thrown will terminate
     * the test run.
     *
     * @param test the test that was executed.
     * @param result the test result.
     */
    default void afterTest(CreekTestCase test, final TestCaseResult result) {}
}

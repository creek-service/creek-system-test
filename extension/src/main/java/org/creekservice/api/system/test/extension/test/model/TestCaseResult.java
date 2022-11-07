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
import java.util.Optional;

/** The result of a single {@link CreekTestCase} execution. */
public interface TestCaseResult {

    /** @return the test case. */
    CreekTestCase testCase();

    /** @return how long the test took to run. */
    Duration duration();

    /** @return {@code true} if the test was skipped because it was disabled. */
    boolean skipped();

    /**
     * Indicates a test failure.
     *
     * <p>i.e. the test was successfully executed, but one of the verification checks failed.
     *
     * @return any test failure information.
     */
    Optional<AssertionError> failure();

    /**
     * Indicates the test did not execute.
     *
     * @return any exception thrown trying to run the test.
     */
    Optional<Exception> error();
}

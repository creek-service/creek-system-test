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

package org.creekservice.api.system.test.extension.test;


import org.creekservice.api.system.test.extension.model.CreekTestCase;
import org.creekservice.api.system.test.extension.model.CreekTestSuite;

/**
 * Listener of lifecycle events for tests.
 *
 * <p>Once registered, the listener will be invoked as each test suite is executed.
 *
 * <p>Throwing an exception from a callback will result in test executing terminating.
 */
public interface TestLifecycleListener {
    default void beforeSuite(CreekTestSuite suite) {}

    default void afterSuite(CreekTestSuite suite) {}

    default void beforeTest(CreekTestCase test) {}

    default void afterTest(CreekTestCase test) {}
}

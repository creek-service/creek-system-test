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

package org.creekservice.internal.system.test.executor.observation;


import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.model.CreekTestCase;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LoggingTestEnvironmentListener implements TestEnvironmentListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(LoggingTestEnvironmentListener.class);

    @Override
    public void beforeSuite(final CreekTestSuite suite) {
        LOGGER.info("Starting suite '" + suite.name() + "'");
    }

    @Override
    public void afterSuite(final CreekTestSuite suite) {
        LOGGER.info("Finished suite '" + suite.name() + "'");
    }

    @Override
    public void beforeTest(final CreekTestCase test) {
        LOGGER.info("Starting test '" + test.name() + "'");
    }

    @Override
    public void afterTest(final CreekTestCase test) {
        LOGGER.info("Finished test '" + test.name() + "'");
    }
}

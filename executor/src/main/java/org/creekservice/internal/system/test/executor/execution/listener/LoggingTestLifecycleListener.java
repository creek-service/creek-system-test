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

package org.creekservice.internal.system.test.executor.execution.listener;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.creekservice.api.system.test.extension.model.TestLifecycleListener;

public final class LoggingTestLifecycleListener implements TestLifecycleListener {

    private static final Logger LOGGER = LogManager.getLogger(LoggingTestLifecycleListener.class);

    @Override
    public void beforeSuite(final String suiteName) {
        LOGGER.info("Starting suite '" + suiteName + "'");
    }

    @Override
    public void afterSuite(final String suiteName) {
        LOGGER.info("Finished suite '" + suiteName + "'");
    }

    @Override
    public void beforeTest(final String testName) {
        LOGGER.info("Starting test '" + testName + "'");
    }

    @Override
    public void afterTest(final String testName) {
        LOGGER.info("Finished test '" + testName + "'");
    }
}

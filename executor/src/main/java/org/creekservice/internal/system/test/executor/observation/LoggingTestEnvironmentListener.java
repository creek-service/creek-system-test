/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.base.type.Throwables;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.model.CreekTestCase;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.model.TestCaseResult;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Lifecycle logging test listener. */
public final class LoggingTestEnvironmentListener implements TestEnvironmentListener {

    private final Logger logger;

    /** Constructor. */
    public LoggingTestEnvironmentListener() {
        this(LoggerFactory.getLogger(LoggingTestEnvironmentListener.class));
    }

    @VisibleForTesting
    LoggingTestEnvironmentListener(final Logger logger) {
        this.logger = requireNonNull(logger, "logger");
    }

    @Override
    public void beforeSuite(final CreekTestSuite suite) {
        logger.info("Starting suite '" + suite.name() + "'");
    }

    @Override
    public void afterSuite(final CreekTestSuite suite, final TestSuiteResult result) {
        if (result.error().isPresent()) {
            final Exception cause = result.error().get();
            logger.info(
                    "Start up failed for suite '"
                            + suite.name()
                            + "': "
                            + cause.getMessage()
                            + System.lineSeparator()
                            + Throwables.stackTrace(cause));
            return;
        }

        final String skipped = result.skipped() == 0 ? "" : " skipped: " + result.skipped();
        final String errors = result.errors() == 0 ? "" : " errors: " + result.errors();
        final String failures = result.failures() == 0 ? "" : " failures: " + result.failures();

        logger.info("Finished suite '" + suite.name() + "'" + skipped + errors + failures);
    }

    @Override
    public void beforeTest(final CreekTestCase test) {
        logger.info("Starting test '" + test.name() + "'");
    }

    @Override
    public void afterTest(final CreekTestCase test, final TestCaseResult result) {
        logger.info("Finished test '" + test.name() + "': " + status(result));
    }

    private static String status(final TestCaseResult result) {
        if (result.skipped()) {
            return "SKIPPED";
        }

        final Optional<AssertionError> failure = result.failure();
        final Optional<Exception> error = result.error();
        if (failure.isEmpty() && error.isEmpty()) {
            return "SUCCESS";
        }

        final String level = error.isPresent() ? "ERROR" : "FAILED";
        final Throwable cause = error.map(Throwable.class::cast).orElseGet(failure::get);

        return level
                + ": "
                + cause.getMessage()
                + System.lineSeparator()
                + Throwables.stackTrace(cause);
    }
}

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

package org.creekservice.internal.system.test.executor.result;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.test.model.TestCaseResult;
import org.creekservice.api.system.test.model.TestCase;

/** Test case result that can be serialized as part of JUnit style test report. */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class CaseResult implements TestCaseResult {

    private final TestCase testCase;
    private final Duration duration;
    private final Optional<AssertionError> failure;
    private final Optional<Exception> error;
    private final boolean skipped;

    /**
     * Factory method to get a test case result builder.
     *
     * @param testCase the test case being executed.
     * @return the test case result builder.
     */
    public static CaseResult.Builder testCaseResult(final TestCase testCase) {
        return new Builder(testCase, Clock.systemUTC());
    }

    private CaseResult(
            final TestCase testCase,
            final Duration duration,
            final Optional<AssertionError> failure,
            final Optional<Exception> error,
            final boolean skipped) {
        this.testCase = requireNonNull(testCase, "testCase");
        this.duration = requireNonNull(duration, "duration");
        this.failure = requireNonNull(failure, "failure");
        this.error = requireNonNull(error, "error");
        this.skipped = skipped;
    }

    @Override
    public TestCase testCase() {
        return testCase;
    }

    @Override
    public Duration duration() {
        return duration;
    }

    @Override
    public Optional<AssertionError> failure() {
        return failure;
    }

    @Override
    public Optional<Exception> error() {
        return error;
    }

    @Override
    public boolean skipped() {
        return skipped;
    }

    @Override
    public String toString() {
        return "CaseResult{"
                + "test="
                + testCase.name()
                + ", duration="
                + duration
                + ", skipped="
                + skipped
                + ", failure="
                + failure.map(Throwable::getMessage).orElse("<none>")
                + ", error="
                + error.map(Throwable::getMessage).orElse("<none>")
                + '}';
    }

    /** Builder of test case results. */
    public static final class Builder {

        private final TestCase testCase;
        private final Clock clock;
        private final Instant start;

        @VisibleForTesting
        Builder(final TestCase testCase, final Clock clock) {
            this.testCase = requireNonNull(testCase, "testCase");
            this.clock = requireNonNull(clock, "clock");
            this.start = clock.instant();
        }

        /**
         * @return a result indicating the test was disabled
         */
        public CaseResult disabled() {
            return new CaseResult(testCase, duration(), Optional.empty(), Optional.empty(), true);
        }

        /**
         * @return a result indicating the test passed
         */
        public CaseResult success() {
            return new CaseResult(testCase, duration(), Optional.empty(), Optional.empty(), false);
        }

        /**
         * A test failure, i.e. the expected outcome did not match.
         *
         * @param cause details of the mismatch.
         * @return a result indicating the test failed.
         */
        public CaseResult failure(final AssertionError cause) {
            return new CaseResult(
                    testCase, duration(), Optional.of(cause), Optional.empty(), false);
        }

        /**
         * Test failed to run.
         *
         * @param cause the cause.
         * @return a result indicating the a test error.
         */
        public CaseResult error(final Exception cause) {
            return new CaseResult(
                    testCase, duration(), Optional.empty(), Optional.of(cause), false);
        }

        private Duration duration() {
            return Duration.between(start, clock.instant());
        }
    }
}

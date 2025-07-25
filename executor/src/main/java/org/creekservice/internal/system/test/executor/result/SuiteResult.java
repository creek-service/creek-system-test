/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;
import org.creekservice.api.system.test.model.TestSuite;

/** Test suite results. */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class SuiteResult implements TestSuiteResult {

    private final TestSuite suite;
    private final Instant start;
    private final Instant finish;
    private final Optional<Exception> error;
    private final List<CaseResult> tests;

    /**
     * Factory method
     *
     * @param testSuite the test suite to build results for
     * @return the results builder
     */
    public static Builder testSuiteResult(final TestSuite testSuite) {
        return new Builder(testSuite, Clock.systemUTC());
    }

    private SuiteResult(
            final TestSuite suite,
            final Instant start,
            final Instant finish,
            final Optional<Exception> error,
            final List<CaseResult> tests) {
        this.suite = requireNonNull(suite, "suite");
        this.tests = List.copyOf(requireNonNull(tests, "tests"));
        this.start = requireNonNull(start, "start");
        this.error = requireNonNull(error, "error");
        this.finish = requireNonNull(finish, "finish");
    }

    @Override
    public TestSuite testSuite() {
        return suite;
    }

    @Override
    public long skipped() {
        return tests.stream().filter(CaseResult::skipped).count();
    }

    @Override
    public long failures() {
        return tests.stream().filter(test -> test.failure().isPresent()).count();
    }

    @Override
    public long errors() {
        return error.map(e -> (long) testSuite().tests().size())
                .orElseGet(() -> tests.stream().filter(test -> test.error().isPresent()).count());
    }

    @Override
    public Instant start() {
        return start;
    }

    @Override
    public Duration duration() {
        return Duration.between(start, finish);
    }

    @Override
    public Optional<Exception> error() {
        return error;
    }

    @Override
    public List<CaseResult> testResults() {
        return tests;
    }

    @Override
    public String toString() {
        return "SuiteResult{"
                + "name="
                + suite.name()
                + ", location="
                + suite.location()
                + ", start="
                + start
                + ", finish="
                + finish
                + ", error="
                + error.map(Throwable::getMessage).orElse("<none>")
                + ", tests="
                + tests
                + '}';
    }

    /** A builder of test suite results. */
    public static final class Builder {

        private final Clock clock;
        private final TestSuite testSuite;
        private final Instant start;
        private final List<CaseResult> tests = new ArrayList<>();

        @VisibleForTesting
        Builder(final TestSuite testSuite, final Clock clock) {
            this.testSuite = requireNonNull(testSuite, "testSuite");
            this.clock = requireNonNull(clock, "clock");
            this.start = clock.instant();
        }

        /**
         * Add a test case result
         *
         * @param result the test case result.
         * @return self.
         */
        public Builder add(final CaseResult result) {
            tests.add(requireNonNull(result, "result"));
            return this;
        }

        /**
         * Build the suite result.
         *
         * @return the suite result.
         */
        public SuiteResult build() {
            return new SuiteResult(testSuite, start, clock.instant(), Optional.empty(), tests);
        }

        /**
         * Build suite result indicating the suite failed to run.
         *
         * <p>For example, because of an error during suite setup.
         *
         * @param cause the exception detailing what failed.
         * @return the suite result.
         */
        public SuiteResult buildError(final Exception cause) {
            return new SuiteResult(
                    testSuite, start, clock.instant(), Optional.of(cause), List.of());
        }
    }
}

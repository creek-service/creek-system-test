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
import static java.util.Objects.requireNonNullElse;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.model.TestCase;

/** Test case result that can be serialized as part of JUnit style test report. */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class TestCaseResult {

    private final String name;
    private final String className;
    private final String duration;
    private final Optional<Failure> failure;
    private final boolean skipped;

    public static TestCaseResult.Builder testCaseResult(final TestCase testCase) {
        return new Builder(testCase, Clock.systemUTC());
    }

    private TestCaseResult(
            final TestCase testCase,
            final Duration duration,
            final Optional<Failure> failure,
            final boolean skipped) {
        this.name = requireNonNull(testCase, "testCase").name();
        this.className = testCase.suite().name();
        this.duration =
                String.format(
                        "%d.%03d",
                        duration.getSeconds(), TimeUnit.NANOSECONDS.toMillis(duration.getNano()));
        this.failure = requireNonNull(failure, "failure");
        this.skipped = skipped;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String name() {
        return name;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String classname() {
        return className;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String time() {
        return duration;
    }

    @JacksonXmlProperty
    public Optional<Failure> failure() {
        return failure;
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JacksonXmlProperty
    public Optional<Object> skipped() {
        return skipped ? Optional.of(new Object()) : Optional.empty();
    }

    public static final class Failure {

        private final Throwable cause;
        private final String stack;

        public Failure(final Throwable cause) {
            this.cause = requireNonNull(cause, "cause");
            this.stack = stackTrace(cause);
        }

        @JacksonXmlProperty(isAttribute = true)
        @JsonGetter("message")
        public String message() {
            return requireNonNullElse(cause.getMessage(), "");
        }

        @JacksonXmlProperty(isAttribute = true)
        @JsonGetter("type")
        public String type() {
            return cause.getClass().getCanonicalName();
        }

        @JacksonXmlText
        @JsonGetter("stack")
        public String stack() {
            return stack;
        }

        @JsonIgnore
        public boolean isError() {
            return cause instanceof Exception;
        }

        @JsonIgnore
        public boolean isFailure() {
            return !isError();
        }

        private static String stackTrace(final Throwable cause) {
            final StringWriter sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        }
    }

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

        public TestCaseResult disabled() {
            return new TestCaseResult(testCase, duration(), Optional.empty(), true);
        }

        public TestCaseResult success() {
            return new TestCaseResult(testCase, duration(), Optional.empty(), false);
        }

        public TestCaseResult failure(final AssertionError cause) {
            return new TestCaseResult(testCase, duration(), Optional.of(new Failure(cause)), false);
        }

        public TestCaseResult error(final Exception cause) {
            return new TestCaseResult(testCase, duration(), Optional.of(new Failure(cause)), false);
        }

        private Duration duration() {
            return Duration.between(start, clock.instant());
        }
    }
}

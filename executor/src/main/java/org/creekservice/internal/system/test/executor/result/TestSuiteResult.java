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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.model.TestSuite;
import org.creekservice.internal.system.test.executor.result.TestCaseResult.Failure;

public final class TestSuiteResult {

    private static final String LOCAL_HOST = localHostName();

    private final String name;
    private final List<TestCaseResult> tests;
    private final String timestamp;
    private final String duration;
    private final String hostName;

    public static Builder testSuiteResult(final TestSuite testSuite) {
        return new Builder(testSuite, Clock.systemUTC(), LOCAL_HOST);
    }

    private TestSuiteResult(
            final String name,
            final String timestamp,
            final String duration,
            final String hostName,
            final List<TestCaseResult> tests) {
        this.name = requireNonNull(name, "name");
        this.hostName = requireNonNull(hostName, "hostName");
        this.tests = List.copyOf(requireNonNull(tests, "tests"));
        this.timestamp = requireNonNull(timestamp, "timestamp");
        this.duration = requireNonNull(duration, "duration");
    }

    @JacksonXmlProperty(isAttribute = true)
    public String name() {
        return name;
    }

    @JacksonXmlProperty(isAttribute = true)
    public long tests() {
        return tests.size();
    }

    @JacksonXmlProperty(isAttribute = true)
    public long skipped() {
        return tests.stream().filter(test -> test.skipped().isPresent()).count();
    }

    @JacksonXmlProperty(isAttribute = true)
    public long failures() {
        return tests.stream()
                .filter(test -> test.failure().filter(Failure::isFailure).isPresent())
                .count();
    }

    @JacksonXmlProperty(isAttribute = true)
    public long errors() {
        return tests.stream()
                .filter(test -> test.failure().filter(Failure::isError).isPresent())
                .count();
    }

    @JacksonXmlProperty(isAttribute = true)
    public String timestamp() {
        return timestamp;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String hostname() {
        return hostName;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String time() {
        return duration;
    }

    @JacksonXmlProperty
    public List<TestCaseResult> testcase() {
        return tests;
    }

    private static String localHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            return "Unknown";
        }
    }

    public static final class Builder {

        private final Clock clock;
        private final TestSuite testSuite;
        private final String hostName;
        private final Instant start;
        private final List<TestCaseResult> tests = new ArrayList<>();

        @VisibleForTesting
        Builder(final TestSuite testSuite, final Clock clock, final String hostName) {
            this.testSuite = requireNonNull(testSuite, "testSuite");
            this.clock = requireNonNull(clock, "clock");
            this.hostName = requireNonNull(hostName, "hostName");
            this.start = clock.instant();
        }

        public Builder add(final TestCaseResult result) {
            tests.add(requireNonNull(result, "result"));
            return this;
        }

        public TestSuiteResult build() {
            return new TestSuiteResult(testSuite.name(), timestamp(), duration(), hostName, tests);
        }

        private String timestamp() {
            final String timestampZ = DateTimeFormatter.ISO_INSTANT.format(start);
            return timestampZ.substring(0, timestampZ.length() - 1);
        }

        private String duration() {
            final Duration duration = Duration.between(start, clock.instant());
            return String.format(
                    "%d.%03d",
                    duration.getSeconds(), TimeUnit.NANOSECONDS.toMillis(duration.getNano()));
        }
    }
}

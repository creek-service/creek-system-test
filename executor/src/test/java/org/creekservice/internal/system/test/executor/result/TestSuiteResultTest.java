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

import static org.creekservice.internal.system.test.executor.result.TestCaseResult.testCaseResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import org.creekservice.api.system.test.model.TestCase;
import org.creekservice.api.system.test.model.TestSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestSuiteResultTest {

    private static final Instant START = Instant.now();
    private static final Instant FINISH = START.plusSeconds(43).minusMillis(101);
    private static final String EXPECTED_TS = timestamp();

    @Mock private TestCase testCase;
    @Mock private TestSuite testSuite;
    @Mock private Clock clock;
    private TestSuiteResult.Builder builder;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(START, FINISH);
        when(testCase.name()).thenReturn("test case");
        when(testCase.suite()).thenReturn(testSuite);
        when(testSuite.name()).thenReturn("test suite");

        builder = new TestSuiteResult.Builder(testSuite, clock, "machine-23");
    }

    @Test
    void shouldBuildEmptyResult() {
        // When:
        final TestSuiteResult result = builder.build();

        // Then:
        assertThat(result.name(), is("test suite"));
        assertThat(result.tests(), is(0L));
        assertThat(result.skipped(), is(0L));
        assertThat(result.failures(), is(0L));
        assertThat(result.errors(), is(0L));
        assertThat(result.timestamp(), is(EXPECTED_TS));
        assertThat(result.hostname(), is("machine-23"));
        assertThat(result.time(), is("42.899"));
        assertThat(result.testcase(), is(empty()));
    }

    @Test
    void shouldBuildWithDisabled() {
        // Given:
        final TestCaseResult disabled = testCaseResult(testCase).disabled();

        // When:
        final TestSuiteResult result = builder.add(disabled).add(disabled).build();

        // Then:
        assertThat(result.name(), is("test suite"));
        assertThat(result.tests(), is(2L));
        assertThat(result.skipped(), is(2L));
        assertThat(result.failures(), is(0L));
        assertThat(result.errors(), is(0L));
        assertThat(result.timestamp(), is(EXPECTED_TS));
        assertThat(result.hostname(), is("machine-23"));
        assertThat(result.time(), is("42.899"));
        assertThat(result.testcase(), contains(disabled, disabled));
    }

    @Test
    void shouldBuildWithSuccess() {
        // Given:
        final TestCaseResult success = testCaseResult(testCase).success();

        // When:
        final TestSuiteResult result = builder.add(success).add(success).build();

        // Then:
        assertThat(result.name(), is("test suite"));
        assertThat(result.tests(), is(2L));
        assertThat(result.skipped(), is(0L));
        assertThat(result.failures(), is(0L));
        assertThat(result.errors(), is(0L));
        assertThat(result.timestamp(), is(EXPECTED_TS));
        assertThat(result.hostname(), is("machine-23"));
        assertThat(result.time(), is("42.899"));
        assertThat(result.testcase(), contains(success, success));
    }

    @Test
    void shouldBuildWithFailure() {
        // Given:
        final TestCaseResult failure = testCaseResult(testCase).failure(new AssertionError());

        // When:
        final TestSuiteResult result = builder.add(failure).add(failure).build();

        // Then:
        assertThat(result.name(), is("test suite"));
        assertThat(result.tests(), is(2L));
        assertThat(result.skipped(), is(0L));
        assertThat(result.failures(), is(2L));
        assertThat(result.errors(), is(0L));
        assertThat(result.timestamp(), is(EXPECTED_TS));
        assertThat(result.hostname(), is("machine-23"));
        assertThat(result.time(), is("42.899"));
        assertThat(result.testcase(), contains(failure, failure));
    }

    @Test
    void shouldBuildWithErrors() {
        // Given:
        final TestCaseResult error = testCaseResult(testCase).error(new RuntimeException());

        // When:
        final TestSuiteResult result = builder.add(error).add(error).build();

        // Then:
        assertThat(result.name(), is("test suite"));
        assertThat(result.tests(), is(2L));
        assertThat(result.skipped(), is(0L));
        assertThat(result.failures(), is(0L));
        assertThat(result.errors(), is(2L));
        assertThat(result.timestamp(), is(EXPECTED_TS));
        assertThat(result.hostname(), is("machine-23"));
        assertThat(result.time(), is("42.899"));
        assertThat(result.testcase(), contains(error, error));
    }

    private static String timestamp() {
        final String timestampZ = DateTimeFormatter.ISO_INSTANT.format(START);
        return timestampZ.substring(0, timestampZ.length() - 1);
    }
}

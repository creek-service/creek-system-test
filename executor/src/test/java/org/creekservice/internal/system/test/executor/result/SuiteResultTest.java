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

import static org.creekservice.internal.system.test.executor.result.CaseResult.testCaseResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import org.creekservice.api.system.test.model.TestCase;
import org.creekservice.api.system.test.model.TestSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SuiteResultTest {

    private static final Instant START = Instant.ofEpochMilli(1665249401600L);
    private static final Instant FINISH = START.plusSeconds(43).minusMillis(101);
    private static final Duration DURATION = Duration.between(START, FINISH);

    @Mock private TestCase testCase;
    @Mock private TestSuite testSuite;
    @Mock private Clock clock;
    private SuiteResult.Builder builder;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(START, FINISH);

        builder = new SuiteResult.Builder(testSuite, clock);
    }

    @Test
    void shouldBuildEmptyResult() {
        // When:
        final SuiteResult result = builder.build();

        // Then:
        assertThat(result.testSuite(), is(testSuite));
        assertThat(result.skipped(), is(0L));
        assertThat(result.failures(), is(0L));
        assertThat(result.errors(), is(0L));
        assertThat(result.start(), is(START));
        assertThat(result.duration(), is(DURATION));
        assertThat(result.testCases(), is(empty()));
    }

    @Test
    void shouldBuildWithDisabled() {
        // Given:
        final CaseResult disabled = testCaseResult(testCase).disabled();

        // When:
        final SuiteResult result = builder.add(disabled).add(disabled).build();

        // Then:
        assertThat(result.testSuite(), is(testSuite));
        assertThat(result.skipped(), is(2L));
        assertThat(result.failures(), is(0L));
        assertThat(result.errors(), is(0L));
        assertThat(result.start(), is(START));
        assertThat(result.duration(), is(DURATION));
        assertThat(result.testCases(), contains(disabled, disabled));
    }

    @Test
    void shouldBuildWithSuccess() {
        // Given:
        final CaseResult success = testCaseResult(testCase).success();

        // When:
        final SuiteResult result = builder.add(success).add(success).build();

        // Then:
        assertThat(result.testSuite(), is(testSuite));
        assertThat(result.skipped(), is(0L));
        assertThat(result.failures(), is(0L));
        assertThat(result.errors(), is(0L));
        assertThat(result.start(), is(START));
        assertThat(result.duration(), is(DURATION));
        assertThat(result.testCases(), contains(success, success));
    }

    @Test
    void shouldBuildWithFailure() {
        // Given:
        final CaseResult failure = testCaseResult(testCase).failure(new AssertionError());

        // When:
        final SuiteResult result = builder.add(failure).add(failure).build();

        // Then:
        assertThat(result.testSuite(), is(testSuite));
        assertThat(result.skipped(), is(0L));
        assertThat(result.failures(), is(2L));
        assertThat(result.errors(), is(0L));
        assertThat(result.start(), is(START));
        assertThat(result.duration(), is(DURATION));
        assertThat(result.testCases(), contains(failure, failure));
    }

    @Test
    void shouldBuildWithErrors() {
        // Given:
        final CaseResult error = testCaseResult(testCase).error(new RuntimeException());

        // When:
        final SuiteResult result = builder.add(error).add(error).build();

        // Then:
        assertThat(result.testSuite(), is(testSuite));
        assertThat(result.skipped(), is(0L));
        assertThat(result.failures(), is(0L));
        assertThat(result.errors(), is(2L));
        assertThat(result.start(), is(START));
        assertThat(result.duration(), is(DURATION));
        assertThat(result.testCases(), contains(error, error));
    }

    @Test
    void shouldToString() {
        // Given:
        final CaseResult success = testCaseResult(testCase).success();

        when(testCase.name()).thenReturn("test-a");
        when(testSuite.name()).thenReturn("suite-1");
        when(testSuite.location()).thenReturn(URI.create("loc:///suite-1"));

        // When:
        final SuiteResult result = builder.add(success).build();

        // Then:
        assertThat(
                result.toString(),
                is(
                        "SuiteResult{"
                                + "name=suite-1, "
                                + "location=loc:///suite-1, "
                                + "start=2022-10-08T17:16:41.600Z, "
                                + "finish=2022-10-08T17:17:24.499Z, "
                                + "tests=["
                                + success
                                + "]}"));
    }
}

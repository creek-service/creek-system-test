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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.creekservice.api.system.test.model.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CaseResultTest {

    private static final Instant START = Instant.now();
    private static final Instant FINISH = START.plusSeconds(43).minusMillis(101);
    private static final Duration DURATION = Duration.between(START, FINISH);

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TestCase testCase;

    @Mock private Clock clock;

    private CaseResult.Builder builder;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(START, FINISH);

        builder = new CaseResult.Builder(testCase, clock);

        when(testCase.name()).thenReturn("test-a");
    }

    @Test
    void shouldBuildDisabled() {
        // When:
        final CaseResult result = builder.disabled();

        // Then:
        assertThat(result.testCase(), is(testCase));
        assertThat(result.duration(), is(DURATION));
        assertThat(result.failure(), is(Optional.empty()));
        assertThat(result.error(), is(Optional.empty()));
        assertThat(result.skipped(), is(true));
        assertThat(
                result.toString(),
                is(
                        "CaseResult{test=test-a, duration=PT42.899S, skipped=true, failure=<none>, error=<none>}"));
    }

    @Test
    void shouldBuildSuccess() {
        // When:
        final CaseResult result = builder.success();

        // Then:
        assertThat(result.testCase(), is(testCase));
        assertThat(result.duration(), is(DURATION));
        assertThat(result.failure(), is(Optional.empty()));
        assertThat(result.error(), is(Optional.empty()));
        assertThat(result.skipped(), is(false));
        assertThat(
                result.toString(),
                is(
                        "CaseResult{test=test-a, duration=PT42.899S, skipped=false, failure=<none>, error=<none>}"));
    }

    @Test
    void shouldBuildFailure() {
        // Given:
        final AssertionError cause = new AssertionError("boom");

        // When:
        final CaseResult result = builder.failure(cause);

        // Then:
        assertThat(result.testCase(), is(testCase));
        assertThat(result.duration(), is(DURATION));
        assertThat(result.failure().orElseThrow(), is(cause));
        assertThat(result.error(), is(Optional.empty()));
        assertThat(result.skipped(), is(false));
        assertThat(
                result.toString(),
                is(
                        "CaseResult{test=test-a, duration=PT42.899S, skipped=false, failure=boom, error=<none>}"));
    }

    @Test
    void shouldBuildError() {
        // Given:
        final Exception cause = new IllegalArgumentException("boom");

        // When:
        final CaseResult result = builder.error(cause);

        // Then:
        assertThat(result.testCase(), is(testCase));
        assertThat(result.duration(), is(DURATION));
        assertThat(result.failure(), is(Optional.empty()));
        assertThat(result.error().orElseThrow(), is(cause));
        assertThat(result.skipped(), is(false));
        assertThat(
                result.toString(),
                is(
                        "CaseResult{test=test-a, duration=PT42.899S, skipped=false, failure=<none>, error=boom}"));
    }
}

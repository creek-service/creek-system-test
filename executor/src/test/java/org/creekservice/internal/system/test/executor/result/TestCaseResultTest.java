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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;

import java.time.Clock;
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
class TestCaseResultTest {

    private static final Instant START = Instant.now();
    private static final Instant FINISH = START.plusSeconds(43).minusMillis(101);

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TestCase testCase;

    @Mock private Clock clock;

    private TestCaseResult.Builder builder;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(START, FINISH);
        when(testCase.name()).thenReturn("test name");
        when(testCase.suite().name()).thenReturn("suite name");

        builder = new TestCaseResult.Builder(testCase, clock);
    }

    @Test
    void shouldBuildDisabled() {
        // When:
        final TestCaseResult result = builder.disabled();

        // Then:
        assertThat(result.name(), is("test name"));
        assertThat(result.classname(), is("suite name"));
        assertThat(result.time(), is("42.899"));
        assertThat(result.failure(), is(Optional.empty()));
        assertThat(result.skipped(), is(not(Optional.empty())));
    }

    @Test
    void shouldBuildSuccess() {
        // When:
        final TestCaseResult result = builder.success();

        // Then:
        assertThat(result.name(), is("test name"));
        assertThat(result.classname(), is("suite name"));
        assertThat(result.time(), is("42.899"));
        assertThat(result.failure(), is(Optional.empty()));
        assertThat(result.skipped(), is(Optional.empty()));
    }

    @Test
    void shouldBuildFailure() {
        // Given:
        final AssertionError cause = new AssertionError("boom");

        // When:
        final TestCaseResult result = builder.failure(cause);

        // Then:
        assertThat(result.name(), is("test name"));
        assertThat(result.classname(), is("suite name"));
        assertThat(result.time(), is("42.899"));
        assertThat(result.failure().orElseThrow().message(), is("boom"));
        assertThat(
                result.failure().orElseThrow().type(), is(AssertionError.class.getCanonicalName()));
        assertThat(
                result.failure().orElseThrow().stack(),
                startsWith("java.lang.AssertionError: boom"));
        assertThat(result.failure().orElseThrow().isFailure(), is(true));
        assertThat(result.failure().orElseThrow().isError(), is(false));
        assertThat(result.skipped(), is(Optional.empty()));
    }

    @Test
    void shouldBuildError() {
        // Given:
        final Exception cause = new IllegalArgumentException("boom");

        // When:
        final TestCaseResult result = builder.error(cause);

        // Then:
        assertThat(result.name(), is("test name"));
        assertThat(result.classname(), is("suite name"));
        assertThat(result.time(), is("42.899"));
        assertThat(result.failure().orElseThrow().message(), is("boom"));
        assertThat(
                result.failure().orElseThrow().type(),
                is(IllegalArgumentException.class.getCanonicalName()));
        assertThat(
                result.failure().orElseThrow().stack(),
                startsWith("java.lang.IllegalArgumentException: boom"));
        assertThat(result.failure().orElseThrow().isFailure(), is(false));
        assertThat(result.failure().orElseThrow().isError(), is(true));
        assertThat(result.skipped(), is(Optional.empty()));
    }
}

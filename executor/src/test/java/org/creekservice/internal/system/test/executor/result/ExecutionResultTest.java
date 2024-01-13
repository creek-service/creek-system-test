/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExecutionResultTest {

    @Mock private SuiteResult suiteResult0;
    @Mock private SuiteResult suiteResult1;
    @Mock private SuiteResult suiteResult2;
    private ExecutionResult result;

    @BeforeEach
    void setUp() {
        result = new ExecutionResult(List.of(suiteResult0, suiteResult1));
    }

    @Test
    void shouldToString() {
        // When:
        final String text = result.toString();

        // Then:
        assertThat(
                text,
                is(
                        "ExecutionResult{results="
                                + lineSeparator()
                                + suiteResult0
                                + ","
                                + lineSeparator()
                                + suiteResult1
                                + lineSeparator()
                                + "}"));
    }

    @Test
    void shouldCountFailures() {
        // Given:
        when(suiteResult0.failures()).thenReturn(1L);
        when(suiteResult1.failures()).thenReturn(2L);

        // Then:
        assertThat(result.failed(), is(3L));
    }

    @Test
    void shouldCountErrors() {
        // Given:
        when(suiteResult0.error()).thenReturn(Optional.of(new RuntimeException()));
        when(suiteResult1.errors()).thenReturn(2L);

        // Then:
        assertThat(result.errors(), is(3L));
    }

    @Test
    void shouldDetectPassed() {
        // Given:
        when(suiteResult0.failures()).thenReturn(1L);
        when(suiteResult1.errors()).thenReturn(1L);

        final ExecutionResult result0 = new ExecutionResult(List.of(suiteResult0));
        final ExecutionResult result1 = new ExecutionResult(List.of(suiteResult1));
        final ExecutionResult result2 = new ExecutionResult(List.of(suiteResult2));

        // Then:
        assertThat(result0.passed(), is(false));
        assertThat(result1.passed(), is(false));
        assertThat(result2.passed(), is(true));
    }
}

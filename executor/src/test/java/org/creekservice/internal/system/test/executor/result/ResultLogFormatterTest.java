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

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import java.util.Optional;
import org.creekservice.api.system.test.extension.test.model.TestCaseResult;
import org.creekservice.api.system.test.extension.test.model.TestExecutionResult;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResultLogFormatterTest {

    @Mock private TestExecutionResult result;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TestSuiteResult suiteResult0;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TestSuiteResult suiteResult1;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TestCaseResult testResult0;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TestCaseResult testResult1;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TestCaseResult testResult2;

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @BeforeEach
    void setUp() {
        doReturn(List.of(suiteResult0, suiteResult1)).when(result).results();
        doReturn(List.of(testResult0, testResult1)).when(suiteResult0).testResults();
        doReturn(List.of(testResult2)).when(suiteResult1).testResults();

        when(testResult0.failure()).thenReturn(Optional.empty());
        when(testResult0.error()).thenReturn(Optional.empty());
        when(testResult1.failure()).thenReturn(Optional.empty());
        when(testResult1.error()).thenReturn(Optional.empty());
        when(testResult2.failure()).thenReturn(Optional.empty());
        when(testResult2.error()).thenReturn(Optional.empty());

        when(testResult0.testCase().suite().name()).thenReturn("suite-0");
        when(testResult1.testCase().suite().name()).thenReturn("suite-0");
        when(testResult2.testCase().suite().name()).thenReturn("suite-1");

        when(testResult0.testCase().name()).thenReturn("test-0");
        when(testResult1.testCase().name()).thenReturn("test-1");
        when(testResult2.testCase().name()).thenReturn("test-2");
    }

    @Test
    void shouldFormatResultWithNoIssues() {
        // When:
        final String text = ResultLogFormatter.formatIssues(result);

        // Then:
        assertThat(text, is(""));
    }

    @Test
    void shouldFormatResultWithIssues() {
        // Given:
        when(testResult0.failure()).thenReturn(Optional.of(new AssertionError("failure reason")));
        when(testResult2.error()).thenReturn(Optional.of(new RuntimeException("error reason")));

        // When:
        final String text = ResultLogFormatter.formatIssues(result);

        // Then:
        assertThat(
                text,
                is(
                        "suite-0:test-0: failure reason"
                                + lineSeparator()
                                + "suite-1:test-2: error reason"));
    }

    @Test
    void shouldFormatSuiteWithError() {
        // Given:
        when(suiteResult0.error()).thenReturn(Optional.of(new RuntimeException("Boom")));
        when(suiteResult0.testResults()).thenReturn(List.of());
        when(suiteResult0.testSuite().name()).thenReturn("suite-0");
        when(testResult2.error()).thenReturn(Optional.of(new RuntimeException("error reason")));

        // When:
        final String text = ResultLogFormatter.formatIssues(result);

        // Then:
        assertThat(text, is("suite-0: Boom" + lineSeparator() + "suite-1:test-2: error reason"));
    }
}

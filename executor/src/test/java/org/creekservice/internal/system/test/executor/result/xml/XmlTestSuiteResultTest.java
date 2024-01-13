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

package org.creekservice.internal.system.test.executor.result.xml;

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.creekservice.api.base.type.Throwables;
import org.creekservice.api.system.test.extension.test.model.CreekTestCase;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.model.TestCaseResult;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
class XmlTestSuiteResultTest {

    private static final ObjectMapper MAPPER = XmlResultMapper.INSTANCE.get();

    @Mock private TestCaseResult caseResult0;
    @Mock private TestCaseResult caseResult1;
    @Mock private TestSuiteResult suiteResult;
    @Mock private CreekTestCase testCase0;
    @Mock private CreekTestCase testCase1;
    @Mock private CreekTestSuite testSuite;
    private XmlTestSuiteResult xmlResult;

    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT")
    @BeforeEach
    void setUp() {
        when(suiteResult.testSuite()).thenReturn(testSuite);
        when(suiteResult.start()).thenReturn(Instant.ofEpochMilli(1234567890));
        when(suiteResult.duration()).thenReturn(Duration.ofMillis(1234567));
        when(suiteResult.skipped()).thenReturn(1L);
        when(suiteResult.errors()).thenReturn(3L);
        when(suiteResult.failures()).thenReturn(4L);
        doReturn(List.of(caseResult0, caseResult1)).when(suiteResult).testResults();

        when(testSuite.name()).thenReturn("the suite");

        xmlResult = new XmlTestSuiteResult(suiteResult, "some-host");
    }

    @Test
    void shouldSerialize() throws Exception {
        // Given:
        when(caseResult0.testCase()).thenReturn(testCase0);
        when(caseResult0.duration()).thenReturn(Duration.ofMillis(12345));
        when(caseResult1.testCase()).thenReturn(testCase1);
        when(caseResult1.duration()).thenReturn(Duration.ofMillis(23587));

        when(testCase0.name()).thenReturn("test 0");
        when(testCase0.suite()).thenReturn(testSuite);
        when(testCase1.name()).thenReturn("test 1");
        when(testCase1.suite()).thenReturn(testSuite);

        // When:
        final String xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(xmlResult);

        // Then:
        assertThat(
                xml,
                is(
                        "<testsuite errors=\"3\" failures=\"4\" hostname=\"some-host\" name=\"the"
                                + " suite\" skipped=\"1\" tests=\"2\" time=\"1234.567\""
                                + " timestamp=\"1970-01-15T06:56:07.890\">"
                                + lineSeparator()
                                + "  <testcase>"
                                + lineSeparator()
                                + "    <testcase classname=\"the suite\" name=\"test 0\""
                                + " time=\"12.345\"/>"
                                + lineSeparator()
                                + "    <testcase classname=\"the suite\" name=\"test 1\""
                                + " time=\"23.587\"/>"
                                + lineSeparator()
                                + "  </testcase>"
                                + lineSeparator()
                                + "</testsuite>"
                                + lineSeparator()));
    }

    @Test
    void shouldSerializeSuiteError() throws Exception {
        // Given:
        final IllegalArgumentException cause = new IllegalArgumentException("Boom");
        when(suiteResult.error()).thenReturn(Optional.of(cause));
        when(suiteResult.errors()).thenReturn(4L);
        when(suiteResult.skipped()).thenReturn(0L);
        when(suiteResult.testResults()).thenReturn(List.of());
        final String stackTrace = Throwables.stackTrace(cause);

        // When:
        final String xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(xmlResult);

        // Then:
        assertThat(
                xml,
                is(
                        "<testsuite errors=\"4\" failures=\"4\" hostname=\"some-host\" name=\"the"
                                + " suite\" skipped=\"0\" tests=\"0\" time=\"1234.567\""
                                + " timestamp=\"1970-01-15T06:56:07.890\">"
                                + lineSeparator()
                                + "  <error message=\"Boom\""
                                + " type=\"java.lang.IllegalArgumentException\">"
                                + stackTrace.replaceAll("\r", "&#xd;")
                                + "</error>"
                                + lineSeparator()
                                + "</testsuite>"
                                + lineSeparator()));
    }
}

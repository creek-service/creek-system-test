/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import org.creekservice.api.base.type.Throwables;
import org.creekservice.api.system.test.extension.test.model.CreekTestCase;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.model.TestCaseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({MockitoExtension.class})
class XmlTestCaseResultTest {

    private static final ObjectMapper MAPPER = XmlResultMapper.INSTANCE.get();

    @Mock private TestCaseResult result;
    @Mock private CreekTestCase testCase;
    @Mock private CreekTestSuite testSuite;
    private XmlTestCaseResult xmlResult;

    @BeforeEach
    void setUp() {
        when(result.testCase()).thenReturn(testCase);
        when(result.duration()).thenReturn(Duration.ofMillis(1234567));

        when(testCase.name()).thenReturn("the test");
        when(testCase.suite()).thenReturn(testSuite);

        when(testSuite.name()).thenReturn("the suite");

        xmlResult = XmlTestCaseResult.from(result);
    }

    @Test
    void shouldSerializeSuccess() throws Exception {
        // When:
        final String xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(xmlResult);

        // Then:
        assertThat(
                xml,
                is(
                        "<testcase classname=\"the suite\" name=\"the test\" time=\"1234.567\"/>"
                                + lineSeparator()));
    }

    @Test
    void shouldSerializeSkipped() throws Exception {
        // Given:
        when(result.skipped()).thenReturn(true);

        // When:
        final String xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(xmlResult);

        // Then:
        assertThat(
                xml,
                is(
                        "<testcase classname=\"the suite\" name=\"the test\" time=\"1234.567\">"
                                + lineSeparator()
                                + "  <skipped/>"
                                + lineSeparator()
                                + "</testcase>"
                                + lineSeparator()));
    }

    @Test
    void shouldSerializeFailed() throws Exception {
        // Given:
        final AssertionError e = new AssertionError("Expectation not met");
        when(result.failure()).thenReturn(Optional.of(e));
        final String stackTrace = Throwables.stackTrace(e);

        // When:
        final String xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(xmlResult);

        // Then:
        assertThat(
                xml,
                is(
                        "<testcase classname=\"the suite\" name=\"the test\" time=\"1234.567\">"
                                + lineSeparator()
                                + "  <failure message=\"Expectation not met\""
                                + " type=\"java.lang.AssertionError\">"
                                + stackTrace
                                + "</failure>"
                                + lineSeparator()
                                + "</testcase>"
                                + lineSeparator()));
    }

    @Test
    void shouldSerializeError() throws Exception {
        // Given:
        final RuntimeException e = new IllegalStateException("bad state");
        when(result.error()).thenReturn(Optional.of(e));
        final String stackTrace = Throwables.stackTrace(e);

        // When:
        final String xml = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(xmlResult);

        // Then:
        assertThat(
                xml,
                is(
                        "<testcase classname=\"the suite\" name=\"the test\" time=\"1234.567\">"
                                + lineSeparator()
                                + "  <error message=\"bad state\""
                                + " type=\"java.lang.IllegalStateException\">"
                                + stackTrace
                                + "</error>"
                                + lineSeparator()
                                + "</testcase>"
                                + lineSeparator()));
    }
}

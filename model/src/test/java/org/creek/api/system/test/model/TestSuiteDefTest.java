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

package org.creek.api.system.test.model;

import static org.creek.api.system.test.model.Disabled.disabled;
import static org.creek.api.system.test.model.LocationAware.UNKNOWN_LOCATION;
import static org.creek.api.system.test.model.TestCaseDef.testCase;
import static org.creek.api.system.test.model.TestSuiteDef.testSuite;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.testing.EqualsTester;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.creek.internal.system.test.parser.SystemTestMapper;
import org.junit.jupiter.api.Test;

class TestSuiteDefTest {

    private static final ObjectMapper MAPPER = SystemTestMapper.create(List.of());

    private static final String TEST_CASE_YAML =
            " name: test one\n" + "    description: test description\n";

    private static final TestCaseDef TEST_CASE_NO_LOCATION =
            testCase("test one", Optional.of("test description"), Optional.empty());

    @Test
    void shouldImplementHashCodeAndEquals() {
        final Disabled disabled = mock(Disabled.class);
        final TestCaseDef testCase = mock(TestCaseDef.class);

        new EqualsTester()
                .addEqualityGroup(
                        testSuite(
                                "name",
                                Optional.of("description"),
                                Optional.of(disabled),
                                List.of("service"),
                                List.of(testCase)),
                        testSuite(
                                "name",
                                Optional.of("description"),
                                Optional.of(disabled),
                                List.of("service"),
                                List.of(testCase)))
                .addEqualityGroup(
                        testSuite(
                                "diff",
                                Optional.of("description"),
                                Optional.of(disabled),
                                List.of("service"),
                                List.of(testCase)))
                .addEqualityGroup(
                        testSuite(
                                "name",
                                Optional.of("diff"),
                                Optional.of(disabled),
                                List.of("service"),
                                List.of(testCase)))
                .addEqualityGroup(
                        testSuite(
                                "name",
                                Optional.of("description"),
                                Optional.of(disabled),
                                List.of("diff"),
                                List.of(testCase)))
                .addEqualityGroup(
                        testSuite(
                                "name",
                                Optional.of("description"),
                                Optional.empty(),
                                List.of("service"),
                                List.of(testCase)))
                .addEqualityGroup(
                        testSuite(
                                "name",
                                Optional.of("description"),
                                Optional.of(disabled),
                                List.of("service"),
                                List.of(testCase, testCase)))
                .addEqualityGroup(
                        testSuite(
                                        "name",
                                        Optional.of("description"),
                                        Optional.of(disabled),
                                        List.of("service"),
                                        List.of(testCase))
                                .withLocation(mock(URI.class)))
                .testEquals();
    }

    @Test
    void shouldReadValid() throws Exception {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test suite\n"
                        + "description: suite description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "services:\n"
                        + " - a_service\n"
                        + "tests:\n"
                        + " - "
                        + TEST_CASE_YAML;

        // When:
        final TestSuiteDef result = parse(yaml);

        // Then:
        assertThat(result.name(), is("a test suite"));
        assertThat(result.description(), is("suite description"));
        assertThat(result.disabled().map(Disabled::reason), is(Optional.of("disabled reason")));
        assertThat(result.location(), is(UNKNOWN_LOCATION));
        assertThat(result.services(), contains("a_service"));
        assertThat(result.tests(), contains(TEST_CASE_NO_LOCATION));
    }

    @Test
    void shouldRequireName() {
        // Given:
        final String yaml =
                "---\n"
                        + "description: suite description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "services:\n"
                        + " - a_service\n"
                        + "tests:\n"
                        + " - "
                        + TEST_CASE_YAML;

        // When:
        final Exception e = assertThrows(JsonProcessingException.class, () -> parse(yaml));

        // Then:
        assertThat(e.getMessage(), containsString("Missing required creator property 'name'"));
    }

    @Test
    void shouldNotRequireDescription() throws Exception {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test suite\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "services:\n"
                        + " - a_service\n"
                        + "tests:\n"
                        + " - "
                        + TEST_CASE_YAML;

        // When:
        final TestSuiteDef result = parse(yaml);

        // Then:
        assertThat(result.description(), is(""));
    }

    @Test
    void shouldNotRequireDisabled() throws Exception {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test suite\n"
                        + "description: suite description\n"
                        + "services:\n"
                        + " - a_service\n"
                        + "tests:\n"
                        + " - "
                        + TEST_CASE_YAML;

        // When:
        final TestSuiteDef result = parse(yaml);

        // Then:
        assertThat(result.disabled(), is(Optional.empty()));
    }

    @Test
    void shouldRequireServices() {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test suite\n"
                        + "description: suite description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "tests:\n"
                        + " - "
                        + TEST_CASE_YAML;

        // When:
        final Exception e = assertThrows(JsonProcessingException.class, () -> parse(yaml));

        // Then:
        assertThat(e.getMessage(), containsString("Missing required creator property 'services'"));
    }

    @Test
    void shouldRequireAtLeastOneService() {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test suite\n"
                        + "description: suite description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "services: []\n"
                        + "tests:\n"
                        + " - "
                        + TEST_CASE_YAML;

        // When:
        final Exception e = assertThrows(JsonProcessingException.class, () -> parse(yaml));

        // Then:
        assertThat(e.getMessage(), containsString("services can not be empty"));
    }

    @Test
    void shouldRequireTests() {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test suite\n"
                        + "description: suite description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "services:\n"
                        + " - a_service\n";

        // When:
        final Exception e = assertThrows(JsonProcessingException.class, () -> parse(yaml));

        // Then:
        assertThat(e.getMessage(), containsString("Missing required creator property 'tests'"));
    }

    @Test
    void shouldRequireAtLeastOneTest() {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test suite\n"
                        + "description: suite description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "services:\n"
                        + " - a_service\n"
                        + "tests: []\n";

        // When:
        final Exception e = assertThrows(JsonProcessingException.class, () -> parse(yaml));

        // Then:
        assertThat(e.getMessage(), containsString("tests can not be empty"));
    }

    private static TestSuiteDef parse(final String yaml) throws Exception {
        return MAPPER.readValue(yaml, TestSuiteDef.class);
    }
}

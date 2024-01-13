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

package org.creekservice.api.system.test.model;

import static org.creekservice.api.system.test.extension.test.model.LocationAware.UNKNOWN_LOCATION;
import static org.creekservice.api.system.test.model.SimpleRef.simpleRef;
import static org.creekservice.api.system.test.model.TestCaseDef.testCase;
import static org.creekservice.api.system.test.model.TestSuiteDef.testSuite;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.testing.EqualsTester;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.creekservice.api.system.test.extension.test.model.Option;
import org.creekservice.api.system.test.parser.ModelType;
import org.creekservice.internal.system.test.parser.SystemTestMapper;
import org.junit.jupiter.api.Test;

class TestSuiteDefTest {

    private static final ObjectMapper MAPPER =
            SystemTestMapper.create(
                    List.of(ModelType.option(TestOption.class, "creek/test-option/1")));

    private static final String TEST_CASE_YAML =
            "name: test one\n"
                    + "   notes: test description\n"
                    + "   expectations:\n"
                    + "    - an_expectation\n";

    private static final TestCaseDef TEST_CASE_NO_LOCATION =
            testCase(
                    "test one",
                    Optional.of("test description"),
                    Optional.empty(),
                    Optional.empty(),
                    List.of(simpleRef("an_expectation")));

    @Test
    void shouldImplementHashCodeAndEquals() {
        final Disabled disabled = mock(Disabled.class);
        final TestCaseDef testCase = mock(TestCaseDef.class);
        final Option option = mock(Option.class);

        new EqualsTester()
                .addEqualityGroup(
                        testSuite(
                                "name",
                                Optional.of("notes"),
                                Optional.of(disabled),
                                List.of("service"),
                                Optional.of(List.of(option)),
                                List.of(testCase)),
                        testSuite(
                                "name",
                                Optional.of("notes"),
                                Optional.of(disabled),
                                List.of("service"),
                                Optional.of(List.of(option)),
                                List.of(testCase)),
                        testSuite(
                                        "name",
                                        Optional.of("notes"),
                                        Optional.of(disabled),
                                        List.of("service"),
                                        Optional.of(List.of(option)),
                                        List.of(testCase))
                                .withLocation(mock(URI.class)))
                .addEqualityGroup(
                        testSuite(
                                "diff",
                                Optional.of("notes"),
                                Optional.of(disabled),
                                List.of("service"),
                                Optional.of(List.of(option)),
                                List.of(testCase)))
                .addEqualityGroup(
                        testSuite(
                                "name",
                                Optional.of("diff"),
                                Optional.of(disabled),
                                List.of("service"),
                                Optional.of(List.of(option)),
                                List.of(testCase)))
                .addEqualityGroup(
                        testSuite(
                                "name",
                                Optional.of("notes"),
                                Optional.of(disabled),
                                List.of("diff"),
                                Optional.of(List.of(option)),
                                List.of(testCase)))
                .addEqualityGroup(
                        testSuite(
                                "name",
                                Optional.of("notes"),
                                Optional.of(disabled),
                                List.of("service"),
                                Optional.of(List.of()),
                                List.of(testCase)))
                .addEqualityGroup(
                        testSuite(
                                "name",
                                Optional.of("notes"),
                                Optional.empty(),
                                List.of("service"),
                                Optional.of(List.of(option)),
                                List.of(testCase)))
                .addEqualityGroup(
                        testSuite(
                                "name",
                                Optional.of("notes"),
                                Optional.of(disabled),
                                List.of("service"),
                                Optional.of(List.of(option)),
                                List.of(testCase, testCase)))
                .testEquals();
    }

    @Test
    void shouldReadValid() throws Exception {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test suite\n"
                        + "notes: suite description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "services:\n"
                        + " - a_service\n"
                        + "options:\n"
                        + " - !creek/test-option/1\n"
                        + "   dummy: text\n"
                        + "tests:\n"
                        + " - "
                        + TEST_CASE_YAML;

        // When:
        final TestSuiteDef result = parse(yaml);

        // Then:
        assertThat(result.name(), is("a test suite"));
        assertThat(result.notes(), is("suite description"));
        assertThat(result.disabled().map(Disabled::reason), is(Optional.of("disabled reason")));
        assertThat(result.location(), is(UNKNOWN_LOCATION));
        assertThat(result.services(), contains("a_service"));
        assertThat(result.options(), hasSize(1));
        assertThat(result.options().get(0), is(instanceOf(TestOption.class)));
        assertThat(result.tests(), contains(TEST_CASE_NO_LOCATION));
    }

    @Test
    void shouldRequireName() {
        // Given:
        final String yaml =
                "---\n"
                        + "notes: suite description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "services:\n"
                        + " - a_service\n"
                        + "options:\n"
                        + " - !creek/test-option/1\n"
                        + "   dummy: text\n"
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
                        + "options:\n"
                        + " - !creek/test-option/1\n"
                        + "   dummy: text\n"
                        + "tests:\n"
                        + " - "
                        + TEST_CASE_YAML;

        // When:
        final TestSuiteDef result = parse(yaml);

        // Then:
        assertThat(result.notes(), is(""));
    }

    @Test
    void shouldNotRequireDisabled() throws Exception {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test suite\n"
                        + "notes: suite description\n"
                        + "services:\n"
                        + " - a_service\n"
                        + "options:\n"
                        + " - !creek/test-option/1\n"
                        + "   dummy: text\n"
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
                        + "notes: suite description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "options:\n"
                        + " - !creek/test-option/1\n"
                        + "   dummy: text\n"
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
                        + "notes: suite description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "services: []\n"
                        + "options:\n"
                        + " - !creek/test-option/1\n"
                        + "   dummy: text\n"
                        + "tests:\n"
                        + " - "
                        + TEST_CASE_YAML;

        // When:
        final Exception e = assertThrows(JsonProcessingException.class, () -> parse(yaml));

        // Then:
        assertThat(e.getMessage(), containsString("services can not be empty"));
    }

    @Test
    void shouldNotRequireOptions() throws Exception {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test suite\n"
                        + "notes: suite description\n"
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
        assertThat(result.options(), is(empty()));
    }

    @Test
    void shouldRequireTests() {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test suite\n"
                        + "notes: suite description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "services:\n"
                        + " - a_service\n"
                        + "options:\n"
                        + " - !creek/test-option/1\n"
                        + "   dummy: text\n";

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
                        + "notes: suite description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "services:\n"
                        + " - a_service\n"
                        + "options:\n"
                        + " - !creek/test-option/1\n"
                        + "   dummy: text\n"
                        + "tests: []\n";

        // When:
        final Exception e = assertThrows(JsonProcessingException.class, () -> parse(yaml));

        // Then:
        assertThat(e.getMessage(), containsString("tests can not be empty"));
    }

    private static TestSuiteDef parse(final String yaml) throws Exception {
        return MAPPER.readValue(yaml, TestSuiteDef.class);
    }

    public static final class TestOption implements Option {

        @JsonCreator
        @SuppressWarnings("checkstyle:RedundantModifier")
        public TestOption(@JsonProperty("dummy") final String dummy) {}
    }
}

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

package org.creekservice.api.system.test.model;

import static org.creekservice.api.system.test.extension.test.model.ModelType.expectationRef;
import static org.creekservice.api.system.test.extension.test.model.ModelType.inputRef;
import static org.creekservice.api.system.test.extension.test.model.ModelType.ref;
import static org.creekservice.api.system.test.model.LocationAware.UNKNOWN_LOCATION;
import static org.creekservice.api.system.test.model.SimpleRef.simpleRef;
import static org.creekservice.api.system.test.model.TestCaseDef.testCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
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
import org.creekservice.api.system.test.extension.test.model.ExpectationRef;
import org.creekservice.api.system.test.extension.test.model.InputRef;
import org.creekservice.internal.system.test.parser.SystemTestMapper;
import org.junit.jupiter.api.Test;

class TestCaseDefTest {

    private static final ObjectMapper MAPPER =
            SystemTestMapper.create(
                    List.of(
                            ref(CustomRef.class, "custom_ref"),
                            inputRef(CustomInputRef.class),
                            expectationRef(CustomExpectationRef.class)));

    @Test
    void shouldImplementHashCodeAndEquals() {
        final Disabled disabled = mock(Disabled.class);
        final Optional<List<InputRef>> inputs = Optional.of(List.of(mock(InputRef.class)));
        final List<ExpectationRef> expectations = List.of(mock(ExpectationRef.class));

        new EqualsTester()
                .addEqualityGroup(
                        testCase(
                                "name",
                                Optional.of("description"),
                                Optional.of(disabled),
                                inputs,
                                expectations),
                        testCase(
                                "name",
                                Optional.of("description"),
                                Optional.of(disabled),
                                inputs,
                                expectations),
                        testCase(
                                        "name",
                                        Optional.of("description"),
                                        Optional.of(disabled),
                                        inputs,
                                        expectations)
                                .withLocation(mock(URI.class)))
                .addEqualityGroup(
                        testCase(
                                "diff",
                                Optional.of("description"),
                                Optional.of(disabled),
                                inputs,
                                expectations))
                .addEqualityGroup(
                        testCase(
                                "name",
                                Optional.of("diff"),
                                Optional.of(disabled),
                                inputs,
                                expectations))
                .addEqualityGroup(
                        testCase(
                                "name",
                                Optional.of("description"),
                                Optional.empty(),
                                inputs,
                                expectations))
                .addEqualityGroup(
                        testCase(
                                "name",
                                Optional.of("description"),
                                Optional.of(disabled),
                                Optional.empty(),
                                expectations))
                .addEqualityGroup(
                        testCase(
                                "name",
                                Optional.of("description"),
                                Optional.of(disabled),
                                inputs,
                                List.of(expectations.get(0), expectations.get(0))))
                .testEquals();
    }

    @Test
    void shouldReadValid() throws Exception {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test case\n"
                        + "description: description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "inputs:\n"
                        + "  - an_input\n"
                        + "expectations:\n"
                        + "  - an_expectation\n";

        // When:
        final TestCaseDef result = parse(yaml);

        // Then:
        assertThat(result.name(), is("a test case"));
        assertThat(result.description(), is("description"));
        assertThat(result.disabled().map(Disabled::reason), is(Optional.of("disabled reason")));
        assertThat(result.inputs(), contains(simpleRef("an_input")));
        assertThat(result.expectations(), contains(simpleRef("an_expectation")));
        assertThat(result.location(), is(UNKNOWN_LOCATION));
    }

    @Test
    void shouldRequireName() {
        // Given:
        final String yaml =
                "---\n"
                        + "description: description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "inputs:\n"
                        + "  - an_input\n"
                        + "expectations:\n"
                        + "  - an_expectation\n";

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
                        + "name: a test case\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "inputs:\n"
                        + "  - an_input\n"
                        + "expectations:\n"
                        + "  - an_expectation\n";

        // When:
        final TestCaseDef result = parse(yaml);

        // Then:
        assertThat(result.description(), is(""));
    }

    @Test
    void shouldNotRequireDisabled() throws Exception {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test case\n"
                        + "description: description\n"
                        + "inputs:\n"
                        + "  - an_input\n"
                        + "expectations:\n"
                        + "  - an_expectation\n";

        // When:
        final TestCaseDef result = parse(yaml);

        // Then:
        assertThat(result.disabled(), is(Optional.empty()));
    }

    @Test
    void shouldNotRequireInput() throws Exception {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test case\n"
                        + "description: description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "expectations:\n"
                        + "  - an_expectation\n";

        // When:
        final TestCaseDef result = parse(yaml);

        // Then:
        assertThat(result.inputs(), is(empty()));
    }

    @Test
    void shouldRequireExpectations() {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test case\n"
                        + "description: description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "inputs:\n"
                        + "  - an_input\n";

        // When:
        final Exception e = assertThrows(JsonProcessingException.class, () -> parse(yaml));

        // Then:
        assertThat(
                e.getMessage(), containsString("Missing required creator property 'expectations'"));
    }

    @Test
    void shouldRequireAtLeastOneExpectation() {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test case\n"
                        + "description: description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "inputs:\n"
                        + "  - an_input\n"
                        + "expectations: []";

        // When:
        final Exception e = assertThrows(JsonProcessingException.class, () -> parse(yaml));

        // Then:
        assertThat(e.getMessage(), containsString("expectations can not be empty"));
    }

    @Test
    void shouldSupportCustomInputRef() throws Exception {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test case\n"
                        + "description: description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "inputs:\n"
                        + "  - \"@type\": custom\n"
                        + "    a: location\n"
                        + "expectations:\n"
                        + "  - an_expectation\n";

        // When:
        final TestCaseDef result = parse(yaml);

        // Then:
        assertThat(result.inputs(), contains(instanceOf(CustomInputRef.class)));
        assertThat(result.inputs().get(0).id(), is("location"));
    }

    @Test
    void shouldSupportCustomExpectationRef() throws Exception {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test case\n"
                        + "description: description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "inputs:\n"
                        + "  - an_input\n"
                        + "expectations:\n"
                        + "  - !<custom>\n"
                        + "    b: location\n";

        // When:
        final TestCaseDef result = parse(yaml);

        // Then:
        assertThat(result.expectations(), contains(instanceOf(CustomExpectationRef.class)));
        assertThat(result.expectations().get(0).id(), is("location"));
    }

    @Test
    void shouldSupportCustomCommonRef() throws Exception {
        // Given:
        final String yaml =
                "---\n"
                        + "name: a test case\n"
                        + "description: description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n"
                        + "inputs:\n"
                        + "  - !<custom_ref>\n"
                        + "    c: input\n"
                        + "expectations:\n"
                        + "  - !<custom_ref>\n"
                        + "    c: expectation\n";

        // When:
        final TestCaseDef result = parse(yaml);

        // Then:
        assertThat(result.inputs(), contains(instanceOf(CustomRef.class)));
        assertThat(result.inputs().get(0).id(), is("input"));
        assertThat(result.expectations(), contains(instanceOf(CustomRef.class)));
        assertThat(result.expectations().get(0).id(), is("expectation"));
    }

    private static TestCaseDef parse(final String yaml) throws Exception {
        return MAPPER.readValue(yaml, TestCaseDef.class);
    }

    private static class BaseCustomRef {

        private final String id;

        BaseCustomRef(final String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    @SuppressWarnings("checkstyle:RedundantModifier")
    public static final class CustomInputRef extends BaseCustomRef implements InputRef {

        @JsonCreator
        public CustomInputRef(@JsonProperty("a") final String location) {
            super(location);
        }
    }

    @SuppressWarnings("checkstyle:RedundantModifier")
    public static final class CustomExpectationRef extends BaseCustomRef implements ExpectationRef {

        @JsonCreator
        public CustomExpectationRef(@JsonProperty("b") final String location) {
            super(location);
        }
    }

    @SuppressWarnings("checkstyle:RedundantModifier")
    public static final class CustomRef extends BaseCustomRef implements InputRef, ExpectationRef {

        @JsonCreator
        public CustomRef(@JsonProperty("c") final String location) {
            super(location);
        }
    }
}

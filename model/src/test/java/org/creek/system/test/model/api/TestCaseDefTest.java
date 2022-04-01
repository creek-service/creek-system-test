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

package org.creek.system.test.model.api;

import static org.creek.system.test.model.api.LocationAware.UNKNOWN_LOCATION;
import static org.creek.system.test.model.api.TestCaseDef.testCase;
import static org.hamcrest.MatcherAssert.assertThat;
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
import org.creek.system.test.parser.internal.SystemTestMapper;
import org.junit.jupiter.api.Test;

class TestCaseDefTest {

    private static final ObjectMapper MAPPER = SystemTestMapper.create(List.of());

    @Test
    void shouldImplementHashCodeAndEquals() {
        final Disabled disabled = mock(Disabled.class);

        new EqualsTester()
                .addEqualityGroup(
                        testCase("name", Optional.of("description"), Optional.of(disabled)),
                        testCase("name", Optional.of("description"), Optional.of(disabled)))
                .addEqualityGroup(
                        testCase("diff", Optional.of("description"), Optional.of(disabled)))
                .addEqualityGroup(testCase("name", Optional.of("diff"), Optional.of(disabled)))
                .addEqualityGroup(testCase("name", Optional.of("description"), Optional.empty()))
                .addEqualityGroup(
                        testCase("name", Optional.of("description"), Optional.of(disabled))
                                .withLocation(mock(URI.class)))
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
                        + "  reason: disabled reason\n";

        // When:
        final TestCaseDef result = parse(yaml);

        // Then:
        assertThat(result.name(), is("a test case"));
        assertThat(result.description(), is("description"));
        assertThat(result.disabled().map(Disabled::reason), is(Optional.of("disabled reason")));
        assertThat(result.location(), is(UNKNOWN_LOCATION));
    }

    @Test
    void shouldRequireName() {
        // Given:
        final String yaml =
                "---\n"
                        + "description: description\n"
                        + "disabled:\n"
                        + "  reason: disabled reason\n";

        // When:
        final Exception e = assertThrows(JsonProcessingException.class, () -> parse(yaml));

        // Then:
        assertThat(e.getMessage(), containsString("Missing required creator property 'name'"));
    }

    @Test
    void shouldNotRequireDescription() throws Exception {
        // Given:
        final String yaml =
                "---\n" + "name: a test case\n" + "disabled:\n" + "  reason: disabled reason\n";

        // When:
        final TestCaseDef result = parse(yaml);

        // Then:
        assertThat(result.description(), is(""));
    }

    @Test
    void shouldNotRequireDisabled() throws Exception {
        // Given:
        final String yaml = "---\n" + "name: a test case\n" + "description: description\n";

        // When:
        final TestCaseDef result = parse(yaml);

        // Then:
        assertThat(result.disabled(), is(Optional.empty()));
    }

    private static TestCaseDef parse(final String yaml) throws Exception {
        return MAPPER.readValue(yaml, TestCaseDef.class);
    }
}

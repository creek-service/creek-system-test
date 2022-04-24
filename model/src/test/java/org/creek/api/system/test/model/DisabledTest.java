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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.testing.EqualsTester;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.creek.internal.system.test.parser.SystemTestMapper;
import org.junit.jupiter.api.Test;

class DisabledTest {

    private static final ObjectMapper MAPPER = SystemTestMapper.create(List.of());

    @Test
    void shouldImplementHashCodeAndEquals() {
        final URI issue = URI.create("http://jira/issue/4");

        new EqualsTester()
                .addEqualityGroup(
                        disabled("reason", Optional.of(issue)),
                        disabled("reason", Optional.of(issue)))
                .addEqualityGroup(disabled("diff", Optional.of(issue)))
                .addEqualityGroup(disabled("reason", Optional.empty()))
                .testEquals();
    }

    @Test
    void shouldReadValid() throws Exception {
        // Given:
        final String yaml = "---\n" + "reason: reason\n" + "issue: http://jira/issue/4\n";

        // When:
        final Disabled result = parse(yaml);

        // Then:
        assertThat(result.reason(), is("reason"));
        assertThat(result.issue(), is(Optional.of(URI.create("http://jira/issue/4"))));
    }

    @Test
    void shouldRequireReason() {
        // Given:
        final String yaml = "---\n" + "issue: http://jira/issue/4\n";

        // When:
        final Exception e = assertThrows(JsonProcessingException.class, () -> parse(yaml));

        // Then:
        assertThat(e.getMessage(), containsString("Missing required creator property 'reason'"));
    }

    @Test
    void shouldNotRequireIssue() throws Exception {
        // Given:
        final String yaml = "---\n" + "reason: reason\n";

        // When:
        final Disabled result = parse(yaml);

        // Then:
        assertThat(result.issue(), is(Optional.empty()));
    }

    private static Disabled parse(final String yaml) throws Exception {
        return MAPPER.readValue(yaml, Disabled.class);
    }
}

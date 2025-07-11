/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.testing.EqualsTester;
import java.net.URI;
import java.util.List;
import org.creekservice.api.system.test.extension.test.model.ExpectationRef;
import org.creekservice.api.system.test.extension.test.model.InputRef;
import org.creekservice.internal.system.test.parser.SystemTestMapper;
import org.junit.jupiter.api.Test;

class SimpleRefTest {

    private static final ObjectMapper MAPPER = SystemTestMapper.create(List.of());

    @Test
    void shouldImplementHashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(simpleRef("some_file"), simpleRef("some_file"))
                .addEqualityGroup(simpleRef("diff"))
                .testEquals();
    }

    @Test
    void shouldSerializeAsString() throws Exception {
        // Given:
        final SimpleRef ref = simpleRef("/some/location");

        // When:
        final String yaml = MAPPER.writeValueAsString(ref);

        // Then:
        assertThat(yaml, is("--- /some/location\n"));
    }

    @Test
    void shouldDeserializeAsInputRef() throws Exception {
        // Given:
        final String yaml = "---\nsome_file";

        // When:
        final InputRef result = MAPPER.readValue(yaml, InputRef.class);

        // Then:
        assertThat(result, is(simpleRef("some_file")));
    }

    @Test
    void shouldDeserializeAsExpectationRef() throws Exception {
        // Given:
        final String yaml = "---\nsome/file";

        // When:
        final ExpectationRef result = MAPPER.readValue(yaml, ExpectationRef.class);

        // Then:
        assertThat(result, is(simpleRef("some/file")));
    }

    @Test
    void shouldDefaultToNoLocation() {
        assertThat(simpleRef("some_file").location(), is(UNKNOWN_LOCATION));
    }

    @Test
    void shouldSetLocation() {
        // Given:
        final URI location = URI.create("file:///some.location");
        final SimpleRef ref = simpleRef("some_file");

        // When:
        final SimpleRef result = ref.withLocation(location);

        // Then:
        assertThat(result.location(), is(location));
    }
}

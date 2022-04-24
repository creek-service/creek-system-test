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

package org.creek.internal.system.test.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.creek.api.system.test.extension.model.Expectation;
import org.creek.api.system.test.extension.model.Input;
import org.creek.api.system.test.extension.model.ModelType;
import org.creek.api.system.test.extension.model.Seed;
import org.creek.api.system.test.model.LocationAware;
import org.creek.api.test.util.TestPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@SuppressWarnings({"unused", "checkstyle:RedundantModifier"})
class SystemTestMapperTest {

    private static final ObjectMapper MAPPER = SystemTestMapper.create(List.of());

    @TempDir private Path tempDir;

    @Test
    void shouldSetLocationOnLocationAwareTypes() throws Exception {
        // Given:
        final String yaml = "---\n" + "name: some name\n";

        final Path file = tempDir.resolve("a dir").resolve("a file.yml");
        TestPaths.write(file, yaml);

        // When:
        final TestType result = MAPPER.readValue(file.toFile(), TestType.class);

        // Then:
        assertThat(result.location().toString(), startsWith("file:///"));
        assertThat(
                result.location().toString(),
                is(tempDir.toAbsolutePath().toUri() + "a%20dir/a%20file.yml:2"));
    }

    @Test
    void shouldFailOnUnknownProperty() {
        // Given:
        final String yaml = "---\n" + "name: some name\n" + "unknown_prop: some value\n";

        // When:
        final Exception e =
                assertThrows(
                        JsonMappingException.class, () -> MAPPER.readValue(yaml, TestType.class));

        // Then:
        assertThat(e.getMessage(), containsString("unknown_prop"));
    }

    @Test
    void shouldDeserialiseFloatsAsDecimal() throws Exception {
        // Given:
        final String yaml = "---\n" + "value: 1.34\n";

        // When:
        final Map<?, ?> result = MAPPER.readValue(yaml, Map.class);

        // Then:
        assertThat(result, hasEntry("value", new BigDecimal("1.34")));
    }

    @Test
    void shouldFailOnNullPrimitives() {
        // Given:
        final String yaml = "---\n" + "primitive:";

        // When:
        final Exception e =
                assertThrows(
                        JsonMappingException.class,
                        () -> MAPPER.readValue(yaml, WithPrimitive.class));

        // Then:
        assertThat(e.getMessage(), containsString("Cannot map `null`"));
    }

    @Test
    void shouldFailOnNulls() {
        // Given:
        final String yaml = "---\n" + "name:";

        // When:
        final Exception e =
                assertThrows(
                        JsonMappingException.class, () -> MAPPER.readValue(yaml, TestType.class));

        // Then:
        assertThat(e.getMessage(), containsString("Null value"));
        assertThat(e.getMessage(), containsString("'name'"));
    }

    @Test
    void shouldFailOnInvalidSubType() {
        // Given:
        MAPPER.registerSubtypes(new NamedType(SubType.class, "sub"));

        final String yaml = "---\n" + "poly:\n" + "  '@type': unknown_sub\n";

        // When:
        final Exception e =
                assertThrows(
                        JsonMappingException.class, () -> MAPPER.readValue(yaml, WithPoly.class));

        // Then:
        assertThat(e.getMessage(), containsString("Could not resolve"));
        assertThat(e.getMessage(), containsString("'unknown_sub'"));
    }

    @Test
    void shouldFailOnDuplicateFieldNames() {
        // Given:
        final String yaml = "---\n" + "name: name1\n" + "name: name2\n";

        // When:
        final Exception e =
                assertThrows(
                        JsonParseException.class, () -> MAPPER.readValue(yaml, TestType.class));

        // Then:
        assertThat(e.getMessage(), containsString("Duplicate field 'name'"));
    }

    @Test
    void shouldOnlyIncludeNonEmpty() throws Exception {
        // When:
        final String yaml = MAPPER.writeValueAsString(new WithEmpty());

        // Then:
        assertThat(yaml, is("---\nnonEmpty: value\n"));
    }

    @Test
    void shouldDeserializeSeedSubTypes() throws Exception {
        // Given:
        final ObjectMapper mapper =
                SystemTestMapper.create(List.of(ModelType.seed(TestSeed.class)));

        final String yaml = "---\n" + "'@type': test\n";

        // When:
        final Seed result = mapper.readValue(yaml, Seed.class);

        // Then:
        assertThat(result, is(instanceOf(TestSeed.class)));
    }

    @Test
    void shouldDeserializeInputSubTypes() throws Exception {
        // Given:
        final ObjectMapper mapper =
                SystemTestMapper.create(List.of(ModelType.input(TestInput.class)));

        final String yaml = "---\n" + "'@type': test\n";

        // When:
        final Input result = mapper.readValue(yaml, Input.class);

        // Then:
        assertThat(result, is(instanceOf(TestInput.class)));
    }

    @Test
    void shouldDeserializeExpectationSubTypes() throws Exception {
        // Given:
        final ObjectMapper mapper =
                SystemTestMapper.create(List.of(ModelType.expectation(TestExpectation.class)));

        final String yaml = "---\n" + "'@type': test\n";

        // When:
        final Expectation result = mapper.readValue(yaml, Expectation.class);

        // Then:
        assertThat(result, is(instanceOf(TestExpectation.class)));
    }

    public static final class TestType implements LocationAware<TestType> {

        private final String name;
        private final URI location;

        @JsonCreator
        public TestType(@JsonProperty("name") final String name) {
            this(name, UNKNOWN_LOCATION);
        }

        private TestType(final String name, final URI location) {
            this.name = name;
            this.location = location;
        }

        public String getName() {
            return name;
        }

        @Override
        public URI location() {
            return location;
        }

        @Override
        public TestType withLocation(final URI location) {
            return new TestType(name, location);
        }
    }

    public static final class WithPrimitive {

        public WithPrimitive(final @JsonProperty("primitive") int i) {}
    }

    public static final class WithEmpty {

        public String getNonEmpty() {
            return "value";
        }

        public String getEmptyString() {
            return "";
        }

        public Optional<Integer> getEmptyOptional() {
            return Optional.empty();
        }

        public List<Long> getEmptyArray() {
            return List.of();
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    public interface BaseType {}

    public static final class SubType implements BaseType {}

    public static final class WithPoly {
        public WithPoly(@JsonProperty("poly") final BaseType poly) {}
    }

    public static final class TestSeed implements Seed {}

    public static final class TestInput implements Input {}

    public static final class TestExpectation implements Expectation {}
}

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

package org.creekservice.internal.system.test.parser;

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
import org.creekservice.api.system.test.extension.test.model.Expectation;
import org.creekservice.api.system.test.extension.test.model.ExpectationRef;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.api.system.test.extension.test.model.InputRef;
import org.creekservice.api.system.test.extension.test.model.LocationAware;
import org.creekservice.api.system.test.extension.test.model.Option;
import org.creekservice.api.system.test.extension.test.model.Ref;
import org.creekservice.api.system.test.parser.ModelType;
import org.creekservice.api.test.util.TestPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@SuppressWarnings({"unused", "checkstyle:RedundantModifier"})
class SystemTestMapperTest {

    private static final ObjectMapper MAPPER = SystemTestMapper.create(List.of());

    @TempDir private Path tempDir;

    @Test
    void shouldSetLocationOnLocationAwareTypes() throws Exception {
        // Given:
        final String yaml = "---\nname: some name\n";

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
        final String yaml = "---\nname: some name\nunknown_prop: some value\n";

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
        final String yaml = "---\nvalue: 1.34";

        // When:
        final Map<?, ?> result = MAPPER.readValue(yaml, Map.class);

        // Then:
        assertThat(result, hasEntry("value", new BigDecimal("1.34")));
    }

    @Test
    void shouldFailOnNullPrimitives() {
        // Given:
        final String yaml = "---\nprimitive:";

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
        final String yaml = "---\nname:";

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

        final String yaml = "---\npoly:\n  '@type': unknown_sub\n";

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
        final String yaml = "---\nname: name1\nname: name2\n";

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
    void shouldDeserializeInputRefSubTypes() throws Exception {
        // Given:
        final ObjectMapper mapper =
                SystemTestMapper.create(List.of(ModelType.inputRef(TestInputRef.class)));

        final String yaml = "---\n'@type': test\n";

        // When:
        final InputRef result = mapper.readValue(yaml, InputRef.class);

        // Then:
        assertThat(result, is(instanceOf(TestInputRef.class)));
    }

    @Test
    void shouldDeserializeExpectationRefSubTypes() throws Exception {
        // Given:
        final ObjectMapper mapper =
                SystemTestMapper.create(
                        List.of(ModelType.expectationRef(TestExpectationRef.class)));

        final String yaml = "---\n'@type': test\n";

        // When:
        final ExpectationRef result = mapper.readValue(yaml, ExpectationRef.class);

        // Then:
        assertThat(result, is(instanceOf(TestExpectationRef.class)));
    }

    @Test
    void shouldDeserializeCommonRefSubTypes() throws Exception {
        // Given:
        final ObjectMapper mapper = SystemTestMapper.create(List.of(ModelType.ref(TestRef.class)));

        final String yaml = "---\n'@type': test\n";

        // When:
        final Ref result = mapper.readValue(yaml, Ref.class);

        // Then:
        assertThat(result, is(instanceOf(TestRef.class)));
    }

    @Test
    void shouldDeserializeInputSubTypes() throws Exception {
        // Given:
        final ObjectMapper mapper =
                SystemTestMapper.create(List.of(ModelType.input(TestInput.class)));

        final String yaml = "---\n'@type': test\n";

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

        final String yaml = "---\n'@type': test\n";

        // When:
        final Expectation result = mapper.readValue(yaml, Expectation.class);

        // Then:
        assertThat(result, is(instanceOf(TestExpectation.class)));
    }

    @Test
    void shouldDeserializeOptionSubTypes() throws Exception {
        // Given:
        final ObjectMapper mapper =
                SystemTestMapper.create(List.of(ModelType.option(TestOption.class)));

        final String yaml = "---\n'@type': test\n";

        // When:
        final Option result = mapper.readValue(yaml, Option.class);

        // Then:
        assertThat(result, is(instanceOf(TestOption.class)));
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

    public static final class TestInputRef implements InputRef {
        @Override
        public String id() {
            return null;
        }
    }

    public static final class TestExpectationRef implements ExpectationRef {
        @Override
        public String id() {
            return null;
        }
    }

    public static final class TestRef implements InputRef, ExpectationRef {
        @Override
        public String id() {
            return null;
        }
    }

    public static final class TestInput implements Input {}

    public static final class TestExpectation implements Expectation {}

    public static final class TestOption implements Option {}
}

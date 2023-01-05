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

package org.creekservice.internal.system.test.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.ContentReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.net.URI;
import org.creekservice.api.system.test.extension.test.model.LocationAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LocationAwareDeserializerTest {

    @Mock(extraInterfaces = ResolvableDeserializer.class)
    private JsonDeserializer<TestType> delegate;

    @Mock private DeserializationContext ctx;
    @Mock private JsonParser jp;
    @Mock private TestType original;
    @Mock private TestType withPath;
    @Mock private JsonLocation location;
    @Mock private ContentReference content;
    private LocationAwareDeserializer<TestType> deserializer;

    @BeforeEach
    void setUp() throws Exception {
        when(delegate.handledType()).thenReturn((Class) TestType.class);

        deserializer = new LocationAwareDeserializer<>(TestType.class, delegate);

        when(jp.currentLocation()).thenReturn(location);
        when(location.contentReference()).thenReturn(ContentReference.unknown());
        when(delegate.deserialize(jp, ctx)).thenReturn(original);
        when(original.withLocation(any())).thenReturn(withPath);
    }

    @Test
    void shouldThrowOnArgumentTypeMismatch() {
        // Given:
        when(delegate.handledType()).thenReturn((Class) String.class);

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new LocationAwareDeserializer(TestType.class, delegate));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "wrong delegate deserializer type: java.lang.String, expected: "
                                + TestType.class.getName()));
    }

    @Test
    void shouldThrowIfArgumentNotLocationAware() {
        // Given:
        when(delegate.handledType()).thenReturn((Class) String.class);

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> new LocationAwareDeserializer(String.class, delegate));

        // Then:
        assertThat(e.getMessage(), is("type not location aware: " + String.class.getName()));
    }

    @Test
    void shouldHandleResolvableDeserializer() throws Exception {
        // When:
        deserializer.resolve(ctx);

        // Then:
        verify((ResolvableDeserializer) delegate).resolve(ctx);
    }

    @Test
    void shouldHandleNonResolvableDeserializer() throws Exception {
        // Given:
        final JsonDeserializer delegate = mock(JsonDeserializer.class);
        when(delegate.handledType()).thenReturn(TestType.class);

        deserializer = new LocationAwareDeserializer<>(TestType.class, delegate);

        // When:
        deserializer.resolve(ctx);

        // Then: did not blow up.
    }

    @Test
    void shouldReturnOriginalIfNotParsingFile() throws Exception {
        // When:
        final TestType result = deserializer.deserialize(jp, ctx);

        // Then:
        assertThat(result, is(sameInstance(original)));
    }

    @Test
    void shouldReturnWithLocationIfParsingFile() throws Exception {
        // Given:
        when(location.getLineNr()).thenReturn(-1);
        when(location.contentReference()).thenReturn(content);
        when(content.getRawContent()).thenReturn(new File("/var/some path"));

        // When:
        final TestType result = deserializer.deserialize(jp, ctx);

        // Then:
        verify(original).withLocation(URI.create("file:///var/some%20path"));
        assertThat(result, is(sameInstance(withPath)));
    }

    @Test
    void shouldReturnWithLocationIfParsingFileAndKnownLineNumber() throws Exception {
        // Given:
        when(location.getLineNr()).thenReturn(22);
        when(location.contentReference()).thenReturn(content);
        when(content.getRawContent()).thenReturn(new File("/var/some path/file.yml"));

        // When:
        final TestType result = deserializer.deserialize(jp, ctx);

        // Then:
        verify(original).withLocation(URI.create("file:///var/some%20path/file.yml:22"));
        assertThat(result, is(sameInstance(withPath)));
    }

    private interface TestType extends LocationAware<TestType> {}
}

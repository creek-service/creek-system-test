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

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import org.creek.api.system.test.model.LocationAware;

/** Deserializer that sets the location on a deserialized type */
final class LocationAwareDeserializer<T extends LocationAware<T>> extends StdDeserializer<T>
        implements ResolvableDeserializer {

    private final JsonDeserializer<T> delegate;

    @SuppressWarnings({"unchecked", "rawtypes"})
    LocationAwareDeserializer(final Class<T> type, final JsonDeserializer<?> delegate) {
        super(type);
        this.delegate = (JsonDeserializer) requireNonNull(delegate, "delegate");

        if (!delegate.handledType().equals(type)) {
            throw new IllegalArgumentException(
                    "wrong delegate deserializer type: "
                            + delegate.handledType().getName()
                            + ", expected: "
                            + type.getName());
        }

        if (!LocationAware.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("type not location aware: " + type.getName());
        }
    }

    @Override
    public void resolve(final DeserializationContext ctx) throws JsonMappingException {
        if (delegate instanceof ResolvableDeserializer) {
            ((ResolvableDeserializer) delegate).resolve(ctx);
        }
    }

    @Override
    public T deserialize(final JsonParser jp, final DeserializationContext ctx) throws IOException {
        final JsonLocation location = jp.currentLocation();
        final T t = delegate.deserialize(jp, ctx);
        return setLocation(t, location);
    }

    private T setLocation(final T t, final JsonLocation location) {
        final Object content = location.contentReference().getRawContent();
        if (!(content instanceof File)) {
            return t;
        }

        final URI filePath = ((File) content).toPath().toAbsolutePath().toUri();
        final String lineNumber = location.getLineNr() == -1 ? "" : ":" + location.getLineNr();
        return t.withLocation(URI.create(filePath + lineNumber));
    }
}

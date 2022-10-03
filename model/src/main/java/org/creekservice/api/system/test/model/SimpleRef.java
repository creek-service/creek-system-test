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

import static java.util.Objects.requireNonNull;
import static org.creekservice.api.base.type.Preconditions.requireNonBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.net.URI;
import java.util.Objects;
import org.creekservice.api.system.test.extension.test.model.ExpectationRef;
import org.creekservice.api.system.test.extension.test.model.InputRef;
import org.creekservice.api.system.test.extension.test.model.LocationAware;

/** A simple reference that holds only the location as the value in the yaml. */
public final class SimpleRef implements InputRef, ExpectationRef, LocationAware<SimpleRef> {

    private final String id;
    private final URI location;

    @JsonCreator
    public static SimpleRef simpleRef(final String location) {
        return new SimpleRef(location, UNKNOWN_LOCATION);
    }

    private SimpleRef(final String id, final URI location) {
        this.id = requireNonBlank(id, "id");
        this.location = requireNonNull(location, "location");
    }

    @JsonValue
    @Override
    public String id() {
        return id;
    }

    @Override
    public URI location() {
        return location;
    }

    @Override
    public SimpleRef withLocation(final URI location) {
        return new SimpleRef(id, location);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SimpleRef simpleRef = (SimpleRef) o;
        // location intentionally excluded:
        return Objects.equals(id, simpleRef.id);
    }

    @Override
    public int hashCode() {
        // location intentionally excluded:
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}

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

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.nio.file.Path;
import java.util.Objects;
import org.creek.api.system.test.extension.model.Ref;

/** A simple reference that holds only the location as the value in the yaml. */
public final class SimpleRef implements Ref {

    private final Path location;

    @JsonCreator
    public static SimpleRef simpleRef(final String location) {
        return new SimpleRef(Path.of(location));
    }

    private SimpleRef(final Path location) {
        this.location = requireNonNull(location, "location");
        if (location.toString().isBlank()) {
            throw new IllegalArgumentException("location can not be blank");
        }
    }

    @Override
    public Path location() {
        return location;
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
        return Objects.equals(location, simpleRef.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location);
    }

    @JsonValue
    @Override
    public String toString() {
        return location.toString();
    }
}

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

import static java.util.Objects.requireNonNull;
import static org.creek.api.base.type.Preconditions.requireNonEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/** Definition of a test case. */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class TestCaseDef implements LocationAware<TestCaseDef> {

    private final String name;
    private final String description;
    private final Optional<Disabled> disabled;
    private final URI location;

    @SuppressWarnings("unused") // Invoked via reflection by Jackson
    @JsonCreator
    public static TestCaseDef testCase(
            @JsonProperty(value = "name", required = true) final String name,
            @JsonProperty("description") final Optional<String> description,
            @JsonProperty("disabled") final Optional<Disabled> disabled) {
        return new TestCaseDef(name, description.orElse(""), disabled, UNKNOWN_LOCATION);
    }

    private TestCaseDef(
            final String name,
            final String description,
            final Optional<Disabled> disabled,
            final URI location) {
        this.name = requireNonNull(name, "name");
        this.description = requireNonNull(description, "description");
        this.disabled = requireNonNull(disabled, "disabled");
        this.location = requireNonNull(location, "location");

        requireNonEmpty(name, "empty");
    }

    @JsonGetter("name")
    @JsonPropertyDescription("Name of the test case")
    public String name() {
        return name;
    }

    @JsonGetter("description")
    @JsonPropertyDescription("Optional description")
    public String description() {
        return description;
    }

    @JsonGetter("disabled")
    @JsonPropertyDescription("Optionally disables the test")
    public Optional<Disabled> disabled() {
        return disabled;
    }

    public URI location() {
        return location;
    }

    public TestCaseDef withLocation(final URI location) {
        return new TestCaseDef(name, description, disabled, location);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TestCaseDef testCase = (TestCaseDef) o;
        return Objects.equals(name, testCase.name)
                && Objects.equals(description, testCase.description)
                && Objects.equals(disabled, testCase.disabled)
                && Objects.equals(location, testCase.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, disabled, location);
    }

    @Override
    public String toString() {
        return "TestCaseDef{"
                + "name='"
                + name
                + '\''
                + ", description='"
                + description
                + '\''
                + ", disabled="
                + disabled
                + ", location="
                + location
                + '}';
    }
}

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
import static org.creekservice.api.base.type.Preconditions.requireNonEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Serialisable definition of a test suite. */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class TestSuiteDef implements LocationAware<TestSuiteDef> {

    private final String name;
    private final String description;
    private final Optional<Disabled> disabled;
    private final List<String> services;
    private final List<TestCaseDef> tests;
    private final URI location;

    @JsonCreator
    public static TestSuiteDef testSuite(
            @JsonProperty(value = "name", required = true) final String name,
            @JsonProperty("description") final Optional<String> description,
            @JsonProperty("disabled") final Optional<Disabled> disabled,
            @JsonProperty(value = "services", required = true) final List<String> services,
            @JsonProperty(value = "tests", required = true) final List<TestCaseDef> tests) {
        return new TestSuiteDef(
                name, description.orElse(""), disabled, UNKNOWN_LOCATION, services, tests);
    }

    private TestSuiteDef(
            final String name,
            final String description,
            final Optional<Disabled> disabled,
            final URI location,
            final List<String> services,
            final List<TestCaseDef> tests) {
        this.name = requireNonNull(name, "name");
        this.description = requireNonNull(description, "description");
        this.disabled = requireNonNull(disabled, "disabled");
        this.location = requireNonNull(location, "location");
        this.services = List.copyOf(requireNonNull(services, "services"));
        this.tests = List.copyOf(requireNonNull(tests, "tests"));

        requireNonEmpty(name, "empty");
        requireNonEmpty(services, "services");
        requireNonEmpty(tests, "tests");
    }

    @JsonGetter("name")
    @JsonPropertyDescription("Name of the suite")
    public String name() {
        return name;
    }

    @JsonGetter("description")
    @JsonPropertyDescription("(Optional) description")
    public String description() {
        return description;
    }

    @JsonGetter("disabled")
    @JsonPropertyDescription("(Optional) if present, the test is disabled")
    public Optional<Disabled> disabled() {
        return disabled;
    }

    @JsonGetter("services")
    @JsonPropertyDescription(
            "List of services to start when running the suite. "
                    + "Services are started in the order defined.")
    public List<String> services() {
        return List.copyOf(services);
    }

    @JsonGetter("tests")
    @JsonPropertyDescription(
            "List of test cases the suite contains. Tests are run in the order defined.")
    public List<TestCaseDef> tests() {
        return List.copyOf(tests);
    }

    public URI location() {
        return location;
    }

    public TestSuiteDef withLocation(final URI location) {
        return new TestSuiteDef(name, description, disabled, location, services, tests);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TestSuiteDef testSuiteDef = (TestSuiteDef) o;

        // Note: location intentionally excluded:
        return Objects.equals(name, testSuiteDef.name)
                && Objects.equals(description, testSuiteDef.description)
                && Objects.equals(disabled, testSuiteDef.disabled)
                && Objects.equals(services, testSuiteDef.services)
                && Objects.equals(tests, testSuiteDef.tests);
    }

    @Override
    public int hashCode() {
        // Note: location intentionally excluded:
        return Objects.hash(name, description, disabled, services, tests);
    }

    @Override
    public String toString() {
        return "TestSuiteDef{"
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
                + ", services="
                + services
                + ", tests="
                + tests
                + '}';
    }
}

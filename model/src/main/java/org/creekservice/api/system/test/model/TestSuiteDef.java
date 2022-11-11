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
import org.creekservice.api.system.test.extension.test.model.LocationAware;
import org.creekservice.api.system.test.extension.test.model.Option;

/** Serialisable definition of a test suite. */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class TestSuiteDef implements LocationAware<TestSuiteDef> {

    private final String name;
    private final String notes;
    private final Optional<Disabled> disabled;
    private final List<String> services;
    private final List<Option> options;
    private final List<TestCaseDef> tests;
    private final URI location;

    /**
     * Factory method.
     *
     * @param name the name of the suite
     * @param notes optional notes
     * @param disabled details of why the test is disabled.
     * @param services list of services under test.
     * @param options list of customisation options.
     * @param tests the tests in the suite.
     * @return the suite def.
     */
    @JsonCreator
    public static TestSuiteDef testSuite(
            @JsonProperty(value = "name", required = true) final String name,
            @JsonProperty("notes") final Optional<String> notes,
            @JsonProperty("disabled") final Optional<Disabled> disabled,
            @JsonProperty(value = "services", required = true) final List<String> services,
            @JsonProperty(value = "options") final Optional<List<Option>> options,
            @JsonProperty(value = "tests", required = true) final List<TestCaseDef> tests) {
        return new TestSuiteDef(
                name,
                notes.orElse(""),
                disabled,
                UNKNOWN_LOCATION,
                services,
                options.orElse(List.of()),
                tests);
    }

    private TestSuiteDef(
            final String name,
            final String notes,
            final Optional<Disabled> disabled,
            final URI location,
            final List<String> services,
            final List<Option> options,
            final List<TestCaseDef> tests) {
        this.name = requireNonNull(name, "name");
        this.notes = requireNonNull(notes, "notes");
        this.disabled = requireNonNull(disabled, "disabled");
        this.location = requireNonNull(location, "location");
        this.services = List.copyOf(requireNonNull(services, "services"));
        this.options = List.copyOf(requireNonNull(options, "options"));
        this.tests = List.copyOf(requireNonNull(tests, "tests"));

        requireNonEmpty(name, "empty");
        requireNonEmpty(services, "services");
        requireNonEmpty(tests, "tests");
    }

    /**
     * @return the name of the suite.
     */
    @JsonGetter("name")
    @JsonPropertyDescription("Name of the suite")
    public String name() {
        return name;
    }

    /**
     * @return optional notes.
     */
    @JsonGetter("notes")
    @JsonPropertyDescription("(Optional) notes")
    public String notes() {
        return notes;
    }

    /**
     * @return information on why the test was disabled.
     */
    @JsonGetter("disabled")
    @JsonPropertyDescription("(Optional) if present, the test is disabled")
    public Optional<Disabled> disabled() {
        return disabled;
    }

    /**
     * @return the list of services under test.
     */
    @JsonGetter("services")
    @JsonPropertyDescription(
            "List of services to start when running the suite. "
                    + "Services are started in the order defined.")
    public List<String> services() {
        return List.copyOf(services);
    }

    /**
     * @return the list of test options.
     */
    @JsonGetter("options")
    @JsonPropertyDescription(
            "List of test options that can be used to configure services and test extensions.")
    public List<Option> options() {
        return List.copyOf(options);
    }

    /**
     * @return the list of test cases.
     */
    @JsonGetter("tests")
    @JsonPropertyDescription(
            "List of test cases the suite contains. Tests are run in the order defined.")
    public List<TestCaseDef> tests() {
        return List.copyOf(tests);
    }

    /**
     * @return the location in the test files the instance was loaded from.
     */
    public URI location() {
        return location;
    }

    /**
     * @param location the the location in the test files the instance was loaded from.
     * @return a new instance with the location set.
     */
    public TestSuiteDef withLocation(final URI location) {
        return new TestSuiteDef(name, notes, disabled, location, services, options, tests);
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
                && Objects.equals(notes, testSuiteDef.notes)
                && Objects.equals(disabled, testSuiteDef.disabled)
                && Objects.equals(services, testSuiteDef.services)
                && Objects.equals(options, testSuiteDef.options)
                && Objects.equals(tests, testSuiteDef.tests);
    }

    @Override
    public int hashCode() {
        // Note: location intentionally excluded:
        return Objects.hash(name, notes, disabled, services, options, tests);
    }

    @Override
    public String toString() {
        return "TestSuiteDef{"
                + "name='"
                + name
                + '\''
                + ", notes='"
                + notes
                + '\''
                + ", disabled="
                + disabled
                + ", location="
                + location
                + ", services="
                + services
                + ", options="
                + options
                + ", tests="
                + tests
                + '}';
    }
}

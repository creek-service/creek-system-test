/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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
import org.creekservice.api.system.test.extension.test.model.ExpectationRef;
import org.creekservice.api.system.test.extension.test.model.InputRef;
import org.creekservice.api.system.test.extension.test.model.LocationAware;

/** Serialisable definition of a test case. */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class TestCaseDef implements LocationAware<TestCaseDef> {

    private final String name;
    private final String notes;
    private final Optional<Disabled> disabled;
    private final List<InputRef> inputs;
    private final List<ExpectationRef> expectations;
    private final URI location;

    /**
     * Factory method.
     *
     * @param name the name of the test.
     * @param notes optional notes.
     * @param disabled flag indicating if test is disabled.
     * @param maybeInputs any test inputs.
     * @param expectations test expectations.
     * @return the new definition.
     */
    @SuppressWarnings("unused") // Invoked via reflection by Jackson
    @JsonCreator
    public static TestCaseDef testCase(
            @JsonProperty(value = "name", required = true) final String name,
            @JsonProperty("notes") final Optional<String> notes,
            @JsonProperty("disabled") final Optional<Disabled> disabled,
            @JsonProperty("inputs") final Optional<? extends List<? extends InputRef>> maybeInputs,
            @JsonProperty(value = "expectations", required = true)
                    final List<? extends ExpectationRef> expectations) {
        final List<? extends InputRef> inputs =
                maybeInputs.isPresent() ? maybeInputs.get() : List.of();
        return new TestCaseDef(
                name, notes.orElse(""), disabled, UNKNOWN_LOCATION, inputs, expectations);
    }

    private TestCaseDef(
            final String name,
            final String notes,
            final Optional<Disabled> disabled,
            final URI location,
            final List<? extends InputRef> inputs,
            final List<? extends ExpectationRef> expectations) {
        this.name = requireNonNull(name, "name");
        this.notes = requireNonNull(notes, "notes");
        this.disabled = requireNonNull(disabled, "disabled");
        this.location = requireNonNull(location, "location");
        this.inputs = List.copyOf(requireNonNull(inputs, "inputs"));
        this.expectations = List.copyOf(requireNonNull(expectations, "expectations"));

        requireNonEmpty(name, "empty");
        requireNonEmpty(expectations, "expectations");
    }

    /**
     * @return the name of the test.
     */
    @JsonGetter("name")
    @JsonPropertyDescription("Name of the test case")
    public String name() {
        return name;
    }

    /**
     * @return any notes.
     */
    @JsonGetter("notes")
    @JsonPropertyDescription("Optional notes")
    public String notes() {
        return notes;
    }

    /**
     * @return optional that will be present if the test is disabled.
     */
    @JsonGetter("disabled")
    @JsonPropertyDescription("(Optional) if present, the test is disabled")
    public Optional<Disabled> disabled() {
        return disabled;
    }

    /**
     * @return test inputs.
     */
    @JsonGetter("inputs")
    @JsonPropertyDescription(
            "(Optional) list of inputs to feed into the system before asserting expectations")
    public List<InputRef> inputs() {
        return List.copyOf(inputs);
    }

    /**
     * @return test expectations.
     */
    @JsonGetter("expectations")
    @JsonPropertyDescription("List of expectations to assert once inputs are processed")
    public List<ExpectationRef> expectations() {
        return List.copyOf(expectations);
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
    public TestCaseDef withLocation(final URI location) {
        return new TestCaseDef(name, notes, disabled, location, inputs, expectations);
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

        // Note: location intentionally excluded:
        return Objects.equals(name, testCase.name)
                && Objects.equals(notes, testCase.notes)
                && Objects.equals(disabled, testCase.disabled)
                && Objects.equals(inputs, testCase.inputs)
                && Objects.equals(expectations, testCase.expectations);
    }

    @Override
    public int hashCode() {
        // Note: location intentionally excluded:
        return Objects.hash(name, notes, disabled, inputs, expectations);
    }

    @Override
    public String toString() {
        return "TestCaseDef{"
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
                + ", inputs="
                + inputs
                + ", expectations="
                + expectations
                + '}';
    }
}

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

package org.creekservice.api.system.test.model;

import static java.util.Objects.requireNonNull;
import static org.creekservice.api.base.type.Preconditions.requireEqual;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import org.creekservice.api.system.test.extension.test.model.CreekTestCase;
import org.creekservice.api.system.test.extension.test.model.Expectation;
import org.creekservice.api.system.test.extension.test.model.Input;

/** A single test case. */
public final class TestCase implements CreekTestCase {

    private final TestCaseDef def;
    private final TestSuite suite;
    private final List<Input> inputs;
    private final List<Expectation> expectations;

    /**
     * Factory method
     *
     * @param inputs the test inputs.
     * @param expectations the test outputs.
     * @param def the test definition.
     * @return the test case instance.
     */
    public static Builder testCase(
            final Collection<Input> inputs,
            final Collection<Expectation> expectations,
            final TestCaseDef def) {
        return new Builder(inputs, expectations, def);
    }

    private TestCase(
            final Collection<Input> inputs,
            final Collection<Expectation> expectations,
            final TestCaseDef def,
            final TestSuite suite) {
        this.def = requireNonNull(def, "def");
        this.suite = requireNonNull(suite, "suite");
        this.inputs = List.copyOf(requireNonNull(inputs, "inputs"));
        this.expectations = List.copyOf(requireNonNull(expectations, "expectations"));

        requireEqual(inputs.size(), def.inputs().size(), "inputs size mismatch");
        requireEqual(expectations.size(), def.expectations().size(), "expectations size mismatch");
    }

    @Override
    public String name() {
        return def.name();
    }

    @Override
    public URI location() {
        return def.location();
    }

    @Override
    public TestSuite suite() {
        return suite;
    }

    @Override
    public List<Input> inputs() {
        return List.copyOf(inputs);
    }

    @Override
    public List<Expectation> expectations() {
        return List.copyOf(expectations);
    }

    /**
     * @return {@code true} if the test is disabled.
     */
    public boolean disabled() {
        return def.disabled().isPresent();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TestCase testCase = (TestCase) o;

        // Note suite deliberately excluded to avoid stack overflow.
        return Objects.equals(def, testCase.def)
                && Objects.equals(inputs, testCase.inputs)
                && Objects.equals(expectations, testCase.expectations);
    }

    @Override
    public int hashCode() {
        // Note suite deliberately excluded to avoid stack overflow.
        return Objects.hash(def, inputs, expectations);
    }

    @Override
    public String toString() {
        // Note suite deliberately excluded to avoid stack overflow.
        return "TestCase{"
                + "def="
                + def
                + ", inputs="
                + inputs
                + ", expectations="
                + expectations
                + '}';
    }

    /** Test case builder. */
    public static final class Builder {

        private final TestCaseDef def;
        private final Collection<Input> inputs;
        private final Collection<Expectation> expectations;

        private Builder(
                final Collection<Input> inputs,
                final Collection<Expectation> expectations,
                final TestCaseDef def) {
            this.inputs = requireNonNull(inputs, "inputs");
            this.expectations = requireNonNull(expectations, "expectations");
            this.def = requireNonNull(def, "def");
        }

        /**
         * Build the test case.
         *
         * @param suite the test suite the test case belongs to.
         * @return the test case.
         */
        public TestCase build(final TestSuite suite) {
            return new TestCase(inputs, expectations, def, suite);
        }
    }
}

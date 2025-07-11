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

package org.creekservice.api.system.test.model;

import static java.util.Objects.requireNonNull;
import static org.creekservice.api.base.type.Preconditions.requireEqual;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.model.Option;

/** A suite of test cases */
public final class TestSuite implements CreekTestSuite {

    private final TestSuiteDef def;
    private final TestPackage pkg;
    private final List<TestCase> tests;

    /**
     * Factory method
     *
     * @param tests the tests in the suite.
     * @param def the suite definition.
     * @return the suite builder.
     */
    public static Builder testSuite(
            final Collection<TestCase.Builder> tests, final TestSuiteDef def) {
        return new Builder(tests, def);
    }

    private TestSuite(
            final Collection<TestCase.Builder> tests,
            final TestSuiteDef def,
            final TestPackage pkg) {
        this.def = requireNonNull(def, "def");
        this.pkg = requireNonNull(pkg, "pkg");
        this.tests =
                requireNonNull(tests, "tests").stream()
                        .map(builder -> builder.build(this))
                        .collect(Collectors.toUnmodifiableList());

        requireEqual(tests.size(), def.tests().size(), "test case size mismatch");
    }

    /**
     * @return the name of the suite.
     */
    @Override
    public String name() {
        return def.name();
    }

    /**
     * @return the location in the test files the instance was loaded from.
     */
    @Override
    public URI location() {
        return def.location();
    }

    /**
     * @return the list of services under test.
     */
    @Override
    public List<String> services() {
        return def.services();
    }

    /**
     * Retrieve any options that are of the supplied {@code type}, or subtypes of.
     *
     * @param type the option type to look up.
     * @param <T> the option type to look up.
     * @return the list of options.
     */
    @Override
    public <T extends Option> List<T> options(final Class<T> type) {
        return def.options().stream()
                .filter(o -> type.isAssignableFrom(o.getClass()))
                .map(type::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<TestCase> tests() {
        return List.copyOf(tests);
    }

    /**
     * @return the package the test belongs to.
     */
    public TestPackage pkg() {
        return pkg;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TestSuite testSuite = (TestSuite) o;
        // Note pkg deliberately excluded to avoid stack overflow.
        return Objects.equals(def, testSuite.def) && Objects.equals(tests, testSuite.tests);
    }

    @Override
    public int hashCode() {
        // Note pkg deliberately excluded to avoid stack overflow.
        return Objects.hash(def, tests);
    }

    @Override
    public String toString() {
        // Note pkg deliberately excluded to avoid stack overflow.
        return "TestSuite{def=" + def + ", tests=" + tests + '}';
    }

    /** Test suite builder */
    public static final class Builder {

        private final TestSuiteDef def;
        private final Collection<TestCase.Builder> tests;

        private Builder(final Collection<TestCase.Builder> tests, final TestSuiteDef def) {
            this.tests = requireNonNull(tests, "tests");
            this.def = requireNonNull(def, "def");
        }

        /**
         * Build the test suite.
         *
         * @param pkg the test package the suite belongs to.
         * @return the test suite.
         */
        public TestSuite build(final TestPackage pkg) {
            return new TestSuite(tests, def, pkg);
        }
    }
}

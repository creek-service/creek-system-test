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
import static org.creek.api.base.type.Preconditions.requireEqual;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** A suite of test cases */
public final class TestSuite {

    private final TestSuiteDef def;
    private final TestPackage pkg;
    private final List<TestCase> tests;

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

    public TestSuiteDef def() {
        return def;
    }

    public TestPackage pkg() {
        return pkg;
    }

    public List<TestCase> tests() {
        return List.copyOf(tests);
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

    public static final class Builder {

        private final TestSuiteDef def;
        private final Collection<TestCase.Builder> tests;

        private Builder(final Collection<TestCase.Builder> tests, final TestSuiteDef def) {
            this.tests = requireNonNull(tests, "tests");
            this.def = requireNonNull(def, "def");
        }

        public TestSuite build(final TestPackage pkg) {
            return new TestSuite(tests, def, pkg);
        }
    }
}

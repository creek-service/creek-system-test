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

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.creekservice.api.system.test.extension.test.model.Input;

/** A package to seed data and test suites. */
public final class TestPackage {

    private final List<Input> seedData;
    private final List<TestSuite> suites;

    /**
     * Factory method
     *
     * @param seedData any seed data to play in before each suite.
     * @param suites the test suites.
     * @return the test package.
     */
    public static TestPackage testPackage(
            final Collection<Input> seedData, final Collection<TestSuite.Builder> suites) {
        return new TestPackage(seedData, suites);
    }

    private TestPackage(
            final Collection<Input> seedData, final Collection<TestSuite.Builder> suites) {
        this.seedData = List.copyOf(requireNonNull(seedData, "seedData"));
        this.suites =
                requireNonEmpty(suites, "suites").stream()
                        .map(builder -> builder.build(this))
                        .collect(Collectors.toUnmodifiableList());
    }

    /**
     * @return any seed data to play in before each suite.
     */
    public List<Input> seedData() {
        return List.copyOf(seedData);
    }

    /**
     * @return the test suites in the package.
     */
    public List<TestSuite> suites() {
        return List.copyOf(suites);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TestPackage that = (TestPackage) o;
        return Objects.equals(seedData, that.seedData) && Objects.equals(suites, that.suites);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seedData, suites);
    }

    @Override
    public String toString() {
        return "TestPackage{" + ", seedData=" + seedData + ", suites=" + suites + '}';
    }
}

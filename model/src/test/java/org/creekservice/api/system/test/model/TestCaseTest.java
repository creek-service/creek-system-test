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

import static org.creekservice.api.system.test.model.Disabled.disabled;
import static org.creekservice.api.system.test.model.TestCase.testCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.testing.EqualsTester;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.creekservice.api.system.test.extension.test.model.Expectation;
import org.creekservice.api.system.test.extension.test.model.ExpectationRef;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.api.system.test.extension.test.model.InputRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestCaseTest {

    @Mock private TestCaseDef def;
    @Mock private TestSuite suite;
    @Mock private InputRef inputRef;
    @Mock private ExpectationRef expectationRef;
    @Mock private Input input;
    @Mock private Expectation expectation;

    @Test
    void shouldImplementHashCodeAndEquals() {
        final Collection<Input> inputs = List.of(input);
        final Collection<Expectation> expectations = List.of(expectation);

        final Collection<Input> diffInputs = List.of();
        final List<Expectation> diffExpectations = List.of(expectation, expectation);

        new EqualsTester()
                .addEqualityGroup(
                        testCase(inputs, expectations, def(inputs, expectations)).build(suite),
                        testCase(inputs, expectations, def(inputs, expectations)).build(suite),
                        testCase(inputs, expectations, def(inputs, expectations))
                                .build(mock(TestSuite.class)))
                .addEqualityGroup(
                        testCase(diffInputs, expectations, def(diffInputs, expectations))
                                .build(suite))
                .addEqualityGroup(
                        testCase(inputs, diffExpectations, def(inputs, diffExpectations))
                                .build(suite))
                .addEqualityGroup(
                        testCase(List.of(), List.of(), def(List.of(), List.of())).build(suite))
                .addEqualityGroup(
                        testCase(List.of(), List.of(), mock(TestCaseDef.class)).build(suite))
                .testEquals();
    }

    @Test
    void shouldThrowIfInputSizeDiffersFromDef() {
        // Given:
        final TestCase.Builder builder = testCase(List.of(input), List.of(expectation), def);
        givenDefInputs(0);

        // When:
        final Exception e =
                assertThrows(IllegalArgumentException.class, () -> builder.build(suite));

        // Then:
        assertThat(e.getMessage(), containsString("inputs size mismatch 1 != 0"));
    }

    @Test
    void shouldThrowIfExpectationSizeDiffersFromDef() {
        // Given:
        final TestCase.Builder builder = testCase(List.of(input), List.of(expectation), def);
        givenDefInputs(1);
        givenDefExpectations(2);

        // When:
        final Exception e =
                assertThrows(IllegalArgumentException.class, () -> builder.build(suite));

        // Then:
        assertThat(e.getMessage(), containsString("expectations size mismatch 1 != 2"));
    }

    @Test
    void shouldSetTestSuiteOnBuild() {
        // Given:
        final TestCase.Builder builder = builder(List.of(input), List.of(expectation));

        // When:
        final TestCase testCase = builder.build(suite);

        // Then:
        assertThat(testCase.suite(), is(sameInstance(suite)));
    }

    @Test
    void shouldReturnTestName() {
        // Given:
        final TestCase testCase = builder(List.of(input), List.of(expectation)).build(suite);
        when(def.name()).thenReturn("Bob");

        // Then:
        assertThat(testCase.name(), is("Bob"));
    }

    @Test
    void shouldReturnLocation() {
        // Given:
        final TestCase testCase = builder(List.of(input), List.of(expectation)).build(suite);
        final URI location = URI.create("file://some.location:24");
        when(def.location()).thenReturn(location);

        // Then:
        assertThat(testCase.location(), is(location));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void shouldReturnImmutableInputs() {
        // Given:
        final List<Input> inputs =
                builder(List.of(input), List.of(expectation)).build(suite).inputs();

        // Then:
        assertThrows(UnsupportedOperationException.class, () -> inputs.remove(0));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void shouldReturnImmutableExpectations() {
        // Given:
        final List<Expectation> expectations =
                builder(List.of(input), List.of(expectation)).build(suite).expectations();

        // Then:
        assertThrows(UnsupportedOperationException.class, () -> expectations.remove(0));
    }

    @Test
    void shouldExposeDisabled() {
        // Given:
        final TestCase testCase = builder(List.of(input), List.of(expectation)).build(suite);
        when(def.disabled()).thenReturn(Optional.empty());

        // Then:
        assertThat(testCase.disabled(), is(false));

        // Given:
        when(def.disabled()).thenReturn(Optional.of(disabled("reason", Optional.empty())));

        // Then:
        assertThat(testCase.disabled(), is(true));
    }

    private TestCaseDef def(
            final Collection<Input> inputs, final Collection<Expectation> expectations) {
        givenDefInputs(inputs.size());
        givenDefExpectations(expectations.size());
        return def;
    }

    private TestCase.Builder builder(
            final List<Input> inputs, final List<Expectation> expectations) {
        return testCase(inputs, expectations, def(inputs, expectations));
    }

    private void givenDefInputs(final int inputs) {
        final List<InputRef> inputDefs =
                IntStream.range(0, inputs).mapToObj(i -> inputRef).collect(Collectors.toList());
        when(def.inputs()).thenReturn(inputDefs);
    }

    private void givenDefExpectations(final int expectations) {
        final List<ExpectationRef> expectationDefs =
                IntStream.range(0, expectations)
                        .mapToObj(i -> expectationRef)
                        .collect(Collectors.toList());
        when(def.expectations()).thenReturn(expectationDefs);
    }
}

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

package org.creekservice.api.system.test.parser;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.google.common.testing.EqualsTester;
import org.creekservice.api.system.test.extension.test.model.Expectation;
import org.creekservice.api.system.test.extension.test.model.ExpectationRef;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.api.system.test.extension.test.model.InputRef;
import org.junit.jupiter.api.Test;

class ModelTypeTest {

    @Test
    void shouldImplementHashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(
                        ModelType.input(TestInput.class), ModelType.input(TestInput.class))
                .addEqualityGroup(
                        ModelType.input(TestInput.class, "test"),
                        ModelType.input(TestInput.class, "test"))
                .addEqualityGroup(ModelType.input(mock(TestInput.class).getClass()))
                .addEqualityGroup(ModelType.expectation(mock(Expectation.class).getClass()))
                .addEqualityGroup(ModelType.expectation(mock(Expectation.class).getClass(), "diff"))
                .testEquals();
    }

    @Test
    void shouldCreateRefWithDerivedNaming() {
        // When:
        final ModelType<TestRef> result = ModelType.ref(TestRef.class);

        // Then:
        assertThat(result.name(), is(empty()));
        assertThat(result.type(), is(TestRef.class));
    }

    @Test
    void shouldCreateRefWithExplicitNaming() {
        // When:
        final ModelType<TestRef> result = ModelType.ref(TestRef.class, "explicit_name");

        // Then:
        assertThat(result.name(), is(of("explicit_name")));
        assertThat(result.type(), is(TestRef.class));
    }

    @Test
    void shouldCreateInputRefWithDerivedNaming() {
        // When:
        final ModelType<TestInputRef> result = ModelType.inputRef(TestInputRef.class);

        // Then:
        assertThat(result.name(), is(empty()));
        assertThat(result.type(), is(TestInputRef.class));
    }

    @Test
    void shouldCreateInputRefWithExplicitNaming() {
        // When:
        final ModelType<TestInputRef> result =
                ModelType.inputRef(TestInputRef.class, "explicit_name");

        // Then:
        assertThat(result.name(), is(of("explicit_name")));
        assertThat(result.type(), is(TestInputRef.class));
    }

    @Test
    void shouldCreateExpectationRefWithDerivedNaming() {
        // When:
        final ModelType<TestExpectationRef> result =
                ModelType.expectationRef(TestExpectationRef.class);

        // Then:
        assertThat(result.name(), is(empty()));
        assertThat(result.type(), is(TestExpectationRef.class));
    }

    @Test
    void shouldCreateExpectationRefWithExplicitNaming() {
        // When:
        final ModelType<TestExpectationRef> result =
                ModelType.expectationRef(TestExpectationRef.class, "explicit_name");

        // Then:
        assertThat(result.name(), is(of("explicit_name")));
        assertThat(result.type(), is(TestExpectationRef.class));
    }

    @Test
    void shouldCreateInputWithDerivedNaming() {
        // When:
        final ModelType<TestInput> result = ModelType.input(TestInput.class);

        // Then:
        assertThat(result.name(), is(empty()));
        assertThat(result.type(), is(TestInput.class));
    }

    @Test
    void shouldCreateInputWithExplicitNaming() {
        // When:
        final ModelType<TestInput> result = ModelType.input(TestInput.class, "explicit_name");

        // Then:
        assertThat(result.name(), is(of("explicit_name")));
        assertThat(result.type(), is(TestInput.class));
    }

    @Test
    void shouldCreateExpectationWithDerivedNaming() {
        // When:
        final ModelType<TestExpectation> result = ModelType.expectation(TestExpectation.class);

        // Then:
        assertThat(result.name(), is(empty()));
        assertThat(result.type(), is(TestExpectation.class));
    }

    @Test
    void shouldCreateExpectationWithExplicitNaming() {
        // When:
        final ModelType<TestExpectation> result =
                ModelType.expectation(TestExpectation.class, "explicit_name");

        // Then:
        assertThat(result.name(), is(of("explicit_name")));
        assertThat(result.type(), is(TestExpectation.class));
    }

    @Test
    void shouldThrowOnIfNotSubtype() {
        assertThrows(IllegalArgumentException.class, () -> ModelType.input(Input.class));
        assertThrows(
                IllegalArgumentException.class, () -> ModelType.input(Input.class, "explicit"));
        assertThrows(
                IllegalArgumentException.class,
                () -> ModelType.expectation(Expectation.class, "explicit"));
    }

    @Test
    void shouldThrowOnEmptyExplicit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ModelType.expectation(TestExpectation.class, " "));
    }

    @Test
    void shouldThrowOnAnonymousTypes() {
        // Given:
        final Input anonymousType = new Input() {};

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> ModelType.input(anonymousType.getClass()));

        // Then:
        assertThat(
                e.getMessage(),
                is("Anonymous/synthetic types are not supported: " + anonymousType.getClass()));
    }

    @Test
    void shouldThrowOnSyntheticTypes() {
        // Given:
        final TestRef lambda = () -> "some location";

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class, () -> ModelType.ref(lambda.getClass()));

        // Then:
        assertThat(
                e.getMessage(),
                is("Anonymous/synthetic types are not supported: " + lambda.getClass()));
    }

    private interface TestRef extends InputRef, ExpectationRef {}

    private interface TestInputRef extends InputRef {}

    private interface TestExpectationRef extends ExpectationRef {}

    private interface TestInput extends Input {}

    private interface TestExpectation extends Expectation {}
}

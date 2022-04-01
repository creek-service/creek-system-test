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

package org.creek.system.test.extension.api.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

class ModelTypeTest {

    @Test
    void shouldImplementHashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(
                        ModelType.seed(TestSeed.class), ModelType.seed(TestSeed.class, "test"))
                .addEqualityGroup(ModelType.seed(mock(Seed.class).getClass()))
                .addEqualityGroup(ModelType.seed(mock(Seed.class).getClass(), "diff"))
                .testEquals();
    }

    @Test
    void shouldCreateSeedWithDerivedNaming() {
        // When:
        final ModelType<TestSeed> result = ModelType.seed(TestSeed.class);

        // Then:
        assertThat(result.name(), is("test"));
        assertThat(result.type(), is(TestSeed.class));
    }

    @Test
    void shouldCreateSeedWithExplicitNaming() {
        // When:
        final ModelType<TestSeed> result = ModelType.seed(TestSeed.class, "explicit_name");

        // Then:
        assertThat(result.name(), is("explicit_name"));
        assertThat(result.type(), is(TestSeed.class));
    }

    @Test
    void shouldCreateInputWithDerivedNaming() {
        // When:
        final ModelType<TestInput> result = ModelType.input(TestInput.class);

        // Then:
        assertThat(result.name(), is("test"));
        assertThat(result.type(), is(TestInput.class));
    }

    @Test
    void shouldCreateInputWithExplicitNaming() {
        // When:
        final ModelType<TestInput> result = ModelType.input(TestInput.class, "explicit_name");

        // Then:
        assertThat(result.name(), is("explicit_name"));
        assertThat(result.type(), is(TestInput.class));
    }

    @Test
    void shouldCreateExpectationWithDerivedNaming() {
        // When:
        final ModelType<TestExpectation> result = ModelType.expectation(TestExpectation.class);

        // Then:
        assertThat(result.name(), is("test"));
        assertThat(result.type(), is(TestExpectation.class));
    }

    @Test
    void shouldCreateExpectationWithExplicitNaming() {
        // When:
        final ModelType<TestExpectation> result =
                ModelType.expectation(TestExpectation.class, "explicit_name");

        // Then:
        assertThat(result.name(), is("explicit_name"));
        assertThat(result.type(), is(TestExpectation.class));
    }

    @Test
    void shouldThrowOnIfNotSubtype() {
        assertThrows(IllegalArgumentException.class, () -> ModelType.seed(Seed.class));
        assertThrows(IllegalArgumentException.class, () -> ModelType.seed(Seed.class, "explicit"));
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
    void shouldDeriveNameOfClassStartingWithLowerCaseLetter() {
        // When:
        final ModelType<?> result = ModelType.expectation(lowerExpectation.class);

        // Then:
        assertThat(result.name(), is("lower"));
    }

    @Test
    void shouldDeriveNameOfNonPostFixedType() {
        // When:
        final ModelType<?> result = ModelType.expectation(Unique.class);

        // Then:
        assertThat(result.name(), is("unique"));
    }

    @Test
    void shouldSplitDerivedNamePartsWithUnderscore() {
        // When:
        final ModelType<?> result = ModelType.expectation(MoreComplexExpectation.class);

        // Then:
        assertThat(result.name(), is("more_complex"));
    }

    private interface TestSeed extends Seed {}

    private interface TestInput extends Input {}

    private interface TestExpectation extends Expectation {}

    @SuppressWarnings("checkstyle:TypeName")
    private interface lowerExpectation extends Expectation {}

    private interface MoreComplexExpectation extends Expectation {}

    private interface Unique extends Expectation {}
}

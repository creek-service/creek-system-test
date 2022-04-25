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

package org.creek.api.system.test.extension.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.google.common.testing.EqualsTester;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ModelTypeTest {

    @Test
    void shouldImplementHashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(
                        ModelType.seed(TestSeed.class), ModelType.seed(TestSeed.class, "test"))
                .addEqualityGroup(ModelType.input(mock(TestInput.class).getClass()))
                .addEqualityGroup(ModelType.seed(mock(Seed.class).getClass()))
                .addEqualityGroup(ModelType.seed(mock(Seed.class).getClass(), "diff"))
                .testEquals();
    }

    @Test
    void shouldCreateRefWithDerivedNaming() {
        // When:
        final ModelType<TestRef> result = ModelType.ref(TestRef.class);

        // Then:
        assertThat(result.name(), is("test"));
        assertThat(result.type(), is(TestRef.class));
    }

    @Test
    void shouldCreateRefWithExplicitNaming() {
        // When:
        final ModelType<TestRef> result = ModelType.ref(TestRef.class, "explicit_name");

        // Then:
        assertThat(result.name(), is("explicit_name"));
        assertThat(result.type(), is(TestRef.class));
    }

    @Test
    void shouldCreateInputRefWithDerivedNaming() {
        // When:
        final ModelType<TestInputRef> result = ModelType.inputRef(TestInputRef.class);

        // Then:
        assertThat(result.name(), is("test"));
        assertThat(result.type(), is(TestInputRef.class));
    }

    @Test
    void shouldCreateInputRefWithExplicitNaming() {
        // When:
        final ModelType<TestInputRef> result =
                ModelType.inputRef(TestInputRef.class, "explicit_name");

        // Then:
        assertThat(result.name(), is("explicit_name"));
        assertThat(result.type(), is(TestInputRef.class));
    }

    @Test
    void shouldCreateExpectationRefWithDerivedNaming() {
        // When:
        final ModelType<TestExpectationRef> result =
                ModelType.expectationRef(TestExpectationRef.class);

        // Then:
        assertThat(result.name(), is("test"));
        assertThat(result.type(), is(TestExpectationRef.class));
    }

    @Test
    void shouldCreateExpectationRefWithExplicitNaming() {
        // When:
        final ModelType<TestExpectationRef> result =
                ModelType.expectationRef(TestExpectationRef.class, "explicit_name");

        // Then:
        assertThat(result.name(), is("explicit_name"));
        assertThat(result.type(), is(TestExpectationRef.class));
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
        final Ref lambda = () -> Paths.get("some location");

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class, () -> ModelType.ref(lambda.getClass()));

        // Then:
        assertThat(
                e.getMessage(),
                is("Anonymous/synthetic types are not supported: " + lambda.getClass()));
    }

    private interface TestRef extends Ref {}

    private interface TestInputRef extends InputRef {}

    private interface TestExpectationRef extends ExpectationRef {}

    private interface TestSeed extends Seed {}

    private interface TestInput extends Input {}

    private interface TestExpectation extends Expectation {}

    @SuppressWarnings("checkstyle:TypeName")
    private interface lowerExpectation extends Expectation {}

    private interface MoreComplexExpectation extends Expectation {}

    private interface Unique extends Expectation {}
}

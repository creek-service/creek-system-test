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

package org.creekservice.internal.system.test.executor.api.test.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.Mockito.mock;

import com.google.common.testing.NullPointerTester;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.system.test.extension.test.model.Expectation;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler;
import org.creekservice.api.system.test.extension.test.model.ExpectationRef;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.api.system.test.extension.test.model.InputHandler;
import org.creekservice.api.system.test.extension.test.model.InputRef;
import org.creekservice.api.system.test.parser.ModelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestModelTest {

    private TestModel model;
    @Mock private InputHandler<TestInput> inputHandler;
    @Mock private ExpectationHandler<TestExpectation> expectationHandler;

    @BeforeEach
    void setUp() {
        model = new TestModel();
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester =
                new NullPointerTester().setDefault(Class.class, DerivedFromEverything.class);
        tester.testAllPublicConstructors(TestModel.class);
        tester.testAllPublicStaticMethods(TestModel.class);
        tester.testAllPublicInstanceMethods(model);
    }

    @Test
    void shouldStartEmpty() {
        assertThat(model.modelTypes(), is(empty()));
        assertThat(model.hasType(TestRef.class), is(false));
        assertThat(model.inputHandler(TestInput.class), is(Optional.empty()));
        assertThat(model.expectationHandler(TestExpectation.class), is(Optional.empty()));
    }

    @Test
    void shouldAddRef() {
        // When:
        model.addRef(TestRef.class);

        // Then:
        assertThat(model.modelTypes(), contains(ModelType.ref(TestRef.class)));
        assertThat(model.hasType(TestRef.class), is(true));
    }

    @Test
    void shouldAddNamedRef() {
        // When:
        model.addRef(TestRef.class).withName("Bob");

        // Then:
        assertThat(model.modelTypes(), contains(ModelType.ref(TestRef.class, "Bob")));
    }

    @Test
    void shouldAddInputRef() {
        // When:
        model.addInputRef(TestInputRef.class);

        // Then:
        assertThat(model.modelTypes(), contains(ModelType.inputRef(TestInputRef.class)));
        assertThat(model.hasType(TestInputRef.class), is(true));
    }

    @Test
    void shouldAddNamedInputRef() {
        // When:
        model.addInputRef(TestInputRef.class).withName("Bob");

        // Then:
        assertThat(model.modelTypes(), contains(ModelType.inputRef(TestInputRef.class, "Bob")));
    }

    @Test
    void shouldAddExpectationRef() {
        // When:
        model.addExpectationRef(TestExpectationRef.class);

        // Then:
        assertThat(
                model.modelTypes(), contains(ModelType.expectationRef(TestExpectationRef.class)));
        assertThat(model.hasType(TestExpectationRef.class), is(true));
    }

    @Test
    void shouldAddNamedExpectationRef() {
        // When:
        model.addExpectationRef(TestExpectationRef.class).withName("Bob");

        // Then:
        assertThat(
                model.modelTypes(),
                contains(ModelType.expectationRef(TestExpectationRef.class, "Bob")));
    }

    @Test
    void shouldAddInput() {
        // When:
        model.addInput(TestInput.class, inputHandler);

        // Then:
        assertThat(model.modelTypes(), contains(ModelType.input(TestInput.class)));
        assertThat(model.hasType(TestInput.class), is(true));
        assertThat(model.inputHandler(TestInput.class), is(Optional.of(inputHandler)));
    }

    @Test
    void shouldAddNamedInput() {
        // When:
        model.addInput(TestInput.class, inputHandler).withName("Bob");

        // Then:
        assertThat(model.modelTypes(), contains(ModelType.input(TestInput.class, "Bob")));
    }

    @Test
    void shouldAddExpectation() {
        // When:
        model.addExpectation(TestExpectation.class, expectationHandler);

        // Then:
        assertThat(model.modelTypes(), contains(ModelType.expectation(TestExpectation.class)));
        assertThat(model.hasType(TestExpectation.class), is(true));
        assertThat(
                model.expectationHandler(TestExpectation.class),
                is(Optional.of(expectationHandler)));
    }

    @Test
    void shouldAddNamedExpectation() {
        // When:
        model.addExpectation(TestExpectation.class, expectationHandler).withName("Bob");

        // Then:
        assertThat(
                model.modelTypes(), contains(ModelType.expectation(TestExpectation.class, "Bob")));
    }

    @Test
    void shouldThrowOnDuplicateAdd() {
        // Given:
        model.addInputRef(TestRef.class);

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> model.addExpectationRef(TestRef.class));

        // Then:
        assertThat(e.getMessage(), is("duplicate type: " + TestRef.class.getName()));
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldThrowIfWrongThread(final String ignored, final Consumer<TestModel> method) {
        // Given:
        model = new TestModel(Thread.currentThread().getId() + 1);

        // Then:
        assertThrows(ConcurrentModificationException.class, () -> method.accept(model));
    }

    @Test
    void shouldHaveThreadingTestForEachPublicMethod() {
        final List<String> publicMethodNames = publicMethodNames();
        final int testedMethodCount = (int) publicMethods().count();
        assertThat(
                "Public methods:\n" + String.join(System.lineSeparator(), publicMethodNames),
                testedMethodCount,
                is(publicMethodNames.size()));
    }

    @SuppressWarnings("unchecked")
    public static Stream<Arguments> publicMethods() {
        return Stream.of(
                Arguments.of("modelTypes", (Consumer<TestModel>) TestModel::modelTypes),
                Arguments.of("hasType", (Consumer<TestModel>) m -> m.hasType(TestRef.class)),
                Arguments.of("addRef", (Consumer<TestModel>) m -> m.addRef(TestRef.class)),
                Arguments.of(
                        "addInputRef",
                        (Consumer<TestModel>) m -> m.addInputRef(TestInputRef.class)),
                Arguments.of(
                        "addExpectationRef",
                        (Consumer<TestModel>) m -> m.addExpectationRef(TestExpectationRef.class)),
                Arguments.of(
                        "addInput",
                        (Consumer<TestModel>)
                                m -> m.addInput(TestInput.class, mock(InputHandler.class))),
                Arguments.of(
                        "addExpectation",
                        (Consumer<TestModel>)
                                m ->
                                        m.addExpectation(
                                                TestExpectation.class,
                                                mock(ExpectationHandler.class))),
                Arguments.of(
                        "inputHandler", (Consumer<TestModel>) m -> m.inputHandler(TestInput.class)),
                Arguments.of(
                        "expectationHandler",
                        (Consumer<TestModel>) m -> m.expectationHandler(TestExpectation.class)));
    }

    private List<String> publicMethodNames() {
        return Arrays.stream(TestModel.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());
    }

    private static final class TestRef implements InputRef, ExpectationRef {
        @Override
        public String id() {
            return null;
        }
    }

    private static final class TestInputRef implements InputRef {
        @Override
        public String id() {
            return null;
        }
    }

    private static final class TestExpectationRef implements ExpectationRef {
        @Override
        public String id() {
            return null;
        }
    }

    private static final class TestInput implements Input {}

    private static final class TestExpectation implements Expectation {}

    private static final class DerivedFromEverything
            implements InputRef, ExpectationRef, Input, Expectation {
        @Override
        public String id() {
            return null;
        }
    }
}

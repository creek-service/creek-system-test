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

package org.creekservice.internal.system.test.executor.api.test.env.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.Mockito.mock;

import com.google.common.testing.NullPointerTester;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestListenersTest {

    @Mock private TestEnvironmentListener listener1;
    @Mock private TestEnvironmentListener listener2;
    private TestListeners listeners;

    @BeforeEach
    void setUp() {
        listeners = new TestListeners();
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(TestListeners.class);
        tester.testAllPublicStaticMethods(TestListeners.class);
        tester.testAllPublicInstanceMethods(listeners);
    }

    @Test
    void shouldAppend() {
        // Given:
        listeners.append(listener1);

        // When:
        listeners.append(listener2);

        // Then:
        assertThat(listeners, contains(listener1, listener2));
    }

    @Test
    void shouldIterateInOrder() {
        // Given:
        listeners.append(listener1);
        listeners.append(listener2);
        final List<TestEnvironmentListener> result = new ArrayList<>(2);

        // When:
        listeners.iterator().forEachRemaining(result::add);

        // Then:
        assertThat(result, contains(listener1, listener2));
    }

    @Test
    void shouldIterateInReverseOrder() {
        // Given:
        listeners.append(listener1);
        listeners.append(listener2);
        final List<TestEnvironmentListener> result = new ArrayList<>(2);

        // When:
        listeners.reverseIterator().forEachRemaining(result::add);

        // Then:
        assertThat(result, contains(listener2, listener1));
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldThrowIfWrongThread(final String ignored, final Consumer<TestListeners> method) {
        // Given:
        listeners = new TestListeners(Thread.currentThread().getId() + 1);

        // Then:
        assertThrows(ConcurrentModificationException.class, () -> method.accept(listeners));
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
                Arguments.of(
                        "append",
                        (Consumer<TestListeners>)
                                l -> l.append(mock(TestEnvironmentListener.class))),
                Arguments.of("iterator", (Consumer<TestListeners>) TestListeners::iterator),
                Arguments.of(
                        "reverseIterator",
                        (Consumer<TestListeners>) TestListeners::reverseIterator),
                Arguments.of(
                        "forEach", (Consumer<TestListeners>) l -> l.forEach(mock(Consumer.class))),
                Arguments.of(
                        "forEachReverse",
                        (Consumer<TestListeners>) l -> l.forEachReverse(mock(Consumer.class))),
                Arguments.of("spliterator", (Consumer<TestListeners>) Iterable::spliterator));
    }

    private List<String> publicMethodNames() {
        return Arrays.stream(TestListeners.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());
    }
}

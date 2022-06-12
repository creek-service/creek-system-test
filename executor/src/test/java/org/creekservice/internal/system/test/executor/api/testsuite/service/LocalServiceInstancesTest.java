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

package org.creekservice.internal.system.test.executor.api.testsuite.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.Mockito.mock;

import com.google.common.testing.NullPointerTester;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.system.test.extension.service.ServiceDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LocalServiceInstancesTest {

    private LocalServiceInstances instances;

    @BeforeEach
    void setUp() {
        instances = new LocalServiceInstances();
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(LocalServiceInstances.class);
        tester.testAllPublicStaticMethods(LocalServiceInstances.class);
        tester.testAllPublicInstanceMethods(instances);
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldThrowIfWrongThread(
            final String ignored, final Consumer<LocalServiceInstances> method) {
        // Given:
        instances = new LocalServiceInstances(Thread.currentThread().getId() + 1);

        // Then:
        assertThrows(ConcurrentModificationException.class, () -> method.accept(instances));
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
                        "clear", (Consumer<LocalServiceInstances>) LocalServiceInstances::clear),
                Arguments.of(
                        "spliterator",
                        (Consumer<LocalServiceInstances>) LocalServiceInstances::spliterator),
                Arguments.of(
                        "iterator",
                        (Consumer<LocalServiceInstances>) LocalServiceInstances::iterator),
                Arguments.of(
                        "add",
                        (Consumer<LocalServiceInstances>)
                                si -> si.add(mock(ServiceDefinition.class))),
                Arguments.of(
                        "forEach",
                        (Consumer<LocalServiceInstances>) si -> si.forEach(mock(Consumer.class))));
    }

    private List<String> publicMethodNames() {
        return Arrays.stream(LocalServiceInstances.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());
    }
}

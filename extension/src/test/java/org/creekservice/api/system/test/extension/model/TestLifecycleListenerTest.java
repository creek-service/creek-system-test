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

package org.creekservice.api.system.test.extension.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TestLifecycleListenerTest {

    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldDoNothingByDefault(
            final String ignored, final Consumer<TestLifecycleListener> method) {
        // Given:
        final TestLifecycleListener listener = new TestLifecycleListener() {};

        // When:
        method.accept(listener);

        // Then: did nothing.
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

    public static Stream<Arguments> publicMethods() {
        return Stream.of(
                Arguments.of(
                        "beforeSuite",
                        (Consumer<TestLifecycleListener>) l -> l.beforeSuite("name")),
                Arguments.of(
                        "afterSuite", (Consumer<TestLifecycleListener>) l -> l.afterSuite("name")),
                Arguments.of(
                        "beforeTest", (Consumer<TestLifecycleListener>) l -> l.beforeSuite("name")),
                Arguments.of(
                        "afterTest", (Consumer<TestLifecycleListener>) l -> l.afterSuite("name")));
    }

    private List<String> publicMethodNames() {
        return Arrays.stream(TestLifecycleListener.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());
    }
}

/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.system.test.extension.test.env.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.ParameterizedInvocationConstants.INDEX_PLACEHOLDER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.system.test.extension.test.model.CreekTestCase;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.model.TestCaseResult;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestEnvironmentListenerTest {

    @Mock private CreekTestSuite suite;
    @Mock private CreekTestCase test;

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldDoNothingByDefault(
            final String ignored,
            final BiConsumer<TestEnvironmentListener, TestEnvironmentListenerTest> method) {
        // Given:
        final TestEnvironmentListener listener = new TestEnvironmentListener() {};

        // When:
        method.accept(listener, this);

        // Then: did nothing.
        verifyNoInteractions(suite, test);
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
                        (BiConsumer<TestEnvironmentListener, TestEnvironmentListenerTest>)
                                (l, t) -> l.beforeSuite(t.suite)),
                Arguments.of(
                        "afterSuite",
                        (BiConsumer<TestEnvironmentListener, TestEnvironmentListenerTest>)
                                (l, t) -> l.afterSuite(t.suite, mock(TestSuiteResult.class))),
                Arguments.of(
                        "beforeTest",
                        (BiConsumer<TestEnvironmentListener, TestEnvironmentListenerTest>)
                                (l, t) -> l.beforeTest(t.test)),
                Arguments.of(
                        "afterTest",
                        (BiConsumer<TestEnvironmentListener, TestEnvironmentListenerTest>)
                                (l, t) -> l.afterTest(t.test, mock(TestCaseResult.class))));
    }

    private List<String> publicMethodNames() {
        return Arrays.stream(TestEnvironmentListener.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());
    }
}

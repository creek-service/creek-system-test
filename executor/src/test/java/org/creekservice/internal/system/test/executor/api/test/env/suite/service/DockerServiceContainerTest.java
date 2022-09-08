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

package org.creekservice.internal.system.test.executor.api.test.env.suite.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.LENIENT;

import com.google.common.testing.NullPointerTester;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.env.suite.service.ConfigurableServiceInstance;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class DockerServiceContainerTest {

    @Mock private ServiceDefinition serviceDef;
    @Mock private ServiceDescriptor serviceDescriptor;
    @Mock private Function<DockerImageName, GenericContainer<?>> containerFactory;

    @Mock(answer = RETURNS_DEEP_STUBS)
    private GenericContainer<?> container;

    private DockerServiceContainer instances;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeEach
    void setUp() {
        instances = new DockerServiceContainer(Thread.currentThread().getId(), containerFactory);

        when(serviceDef.name()).thenReturn("bob");
        when(serviceDef.dockerImage()).thenReturn("bob:latest");
        when(containerFactory.apply(any())).thenReturn((GenericContainer) container);
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(DockerServiceContainer.class);
        tester.testAllPublicStaticMethods(DockerServiceContainer.class);
        tester.testAllPublicInstanceMethods(instances);
    }

    @Test
    void shouldSupportNoDescriptor() {
        // Given:
        when(serviceDef.descriptor()).thenReturn(Optional.empty());

        // When:
        final ServiceInstance instance = instances.add(serviceDef);

        // Then:
        assertThat(instance.descriptor(), is(Optional.empty()));
    }

    @Test
    void shouldExposeDescriptor() {
        // Given:
        doReturn(Optional.of(serviceDescriptor)).when(serviceDef).descriptor();

        // When:
        final ServiceInstance instance = instances.add(serviceDef);

        // Then:
        assertThat(instance.descriptor(), is(Optional.of(serviceDescriptor)));
    }

    @Test
    void shouldInvokeConfigureCallbackOnAdd() {
        // When:
        final ConfigurableServiceInstance instance = instances.add(serviceDef);

        // Then:
        verify(serviceDef).configureInstance(instance);
    }

    @Test
    void shouldNotInvokeStartedCallbackOnAdd() {
        // When:
        instances.add(serviceDef);

        // Then:
        verify(serviceDef, never()).instanceStarted(any());
    }

    @Test
    void shouldCallStartedCallbackOnInstanceStart() {
        // Given:
        final ServiceInstance instance = instances.add(serviceDef);

        // When:
        instance.start();

        // Then:
        verify(serviceDef).instanceStarted(instance);
    }

    @Test
    void shouldGetByName() {
        // Given:
        final ServiceInstance instance = instances.add(serviceDef);

        // When:
        final ConfigurableServiceInstance result = instances.get(instance.name());

        // Then:
        assertThat(result, is(sameInstance(instance)));
    }

    @Test
    void shouldThrowOnGetWithUnknownName() {
        // When:
        final Exception e =
                assertThrows(IllegalArgumentException.class, () -> instances.get("some name"));

        // Then:
        assertThat(e.getMessage(), is("No instance found with name: some name"));
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldThrowIfWrongThread(
            final String ignored, final Consumer<DockerServiceContainer> method) {
        // Given:
        instances =
                new DockerServiceContainer(Thread.currentThread().getId() + 1, containerFactory);

        // Then:
        assertThrows(ConcurrentModificationException.class, () -> method.accept(instances));
    }

    @Test
    void shouldHaveThreadingTestForEachPublicMethod() {
        final List<String> publicMethodNames = publicMethodNames();
        final List<String> tested = testedMethodNames();
        assertThat(
                "Public methods:\n"
                        + String.join(System.lineSeparator(), publicMethodNames)
                        + "\nTested methods:\n"
                        + String.join(System.lineSeparator(), tested),
                tested,
                hasSize(publicMethodNames.size()));
    }

    @SuppressWarnings("unchecked")
    public static Stream<Arguments> publicMethods() {
        return Stream.of(
                Arguments.of(
                        "clear", (Consumer<DockerServiceContainer>) DockerServiceContainer::clear),
                Arguments.of(
                        "spliterator",
                        (Consumer<DockerServiceContainer>) DockerServiceContainer::spliterator),
                Arguments.of(
                        "iterator",
                        (Consumer<DockerServiceContainer>) DockerServiceContainer::iterator),
                Arguments.of(
                        "stream",
                        (Consumer<DockerServiceContainer>) DockerServiceContainer::stream),
                Arguments.of(
                        "add",
                        (Consumer<DockerServiceContainer>)
                                si -> si.add(mock(ServiceDefinition.class))),
                Arguments.of("get", (Consumer<DockerServiceContainer>) si -> si.get("")),
                Arguments.of(
                        "forEach",
                        (Consumer<DockerServiceContainer>) si -> si.forEach(mock(Consumer.class))));
    }

    private static List<String> testedMethodNames() {
        return publicMethods()
                .map(a -> (String) a.get()[0])
                .collect(Collectors.toUnmodifiableList());
    }

    private List<String> publicMethodNames() {
        return Arrays.stream(DockerServiceContainer.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());
    }
}

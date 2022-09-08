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

package org.creekservice.internal.system.test.executor.api.component.definition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.testing.NullPointerTester;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.system.test.extension.component.definition.AggregateDefinition;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinition;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ComponentDefinitionsTest {

    @ParameterizedTestByComponentType
    void shouldThrowNPEs(
            final String ignored,
            final Function<List<ComponentDescriptor>, ComponentDefinitions<?>> factory,
            final ComponentDescriptor descriptor0,
            final ComponentDescriptor descriptor1) {
        // Given:
        final ComponentDefinitions<?> components = factory.apply(List.of(descriptor0, descriptor1));
        final NullPointerTester tester = new NullPointerTester();

        // Then:
        tester.testAllPublicConstructors(ComponentDefinitions.class);
        tester.testAllPublicStaticMethods(ComponentDefinitions.class);
        tester.testAllPublicInstanceMethods(components);
    }

    @ParameterizedTestByComponentType
    void shouldFilterByDescriptorType(
            final String ignored,
            final Function<List<ComponentDescriptor>, ComponentDefinitions<?>> factory,
            final ComponentDescriptor descriptor0,
            final ComponentDescriptor descriptor1) {
        // Given:
        final ComponentDescriptor other = mock(ComponentDescriptor.class);
        when(other.name()).thenReturn("other");

        // When:
        final ComponentDefinitions<?> components =
                factory.apply(List.of(descriptor0, other, descriptor1));

        // Then:
        components.get(descriptor0.name());
        assertThrows(RuntimeException.class, () -> components.get(other.name()));
        components.get(descriptor1.name());
    }

    @ParameterizedTestByComponentType
    void shouldThrowOnNameClashWithAMessageIncludeJarLocations(
            final String componentType,
            final Function<List<ComponentDescriptor>, ComponentDefinitions<?>> factory,
            final ComponentDescriptor descriptor0,
            final ComponentDescriptor descriptor1) {
        // Given:
        when(descriptor0.name()).thenReturn("component-1");

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> factory.apply(List.of(descriptor0, descriptor1)));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Two or more "
                                + componentType
                                + " descriptors where found with the same name. Names must be unique."));
        assertThat(e.getMessage(), containsString("name: component-1"));
        assertThat(
                e.getMessage(),
                matchesPattern(
                        Pattern.compile(
                                ".*file:/.*creek-platform-metadata-\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?\\.jar.*",
                                Pattern.DOTALL)));
    }

    @ParameterizedTestByComponentType
    void shouldGetByName(
            final String ignored,
            final Function<List<ComponentDescriptor>, ComponentDefinitions<?>> factory,
            final ComponentDescriptor descriptor0,
            final ComponentDescriptor descriptor1) {
        // Given:
        final ComponentDefinitions<?> components = factory.apply(List.of(descriptor0, descriptor1));

        // When:
        final ComponentDefinition result = components.get("component-0");

        // Then:
        assertThat(result.name(), is("component-0"));
        assertThat(result.descriptor(), is(Optional.of(descriptor0)));
    }

    @ParameterizedTestByComponentType
    void shouldThrowFromGetOnUnknownServiceName(
            final String componentType,
            final Function<List<ComponentDescriptor>, ComponentDefinitions<?>> factory,
            final ComponentDescriptor descriptor0,
            final ComponentDescriptor descriptor1) {
        // Given:
        final ComponentDefinitions<?> components = factory.apply(List.of(descriptor0, descriptor1));

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class, () -> components.get("unknown-component"));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Unknown "
                                + componentType
                                + ": unknown-component. Known "
                                + componentType
                                + "s are: [component-0, component-1]"));
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldThrowIfWrongThread(
            final String ignored, final Consumer<ComponentDefinitions<?>> method) {
        // Given:
        final ComponentDefinitions<ServiceDefinition> services =
                ComponentDefinitions.serviceDefinitions(
                        List.of(), Thread.currentThread().getId() + 1);

        // Then:
        assertThrows(ConcurrentModificationException.class, () -> method.accept(services));
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
                        "get",
                        (Consumer<ComponentDefinitions<ServiceDefinition>>) s -> s.get("name")),
                Arguments.of(
                        "iterator",
                        (Consumer<ComponentDefinitions<ServiceDefinition>>)
                                ComponentDefinitions::iterator),
                Arguments.of(
                        "spliterator",
                        (Consumer<ComponentDefinitions<ServiceDefinition>>)
                                ComponentDefinitions::spliterator),
                Arguments.of(
                        "stream",
                        (Consumer<ComponentDefinitions<ServiceDefinition>>)
                                ComponentDefinitions::stream),
                Arguments.of(
                        "forEach",
                        (Consumer<ComponentDefinitions<ServiceDefinition>>)
                                s -> s.forEach(mock(Consumer.class))));
    }

    private List<String> publicMethodNames() {
        return Arrays.stream(ComponentDefinitions.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());
    }

    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("componentTypes")
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ParameterizedTestByComponentType {}

    static Stream<Arguments> componentTypes() {

        final ServiceDescriptor serviceDescriptor0 = mock(ServiceDescriptor.class);
        when(serviceDescriptor0.name()).thenReturn("component-0");
        final ServiceDescriptor serviceDescriptor1 = mock(ServiceDescriptor.class);
        when(serviceDescriptor1.name()).thenReturn("component-1");

        final AggregateDescriptor aggregateDescriptor0 = mock(AggregateDescriptor.class);
        when(aggregateDescriptor0.name()).thenReturn("component-0");
        final AggregateDescriptor aggregateDescriptor1 = mock(AggregateDescriptor.class);
        when(aggregateDescriptor1.name()).thenReturn("component-1");

        final Function<List<ComponentDescriptor>, ComponentDefinitions<AggregateDefinition>>
                aggFunc = ComponentDefinitions::aggregateDefinitions;

        final Function<List<ComponentDescriptor>, ComponentDefinitions<ServiceDefinition>>
                serviceFunc = ComponentDefinitions::serviceDefinitions;

        return Stream.of(
                Arguments.of("aggregate", aggFunc, aggregateDescriptor0, aggregateDescriptor1),
                Arguments.of("service", serviceFunc, serviceDescriptor0, serviceDescriptor1));
    }
}

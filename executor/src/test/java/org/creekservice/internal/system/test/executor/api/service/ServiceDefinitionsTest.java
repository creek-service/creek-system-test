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

package org.creekservice.internal.system.test.executor.api.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.testing.NullPointerTester;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.system.test.extension.service.ServiceDefinition;
import org.creekservice.api.system.test.test.services.TestServiceDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceDefinitionsTest {

    @Mock private ServiceDescriptor serviceDescriptor0;
    private final ServiceDescriptor serviceDescriptor1 = new TestServiceDescriptor();
    private ServiceDefinitions services;

    @BeforeEach
    void setUp() {
        when(serviceDescriptor0.name()).thenReturn("service-0");

        services = new ServiceDefinitions(List.of(serviceDescriptor0, serviceDescriptor1));
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(ServiceDefinitions.class);
        tester.testAllPublicStaticMethods(ServiceDefinitions.class);
        tester.testAllPublicInstanceMethods(services);
    }

    @Test
    void shouldIgnoreNonServiceDescriptors() {
        // When:
        services =
                new ServiceDefinitions(
                        List.of(
                                serviceDescriptor0,
                                mock(ComponentDescriptor.class),
                                mock(AggregateDescriptor.class),
                                serviceDescriptor1));

        // Then:
        final List<ServiceDefinition> defs = new ArrayList<>(2);
        services.forEach(defs::add);
        assertThat(defs, hasSize(2));
    }

    @Test
    void shouldThrowOnServiceNameClashWithAMessageIncludeJarLocations() {
        // Given:
        when(serviceDescriptor0.name()).thenReturn(serviceDescriptor1.name());

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new ServiceDefinitions(
                                        List.of(serviceDescriptor0, serviceDescriptor1)));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Two or more ServiceDescriptors where found with the same name. Names must be unique."));
        assertThat(e.getMessage(), containsString("service_name: test-service"));
        assertThat(
                e.getMessage(),
                matchesPattern(
                        Pattern.compile(
                                ".*file:/.*creek-platform-metadata-\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?\\.jar.*",
                                Pattern.DOTALL)));
        assertThat(
                e.getMessage(),
                matchesPattern(
                        Pattern.compile(
                                ".*file:/.*creek-system-test-test-services-\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?\\.jar.*",
                                Pattern.DOTALL)));
    }

    @Test
    void shouldGetServiceName() {
        assertThat(services.get("service-0").name(), is("service-0"));
        assertThat(
                services.get(TestServiceDescriptor.SERVICE_NAME).name(),
                is(TestServiceDescriptor.SERVICE_NAME));
    }

    @Test
    void shouldGetDockerImageName() {
        assertThat(
                services.get(TestServiceDescriptor.SERVICE_NAME).dockerImage(),
                is("ghcr.io/creekservice/creek-system-test-test-service:latest"));
    }

    @Test
    void shouldGetServiceDescriptor() {
        assertThat(
                services.get(TestServiceDescriptor.SERVICE_NAME).descriptor().orElse(null),
                is(instanceOf(TestServiceDescriptor.class)));
    }

    @Test
    void shouldThrowFromGetOnUnknownServiceName() {
        // When:
        final Exception e =
                assertThrows(IllegalArgumentException.class, () -> services.get("unknown-service"));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Unknown service: unknown-service. Known services are: [service-0, test-service]"));
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldThrowIfWrongThread(final String ignored, final Consumer<ServiceDefinitions> method) {
        // Given:
        services = new ServiceDefinitions(List.of(), Thread.currentThread().getId() + 1);

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
                Arguments.of("get", (Consumer<ServiceDefinitions>) s -> s.get("name")),
                Arguments.of(
                        "iterator", (Consumer<ServiceDefinitions>) ServiceDefinitions::iterator),
                Arguments.of(
                        "spliterator",
                        (Consumer<ServiceDefinitions>) ServiceDefinitions::spliterator),
                Arguments.of("stream", (Consumer<ServiceDefinitions>) ServiceDefinitions::stream),
                Arguments.of(
                        "forEach",
                        (Consumer<ServiceDefinitions>) s -> s.forEach(mock(Consumer.class))));
    }

    private List<String> publicMethodNames() {
        return Arrays.stream(ServiceDefinitions.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());
    }
}

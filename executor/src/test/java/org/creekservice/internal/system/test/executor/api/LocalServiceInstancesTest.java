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

package org.creekservice.internal.system.test.executor.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.system.test.extension.service.ServiceDefinition;
import org.creekservice.api.system.test.extension.service.ServiceInstance;
import org.creekservice.internal.system.test.executor.api.LocalServiceInstances.Instance;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.DockerClientFactory;

@ExtendWith(MockitoExtension.class)
class LocalServiceInstancesTest {

    @Mock(strictness = LENIENT)
    private ServiceDefinition serviceDef;

    private LocalServiceInstances instances;

    private final DockerClient dockerClient = DockerClientFactory.lazyClient();

    @BeforeEach
    void setUp() {
        instances = new LocalServiceInstances();

        when(serviceDef.name()).thenReturn("test-service");
        when(serviceDef.dockerImage()).thenReturn("ghcr.io/creekservice/test-service");
    }

    @Test
    void shouldStartAndStopServices() {
        // When:
        final ServiceInstance instance0 = instances.start(serviceDef);
        final ServiceInstance instance1 = instances.start(serviceDef);

        // Then:
        assertThat(instances(instances), contains(instance0, instance1));
        assertThat(instance0, is(running(true)));
        assertThat(instance1, is(running(true)));

        // When:
        instance0.stop();

        // Then:
        assertThat(instance0, is(running(false)));
        assertThat(instance1, is(running(true)));

        // When:
        instance1.stop();

        // Then:
        assertThat(instances(instances), contains(instance0, instance1));
        assertThat(instance0, is(running(false)));
        assertThat(instance1, is(running(false)));
    }

    @Test
    void shouldDoNothingOnSubsequentServiceStops() {
        // Given:
        final ServiceInstance instance = instances.start(serviceDef);
        instance.stop();
        assertThat(instance, is(running(false)));

        // When:
        instance.stop();

        // Then: no error and nothing has changed:
        assertThat(instance, is(running(false)));
    }

    @Test
    void shouldRestartService() {
        // Given:
        final ServiceInstance instance = instances.start(serviceDef);
        instance.stop();
        assertThat(instance, is(running(false)));

        // When:
        instance.start();

        // Then: no error and nothing has changed:
        assertThat(instance, is(running(true)));
    }

    @Test
    void shouldThrowOnServiceStartFailure() {
        // Given:
        when(serviceDef.dockerImage()).thenReturn("i-do-not-exist");

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> instances.start(serviceDef));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith("Failed to start service: test-service, image: i-do-not-exist:latest"));
        assertThat(e.getMessage(), containsString("Cause: Container startup failed"));
        assertThat("should not track failed instance", instances(instances), is(empty()));
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

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicInstanceMethods")
    void shouldThrowIfWrongThreadForInstance(
            final String ignored, final Consumer<Instance> method) {
        // Given:
        final Instance instance = (Instance) instances.start(serviceDef);
        final ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            // Then:
            final Future<?> result = executor.submit(() -> method.accept(instance));
            final Exception e = assertThrows(ExecutionException.class, result::get);
            assertThat(e.getCause(), is(instanceOf(ConcurrentModificationException.class)));
        } finally {
            instance.stop();
            executor.shutdown();
        }
    }

    @Test
    void shouldHaveThreadingTestForEachInstancePublicMethod() {
        final List<String> publicMethodNames = publicInstanceMethodNames();
        final int testedMethodCount = (int) publicInstanceMethods().count();
        assertThat(
                "Public methods:\n" + String.join(System.lineSeparator(), publicMethodNames),
                testedMethodCount,
                is(publicMethodNames.size()));
    }

    private Matcher<ServiceInstance> running(final boolean expectedRunState) {
        return new TypeSafeDiagnosingMatcher<>() {
            @Override
            protected boolean matchesSafely(
                    final ServiceInstance item, final Description mismatchDescription) {
                describeTo(mismatchDescription);

                final boolean testContainersRunState = item.running();
                final boolean testContainersStateMatch = testContainersRunState == expectedRunState;
                if (!testContainersStateMatch) {
                    mismatchDescription
                            .appendText(" but testContainersRunState was ")
                            .appendValue(testContainersRunState);
                }

                final boolean dockerContainerRunState = dockerContainerRunState(item);
                final boolean dockerContainerStateMatch =
                        dockerContainerRunState == expectedRunState;
                if (!dockerContainerStateMatch) {
                    if (!testContainersStateMatch) {
                        mismatchDescription.appendText(" and ");
                    }

                    mismatchDescription
                            .appendText(" but dockerContainerRunState was ")
                            .appendValue(dockerContainerRunState);
                }
                return testContainersStateMatch && dockerContainerStateMatch;
            }

            @Override
            public void describeTo(final Description description) {
                description
                        .appendText("Docker container with run state ")
                        .appendValue(expectedRunState);
            }
        };
    }

    private boolean dockerContainerRunState(final ServiceInstance instance) {
        final String containerId = ((Instance) instance).cachedContainerId();
        try {
            return Boolean.TRUE.equals(
                    dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning());
        } catch (NotFoundException e) {
            return false;
        }
    }

    private static List<ServiceInstance> instances(final LocalServiceInstances instances) {
        final List<ServiceInstance> result = new ArrayList<>(2);
        instances.forEach(result::add);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Stream<Arguments> publicMethods() {
        return Stream.of(
                Arguments.of(
                        "spliterator",
                        (Consumer<LocalServiceInstances>) LocalServiceInstances::spliterator),
                Arguments.of(
                        "iterator",
                        (Consumer<LocalServiceInstances>) LocalServiceInstances::iterator),
                Arguments.of(
                        "start",
                        (Consumer<LocalServiceInstances>)
                                si -> si.start(mock(ServiceDefinition.class))),
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

    public static Stream<Arguments> publicInstanceMethods() {
        return Stream.of(
                Arguments.of("start", (Consumer<Instance>) Instance::start),
                Arguments.of("stop", (Consumer<Instance>) Instance::stop),
                Arguments.of("running", (Consumer<Instance>) Instance::running));
    }

    private List<String> publicInstanceMethodNames() {
        return Arrays.stream(Instance.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());
    }
}

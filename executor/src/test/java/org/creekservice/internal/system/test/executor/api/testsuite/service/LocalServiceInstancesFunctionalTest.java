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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.exception.NotFoundException;
import com.google.common.testing.NullPointerTester;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.system.test.extension.service.ServiceDefinition;
import org.creekservice.api.system.test.extension.service.ServiceInstance;
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
class LocalServiceInstancesFunctionalTest {

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
    void shouldThrowNPEs() {
        final NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(LocalServiceInstances.class);
        tester.testAllPublicStaticMethods(LocalServiceInstances.class);
        tester.testAllPublicInstanceMethods(instances);
    }

    @Test
    void shouldAddMultipleServiceInstances() {
        // Given:
        final ServiceInstance instance0 = instances.add(serviceDef);

        // When:
        final ServiceInstance instance1 = instances.add(serviceDef);

        // Then:
        assertThat(instances(instances), contains(instance0, instance1));
    }

    @Test
    void shouldNotStartServicesOnAdd() {
        // When:
        final ServiceInstance instance = instances.add(serviceDef);

        // Then:
        assertThat(instance, is(running(false)));
    }

    @Test
    void shouldStartAndStopServices() {
        // Given:
        final ServiceInstance instance0 = instances.add(serviceDef);
        final ServiceInstance instance1 = instances.add(serviceDef);

        // When:
        instance0.start();

        // Then:
        assertThat(instance0, is(running(true)));
        assertThat(instance1, is(running(false)));

        // When:
        instance1.start();

        // Then:
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
        assertThat(instance0, is(running(false)));
        assertThat(instance1, is(running(false)));
    }

    @Test
    void shouldKeepStoppedServices() {
        // Given:
        final ServiceInstance instance = instances.add(serviceDef);
        instance.start();

        // When:
        instance.stop();

        // Then:
        assertThat(instances(instances), contains(instance));
    }

    @Test
    void shouldDoNothingOnSubsequentServiceStarts() {
        // Given:
        final ServiceInstance instance = instances.add(serviceDef);
        instance.start();
        assertThat(instance, is(running(true)));

        // When:
        instance.start();

        // Then: no error and nothing has changed:
        assertThat(instance, is(running(true)));
    }

    @Test
    void shouldDoNothingOnSubsequentServiceStops() {
        // Given:
        final ServiceInstance instance = instances.add(serviceDef);
        instance.start();
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
        final ServiceInstance instance = instances.add(serviceDef);
        instance.start();
        instance.stop();

        // When:
        instance.start();

        // Then:
        assertThat(instance, is(running(true)));
    }

    @Test
    void shouldThrowOnServiceStartFailure() {
        // Given:
        when(serviceDef.dockerImage()).thenReturn("i-do-not-exist");
        final ServiceInstance instance = instances.add(serviceDef);

        // When:
        final Exception e = assertThrows(RuntimeException.class, instance::start);

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Failed to start service: test-service-0, image: i-do-not-exist:latest"));
        assertThat(e.getMessage(), containsString("Cause: Container startup failed"));
    }

    @Test
    void shouldResetInstanceNamingOnClear() {
        // Given:
        final ServiceInstance i0 = instances.add(serviceDef);

        // When:
        instances.clear();

        // Then:
        assertThat(instances.add(serviceDef).name(), is(i0.name()));
    }

    @Test
    void shouldClearInstances() {
        // Given:
        instances.add(serviceDef);

        // When:
        instances.clear();

        // Then:
        assertThat(instances(instances), is(empty()));
    }

    @Test
    void shouldThrowOnClearIfAnyInstancesAreRunning() {
        // Given:
        instances.add(serviceDef);
        instances.add(serviceDef).start();
        instances.add(serviceDef).start();

        // When:
        final Exception e = assertThrows(IllegalStateException.class, instances::clear);

        // Then:
        assertThat(
                e.getMessage(),
                is("The following services are still running: test-service-1, test-service-2"));
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
        final String containerId = ((InstanceUnderTest) instance).cachedContainerId();
        if (containerId == null) {
            return false; // Never started
        }

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

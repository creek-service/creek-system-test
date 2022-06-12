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
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.testing.NullPointerTester;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(MockitoExtension.class)
class InstanceUnderTestTest {

    private static final DockerImageName IMAGE_NAME =
            DockerImageName.parse("ghcr.io/creekservice/test-service:latest");

    @Mock private GenericContainer<?> container;

    private InstanceUnderTest instance;

    @BeforeEach
    void setUp() {
        instance = new InstanceUnderTest("a-0", IMAGE_NAME, container);
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(InstanceUnderTest.class);
        tester.testAllPublicStaticMethods(InstanceUnderTest.class);
        tester.testAllPublicInstanceMethods(instance);
    }

    @Test
    void shouldExposeName() {
        assertThat(instance.name(), is("a-0"));
    }

    @Test
    void shouldReportRunningIfContainerHasId() {
        // Given:
        when(container.getContainerId()).thenReturn("bob");

        // Then:
        assertThat(instance.running(), is(true));
    }

    @Test
    void shouldReportNotRunningIfContainerHasNoId() {
        // Given:
        when(container.getContainerId()).thenReturn(null);

        // Then:
        assertThat(instance.running(), is(false));
    }

    @Test
    void shouldStart() {
        // Given:
        givenNotRunning();

        // When:
        instance.start();

        // Then:
        verify(container).start();
    }

    @Test
    void shouldIgnoreStartIfRunning() {
        // Given:
        when(container.getContainerId()).thenReturn("bob");

        // When:
        instance.start();

        // Then:
        verify(container, never()).start();
    }

    @Test
    void shouldStop() {
        // Given:
        when(container.getContainerId()).thenReturn("bob");

        // When:
        instance.stop();

        // Then:
        verify(container).stop();
    }

    @Test
    void shouldIgnoreStopIfNotRunning() {
        // Given:
        givenNotRunning();

        // When:
        instance.stop();

        // Then:
        verify(container, never()).stop();
    }

    @Test
    void shouldThrowOnServiceStartFailure() {
        // Given:
        final RuntimeException cause = new RuntimeException("Boom");
        doThrow(cause).when(container).start();

        // When:
        final Exception e = assertThrows(RuntimeException.class, instance::start);

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Failed to start service: a-0, image: ghcr.io/creekservice/test-service:latest"));
        assertThat(e.getCause(), is(sameInstance(cause)));
    }

    @Test
    void shouldThrowOnModifyIfRunning() {
        // Given:
        when(container.getContainerId()).thenReturn(null).thenReturn("bob");

        instance.start();

        // When:
        final Exception e = assertThrows(IllegalStateException.class, instance::modify);

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "A service can not be modified when running. "
                                + "service: a-0 (ghcr.io/creekservice/test-service:latest) with container-id bob"));
    }

    @Test
    void shouldAddEnv() {
        // Given:
        givenNotRunning();

        // When:
        instance.modify().withEnv("k0", "v0").withEnv("k1", "v1");

        // Then:
        verify(container).withEnv("k0", "v0");
        verify(container).withEnv("k1", "v1");
    }

    @Test
    void shouldAddEnvMap() {
        // Given:
        givenNotRunning();

        // When:
        instance.modify().withEnv(Map.of("k0", "v0", "k1", "v1")).withEnv(Map.of("k2", "v2"));

        // Then:
        verify(container).withEnv("k0", "v0");
        verify(container).withEnv("k1", "v1");
        verify(container).withEnv("k2", "v2");
    }

    @Test
    void shouldAddExposedPorts() {
        // Given:
        givenNotRunning();

        // When:
        instance.modify().withExposedPorts(10, 11).withExposedPorts(12);

        // Then:
        verify(container).withExposedPorts(10);
        verify(container).withExposedPorts(11);
        verify(container).withExposedPorts(12);
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("publicMethods")
    void shouldThrowIfWrongThread(final String ignored, final Consumer<InstanceUnderTest> method) {
        // Given:
        instance =
                new InstanceUnderTest(
                        "a-0", IMAGE_NAME, container, Thread.currentThread().getId() + 1);

        // Then:
        assertThrows(ConcurrentModificationException.class, () -> method.accept(instance));
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

    private void givenNotRunning() {
        when(container.getContainerId()).thenReturn(null);
    }

    public static Stream<Arguments> publicMethods() {
        return Stream.of(
                Arguments.of("name", (Consumer<InstanceUnderTest>) InstanceUnderTest::name),
                Arguments.of("start", (Consumer<InstanceUnderTest>) InstanceUnderTest::start),
                Arguments.of("stop", (Consumer<InstanceUnderTest>) InstanceUnderTest::stop),
                Arguments.of("running", (Consumer<InstanceUnderTest>) InstanceUnderTest::running),
                Arguments.of("modify", (Consumer<InstanceUnderTest>) InstanceUnderTest::modify),
                Arguments.of("withEnv", (Consumer<InstanceUnderTest>) i -> i.withEnv("k", "v")),
                Arguments.of(
                        "withEnv(Map)",
                        (Consumer<InstanceUnderTest>) i -> i.withEnv(Map.of("k", "v"))),
                Arguments.of(
                        "withExposedPorts",
                        (Consumer<InstanceUnderTest>) InstanceUnderTest::withExposedPorts));
    }

    private List<String> publicMethodNames() {
        return Arrays.stream(InstanceUnderTest.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .map(Method::toGenericString)
                .collect(Collectors.toUnmodifiableList());
    }
}

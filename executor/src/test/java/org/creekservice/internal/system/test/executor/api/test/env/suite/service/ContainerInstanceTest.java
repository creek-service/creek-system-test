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
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Streams;
import com.google.common.testing.NullPointerTester;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.creekservice.api.base.type.RuntimeIOException;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.system.test.extension.test.env.suite.service.ConfigurableServiceInstance;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(MockitoExtension.class)
class ContainerInstanceTest {

    private static final DockerImageName IMAGE_NAME =
            DockerImageName.parse("ghcr.io/creekservice/creek-system-test-test-service:latest");

    @Mock(answer = RETURNS_DEEP_STUBS)
    private GenericContainer<?> container;

    @Mock private ServiceDescriptor descriptor;
    @Mock private Consumer<ServiceInstance> startedCallback;
    @Mock private Container.ExecResult containerExecResult;

    private ContainerInstance instance;

    @BeforeEach
    void setUp() {
        instance =
                new ContainerInstance(
                        "a-0", IMAGE_NAME, container, Optional.empty(), startedCallback);
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester =
                new NullPointerTester()
                        .setDefault(String.class, "non-blank")
                        .setDefault(
                                DockerImageName.class,
                                DockerImageName.parse("some/service:latest"));

        tester.testAllPublicConstructors(ContainerInstance.class);
        tester.testAllPublicStaticMethods(ContainerInstance.class);
        tester.testAllPublicInstanceMethods(instance);
    }

    @Test
    void shouldExposeName() {
        assertThat(instance.name(), is("a-0"));
    }

    @Test
    void shouldExposeNoDescriptor() {
        assertThat(instance.descriptor(), is(Optional.empty()));
    }

    @Test
    void shouldExposeDescriptor() {
        assertThat(
                new ContainerInstance(
                                "a-0",
                                IMAGE_NAME,
                                container,
                                Optional.of(descriptor),
                                startedCallback)
                        .descriptor(),
                is(Optional.of(descriptor)));
    }

    @Test
    void shouldReportRunningIfContainerHasId() {
        // Given:
        givenRunning();

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
        givenRunning();

        // When:
        instance.start();

        // Then:
        verify(container, never()).start();
    }

    @Test
    void shouldStop() {
        // Given:
        givenRunning();

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
                        "Failed to start service: a-0, image: ghcr.io/creekservice/creek-system-test-test-service:latest"));
        assertThat(e.getCause(), is(sameInstance(cause)));
    }

    @Test
    void shouldInvokeCallbackAfterStart() {
        // Given:
        givenNotRunning();

        // When:
        instance.start();

        // Then:
        final InOrder inOrder = inOrder(container, startedCallback);
        inOrder.verify(container).start();
        inOrder.verify(startedCallback).accept(instance);
    }

    @Test
    void shouldNotInvokeCallbackOnSubsequentStarts() {
        // Given:
        givenRunning();

        // When:
        instance.start();

        // Then:
        verify(startedCallback, never()).accept(any());
    }

    @Test
    void shouldNotInvokeCallbackIfStartThrows() {
        // Given:
        doThrow(new RuntimeException("Boom")).when(container).start();

        // When:
        assertThrows(RuntimeException.class, instance::start);

        // Then:
        verify(startedCallback, never()).accept(any());
    }

    @Test
    void shouldExposeTestNetworkPorts() {
        // Given:
        final int port = 253;
        final int mapped = 11253;
        when(container.getMappedPort(anyInt())).thenReturn(mapped);
        // When:
        final int result = instance.testNetworkPort(port);

        // Then:
        verify(container).getMappedPort(port);
        assertThat(result, is(mapped));
    }

    @Test
    void shouldExposeInstanceNameAsServiceNetworkHostName() {
        assertThat(instance.serviceNetworkHostname(), is(instance.name()));
    }

    @Test
    void shouldExposeTestNetworkHostName() {
        // Given:
        when(container.getHost()).thenReturn("some-external-host");

        // Then:
        assertThat(instance.testNetworkHostname(), is("some-external-host"));
    }

    @Test
    void shouldThrowOnExecInContainerIfNotRunning() {
        // Given:
        givenNotRunning();

        // When:
        final Exception e = assertThrows(IllegalStateException.class, instance::execOnInstance);

        // Then:§
        assertThat(
                e.getMessage(),
                is(
                        "Container not running. service: a-0 (ghcr.io/creekservice/creek-system-test-test-service:latest)"));
    }

    @Test
    void shouldExecInContainer() throws Exception {
        // Given:
        givenRunning();
        when(container.execInContainer(any())).thenReturn(containerExecResult);
        when(containerExecResult.getExitCode()).thenReturn(22);
        when(containerExecResult.getStdout()).thenReturn("stdout stuff");
        when(containerExecResult.getStderr()).thenReturn("stderr stuff");

        // When:
        final ServiceInstance.ExecResult result = instance.execOnInstance("some", "command");

        // Then:
        verify(container).execInContainer("some", "command");
        assertThat(result.exitCode(), is(22));
        assertThat(result.stdout(), is("stdout stuff"));
        assertThat(result.stderr(), is("stderr stuff"));
    }

    @Test
    void shouldThrowOnExecIfContainerThrowsIOException() throws Exception {
        // Given:
        givenRunning();
        final IOException cause = new IOException("Boom");
        when(container.execInContainer(any())).thenThrow(cause);

        // When:
        final Exception e = assertThrows(RuntimeIOException.class, instance::execOnInstance);

        // Then:
        assertThat(e.getCause(), is(cause));
    }

    @Test
    void shouldThrowAndSetInterruptedOnExecIfContainerThrowsInterruptedException()
            throws Exception {
        // Given:
        givenRunning();
        final InterruptedException cause = new InterruptedException("Boom");
        when(container.execInContainer(any())).thenThrow(cause);

        // When:
        final Exception e = assertThrows(RuntimeException.class, instance::execOnInstance);

        // Then:
        assertThat(e.getCause(), is(cause));
        assertThat(Thread.interrupted(), is(true));
    }

    @Test
    void shouldAddEnv() {
        // Given:
        givenNotRunning();

        // When:
        final ConfigurableServiceInstance result = instance.addEnv("k0", "v0").addEnv("k1", "v1");

        // Then:
        verify(container).withEnv("k0", "v0");
        verify(container).withEnv("k1", "v1");
        assertThat(result, is(instance));
    }

    @Test
    void shouldAddEnvMap() {
        // Given:
        givenNotRunning();

        // When:
        final ConfigurableServiceInstance result =
                instance.addEnv(Map.of("k0", "v0", "k1", "v1")).addEnv(Map.of("k2", "v2"));

        // Then:
        verify(container).withEnv("k0", "v0");
        verify(container).withEnv("k1", "v1");
        verify(container).withEnv("k2", "v2");
        assertThat(result, is(instance));
    }

    @Test
    void shouldAddExposedPorts() {
        // Given:
        givenNotRunning();

        // When:
        final ConfigurableServiceInstance result =
                instance.addExposedPorts(10, 11).addExposedPorts(12);

        // Then:
        verify(container).addExposedPorts(10, 11);
        verify(container).addExposedPorts(12);
        assertThat(result, is(instance));
    }

    @Test
    void shouldSetCommand() {
        // Given:
        givenNotRunning();

        // When:
        final ConfigurableServiceInstance result = instance.setCommand("a", "b", "c");

        // Then:
        verify(container).withCommand("a", "b", "c");
        assertThat(result, is(instance));
    }

    @Test
    void shouldSetStartUpLogMessageToWaitFor() {
        // Given:
        givenNotRunning();

        // When:
        final ConfigurableServiceInstance result = instance.setStartupLogMessage(".*started.*", 2);

        // Then:
        verify(container).setWaitStrategy(isA(LogMessageWaitStrategy.class));
        assertThat(result, is(instance));
    }

    @Test
    void shouldSetStartUpTimeout() {
        // Given:
        givenNotRunning();
        final Duration timeout = Duration.ofHours(33);

        // When:
        final ConfigurableServiceInstance result = instance.setStartupTimeout(timeout);

        // Then:
        verify(container).withStartupTimeout(timeout);
        assertThat(result, is(instance));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldSetCustomStartupTimeoutWhenSettingCustomLogMessageToWaitFor() {
        // Given:
        givenNotRunning();
        final Duration timeout = Duration.ofHours(33);
        instance.setStartupTimeout(timeout);
        clearInvocations(container);

        // When:
        final ConfigurableServiceInstance result = instance.setStartupLogMessage(".*started.*", 2);

        // Then:
        verify(container).withStartupTimeout(timeout);
        assertThat(result, is(instance));
    }

    @Test
    void shouldSetStartupAttempts() {
        // Given:
        givenNotRunning();

        // When:
        final ConfigurableServiceInstance result = instance.setStartupAttempts(23);

        // Then:
        verify(container).withStartupAttempts(23);
        assertThat(result, is(instance));
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("methods")
    void shouldThrowIfWrongThread(final String ignored, final Consumer<ContainerInstance> method) {
        // Given:
        instance =
                new ContainerInstance(
                        "a-0",
                        IMAGE_NAME,
                        container,
                        Optional.empty(),
                        startedCallback,
                        Thread.currentThread().getId() + 1);

        // Then:
        assertThrows(ConcurrentModificationException.class, () -> method.accept(instance));
    }

    @Test
    void shouldHaveThreadingTestForEachNonConfigureMethod() {
        final List<String> methodNames = methodNames();
        final List<String> tested = testedMethodNames();
        final List<String> notTested = notTested(methodNames, tested);
        assertThat(
                "Not tested:\n"
                        + String.join(System.lineSeparator(), notTested)
                        + "\n\nMethods:\n"
                        + String.join(System.lineSeparator(), methodNames)
                        + "\n\nTested methods:\n"
                        + String.join(System.lineSeparator(), tested),
                tested,
                hasSize(methodNames.size()));
    }

    @SuppressWarnings("unused")
    @ParameterizedTest(name = "[" + INDEX_PLACEHOLDER + "] {0}")
    @MethodSource("configureMethods")
    void shouldThrowOnConfigureMethodsIfRunning(
            final String ignored, final Consumer<ConfigurableServiceInstance> method) {
        // Given:
        givenRunning();

        // When:
        final Exception e =
                assertThrows(IllegalStateException.class, () -> method.accept(instance));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "A service can not be modified when running. "
                                + "service: a-0 (ghcr.io/creekservice/creek-system-test-test-service:latest) with container-id bob"));
    }

    @Test
    void shouldHaveThrowingTestForEachConfigureMethod() {
        final List<String> methodNames = configureMethodNames();
        final List<String> tested = testedConfigureMethodNames();
        final List<String> notTested = notTested(methodNames, tested);
        assertThat(
                "Not tested:\n"
                        + String.join(System.lineSeparator(), notTested)
                        + "\n\nMethods:\n"
                        + String.join(System.lineSeparator(), methodNames)
                        + "\n\nTested methods:\n"
                        + String.join(System.lineSeparator(), tested),
                tested,
                hasSize(methodNames.size()));
    }

    private void givenRunning() {
        when(container.getContainerId()).thenReturn("bob");
    }

    private void givenNotRunning() {
        when(container.getContainerId()).thenReturn(null);
    }

    public static Stream<Arguments> methods() {
        return Streams.concat(
                configureMethods(),
                Stream.of(
                        Arguments.of("name", (Consumer<ContainerInstance>) ContainerInstance::name),
                        Arguments.of(
                                "start", (Consumer<ContainerInstance>) ContainerInstance::start),
                        Arguments.of("stop", (Consumer<ContainerInstance>) ContainerInstance::stop),
                        Arguments.of(
                                "running",
                                (Consumer<ContainerInstance>) ContainerInstance::running),
                        Arguments.of(
                                "serviceNetworkHostname",
                                (Consumer<ContainerInstance>)
                                        ContainerInstance::serviceNetworkHostname),
                        Arguments.of(
                                "testNetworkHostname",
                                (Consumer<ContainerInstance>)
                                        ContainerInstance::testNetworkHostname),
                        Arguments.of(
                                "descriptor",
                                (Consumer<ContainerInstance>) ContainerInstance::descriptor),
                        Arguments.of(
                                "containerId",
                                (Consumer<ContainerInstance>) ContainerInstance::containerId),
                        Arguments.of(
                                "mappedPort",
                                (Consumer<ContainerInstance>) i -> i.testNetworkPort(9)),
                        Arguments.of(
                                "execOnInstance",
                                (Consumer<ContainerInstance>) ContainerInstance::execOnInstance)));
    }

    private static List<String> testedMethodNames() {
        return methods().map(a -> (String) a.get()[0]).collect(Collectors.toUnmodifiableList());
    }

    private List<String> methodNames() {
        return Arrays.stream(ContainerInstance.class.getMethods())
                .filter(m -> !m.getDeclaringClass().equals(Object.class))
                .filter(m -> !m.isSynthetic())
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toUnmodifiableList());
    }

    public static Stream<Arguments> configureMethods() {
        return Stream.of(
                Arguments.of(
                        "addEnv", (Consumer<ConfigurableServiceInstance>) i -> i.addEnv("k", "v")),
                Arguments.of(
                        "addEnv",
                        (Consumer<ConfigurableServiceInstance>) i -> i.addEnv(Map.of("k", "v"))),
                Arguments.of(
                        "setStartupAttempts",
                        (Consumer<ConfigurableServiceInstance>) i -> i.setStartupAttempts(1)),
                Arguments.of(
                        "setStartupLogMessage",
                        (Consumer<ConfigurableServiceInstance>) i -> i.setStartupLogMessage("", 1)),
                Arguments.of(
                        "setStartupTimeout",
                        (Consumer<ConfigurableServiceInstance>)
                                i -> i.setStartupTimeout(Duration.ZERO)),
                Arguments.of(
                        "addExposedPorts",
                        (Consumer<ConfigurableServiceInstance>)
                                ConfigurableServiceInstance::addExposedPorts),
                Arguments.of(
                        "setCommand",
                        (Consumer<ConfigurableServiceInstance>)
                                ConfigurableServiceInstance::setCommand));
    }

    private static List<String> testedConfigureMethodNames() {
        return configureMethods()
                .map(a -> (String) a.get()[0])
                .collect(Collectors.toUnmodifiableList());
    }

    private List<String> configureMethodNames() {
        return Arrays.stream(ConfigurableServiceInstance.class.getDeclaredMethods())
                .filter(m -> !m.isSynthetic())
                .map(Method::getName)
                .collect(Collectors.toUnmodifiableList());
    }

    private static List<String> notTested(final List<String> all, final List<String> tested) {
        final ArrayList<String> notTested = new ArrayList<>(all);
        tested.forEach(notTested::remove);
        return notTested;
    }
}

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

package org.creekservice.internal.system.test.executor.api.test.env.suite.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.quality.Strictness.LENIENT;

import com.google.common.testing.NullPointerTester;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.creekservice.api.system.test.executor.ExecutorOptions;
import org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Values;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

@SuppressWarnings("resource")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ContainerFactoryTest {

    private static final Path HOST_PATH = Paths.get("host/path");
    private static final Path CONTAINER_PATH = Paths.get("container/path");
    private static final DockerImageName IMAGE_NAME =
            DockerImageName.parse("ghcr.io/creek-service/creek-system-test-test-service:latest");
    private static final int BASE_SERVICE_DEBUG_PORT = 9000;
    private static final String SERVICE_NAME = "bob";
    private static final String INSTANCE_NAME = "bob:1";
    @Mock private ServiceDebugInfo serviceDebugInfo;
    @Mock private RegularContainerFactory regularFactory;
    @Mock private Supplier<Network> networkSupplier;
    @Mock private DebugContainerFactory debugFactory;
    @Mock private GenericContainer<?> container;
    @Mock private ExecutorOptions.MountInfo mount;
    @Mock private Network network0;
    @Mock private Network network1;

    private ContainerFactory containerFactory;

    @BeforeEach
    void setUp() {
        when(serviceDebugInfo.baseServicePort()).thenReturn(BASE_SERVICE_DEBUG_PORT);
        when(networkSupplier.get()).thenReturn(network0, network1);

        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(),
                        Map.of(),
                        regularFactory,
                        debugFactory,
                        networkSupplier);

        doReturn(container).when(regularFactory).create(any());
        doReturn(container).when(debugFactory).create(any(), anyInt());
        doReturn(container).when(container).withNetwork(any());
        doReturn(container).when(container).withNetworkAliases(any());
        doReturn(container).when(container).withLogConsumer(any());

        when(mount.hostPath()).thenReturn(HOST_PATH);
        when(mount.containerPath()).thenReturn(CONTAINER_PATH);
        when(mount.readOnly()).thenReturn(true);
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester = new NullPointerTester();
        tester.setDefault(ServiceDebugInfo.class, serviceDebugInfo);
        tester.testAllPublicConstructors(ContainerFactory.class);
        tester.testAllPublicStaticMethods(ContainerFactory.class);
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldCreateNonDebugContainer(final boolean serviceUnderTest) {
        // Given:
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(false);

        // When:
        final GenericContainer<?> result =
                containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);

        // Then:
        verify(regularFactory).create(IMAGE_NAME);
        verify(debugFactory, never()).create(any(), anyInt());
        assertThat(result, is(container));
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldCreateDebugContainer(final boolean serviceUnderTest) {
        // Given:
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);

        // When:
        final GenericContainer<?> result =
                containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);

        // Then:
        verify(debugFactory).create(IMAGE_NAME, BASE_SERVICE_DEBUG_PORT);
        verify(regularFactory, never()).create(any());
        assertThat(result, is(container));
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldCreateDockerContainerWithUniqueDebugPort(final boolean serviceUnderTest) {
        // Given:
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);
        clearInvocations(debugFactory);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);

        // Then:
        verify(debugFactory).create(IMAGE_NAME, BASE_SERVICE_DEBUG_PORT + 1);
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldResetDebugPortAfterSuite(final boolean serviceUnderTest) {
        // Given:
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);
        clearInvocations(debugFactory);

        // When:
        containerFactory.afterSuite(null, null);

        // Then:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);
        verify(debugFactory).create(IMAGE_NAME, BASE_SERVICE_DEBUG_PORT);
    }

    @CartesianTest
    void shouldSetNetwork(
            @Values(booleans = {true, false}) final boolean serviceUnderTest,
            @Values(booleans = {true, false}) final boolean debug) {
        // Given:
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(debug);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);

        // Then:
        verify(container).withNetwork(network0);
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldSetDifferentNetworkOnEachSuite(final boolean serviceUnderTest) {
        // Given:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);
        verify(container).withNetwork(network0);

        // When:
        containerFactory.afterSuite(null, null);

        // Then:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);
        verify(container).withNetwork(network1);
    }

    @Test
    void shouldCloseNetworkAfterSuite() {
        // Given:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true);

        // When:
        containerFactory.afterSuite(null, null);

        // Then:
        verify(network0).close();
    }

    @CartesianTest
    void shouldSetNetworkAlias(
            @Values(booleans = {true, false}) final boolean serviceUnderTest,
            @Values(booleans = {true, false}) final boolean debug) {
        // Given:
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(debug);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);

        // Then:
        verify(container).withNetworkAliases(INSTANCE_NAME);
    }

    @CartesianTest
    void shouldSetLogConsumer(
            @Values(booleans = {true, false}) final boolean serviceUnderTest,
            @Values(booleans = {true, false}) final boolean debug) {
        // Given:
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(debug);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);

        // Then:
        verify(container).withLogConsumer(isA(Slf4jLogConsumer.class));
    }

    @CartesianTest
    void shouldNotSetEnvIfEnvIsEmpty(
            @Values(booleans = {true, false}) final boolean serviceUnderTest,
            @Values(booleans = {true, false}) final boolean debug) {
        // Given:
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(debug);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);

        // Then:
        verify(container, never()).withEnv(any());
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldSetEnvOnServicesUnderTest(final boolean debug) {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(),
                        Map.of("a", "b"),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(debug);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true);

        // Then:
        verify(container).withEnv(Map.of("a", "b"));
    }

    @Test
    void shouldSetDebugEnvOnServicesUnderTestIfBeingDebugged() {
        // Given:
        when(serviceDebugInfo.env()).thenReturn(Map.of("a", "b"));
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true);

        // Then:
        verify(container).withEnv(Map.of("a", "b"));
    }

    @Test
    void shouldOverwriteEnvWithDebugEnvOnServicesUnderTestIfBeingDebugged() {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(),
                        Map.of("common", "env-1", "env-only", "env-2"),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(serviceDebugInfo.env())
                .thenReturn(Map.of("common", "debug-1", "debug-only", "debug-2"));
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true);

        // Then:
        verify(container)
                .withEnv(Map.of("common", "debug-1", "env-only", "env-2", "debug-only", "debug-2"));
    }

    @Test
    void shouldNotSetDebugEnvOnServicesUnderTestIfNotBeingDebugged() {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(),
                        Map.of("a", "orig"),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(serviceDebugInfo.env()).thenReturn(Map.of("a", "debug"));
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(false);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true);

        // Then:
        verify(container).withEnv(Map.of("a", "orig"));
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldNotSetEnvOn3rdPartyService(final boolean debug) {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(),
                        Map.of("a", "b"),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(debug);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, false);

        // Then:
        verify(container, never()).withEnv(any());
    }

    @Test
    void shouldSetDebugEnvOn3rdPartyServiceIfDebugging() {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(),
                        Map.of("a", "orig"),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(serviceDebugInfo.env()).thenReturn(Map.of("a", "debug"));
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, false);

        // Then:
        verify(container).withEnv(Map.of("a", "debug"));
    }

    @Test
    void shouldNotSetDebugEnvOn3rdPartyServiceIfNotDebugging() {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(),
                        Map.of("a", "orig"),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(serviceDebugInfo.env()).thenReturn(Map.of("a", "debug"));
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(false);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, false);

        // Then:
        verify(container, never()).withEnv(any());
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldReplaceServiceDebugPortInJavaToolOptions(final boolean serviceUnderTest) {
        // Given:
        when(serviceDebugInfo.env())
                .thenReturn(
                        Map.of(
                                "JAVA_TOOL_OPTIONS",
                                "a${SERVICE_DEBUG_PORT}b${SERVICE_DEBUG_PORT}c"));
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);

        // Then:
        verify(container).withEnv(Map.of("JAVA_TOOL_OPTIONS", "a9000b9000c"));
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldNotReplaceServiceDebugPortInOtherEnvVars(final boolean serviceUnderTest) {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(),
                        Map.of("a", "${SERVICE_DEBUG_PORT}"),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(serviceDebugInfo.env()).thenReturn(Map.of("b", "${SERVICE_DEBUG_PORT}"));
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest);

        // Then:
        final Map<String, String> expectedA = Map.of("b", "${SERVICE_DEBUG_PORT}");
        final Map<String, String> expectedB =
                Map.of("a", "${SERVICE_DEBUG_PORT}", "b", "${SERVICE_DEBUG_PORT}");
        verify(container).withEnv(argThat(either(is(expectedA)).or(is(expectedB))));
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldSetMountsOnServicesUnderTest(final boolean debug) {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(mount),
                        Map.of(),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(debug);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true);

        // Then:
        verify(container)
                .withFileSystemBind(
                        HOST_PATH.toString(), CONTAINER_PATH.toString(), BindMode.READ_ONLY);
    }

    @Test
    void shouldSetMountsOn3rdPartyServiceIfDebugging() {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(mount),
                        Map.of(),
                        regularFactory,
                        debugFactory,
                        networkSupplier);

        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, false);

        // Then:
        verify(container)
                .withFileSystemBind(
                        HOST_PATH.toString(), CONTAINER_PATH.toString(), BindMode.READ_ONLY);
    }

    @Test
    void shouldNotSetMountsOn3rdPartyServiceIfNotDebugging() {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(mount),
                        Map.of(),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(false);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, false);

        // Then:
        verify(container, never()).withFileSystemBind(any(), any(), any());
    }

    @Test
    void shouldSetWritableMountsToo() {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(mount),
                        Map.of(),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(mount.readOnly()).thenReturn(false);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true);

        // Then:
        verify(container)
                .withFileSystemBind(
                        HOST_PATH.toString(), CONTAINER_PATH.toString(), BindMode.READ_WRITE);
    }
}

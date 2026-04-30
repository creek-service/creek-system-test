/*
 * Copyright 2022-2026 Creek Contributors (https://github.com/creek-service)
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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
import org.creekservice.api.system.test.executor.ExecutorOptions.CopyDirection;
import org.creekservice.internal.system.test.executor.api.test.env.suite.service.ContainerFactory.CreatedContainer;
import org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Values;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@SuppressWarnings("resource")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = LENIENT)
class ContainerFactoryTest {

    private static final Path CONTAINER_PATH = Paths.get("container/path");
    private static final DockerImageName IMAGE_NAME =
            DockerImageName.parse("ghcr.io/creek-service/creek-system-test-test-service:latest");
    private static final int BASE_SERVICE_DEBUG_PORT = 9000;
    private static final String SERVICE_NAME = "bob";
    private static final String INSTANCE_NAME = "bob:1";
    @TempDir Path hostDir;
    @Mock private ServiceDebugInfo serviceDebugInfo;
    @Mock private RegularContainerFactory regularFactory;
    @Mock private Supplier<Network> networkSupplier;
    @Mock private DebugContainerFactory debugFactory;
    @Mock private GenericContainer<?> container;
    @Mock private ExecutorOptions.DirectoryInfo mount;
    @Mock private Network network0;
    @Mock private Network network1;
    @Captor private ArgumentCaptor<MountableFile> mountableCaptor;

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

        doReturn(container).when(regularFactory).create(any(), any());
        doReturn(container).when(debugFactory).create(any(), anyInt(), any());
        doReturn(container).when(container).withNetwork(any());
        doReturn(container).when(container).withNetworkAliases(any());
        doReturn(container).when(container).withLogConsumer(any());
        doReturn(container).when(container).withCopyFileToContainer(any(), any());

        when(mount.hostPath()).thenReturn(hostDir);
        when(mount.containerPath()).thenReturn(CONTAINER_PATH);
        when(mount.direction()).thenReturn(CopyDirection.COPY_TO_CONTAINER);
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
        final CreatedContainer result =
                containerFactory.create(
                        IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});

        // Then:
        verify(regularFactory).create(eq(IMAGE_NAME), any());
        verify(debugFactory, never()).create(any(), anyInt(), any());
        assertThat(result.container(), is(container));
        assertThat(result.transferables(), is(empty()));
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldCreateDebugContainer(final boolean serviceUnderTest) {
        // Given:
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);

        // When:
        final CreatedContainer result =
                containerFactory.create(
                        IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});

        // Then:
        verify(debugFactory).create(eq(IMAGE_NAME), eq(BASE_SERVICE_DEBUG_PORT), any());
        verify(regularFactory, never()).create(any(), any());
        assertThat(result.container(), is(container));
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldCreateDockerContainerWithUniqueDebugPort(final boolean serviceUnderTest) {
        // Given:
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);
        containerFactory.create(
                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});
        clearInvocations(debugFactory);

        // When:
        containerFactory.create(
                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});

        // Then:
        verify(debugFactory).create(eq(IMAGE_NAME), eq(BASE_SERVICE_DEBUG_PORT + 1), any());
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldResetDebugPortAfterSuite(final boolean serviceUnderTest) {
        // Given:
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);
        containerFactory.create(
                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});
        clearInvocations(debugFactory);

        // When:
        containerFactory.afterSuite(null, null);

        // Then:
        containerFactory.create(
                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});
        verify(debugFactory).create(eq(IMAGE_NAME), eq(BASE_SERVICE_DEBUG_PORT), any());
    }

    @CartesianTest
    void shouldSetNetwork(
            @Values(booleans = {true, false}) final boolean serviceUnderTest,
            @Values(booleans = {true, false}) final boolean debug) {
        // Given:
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(debug);

        // When:
        containerFactory.create(
                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});

        // Then:
        verify(container).withNetwork(network0);
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldSetDifferentNetworkOnEachSuite(final boolean serviceUnderTest) {
        // Given:
        containerFactory.create(
                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});
        verify(container).withNetwork(network0);

        // When:
        containerFactory.afterSuite(null, null);

        // Then:
        containerFactory.create(
                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});
        verify(container).withNetwork(network1);
    }

    @Test
    void shouldCloseNetworkAfterSuite() {
        // Given:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {});

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
        containerFactory.create(
                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});

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
        containerFactory.create(
                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});

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
        containerFactory.create(
                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});

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
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {});

        // Then:
        verify(container).withEnv(Map.of("a", "b"));
    }

    @Test
    void shouldSetDebugEnvOnServicesUnderTestIfBeingDebugged() {
        // Given:
        when(serviceDebugInfo.env()).thenReturn(Map.of("a", "b"));
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {});

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
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {});

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
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {});

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
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, false, () -> {});

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
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, false, () -> {});

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
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, false, () -> {});

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
        containerFactory.create(
                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});

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
        containerFactory.create(
                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, serviceUnderTest, () -> {});

        // Then:
        final Map<String, String> expectedA = Map.of("b", "${SERVICE_DEBUG_PORT}");
        final Map<String, String> expectedB =
                Map.of("a", "${SERVICE_DEBUG_PORT}", "b", "${SERVICE_DEBUG_PORT}");
        verify(container).withEnv(argThat(either(is(expectedA)).or(is(expectedB))));
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldReplaceServiceInstanceNameInEnvVarsOfServicesUnderTest(final boolean debug) {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(),
                        Map.of("JAVA_TOOL_OPTIONS", "destfile=/${SERVICE_INSTANCE_NAME}.exec"),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(debug);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {});

        // Then:
        verify(container)
                .withEnv(Map.of("JAVA_TOOL_OPTIONS", "destfile=/" + INSTANCE_NAME + ".exec"));
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldReplaceServiceInstanceNameInAllEnvVars(final boolean debug) {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(),
                        Map.of(
                                "VAR_A", "a-${SERVICE_INSTANCE_NAME}",
                                "VAR_B", "b-${SERVICE_INSTANCE_NAME}"),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(debug);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {});

        // Then:
        verify(container)
                .withEnv(
                        Map.of(
                                "VAR_A", "a-" + INSTANCE_NAME,
                                "VAR_B", "b-" + INSTANCE_NAME));
    }

    @Test
    void shouldReplaceServiceInstanceNameInDebugEnvVars() {
        // Given:
        when(serviceDebugInfo.env())
                .thenReturn(Map.of("JAVA_TOOL_OPTIONS", "destfile=/${SERVICE_INSTANCE_NAME}.exec"));
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, false, () -> {});

        // Then:
        verify(container)
                .withEnv(Map.of("JAVA_TOOL_OPTIONS", "destfile=/" + INSTANCE_NAME + ".exec"));
    }

    @Test
    void shouldNotReplaceServiceInstanceNameInEnvVarsOf3rdPartyServices() {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(),
                        Map.of("JAVA_TOOL_OPTIONS", "destfile=/${SERVICE_INSTANCE_NAME}.exec"),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(false);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, false, () -> {});

        // Then: no env set at all (3rd party service with no debug env)
        verify(container, never()).withEnv(any());
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void shouldCopyDirectoriesIntoServicesUnderTest(final boolean debug) {
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
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {});

        // Then:
        verify(container)
                .withCopyFileToContainer(mountableCaptor.capture(), eq(CONTAINER_PATH + "/"));
        final MountableFile hostFile = mountableCaptor.getValue();
        assertThat(hostFile.getResolvedPath(), is(hostDir.toAbsolutePath().toString()));
    }

    @Test
    void shouldCopyDirectoriesInto3rdPartyServiceIfDebugging() {
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
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, false, () -> {});

        // Then:
        verify(container)
                .withCopyFileToContainer(mountableCaptor.capture(), eq(CONTAINER_PATH + "/"));
        final MountableFile hostFile = mountableCaptor.getValue();
        assertThat(hostFile.getResolvedPath(), is(hostDir.toAbsolutePath().toString()));
    }

    @Test
    void shouldNotCopyMountsInto3rdPartyServiceIfNotDebugging() {
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
        final CreatedContainer result =
                containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, false, () -> {});

        // Then:
        verify(container, never()).withCopyFileToContainer(any(), any());
        assertThat(result.transferables(), is(empty()));
    }

    @Test
    void shouldThrowIfTransferableDoesNotExist() {
        // Given:
        final Path nonExistentPath = hostDir.resolve("does-not-exist");
        when(mount.hostPath()).thenReturn(nonExistentPath);
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(mount),
                        Map.of(),
                        regularFactory,
                        debugFactory,
                        networkSupplier);

        // When / Then:
        assertThrows(
                Exception.class,
                () ->
                        containerFactory.create(
                                IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {}));
    }

    @Test
    void shouldNotReturnTransferablesThatAreFullyActioned() {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(mount),
                        Map.of(),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(mount.direction()).thenReturn(CopyDirection.COPY_TO_CONTAINER);

        // When:
        final CreatedContainer result =
                containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {});

        // Then:
        assertThat(result.transferables(), not(hasItem(mount)));
    }

    @ParameterizedTest
    @EnumSource(
            value = CopyDirection.class,
            names = {"COPY_FROM_CONTAINER", "COPY_TO_AND_FROM_CONTAINER"})
    void shouldReturnTransferablesThatNeedActioning(final CopyDirection direction) {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(mount),
                        Map.of(),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(mount.direction()).thenReturn(direction);

        // When:
        final CreatedContainer result =
                containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {});

        // Then:
        assertThat(result.transferables(), hasItem(mount));
    }

    @Test
    void shouldNotCopyToContainerWhenDirectionIsCopyFromContainer() {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(mount),
                        Map.of(),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(mount.direction()).thenReturn(CopyDirection.COPY_FROM_CONTAINER);

        // When:
        containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {});

        // Then:
        verify(container, never()).withCopyFileToContainer(any(), any());
    }

    @Test
    void shouldReturnCopyFromContainerInWritableCopies() {
        // Given:
        containerFactory =
                new ContainerFactory(
                        serviceDebugInfo,
                        List.of(mount),
                        Map.of(),
                        regularFactory,
                        debugFactory,
                        networkSupplier);
        when(mount.direction()).thenReturn(CopyDirection.COPY_FROM_CONTAINER);

        // When:
        final CreatedContainer result =
                containerFactory.create(IMAGE_NAME, INSTANCE_NAME, SERVICE_NAME, true, () -> {});

        // Then:
        assertThat(result.transferables(), hasItem(mount));
    }
}

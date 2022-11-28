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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNullElse;
import static org.creekservice.api.test.hamcrest.AssertEventually.assertThatEventually;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assume.assumeThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.async.ResultCallbackTemplate;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ContainerConfig;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.InternetProtocol;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.env.suite.service.ConfigurableServiceInstance;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstance;
import org.creekservice.internal.system.test.executor.execution.debug.DebugToolOptions;
import org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.DockerClientFactory;

@ExtendWith(MockitoExtension.class)
class DockerServiceContainerFunctionalTest {

    private static final String SERVICE_NAME = "test-service";
    private static final String SERVICE_IMAGE =
            "ghcr.io/creek-service/creek-system-test-test-service";

    private DockerServiceContainer instances;

    private final DockerClient dockerClient = DockerClientFactory.lazyClient();
    @Mock private ServiceDefinition serviceDef;
    @Mock private ServiceDebugInfo serviceDebugInfo;

    @BeforeEach
    void setUp() {
        when(serviceDebugInfo.attachMePort()).thenReturn(7857);
        when(serviceDebugInfo.baseServicePort()).thenReturn(8000);
        instances = new DockerServiceContainer(serviceDebugInfo);

        when(serviceDef.name()).thenReturn(SERVICE_NAME);
        when(serviceDef.dockerImage()).thenReturn(SERVICE_IMAGE);
    }

    @AfterEach
    void tearDown() {
        instances.forEach(ServiceInstance::stop);
        instances.clear();
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
        final String instanceId = instanceId(instance);

        // Then:
        assertThat(instance, is(running(false, instanceId)));
    }

    @Test
    void shouldStartAndStopServices() {
        // Given:
        final ServiceInstance instance0 = instances.add(serviceDef);
        final ServiceInstance instance1 = instances.add(serviceDef);
        String instanceId0 = instanceId(instance0);
        String instanceId1 = instanceId(instance1);

        // When:
        instance0.start();

        // Then:
        assertThat(instance0, is(running(true, instanceId0)));
        assertThat(instance1, is(running(false, instanceId1)));
        instanceId0 = instanceId(instance0);

        // When:
        instance1.start();

        // Then:
        assertThat(instance0, is(running(true, instanceId0)));
        assertThat(instance1, is(running(true, instanceId1)));
        instanceId1 = instanceId(instance1);

        // When:
        instance0.stop();

        // Then:
        assertThat(instance0, is(running(false, instanceId0)));
        assertThat(instance1, is(running(true, instanceId1)));

        // When:
        instance1.stop();

        // Then:
        assertThat(instance0, is(running(false, instanceId0)));
        assertThat(instance1, is(running(false, instanceId1)));
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
        final String instanceId = instanceId(instance);
        assertThat(instance, is(running(true, instanceId)));

        // When:
        instance.start();

        // Then: no error and nothing has changed:
        assertThat(instance, is(running(true, instanceId)));
    }

    @Test
    void shouldDoNothingOnSubsequentServiceStops() {
        // Given:
        final ServiceInstance instance = instances.add(serviceDef);
        instance.start();
        final String instanceId = instanceId(instance);
        instance.stop();
        assertThat(instance, is(running(false, instanceId)));

        // When:
        instance.stop();

        // Then: no error and nothing has changed:
        assertThat(instance, is(running(false, instanceId)));
    }

    @Test
    void shouldRestartService() {
        // Given:
        final ServiceInstance instance = instances.add(serviceDef);
        instance.start();
        final String oldInstanceId = instanceId(instance);
        instance.stop();

        // When:
        instance.start();

        // Then:
        assertThat(dockerContainerRunState(oldInstanceId), is(false));
        assertThat(dockerContainerRunState(instanceId(instance)), is(true));
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

    @SuppressWarnings("ConstantConditions")
    @Test
    void shouldSetEnvOnInstance() {
        // Given:
        final ConfigurableServiceInstance instance = instances.add(serviceDef);

        // When:
        instance.addEnv("CREEK_TEST_ENV_KEY_0", "expected value");

        // Then:
        instance.start();
        assertThat(instanceConfig(instance).getEnv(), is(notNullValue()));
        assertThat(
                List.of(instanceConfig(instance).getEnv()),
                hasItem("CREEK_TEST_ENV_KEY_0=expected value"));
    }

    @Test
    void shouldSetExposedPortOnInstance() {
        // Given:
        final ConfigurableServiceInstance instance = instances.add(serviceDef);
        instance.setStartupLogMessage(".*started.*", 1);

        // When:
        instance.addExposedPorts(8080);

        // Then:
        instance.start();
        assertThat(
                instanceConfig(instance).getExposedPorts(),
                is(new ExposedPort[] {new ExposedPort(8080, InternetProtocol.TCP)}));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void shouldDebugService() {
        // Given:
        assumeThat(
                "Skipping as attachMe agent not installed",
                DebugToolOptions.agentJarFile(),
                is(not(Optional.empty())));

        when(serviceDebugInfo.shouldDebug(any(), any())).thenReturn(true);
        final ConfigurableServiceInstance instance = instances.add(serviceDef);
        final int start = (int) System.currentTimeMillis() / 1000;

        // When:
        instance.start();

        // Then:
        final StringBuilder instanceLogs = trackContainerLogs(instance, start);
        final ContainerConfig containerConfig = instanceConfig(instance);
        assertThat(instanceConfig(instance).getEnv(), is(notNullValue()));
        assertThat(
                List.of(containerConfig.getEnv()),
                hasItem(
                        "JAVA_TOOL_OPTIONS="
                                + "-javaagent:/opt/creek/agent/attachme-agent-1.2.1.jar=port:7857,host:host.docker.internal "
                                + "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:8000"));
        assertThat(
                containerConfig.getExposedPorts(),
                is(new ExposedPort[] {new ExposedPort(8000, InternetProtocol.TCP)}));
        assertThatEventually(
                instanceLogs::toString,
                containsString("Listening for transport dt_socket at address: 8000"));
    }

    private StringBuilder trackContainerLogs(
            final ConfigurableServiceInstance instance, final int start) {
        final StringBuilder builder = new StringBuilder();
        dockerClient
                .logContainerCmd(((ContainerInstance) instance).containerId())
                .withStdErr(true)
                .withStdOut(true)
                .withFollowStream(true)
                .withSince(start)
                .exec(
                        new ResultCallbackTemplate<ResultCallback.Adapter<Frame>, Frame>() {
                            @Override
                            public void onNext(final Frame frame) {
                                builder.append(new String(frame.getPayload(), UTF_8));
                            }
                        });
        return builder;
    }

    private Matcher<ServiceInstance> running(
            final boolean expectedRunState, final String cachedInstanceId) {
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

                final boolean dockerContainerRunState =
                        dockerContainerRunState(item, cachedInstanceId);
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

    private boolean dockerContainerRunState(
            final ServiceInstance instance, final String cachedInstanceId) {
        final String containerId = ((ContainerInstance) instance).containerId();
        if (containerId == null && cachedInstanceId == null) {
            return false; // Never started
        }

        if (containerId != null && cachedInstanceId != null) {
            assertThat(containerId, is(cachedInstanceId));
        }

        final String id = requireNonNullElse(containerId, cachedInstanceId);
        return dockerContainerRunState(id);
    }

    private boolean dockerContainerRunState(final String containerId) {
        try {
            return Boolean.TRUE.equals(
                    dockerClient.inspectContainerCmd(containerId).exec().getState().getRunning());
        } catch (NotFoundException e) {
            return false;
        }
    }

    private ContainerConfig instanceConfig(final ServiceInstance instance) {
        return dockerClient
                .inspectContainerCmd(((ContainerInstance) instance).containerId())
                .exec()
                .getConfig();
    }

    private static List<ServiceInstance> instances(final DockerServiceContainer instances) {
        final List<ServiceInstance> result = new ArrayList<>(2);
        instances.forEach(result::add);
        return result;
    }

    private static String instanceId(final ServiceInstance instance) {
        return ((ContainerInstance) instance).containerId();
    }
}

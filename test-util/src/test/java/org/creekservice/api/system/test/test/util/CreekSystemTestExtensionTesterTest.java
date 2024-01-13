/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.system.test.test.util;

import static org.creekservice.api.system.test.executor.ExecutorOptions.ServiceDebugInfo.DEFAULT_BASE_DEBUG_PORT;
import static org.creekservice.api.system.test.test.util.CreekSystemTestExtensionTester.mount;
import static org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo.serviceDebugInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import org.creekservice.api.system.test.executor.ExecutorOptions.MountInfo;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstance;
import org.creekservice.api.system.test.extension.test.env.suite.service.ServiceInstanceContainer;
import org.creekservice.api.system.test.extension.test.model.Expectation;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler;
import org.creekservice.api.system.test.extension.test.model.ExpectationRef;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.api.system.test.extension.test.model.InputHandler;
import org.creekservice.api.system.test.extension.test.model.InputRef;
import org.creekservice.api.system.test.test.util.CreekSystemTestExtensionTester.YamlParserBuilder;
import org.creekservice.internal.system.test.executor.api.component.definition.ComponentDefinitions;
import org.creekservice.internal.system.test.executor.api.test.env.suite.service.DockerServiceContainer;
import org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("ContainerisedTest")
@ExtendWith(MockitoExtension.class)
class CreekSystemTestExtensionTesterTest {

    @Mock(strictness = LENIENT)
    private ServiceDefinition serviceDef;

    private CreekSystemTestExtensionTester tester;
    private CreekSystemTestExtensionTester.TesterBuilder builder;

    @BeforeEach
    void setUp() {
        builder = CreekSystemTestExtensionTester.tester();
        tester = builder.build();

        when(serviceDef.name()).thenReturn("bob");
        when(serviceDef.dockerImage())
                .thenReturn("ghcr.io/creek-service/creek-system-test-test-service");
    }

    @Test
    void shouldLoadExtensions() {
        assertThat(tester.accessibleExtensions(), is(empty()));
    }

    @Test
    void shouldExposeServiceDefinitions() {
        assertThat(tester.serviceDefinitions(), is(instanceOf(ComponentDefinitions.class)));
        assertThat(tester.serviceDefinitions().get("test-service"), is(notNullValue()));
    }

    @Test
    void shouldExposeAggregateDefinitions() {
        assertThat(tester.aggregateDefinitions(), is(instanceOf(ComponentDefinitions.class)));
        assertThat(tester.aggregateDefinitions().get("test-agg"), is(notNullValue()));
    }

    @Test
    void shouldExposeDockerBasedServicesContainer() {
        assertThat(tester.dockerServicesContainer(), is(instanceOf(DockerServiceContainer.class)));
    }

    @Test
    void shouldNotReturnContainerIdIfNonRunning() {
        // Given:
        tester.dockerServicesContainer().add(serviceDef);

        // When:
        final Map<String, String> result = tester.runningContainerIds();

        // Then:
        assertThat(result.entrySet(), is(empty()));
    }

    @Test
    void shouldReturnRunningContainerIds() {
        // Given:
        final ServiceInstanceContainer services = tester.dockerServicesContainer();
        final ServiceInstance instance = services.add(serviceDef);
        instance.start();

        try {

            // When:
            final Map<String, String> result = tester.runningContainerIds();

            // Then:
            assertThat(result.entrySet(), hasSize(1));
            assertThat(result, hasKey(instance.name()));
            assertThat(result.get(instance.name()), is(not(blankOrNullString())));
        } finally {
            instance.stop();
        }
    }

    @Test
    void shouldClear() {
        // Given:
        final ServiceInstanceContainer services = tester.dockerServicesContainer();
        services.add(serviceDef);
        assertThat(services.iterator().hasNext(), is(true));

        // When:
        tester.clearServices();

        // Then:
        assertThat(services.iterator().hasNext(), is(false));
    }

    @Test
    void shouldDefaultToNoServicesBeingDebug() {
        assertThat(tester.serviceDebugInfo(), is(ServiceDebugInfo.none()));
    }

    @Test
    void shouldSupportConfiguringServiceDebugInfo() {
        // Given:
        final ServiceDebugInfo debugServiceInfo =
                serviceDebugInfo(321, Set.of("a"), Set.of("b"), Map.of("k", "v"));
        tester = builder.withDebugServices(debugServiceInfo).build();

        // Then:
        assertThat(tester.serviceDebugInfo(), is(debugServiceInfo));
    }

    @Test
    void shouldSupportConfiguringServicesForDebugging() {
        // Given:
        tester = builder.withDebugServices("a").build();

        // Then:
        assertThat(
                tester.serviceDebugInfo(),
                is(serviceDebugInfo(DEFAULT_BASE_DEBUG_PORT, Set.of("a"), Set.of(), Map.of())));
    }

    @Test
    void shouldSupportMounts() {
        // Given:
        tester =
                builder.withReadOnlyMount(Paths.get("r/host/path"), Paths.get("r/container/path"))
                        .withWritableMount(Paths.get("w/host/path"), Paths.get("w/container/path"))
                        .build();

        // Then:
        assertThat(
                tester.mountInfo(),
                contains(
                        mount(Paths.get("r/host/path"), Paths.get("r/container/path"), true),
                        mount(Paths.get("w/host/path"), Paths.get("w/container/path"), false)));

        assertThat(tester.mountInfo().get(0).hostPath(), is(Path.of("r/host/path")));
        assertThat(tester.mountInfo().get(0).containerPath(), is(Path.of("r/container/path")));
        assertThat(tester.mountInfo().get(0).readOnly(), is(true));
    }

    @Test
    void shouldSupportEnv() {
        // Given:
        tester = builder.withEnv("k", "v").withEnv("k2", "v2").build();

        // Then:
        assertThat(tester.env(), is(Map.of("k", "v", "k2", "v2")));
    }

    @Test
    void shouldBuildRefParser() throws Exception {
        // Given:
        final YamlParserBuilder builder = CreekSystemTestExtensionTester.yamlParser();
        builder.model().addRef(TestRef.class).withName("test/ref");

        // When:
        final ModelParser parser = builder.build();

        // Then: formatting:off:
        final String yaml = "---\n"
                + "!test/ref\n"
                + "id: bob";
        // formatting:on:

        final TestRef parsed = parser.parseRef(yaml, TestRef.class);
        assertThat(parsed.id(), is("bob"));
    }

    @Test
    void shouldBuildInputParser() throws Exception {
        // Given:
        final YamlParserBuilder builder = CreekSystemTestExtensionTester.yamlParser();
        builder.model()
                .addInput(TestInput.class, mock(TestInputHandler.class))
                .withName("test/input");

        // When:
        final ModelParser parser = builder.build();

        // Then: formatting:off:
        final String yaml = "---\n"
                + "!test/input\n"
                + "name: bob";
        // formatting:on:

        final TestInput parsed = parser.parseInput(yaml, TestInput.class);
        assertThat(parsed.name, is("bob"));
    }

    @Test
    void shouldBuildExpectationParser() throws Exception {
        // Given:
        final YamlParserBuilder builder = CreekSystemTestExtensionTester.yamlParser();
        builder.model()
                .addExpectation(TestExpectation.class, mock(TestExpectationHandler.class))
                .withName("test/expectation");

        // When:
        final ModelParser parser = builder.build();

        // Then: formatting:off:
        final String yaml = "---\n"
                + "!test/expectation\n"
                + "name: bob";
        // formatting:on:

        final TestExpectation parsed = parser.parseExpectation(yaml, TestExpectation.class);
        assertThat(parsed.name, is("bob"));
    }

    @Test
    void shouldBuildOtherParser() throws Exception {
        // Given:
        final YamlParserBuilder builder = CreekSystemTestExtensionTester.yamlParser();

        // When:
        final ModelParser parser = builder.build();

        // Then: formatting:off:
        final String yaml = "---\n"
                + "bob";
        // formatting:on:

        final String parsed = parser.parseOther(yaml, String.class);
        assertThat(parsed, is("bob"));
    }

    @Test
    void shouldThrowOnParseFailed() {
        // Given:
        final ModelParser parser = CreekSystemTestExtensionTester.yamlParser().build();

        // When:
        final Exception e =
                assertThrows(IOException.class, () -> parser.parseOther("not-yaml", Integer.class));

        // Then:
        assertThat(e.getMessage(), startsWith("Cannot deserialize value"));
    }

    @Test
    void shouldImplementHashCodeAndEqualsOnMount() {
        new EqualsTester()
                .addEqualityGroup(
                        mount(Paths.get("r/host/path"), Paths.get("r/container/path"), true),
                        mount(Paths.get("r/host/path"), Paths.get("r/container/path"), true))
                .addEqualityGroup(mount(Paths.get("diff"), Paths.get("r/container/path"), true))
                .addEqualityGroup(mount(Paths.get("r/host/path"), Paths.get("diff"), true))
                .addEqualityGroup(
                        mount(Paths.get("r/host/path"), Paths.get("r/container/path"), false))
                .testEquals();
    }

    @Test
    void shouldToStringOnMount() {
        // Given:
        final MountInfo mount =
                mount(Paths.get("r/host/path"), Paths.get("r/container/path"), true);

        // Then:
        assertThat(
                mount.toString(),
                is("Mount{hostPath=r/host/path, containerPath=r/container/path, readOnly=true}"));
    }

    public static final class TestRef implements InputRef, ExpectationRef {

        private final String id;

        @SuppressWarnings("RedundantModifier")
        public TestRef(@JsonProperty("id") final String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }
    }

    public static final class TestInput implements Input {
        private final String name;

        @SuppressWarnings("RedundantModifier")
        public TestInput(@JsonProperty("name") final String id) {
            this.name = id;
        }
    }

    private interface TestInputHandler extends InputHandler<TestInput> {}

    public static final class TestExpectation implements Expectation {
        private final String name;

        @SuppressWarnings("RedundantModifier")
        public TestExpectation(@JsonProperty("name") final String id) {
            this.name = id;
        }
    }

    private interface TestExpectationHandler extends ExpectationHandler<TestExpectation> {}
}

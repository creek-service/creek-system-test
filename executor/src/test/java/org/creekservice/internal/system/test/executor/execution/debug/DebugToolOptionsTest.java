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

package org.creekservice.internal.system.test.executor.execution.debug;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DebugToolOptionsTest {

    @TempDir private Path testDir;

    @Test
    void shouldBuildJavaToolOptionsIfAgentJarPresent() {
        assertThat(
                DebugToolOptions.javaToolOptions(
                        7857, 8000, Optional.of("attachme-agent-1.1.0.jar")),
                is(
                        Optional.of(
                                "-javaagent:/opt/creek/agent/attachme-agent-1.1.0.jar="
                                        + "port:7857,host:host.docker.internal "
                                        + "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:8000")));
    }

    @Test
    void shouldReturnEmptyIfAgentJarMissing() {
        assertThat(
                DebugToolOptions.javaToolOptions(7857, 8000, Optional.empty()),
                is(Optional.empty()));
    }

    @Test
    void shouldReturnEmptyIfNoAttachMeDir() {
        assertThat(DebugToolOptions.attacheMeJarPath(testDir), is(Optional.empty()));
    }

    @Test
    void shouldReturnEmptyIfNoAgentJar() throws Exception {
        // Given:
        Files.createDirectories(testDir.resolve(".attachme"));

        // Then:
        assertThat(DebugToolOptions.attacheMeJarPath(testDir), is(Optional.empty()));
    }

    @Test
    void shouldPickLatestAgentJar() throws Exception {
        // Given:
        final Path attachMe = testDir.resolve(".attachme");
        Files.createDirectories(attachMe);
        Files.createFile(attachMe.resolve("aaa.jar"));
        Files.createFile(attachMe.resolve("attachme-agent-1.0.0.jar"));
        Files.createFile(attachMe.resolve("attachme-agent-1.1.0.jar"));
        Files.createFile(attachMe.resolve("bbb.jar"));

        // Then:
        assertThat(
                DebugToolOptions.attacheMeJarPath(testDir),
                is(Optional.of("attachme-agent-1.1.0.jar")));
    }
}

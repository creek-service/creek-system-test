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


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;
import org.creekservice.api.base.annotation.VisibleForTesting;

/** Leverage IntelliJ's AttacheMe plugin to support debugging of docker containers */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class DebugToolOptions {

    private static final PathMatcher JAR_MATCHER =
            FileSystems.getDefault().getPathMatcher("glob:**.jar");
    private static final Optional<Path> AGENT_JAR_FILE =
            findAttacheMeAgentJar(Paths.get(System.getProperty("user.home")));

    private DebugToolOptions() {}

    /**
     * @param attachMePort the port the attachMe agent will use to call out to the attachMe plugin
     *     to request the debugger to attach.
     * @param serviceDebugPort the port the service should listen on for the debugger to attach.
     * @return {@link Optional#empty()} if no attach me jar found in users home directory, else the
     *     required {@code JAVA_TOOL_OPTIONS}.
     */
    public static Optional<String> javaToolOptions(
            final int attachMePort, final int serviceDebugPort) {
        return javaToolOptions(attachMePort, serviceDebugPort, AGENT_JAR_FILE);
    }

    /** @return the path to the AttachMe agent jar file, if present in the users home directory. */
    public static Optional<Path> agentJarFile() {
        return AGENT_JAR_FILE;
    }

    @VisibleForTesting
    static Optional<String> javaToolOptions(
            final int attachMePort, final int serviceDebugPort, final Optional<Path> agentJarFile) {
        return agentJarFile.map(
                jar ->
                        "-javaagent:/opt/creek/agent/"
                                + jar
                                + "=port:"
                                + attachMePort
                                + ",host:host.docker.internal -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:"
                                + serviceDebugPort);
    }

    @VisibleForTesting
    static Optional<Path> findAttacheMeAgentJar(final Path homeDir) {
        final Path dir = homeDir.resolve(".attachme");
        if (Files.notExists(dir)) {
            return Optional.empty();
        }

        try (Stream<Path> stream = Files.list(dir)) {
            return stream.filter(Files::isRegularFile)
                    .filter(JAR_MATCHER::matches)
                    .map(Path::getFileName)
                    .filter(p -> p.toString().startsWith("attachme-agent-"))
                    .sorted()
                    .reduce((l, r) -> r);
        } catch (final IOException e) {
            return Optional.empty();
        }
    }
}

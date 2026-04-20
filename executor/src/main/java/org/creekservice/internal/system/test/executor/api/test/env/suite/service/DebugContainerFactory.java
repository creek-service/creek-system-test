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

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

final class DebugContainerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugContainerFactory.class);

    GenericContainer<?> create(
            final DockerImageName imageName,
            final int serviceDebugPort,
            final Runnable startingHook) {

        LOGGER.info(
                "Creating debuggable container. image-name: "
                        + imageName
                        + ", service-debug-port: "
                        + serviceDebugPort);

        return new HookableFixedHostPortContainer(imageName, startingHook)
                .withFixedExposedPort(serviceDebugPort, serviceDebugPort);
    }

    /**
     * A {@link FixedHostPortGenericContainer} that fires a starting hook from {@link
     * #containerIsStarting} — after the container process has started and mapped ports are
     * assigned, but before the wait strategy completes.
     */
    @SuppressWarnings("deprecation") // Deprecated as uncommon, but this is a valid use case.
    @SuppressFBWarnings("EQ_DOESNT_OVERRIDE_EQUALS")
    private static final class HookableFixedHostPortContainer
            extends FixedHostPortGenericContainer<HookableFixedHostPortContainer> {

        private final Runnable startingHook;

        HookableFixedHostPortContainer(
                final DockerImageName imageName, final Runnable startingHook) {
            super(imageName.toString());
            this.startingHook = requireNonNull(startingHook, "startingHook");
        }

        @Override
        protected void containerIsStarting(
                final com.github.dockerjava.api.command.InspectContainerResponse containerInfo) {
            super.containerIsStarting(containerInfo);
            startingHook.run();
        }
    }
}

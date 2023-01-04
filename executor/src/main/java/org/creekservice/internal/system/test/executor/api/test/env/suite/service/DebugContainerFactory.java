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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

final class DebugContainerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugContainerFactory.class);

    @SuppressWarnings("deprecation") // Deprecated as uncommon, but this is valid use case.
    GenericContainer<?> create(final DockerImageName imageName, final int serviceDebugPort) {

        LOGGER.info(
                "Creating debuggable container. image-name: "
                        + imageName
                        + ", service-debug-port: "
                        + serviceDebugPort);

        return new FixedHostPortGenericContainer<>(imageName.toString())
                .withFixedExposedPort(serviceDebugPort, serviceDebugPort);
    }
}

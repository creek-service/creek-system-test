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

package org.creekservice.api.system.test.executor;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.creekservice.api.base.type.JarVersion;
import org.creekservice.internal.system.test.executor.PicoCliParser;

/** Entry point for running system tests */
public final class SystemTestExecutor {

    private static final Logger LOGGER = LogManager.getLogger(SystemTestExecutor.class);

    private SystemTestExecutor() {}

    public static void main(final String... args) {
        try {
            PicoCliParser.parse(args).ifPresent(SystemTestExecutor::run);
        } catch (final Exception e) {
            LOGGER.fatal(e.getMessage(), e);
            System.exit(1);
        }
    }

    public static void run(final ExecutorOptions options) {
        if (options.echoOnly()) {
            LOGGER.info(
                    "SystemTestExecutor: "
                            + JarVersion.jarVersion(SystemTestExecutor.class).orElse("unknown"));
            LOGGER.info(options);
        }
    }
}

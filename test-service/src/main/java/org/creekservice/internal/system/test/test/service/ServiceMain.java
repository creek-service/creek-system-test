/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.internal.system.test.test.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.creekservice.api.observability.lifecycle.BasicLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServiceMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMain.class);

    private ServiceMain() {}

    public static void main(final String... args) {
        dumpEnv();
        maybeFail();
        logMount();
        doLogging();
        awaitShutdown();
    }

    private static void dumpEnv() {
        System.getenv().entrySet().stream()
                .filter(e -> e.getKey().startsWith("CREEK_"))
                .forEach(e -> LOGGER.info("Env: " + e.getKey() + "=" + e.getValue()));
    }

    private static void maybeFail() {
        if (System.getenv("CREEK_SERVICE_SHOULD_FAIL") != null) {
            LOGGER.error("CREEK_SERVICE_SHOULD_FAIL is set, so this service is going bye-byes!");
            throw new RuntimeException("Service going BOOM!");
        }
    }

    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    private static void logMount() {
        final Path mount = Paths.get("/opt/creek/test_mount");
        if (!Files.isDirectory(mount)) {
            LOGGER.info("/opt/creek/test_mount : not-present");
            return;
        }

        if (Files.isWritable(mount)) {
            LOGGER.info("/opt/creek/test_mount : writable");
        } else {
            LOGGER.info("/opt/creek/test_mount : read-only");
        }

        if (Files.isRegularFile(mount.resolve("some.file"))) {
            LOGGER.info("some.file : present");
        } else {
            LOGGER.info("some.file : not-present");
        }
    }

    private static void doLogging() {
        System.out.println("System.out logging");
        System.err.println("System.err logging");
        LOGGER.info("LOGGER.info logging");
        LOGGER.error("LOGGER.error logging");

        // Log the line the system tests is waiting for to indicate the service has started:
        LOGGER.info(BasicLifecycle.started.logMessage(BasicLifecycle.SERVICE_TYPE));
    }

    @SuppressWarnings("BusyWait")
    private static void awaitShutdown() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}

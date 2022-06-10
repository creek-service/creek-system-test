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

package org.creekservice.internal.system.test.test.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServiceMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMain.class);

    private ServiceMain() {}

    public static void main(final String... args) {
        doLogging();
        awaitShutdown();
    }

    private static void doLogging() {
        System.out.println("System.out logging");
        System.err.println("System.err logging");
        LOGGER.info("LOGGER.info logging");
        LOGGER.error("LOGGER.error logging");

        LOGGER.info("some.lifecycle.event=started");
    }

    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
    private static void awaitShutdown() {
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // meh
            }
        }
    }
}

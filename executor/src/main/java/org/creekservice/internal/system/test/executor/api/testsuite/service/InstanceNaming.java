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

package org.creekservice.internal.system.test.executor.api.testsuite.service;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/** Naming strategy for service instances. */
public final class InstanceNaming {

    private final Map<String, AtomicInteger> names = new HashMap<>();

    public String instanceName(final String serviceName) {
        requireNonNull(serviceName, "serviceName");
        final AtomicInteger counter = names.computeIfAbsent(serviceName, k -> new AtomicInteger());
        return serviceName + "-" + counter.getAndIncrement();
    }

    public void clear() {
        names.clear();
    }
}

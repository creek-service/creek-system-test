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

package org.creekservice.internal.system.test.executor.api;


import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.service.ServiceContainer;
import org.creekservice.api.system.test.extension.service.ServiceDefinition;
import org.creekservice.api.system.test.extension.service.ServiceInstance;

/** A local, docker based, implementation of {@link ServiceContainer}. */
public final class LocalServiceInstances implements ServiceContainer {

    private final long threadId;
    private final List<ServiceInstance> instances = new ArrayList<>();

    public LocalServiceInstances() {
        this(Thread.currentThread().getId());
    }

    @VisibleForTesting
    LocalServiceInstances(final long threadId) {
        this.threadId = threadId;
    }

    @Override
    public ServiceInstance start(final ServiceDefinition def) {
        throwIfNotOnCorrectThread();
        final Instance instance = new Instance(def);
        instances.add(instance);
        return instance;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<ServiceInstance> iterator() {
        throwIfNotOnCorrectThread();
        return instances.iterator();
    }

    private void throwIfNotOnCorrectThread() {
        if (Thread.currentThread().getId() != threadId) {
            throw new ConcurrentModificationException("Class is not thread safe");
        }
    }

    private static final class Instance implements ServiceInstance {
        Instance(final ServiceDefinition def) {}

        @Override
        public void stop() {}
    }
}

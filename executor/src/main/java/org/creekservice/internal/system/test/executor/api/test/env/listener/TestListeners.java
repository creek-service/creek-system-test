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

package org.creekservice.internal.system.test.executor.api.test.env.listener;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.base.type.Iterators;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.env.listener.TestListenerContainer;

/** Implementation of {@link TestListenerContainer} */
public final class TestListeners implements TestListenerContainer {

    private final long threadId;
    private final List<TestEnvironmentListener> listeners = new ArrayList<>();

    /** Constructor. */
    public TestListeners() {
        this(Thread.currentThread().getId());
    }

    @VisibleForTesting
    TestListeners(final long threadId) {
        this.threadId = threadId;
    }

    @Override
    public void append(final TestEnvironmentListener listener) {
        throwIfNotOnCorrectThread();
        listeners.add(requireNonNull(listener, "listener"));
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<TestEnvironmentListener> iterator() {
        throwIfNotOnCorrectThread();
        return listeners.iterator();
    }

    @Override
    public Iterator<TestEnvironmentListener> reverseIterator() {
        throwIfNotOnCorrectThread();
        return Iterators.reverseIterator(listeners);
    }

    private void throwIfNotOnCorrectThread() {
        if (Thread.currentThread().getId() != threadId) {
            throw new ConcurrentModificationException("Class is not thread safe");
        }
    }
}

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

package org.creekservice.api.system.test.extension.test.env.listener;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.function.Consumer;

/** Collection of test package listeners */
public interface TestListenerCollection extends Iterable<TestEnvironmentListener> {

    /**
     * Returns an iterator that iterators over the collections elements in reverse order.
     *
     * @return a reverse iterator.
     */
    Iterator<TestEnvironmentListener> reverseIterator();

    /**
     * Performs the given action for each element of the Iterable until all elements have been
     * processed or the action throws an exception. Actions are performed in the reverse order of
     * iteration. Exceptions thrown by the action are relayed to the caller.
     *
     * @param action The action to be performed for each element
     */
    default void forEachReverse(Consumer<? super TestEnvironmentListener> action) {
        requireNonNull(action);
        final Iterator<TestEnvironmentListener> it = reverseIterator();
        while (it.hasNext()) {
            action.accept(it.next());
        }
    }
}

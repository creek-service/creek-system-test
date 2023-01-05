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

package org.creekservice.api.system.test.extension.test.env.listener;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import org.creekservice.api.base.type.Iterators;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestListenerCollectionTest {

    @Mock private Consumer<? super TestEnvironmentListener> action;
    @Mock private TestEnvironmentListener listener0;
    @Mock private TestEnvironmentListener listener1;

    @Test
    void shouldNotCallActionIfEmpty() {
        // Given:
        final TestListenerCollection collection = new TestImpl(List.of());

        // When:
        collection.forEachReverse(action);

        // Then:
        verifyNoInteractions(action);
    }

    @Test
    void shouldCallActionInReverse() {
        // Given:
        final TestListenerCollection collection = new TestImpl(List.of(listener0, listener1));

        // When:
        collection.forEachReverse(action);

        // Then:
        final InOrder inOrder = inOrder(action);
        inOrder.verify(action).accept(listener1);
        inOrder.verify(action).accept(listener0);
    }

    private static final class TestImpl implements TestListenerCollection {

        final List<TestEnvironmentListener> listeners;

        TestImpl(final List<TestEnvironmentListener> listeners) {
            this.listeners = listeners;
        }

        @Override
        public Iterator<TestEnvironmentListener> reverseIterator() {
            return Iterators.reverseIterator(listeners);
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Iterator<TestEnvironmentListener> iterator() {
            return listeners.iterator();
        }
    }
}

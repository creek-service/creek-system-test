/*
 * Copyright 2022-2026 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.internal.system.test.executor.execution.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.creekservice.api.system.test.extension.test.env.listener.TestEnvironmentListener;
import org.creekservice.api.system.test.extension.test.env.listener.TestListenerCollection;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.internal.system.test.executor.execution.input.Inputters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeedingListenerTest {

    @Mock private Inputters inputters;
    @Mock private TestListenerCollection listeners;
    @Mock private TestEnvironmentListener observer;
    @Mock private TestEnvironmentListener observer2;
    @Mock private CreekTestSuite suite;
    private SeedingListener listener;

    @BeforeEach
    void setUp() {
        listener = new SeedingListener(inputters, listeners);
        org.mockito.Mockito.lenient()
                .when(listeners.iterator())
                .thenReturn(Collections.emptyIterator());
    }

    @Test
    void shouldInjectSeedDataOnBeforeSuite() {
        // Given:
        final List<Input> seedData = List.of();
        doReturn(seedData).when(suite).seedData();

        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(inputters).input(seedData, suite);
    }

    @Test
    void shouldThrowIfInputtersThrows() {
        // Given:
        final RuntimeException expected = new RuntimeException("Boom");
        doThrow(expected).when(inputters).input(any(), any());

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> listener.beforeSuite(suite));

        // Then:
        assertThat(e, is(expected));
    }

    @Test
    void shouldNotifyListenersAfterSeeding() {
        // Given:
        when(listeners.iterator()).thenReturn(List.of(observer, observer2).iterator());

        // When:
        listener.beforeSuite(suite);

        // Then:
        final InOrder order = inOrder(inputters, observer, observer2);
        order.verify(inputters).input(any(), any());
        order.verify(observer).afterSeeding(suite);
        order.verify(observer2).afterSeeding(suite);
    }
}

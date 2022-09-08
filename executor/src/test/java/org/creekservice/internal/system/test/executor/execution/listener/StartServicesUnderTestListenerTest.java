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

package org.creekservice.internal.system.test.executor.execution.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Supplier;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.suite.service.ServiceInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StartServicesUnderTestListenerTest {

    @Mock private CreekTestSuite suite;

    @Mock(strictness = LENIENT)
    private Supplier<List<ServiceInstance>> servicesSupplier;

    @Mock private ServiceInstance instance0;
    @Mock private ServiceInstance instance1;
    @Mock private ServiceInstance instance2;
    private StartServicesUnderTestListener listener;

    @BeforeEach
    void setUp() {
        listener = new StartServicesUnderTestListener(servicesSupplier);

        when(servicesSupplier.get()).thenReturn(List.of(instance0, instance1, instance2));
    }

    @Test
    void shouldStartServicesBeforeSuiteInOrder() {
        // When:
        listener.beforeSuite(suite);

        // Then:
        final InOrder inOrder = inOrder(instance0, instance1, instance2);
        inOrder.verify(instance0).start();
        inOrder.verify(instance1).start();
        inOrder.verify(instance2).start();
    }

    @Test
    void shouldStopServicesAfterSuiteInReverseOrder() {
        // Given:
        listener.beforeSuite(suite);
        when(servicesSupplier.get()).thenThrow(new AssertionError("Impl should cache instances"));

        // When:
        listener.afterSuite(suite);

        // Then:
        final InOrder inOrder = inOrder(instance0, instance1, instance2);
        inOrder.verify(instance2).stop();
        inOrder.verify(instance1).stop();
        inOrder.verify(instance0).stop();
    }

    @Test
    void shouldThrowOnServiceStartFailure() {
        // Given:
        final RuntimeException expected = new RuntimeException("Boom");
        doThrow(expected).when(instance0).start();

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> listener.beforeSuite(suite));

        // Then:
        assertThat(e, is(sameInstance(expected)));
    }

    @Test
    void shouldStopStartingServicesIfOnethrows() {
        // Given:
        doThrow(new RuntimeException("Boom")).when(instance1).start();

        // When:
        assertThrows(RuntimeException.class, () -> listener.beforeSuite(suite));

        // Then:
        verify(instance0).start();
        verify(instance2, never()).start();
    }

    @Test
    void shouldStopStartedServicesOnAfterSuiteEvenIfBeforeSuiteThrew() {
        // Given:
        doThrow(new RuntimeException("Boom")).when(instance1).start();
        assertThrows(Exception.class, () -> listener.beforeSuite(suite));

        // When:
        listener.afterSuite(suite);

        // Then:
        verify(instance0).stop();
        verify(instance1, never()).stop();
        verify(instance2, never()).stop();
    }
}

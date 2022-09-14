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

package org.creekservice.internal.system.test.executor.execution.debug;

import static org.creekservice.internal.system.test.executor.execution.debug.ServiceDebugInfo.serviceDebugInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import java.util.Set;
import org.creekservice.api.system.test.executor.ExecutorOptions;
import org.junit.jupiter.api.Test;

class ServiceDebugInfoTest {

    @Test
    void shouldThrowNPE() {
        final NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(ServiceDebugInfo.class);
        tester.testAllPublicStaticMethods(ServiceDebugInfo.class);
        tester.testAllPublicInstanceMethods(ServiceDebugInfo.none());
        tester.testAllPublicInstanceMethods(
                ServiceDebugInfo.serviceDebugInfo(7548, 8000, Set.of("a"), Set.of("b")));
    }

    @Test
    void shouldImplementHashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(
                        ServiceDebugInfo.none(),
                        ServiceDebugInfo.none(),
                        serviceDebugInfo(7548, 8000, Set.of(), Set.of()))
                .addEqualityGroup(
                        serviceDebugInfo(7548, 8000, Set.of("s"), Set.of("i")),
                        serviceDebugInfo(7548, 8000, Set.of("S"), Set.of("I")))
                .addEqualityGroup(serviceDebugInfo(1, 8000, Set.of("s"), Set.of("i")))
                .addEqualityGroup(serviceDebugInfo(7548, 1, Set.of("s"), Set.of("i")))
                .addEqualityGroup(serviceDebugInfo(7548, 8000, Set.of("diff"), Set.of("i")))
                .addEqualityGroup(serviceDebugInfo(7548, 8000, Set.of("s"), Set.of("diff")))
                .testEquals();
    }

    @Test
    void shouldNotCopyImmutable() {
        // Given:
        final ServiceDebugInfo info = serviceDebugInfo(7548, 8000, Set.of("s"), Set.of("i"));

        // When:
        final ServiceDebugInfo result = ServiceDebugInfo.copyOf(info);

        // Then:
        assertThat(result, is(sameInstance(info)));
    }

    @Test
    void shouldCopy() {
        // Given:
        final ExecutorOptions.ServiceDebugInfo info = mock(ExecutorOptions.ServiceDebugInfo.class);
        when(info.attachMePort()).thenReturn(5897);
        when(info.baseServicePort()).thenReturn(3894);
        when(info.serviceNames()).thenReturn(Set.of("s"));
        when(info.serviceInstanceNames()).thenReturn(Set.of("i"));

        // When:
        final ServiceDebugInfo result = ServiceDebugInfo.copyOf(info);

        // Then:
        assertThat(result, is(serviceDebugInfo(5897, 3894, Set.of("s"), Set.of("i"))));
    }

    @Test
    void shouldThrowOnInvalidAttachMePort() {
        assertThrows(
                IllegalArgumentException.class,
                () -> serviceDebugInfo(-1, 8000, Set.of("s"), Set.of("i")));
        assertThrows(
                IllegalArgumentException.class,
                () -> serviceDebugInfo(0, 8000, Set.of("s"), Set.of("i")));
    }

    @Test
    void shouldThrowOnInvalidBaseServiceMePort() {
        assertThrows(
                IllegalArgumentException.class,
                () -> serviceDebugInfo(7548, -1, Set.of("s"), Set.of("i")));
        assertThrows(
                IllegalArgumentException.class,
                () -> serviceDebugInfo(7548, 0, Set.of("s"), Set.of("i")));
    }

    @Test
    void shouldThrowOnMatchingPorts() {
        assertThrows(
                IllegalArgumentException.class,
                () -> serviceDebugInfo(700, 700, Set.of("s"), Set.of("i")));
    }

    @Test
    void shouldReturnNoServicesFromNone() {
        assertThat(ServiceDebugInfo.none().serviceNames(), is(empty()));
    }

    @Test
    void shouldKnownWhichServicesToDebug() {
        // Given:
        final ServiceDebugInfo info = serviceDebugInfo(7548, 8000, Set.of("s0", "s1"), Set.of("i"));

        // Then:
        assertThat(info.shouldDebug("s0", "n/a"), is(true));
        assertThat(info.shouldDebug("s1", "n/a"), is(true));
        assertThat(info.shouldDebug("s2", "n/a"), is(false));
    }

    @Test
    void shouldKnownWhichInstancesToDebug() {
        // Given:
        final ServiceDebugInfo info = serviceDebugInfo(7548, 8000, Set.of("s"), Set.of("i0", "i1"));

        // Then:
        assertThat(info.shouldDebug("n/a", "i0"), is(true));
        assertThat(info.shouldDebug("n/a", "i1"), is(true));
        assertThat(info.shouldDebug("n/a", "i2"), is(false));
    }

    @Test
    void shouldBeCaseInsensitive() {
        final ServiceDebugInfo info = serviceDebugInfo(7548, 8000, Set.of("s"), Set.of("I"));
        assertThat(info.shouldDebug("S", "n/a"), is(true));
        assertThat(info.shouldDebug("n/a", "i"), is(true));
    }
}

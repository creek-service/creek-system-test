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
import java.util.Map;
import java.util.Set;
import org.creekservice.api.system.test.executor.ExecutorOptions;
import org.junit.jupiter.api.Test;

class ServiceDebugInfoTest {

    @Test
    void shouldThrowNPE() {
        final NullPointerTester tester =
                new NullPointerTester().setDefault(Set.class, Set.of("something"));
        tester.testAllPublicConstructors(ServiceDebugInfo.class);
        tester.testAllPublicStaticMethods(ServiceDebugInfo.class);
        tester.testAllPublicInstanceMethods(ServiceDebugInfo.none());
        tester.testAllPublicInstanceMethods(
                ServiceDebugInfo.serviceDebugInfo(8000, Set.of("a"), Set.of("b"), Map.of()));
    }

    @Test
    void shouldImplementHashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(
                        ServiceDebugInfo.none(),
                        ServiceDebugInfo.none(),
                        serviceDebugInfo(8000, Set.of(), Set.of(), Map.of()))
                .addEqualityGroup(
                        serviceDebugInfo(8000, Set.of("s"), Set.of("i"), Map.of("k", "v")),
                        serviceDebugInfo(8000, Set.of("S"), Set.of("I"), Map.of("k", "v")))
                .addEqualityGroup(serviceDebugInfo(1, Set.of("s"), Set.of("i"), Map.of("k", "v")))
                .addEqualityGroup(
                        serviceDebugInfo(8000, Set.of("diff"), Set.of("i"), Map.of("k", "v")))
                .addEqualityGroup(
                        serviceDebugInfo(8000, Set.of("s"), Set.of("diff"), Map.of("k", "v")))
                .addEqualityGroup(
                        serviceDebugInfo(8000, Set.of("s"), Set.of("i"), Map.of("K", "v")))
                .addEqualityGroup(
                        serviceDebugInfo(8000, Set.of("s"), Set.of("i"), Map.of("k", "V")))
                .testEquals();
    }

    @Test
    void shouldNotCopyImmutable() {
        // Given:
        final ServiceDebugInfo info =
                serviceDebugInfo(8000, Set.of("s"), Set.of("i"), Map.of("k", "v"));

        // When:
        final ServiceDebugInfo result = ServiceDebugInfo.copyOf(info);

        // Then:
        assertThat(result, is(sameInstance(info)));
    }

    @Test
    void shouldCopy() {
        // Given:
        final ExecutorOptions.ServiceDebugInfo info = mock(ExecutorOptions.ServiceDebugInfo.class);
        when(info.baseServicePort()).thenReturn(3894);
        when(info.serviceNames()).thenReturn(Set.of("s"));
        when(info.serviceInstanceNames()).thenReturn(Set.of("i"));
        when(info.env()).thenReturn(Map.of("k", "v"));

        // When:
        final ServiceDebugInfo result = ServiceDebugInfo.copyOf(info);

        // Then:
        assertThat(result, is(serviceDebugInfo(3894, Set.of("s"), Set.of("i"), Map.of("k", "v"))));
    }

    @Test
    void shouldThrowOnInvalidBaseServiceMePort() {
        assertThrows(
                IllegalArgumentException.class,
                () -> serviceDebugInfo(-1, Set.of("s"), Set.of("i"), Map.of()));
        assertThrows(
                IllegalArgumentException.class,
                () -> serviceDebugInfo(0, Set.of("s"), Set.of("i"), Map.of()));
    }

    @Test
    void shouldReturnNoServicesFromNone() {
        assertThat(ServiceDebugInfo.none().serviceNames(), is(empty()));
    }

    @Test
    void shouldKnownWhichServicesToDebug() {
        // Given:
        final ServiceDebugInfo info =
                serviceDebugInfo(8000, Set.of("s0", "s1"), Set.of("i"), Map.of());

        // Then:
        assertThat(info.shouldDebug("s0", "n/a"), is(true));
        assertThat(info.shouldDebug("s1", "n/a"), is(true));
        assertThat(info.shouldDebug("s2", "n/a"), is(false));
    }

    @Test
    void shouldKnownWhichInstancesToDebug() {
        // Given:
        final ServiceDebugInfo info =
                serviceDebugInfo(8000, Set.of("s"), Set.of("i0", "i1"), Map.of());

        // Then:
        assertThat(info.shouldDebug("n/a", "i0"), is(true));
        assertThat(info.shouldDebug("n/a", "i1"), is(true));
        assertThat(info.shouldDebug("n/a", "i2"), is(false));
    }

    @Test
    void shouldBeCaseInsensitive() {
        final ServiceDebugInfo info = serviceDebugInfo(8000, Set.of("s"), Set.of("I"), Map.of());
        assertThat(info.shouldDebug("S", "n/a"), is(true));
        assertThat(info.shouldDebug("n/a", "i"), is(true));
    }

    @Test
    void shouldTrackEnvToSetOnServicesBeingDebugged() {
        final ServiceDebugInfo info =
                serviceDebugInfo(8000, Set.of("s"), Set.of("I"), Map.of("k", "v"));
        assertThat(info.env(), is(Map.of("k", "v")));
    }
}

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

package org.creekservice.api.system.test.extension.test.env.suite.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceInstanceCollectionTest {

    @Mock private ConfigurableServiceInstance instance0;
    @Mock private ConfigurableServiceInstance instance1;

    @Test
    void shouldStream() {
        // Given:
        final ServiceInstanceCollection services = new TestServiceCollection();

        // When:
        final Stream<ConfigurableServiceInstance> s = services.stream();

        // Then:
        assertThat(s.collect(Collectors.toList()), is(List.of(instance0, instance1)));
    }

    private final class TestServiceCollection implements ServiceInstanceCollection {

        @Override
        public ConfigurableServiceInstance get(final String name) {
            return null;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Iterator<ConfigurableServiceInstance> iterator() {
            return List.of(instance0, instance1).iterator();
        }
    }
}

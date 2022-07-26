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

package org.creekservice.api.system.test.extension.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ServiceDefinitionCollectionTest {

    @Test
    void shouldStream() {
        // Given:
        final TestContainer collection = new TestContainer();

        // When:
        final List<ServiceDefinition> result = collection.stream().collect(Collectors.toList());

        // Then:
        assertThat(result, is(collection.defs));
    }

    private static final class TestContainer implements ServiceDefinitionCollection {

        final List<ServiceDefinition> defs =
                List.of(mock(ServiceDefinition.class), mock(ServiceDefinition.class));

        @Override
        public ServiceDefinition get(final String serviceName) {
            return null;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Iterator<ServiceDefinition> iterator() {
            return defs.iterator();
        }
    }
}

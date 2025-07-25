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

package org.creekservice.internal.system.test.executor.api.component.definition;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.when;

import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregateDescriptorBasedDefinitionTest {

    @Mock private AggregateDescriptor descriptor;
    private AggregateDescriptorBasedDefinition def;

    @BeforeEach
    void setUp() {
        def = new AggregateDescriptorBasedDefinition(descriptor);
    }

    @Test
    void shouldGetName() {
        // Given:
        when(descriptor.name()).thenReturn("some name");

        // Then:
        assertThat(def.name(), is("some name"));
    }

    @Test
    void shouldGetAggregateDescriptor() {
        assertThat(def.descriptor().orElse(null), is(sameInstance(descriptor)));
    }
}

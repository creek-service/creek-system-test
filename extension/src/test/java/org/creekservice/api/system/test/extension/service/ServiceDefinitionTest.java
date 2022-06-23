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
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceDefinitionTest {

    @Mock private ServiceInstance instance;
    private ServiceDefinition def;

    @BeforeEach
    void setUp() {
        def =
                new ServiceDefinition() {
                    @Override
                    public String name() {
                        return null;
                    }

                    @Override
                    public String dockerImage() {
                        return null;
                    }
                };
    }

    @Test
    void shouldNotHaveDescriptorByDefault() {
        assertThat(def.descriptor(), is(Optional.empty()));
    }

    @Test
    void shouldDoNothingInConfigureInstance() {
        // When:
        def.configureInstance(instance);

        // Then:
        verifyNoInteractions(instance);
    }

    @Test
    void shouldNoNothingInInstanceStarted() {
        // When:
        def.instanceStarted(instance);

        // Then:
        verifyNoInteractions(instance);
    }
}

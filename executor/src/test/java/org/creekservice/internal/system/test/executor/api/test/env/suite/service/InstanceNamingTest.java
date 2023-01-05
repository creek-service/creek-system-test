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

package org.creekservice.internal.system.test.executor.api.test.env.suite.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InstanceNamingTest {

    private InstanceNaming strategy;

    @BeforeEach
    void setUp() {
        strategy = new InstanceNaming();
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(InstanceNaming.class);
        tester.testAllPublicStaticMethods(InstanceNaming.class);
        tester.testAllPublicInstanceMethods(strategy);
    }

    @Test
    void shouldGenerateUniqueServiceNames() {
        assertThat(strategy.instanceName("a"), is("a-0"));
        assertThat(strategy.instanceName("a"), is("a-1"));
        assertThat(strategy.instanceName("b"), is("b-0"));
        assertThat(strategy.instanceName("a"), is("a-2"));
    }

    @Test
    void shouldClear() {
        // Given:
        strategy.instanceName("a");

        // When:
        strategy.clear();

        // Then:
        assertThat(strategy.instanceName("a"), is("a-0"));
    }
}

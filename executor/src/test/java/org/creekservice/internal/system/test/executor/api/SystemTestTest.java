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

package org.creekservice.internal.system.test.executor.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.testing.NullPointerTester;
import org.creekservice.api.system.test.extension.component.definition.AggregateDefinition;
import org.creekservice.api.system.test.extension.component.definition.ServiceDefinition;
import org.creekservice.internal.service.api.ComponentModel;
import org.creekservice.internal.system.test.executor.api.component.definition.ComponentDefinitions;
import org.creekservice.internal.system.test.executor.api.test.model.TestModel;
import org.creekservice.internal.system.test.executor.api.test.suite.TestSuiteEnv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemTestTest {

    @Mock private TestModel testModel;
    @Mock private ComponentModel componentModel;
    @Mock private TestSuiteEnv testEnv;
    @Mock private ComponentDefinitions<ServiceDefinition> services;
    @Mock private ComponentDefinitions<AggregateDefinition> aggregates;
    private SystemTest api;

    @BeforeEach
    void setUp() {
        api = new SystemTest(testModel, componentModel, testEnv, services, aggregates);
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(SystemTest.class);
        tester.testAllPublicStaticMethods(SystemTest.class);
        tester.testAllPublicInstanceMethods(api);
    }

    @Test
    void shouldExposeModel() {
        assertThat(api.test().model(), is(sameInstance(testModel)));
    }

    @Test
    void shouldExposeTestEnv() {
        assertThat(api.test().suite(), is(sameInstance(testEnv)));
    }

    @Test
    void shouldExposeServiceDefinitions() {
        assertThat(api.component().definitions().service(), is(sameInstance(services)));
    }

    @Test
    void shouldExposeAggregateDefinitions() {
        assertThat(api.component().definitions().aggregate(), is(sameInstance(aggregates)));
    }
}

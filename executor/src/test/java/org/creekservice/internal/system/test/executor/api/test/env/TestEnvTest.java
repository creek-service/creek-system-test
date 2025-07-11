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

package org.creekservice.internal.system.test.executor.api.test.env;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

import com.google.common.testing.NullPointerTester;
import org.creekservice.internal.system.test.executor.api.test.env.listener.TestListeners;
import org.creekservice.internal.system.test.executor.api.test.env.suite.TestSuiteEnv;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TestEnvTest {

    @Mock private TestListeners listeners;
    @Mock private TestSuiteEnv suite;
    private TestEnv testEnv;

    @BeforeEach
    void setUp() {
        testEnv = new TestEnv(listeners, suite);
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(TestEnv.class);
        tester.testAllPublicStaticMethods(TestEnv.class);
        tester.testAllPublicInstanceMethods(testEnv);
    }

    @Test
    void shouldExposeListeners() {
        assertThat(testEnv.listeners(), is(sameInstance(listeners)));
    }

    @Test
    void shouldExposeCurrentSuite() {
        assertThat(testEnv.currentSuite(), is(sameInstance(suite)));
    }
}

/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@ExtendWith(MockitoExtension.class)
class RegularContainerFactoryTest {

    @Mock private DockerImageName imageName;
    private RegularContainerFactory containerFactory;

    @BeforeEach
    void setUp() {
        containerFactory = new RegularContainerFactory();
    }

    @Test
    void shouldThrowNPEs() {
        final NullPointerTester tester = new NullPointerTester();
        tester.testAllPublicConstructors(RegularContainerFactory.class);
        tester.testAllPublicStaticMethods(RegularContainerFactory.class);
        tester.testAllPublicInstanceMethods(containerFactory);
    }

    @Test
    void shouldReturnGenericContainer() {
        // When:
        final GenericContainer<?> result = containerFactory.create(imageName);

        // Then:
        assertThat(result, is(not(instanceOf(FixedHostPortGenericContainer.class))));
    }
}

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

package org.creekservice.api.system.test.model;

import static org.creekservice.api.system.test.model.TestPackage.testPackage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.testing.EqualsTester;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestPackageTest {

    @Mock private Path root;
    @Mock private Input seed;
    @Mock private TestSuite.Builder suiteBuilder;
    @Mock private TestSuite suite;

    @BeforeEach
    void setUp() {
        when(suiteBuilder.build(any())).thenReturn(suite);
    }

    @Test
    void shouldImplementHashCodeAndEquals() {
        final Collection<Input> seedData = List.of(seed);
        final Collection<TestSuite.Builder> suites = List.of(suiteBuilder);
        new EqualsTester()
                .addEqualityGroup(testPackage(seedData, suites), testPackage(seedData, suites))
                .addEqualityGroup(testPackage(List.of(), suites))
                .addEqualityGroup(testPackage(seedData, List.of(suiteBuilder, suiteBuilder)))
                .testEquals();
    }

    @Test
    void shouldThrowIfNoTestSuites() {
        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> testPackage(List.of(seed), List.of()));

        // Then:
        assertThat(e.getMessage(), containsString("suites can not be empty"));
    }

    @Test
    void shouldSetPackageOnTestSuite() {
        // When:
        final TestPackage testPackage = testPackage(List.of(seed), List.of(suiteBuilder));

        // Then:
        verify(suiteBuilder).build(testPackage);
    }
}

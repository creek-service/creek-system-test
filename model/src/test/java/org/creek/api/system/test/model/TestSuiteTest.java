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

package org.creek.api.system.test.model;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.testing.EqualsTester;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestSuiteTest {

    @Mock private TestCase.Builder testBuilder;
    @Mock private TestCase testCase;
    @Mock private TestSuiteDef def;
    @Mock private TestCaseDef testDef;
    @Mock private TestPackage pkg;

    @BeforeEach
    void setUp() {
        when(testBuilder.build(any())).thenReturn(testCase);
    }

    @Test
    void shouldImplementHashCodeAndEquals() {
        final Collection<TestCase.Builder> testCases = List.of(testBuilder);

        new EqualsTester()
                .addEqualityGroup(
                        TestSuite.testSuite(testCases, def(testCases)).build(pkg),
                        TestSuite.testSuite(testCases, def(testCases)).build(pkg),
                        TestSuite.testSuite(testCases, def(testCases))
                                .build(mock(TestPackage.class)))
                .addEqualityGroup(TestSuite.testSuite(List.of(), def(List.of())).build(pkg))
                .addEqualityGroup(TestSuite.testSuite(List.of(), mock(TestSuiteDef.class)).build(pkg))
                .testEquals();
    }

    @Test
    void shouldThrowOnTestCaseSizeMismatch() {
        // Given:
        final TestSuite.Builder builder = TestSuite.testSuite(List.of(), def);
        givenDefTestCases(1);

        // When:
        final Exception e = assertThrows(IllegalArgumentException.class, () -> builder.build(pkg));

        // Then:
        assertThat(e.getMessage(), containsString("test case size mismatch 0 != 1"));
    }

    @Test
    void shouldSetSuiteOnTestCase() {
        // Given:
        final List<TestCase.Builder> testBuilders = List.of(testBuilder);
        final TestSuite.Builder builder = TestSuite.testSuite(testBuilders, def(testBuilders));

        // When:
        final TestSuite suite = builder.build(pkg);

        // Then:
        verify(testBuilder).build(suite);
    }

    private TestSuiteDef def(final Collection<TestCase.Builder> testCases) {
        givenDefTestCases(testCases.size());
        return def;
    }

    private void givenDefTestCases(final int tests) {
        final List<TestCaseDef> testDefs =
                IntStream.range(0, tests).mapToObj(i -> testDef).collect(Collectors.toList());
        when(def.tests()).thenReturn(testDefs);
    }
}

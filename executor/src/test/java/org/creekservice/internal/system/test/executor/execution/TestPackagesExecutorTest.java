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

package org.creekservice.internal.system.test.executor.execution;

import static org.creekservice.internal.system.test.executor.result.CaseResult.testCaseResult;
import static org.creekservice.internal.system.test.executor.result.SuiteResult.testSuiteResult;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;
import org.creekservice.api.system.test.extension.test.model.TestExecutionResult;
import org.creekservice.api.system.test.model.TestCase;
import org.creekservice.api.system.test.model.TestPackage;
import org.creekservice.api.system.test.model.TestSuite;
import org.creekservice.api.system.test.parser.TestPackagesLoader;
import org.creekservice.internal.system.test.executor.result.ResultsWriter;
import org.creekservice.internal.system.test.executor.result.SuiteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestPackagesExecutorTest {

    @Mock private TestPackagesLoader loader;
    @Mock private TestSuiteExecutor suiteExecutor;
    @Mock private ResultsWriter resultsWriter;
    @Mock private TestPackage pkg1;
    @Mock private TestPackage pkg2;
    @Mock private TestCase test1;
    @Mock private TestCase test2;
    @Mock private TestSuite suite1;
    @Mock private TestSuite suite2;
    @Mock private TestSuite suite3;
    private TestPackagesExecutor executor;

    @BeforeEach
    void setUp() {
        executor = new TestPackagesExecutor(loader, suiteExecutor, resultsWriter);

        when(suiteExecutor.executeSuite(any()))
                .thenAnswer(inv -> testSuiteResult(inv.getArgument(0)).build());

        when(loader.stream()).thenReturn(Stream.of(pkg1, pkg2));
        when(pkg1.suites()).thenReturn(List.of(suite1, suite2));
        when(pkg2.suites()).thenReturn(List.of(suite3));

        when(test1.name()).thenReturn("test1");
        when(test1.suite()).thenReturn(suite1);
        when(test2.name()).thenReturn("test2");
        when(test2.suite()).thenReturn(suite2);
        when(suite1.name()).thenReturn("suite1");
        when(suite2.name()).thenReturn("suite2");
        when(suite3.name()).thenReturn("suite3");
    }

    @Test
    void shouldNotWriteResultsIfThereAreNone() {
        // Given:
        when(loader.stream()).thenReturn(Stream.of());

        // When:
        final TestExecutionResult results = executor.execute();

        // Then:
        verify(resultsWriter, never()).write(any());
        assertThat(results.isEmpty(), is(true));
    }

    @Test
    void shouldExecuteEachPackageSuites() {
        // When:
        executor.execute();

        // Then:
        verify(suiteExecutor).executeSuite(suite1);
        verify(suiteExecutor).executeSuite(suite2);
        verify(suiteExecutor).executeSuite(suite3);
    }

    @Test
    void shouldCombineResults() {
        // Given:
        final SuiteResult suite1Result =
                testSuiteResult(suite1)
                        .add(testCaseResult(test1).failure(new AssertionError()))
                        .add(testCaseResult(test1).error(new RuntimeException()))
                        .build();

        final SuiteResult suite2Result =
                testSuiteResult(suite1)
                        .add(testCaseResult(test2).error(new RuntimeException()))
                        .add(testCaseResult(test2).error(new RuntimeException()))
                        .build();

        when(suiteExecutor.executeSuite(suite1)).thenReturn(suite1Result);

        when(suiteExecutor.executeSuite(suite2)).thenReturn(suite2Result);

        // When:
        final TestExecutionResult result = executor.execute();

        // Then:
        assertThat(result.failed(), is(1L));
        assertThat(result.errors(), is(3L));
    }
}

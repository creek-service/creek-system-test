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

package org.creekservice.internal.system.test.executor.observation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.creekservice.api.base.type.Throwables;
import org.creekservice.api.system.test.extension.test.model.CreekTestCase;
import org.creekservice.api.system.test.extension.test.model.CreekTestSuite;
import org.creekservice.api.system.test.extension.test.model.TestCaseResult;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class LoggingTestEnvironmentListenerTest {

    @Mock private Logger logger;
    @Mock private CreekTestSuite suite;
    @Mock private CreekTestCase test;
    @Mock private TestCaseResult testResult;
    @Mock private TestSuiteResult suiteResult;
    private LoggingTestEnvironmentListener listener;

    @BeforeEach
    void setUp() {
        listener = new LoggingTestEnvironmentListener(logger);
    }

    @Test
    void shouldLogBeforeSuite() {
        // Given:
        when(suite.name()).thenReturn("Bob");

        // When:
        listener.beforeSuite(suite);

        // Then:
        verify(logger).info("Starting suite 'Bob'");
    }

    @Test
    void shouldLogBeforeTest() {
        // Given:
        when(test.name()).thenReturn("Bob");

        // When:
        listener.beforeTest(test);

        // Then:
        verify(logger).info("Starting test 'Bob'");
    }

    @Test
    void shouldLogAfterSuccessfulTest() {
        // Given:
        when(test.name()).thenReturn("Bob");

        // When:
        listener.afterTest(test, testResult);

        // Then:
        verify(logger).info("Finished test 'Bob': SUCCESS");
    }

    @Test
    void shouldLogAfterSkippedTest() {
        // Given:
        when(test.name()).thenReturn("Bob");
        when(testResult.skipped()).thenReturn(true);

        // When:
        listener.afterTest(test, testResult);

        // Then:
        verify(logger).info("Finished test 'Bob': SKIPPED");
    }

    @Test
    void shouldLogAfterErroredTest() {
        // Given:
        when(test.name()).thenReturn("Bob");
        final RuntimeException cause = new RuntimeException("msg");
        when(testResult.error()).thenReturn(Optional.of(cause));

        // When:
        listener.afterTest(test, testResult);

        // Then:
        verify(logger).info("Finished test 'Bob': ERROR: msg\n" + Throwables.stackTrace(cause));
    }

    @Test
    void shouldLogAfterFailedTest() {
        // Given:
        when(test.name()).thenReturn("Bob");
        final AssertionError cause = new AssertionError("msg");
        when(testResult.failure()).thenReturn(Optional.of(cause));

        // When:
        listener.afterTest(test, testResult);

        // Then:
        verify(logger).info("Finished test 'Bob': FAILED: msg\n" + Throwables.stackTrace(cause));
    }

    @Test
    void shouldLogAfterSuccessfulSuite() {
        // Given:
        when(suite.name()).thenReturn("Bob");

        // When:
        listener.afterSuite(suite, suiteResult);

        // Then:
        verify(logger).info("Finished suite 'Bob'");
    }

    @Test
    void shouldLogAfterOtherSuite() {
        // Given:
        when(suite.name()).thenReturn("Bob");
        when(suiteResult.skipped()).thenReturn(1L);
        when(suiteResult.errors()).thenReturn(2L);
        when(suiteResult.failures()).thenReturn(3L);

        // When:
        listener.afterSuite(suite, suiteResult);

        // Then:
        verify(logger).info("Finished suite 'Bob' skipped: 1 errors: 2 failures: 3");
    }
}

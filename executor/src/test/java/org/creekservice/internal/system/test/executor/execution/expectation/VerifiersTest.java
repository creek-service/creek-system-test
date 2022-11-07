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

package org.creekservice.internal.system.test.executor.execution.expectation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.creekservice.api.system.test.extension.test.model.Expectation;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler.Verifier;
import org.creekservice.api.system.test.extension.test.model.Option;
import org.creekservice.api.system.test.extension.test.model.TestModelContainer;
import org.creekservice.api.system.test.model.TestCase;
import org.creekservice.api.system.test.model.TestSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VerifiersTest {

    private static final Duration TIMEOUT = Duration.ofMillis(123457);

    @Mock private TestCase testCase;
    @Mock private TestSuite testSuite;
    @Mock private TestModelContainer model;
    @Mock private ExpectationHandler<ExpectationA> handlerA;
    @Mock private ExpectationHandler<ExpectationB> handlerB;
    @Mock private Verifier verifierA;
    @Mock private Verifier verifierB;
    @Captor private ArgumentCaptor<ExpectationHandler.ExpectationOptions> optionsCaptor;
    private Verifiers verifiers;

    @BeforeEach
    void setUp() {
        verifiers = new Verifiers(model, TIMEOUT);

        doReturn(Optional.of(handlerA)).when(model).expectationHandler(ExpectationA.class);
        doReturn(Optional.of(handlerB)).when(model).expectationHandler(ExpectationB.class);

        when(testCase.suite()).thenReturn(testSuite);
        when(handlerA.prepare(any(), any())).thenReturn(verifierA);
        when(handlerB.prepare(any(), any())).thenReturn(verifierB);
    }

    @Test
    void shouldThrowOnUnknownExpectationType() {
        // Given:
        final Expectation unknown = mock(Expectation.class);

        // When:
        final Exception e =
                assertThrows(
                        RuntimeException.class,
                        () -> verifiers.prepare(List.of(unknown), testCase));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "No handler registered for expectation type: "
                                + "org.creekservice.api.system.test.extension.test.model.Expectation$MockitoMock"));
    }

    @Test
    void shouldPassGroupedExpectationsToHandlers() {
        // Given:
        final ExpectationA e0 = new ExpectationA();
        final ExpectationB e1 = new ExpectationB();
        final ExpectationA e2 = new ExpectationA();

        // When:
        verifiers.prepare(List.of(e0, e1, e2), testCase);

        // Then:
        verify(handlerA).prepare(eq(List.of(e0, e2)), any());
        verify(handlerB).prepare(eq(List.of(e1)), any());
    }

    @Test
    void shouldPassTimeoutToHandlers() {
        // Given:
        final ExpectationA e0 = new ExpectationA();

        // When:
        verifiers.prepare(List.of(e0), testCase);

        // Then:
        verify(handlerA).prepare(any(), optionsCaptor.capture());
        assertThat(optionsCaptor.getValue().timeout(), is(TIMEOUT));
    }

    @Test
    void shouldExposeOptionsToHandlers() {
        // Given:
        final ExpectationA e0 = new ExpectationA();
        verifiers.prepare(List.of(e0), testCase);
        verify(handlerA).prepare(any(), optionsCaptor.capture());

        // When:
        optionsCaptor.getValue().get(Option.class);

        // Then:
        verify(testSuite).options(Option.class);
    }

    @Test
    void shouldCollapseReturnedVerifiers() {
        // Given:
        final ExpectationA e0 = new ExpectationA();
        final ExpectationB e1 = new ExpectationB();

        final Verifier verifier = verifiers.prepare(List.of(e0, e1), testCase);

        verifyNoInteractions(verifierA, verifierB);

        // When:
        verifier.verify();

        // Then:
        verify(verifierA).verify();
        verify(verifierB).verify();
    }

    private static final class ExpectationA implements Expectation {}

    private static final class ExpectationB implements Expectation {}
}

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

package org.creekservice.internal.system.test.executor.execution.input;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.api.system.test.extension.test.model.InputHandler;
import org.creekservice.api.system.test.extension.test.model.InputHandler.InputOptions;
import org.creekservice.api.system.test.extension.test.model.Option;
import org.creekservice.api.system.test.extension.test.model.TestModelContainer;
import org.creekservice.api.system.test.model.TestSuite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InputtersTest {

    @Mock private TestSuite testSuite;
    @Mock private TestModelContainer model;
    @Mock private Input0 input0;
    @Mock private Input1 input1;
    @Mock private InputHandler<Input0> inputHandler0;
    @Mock private InputHandler<Input1> inputHandler1;
    @Captor private ArgumentCaptor<InputOptions> optionsCaptor;
    private Inputters inputters;

    @BeforeEach
    void setUp() {
        inputters = new Inputters(model);

        doReturn(Optional.of(inputHandler0)).when(model).inputHandler(input0.getClass());
        doReturn(Optional.of(inputHandler1)).when(model).inputHandler(input1.getClass());
    }

    @Test
    void shouldThrowIfNoHandlerRegistered() {
        // Given:
        when(model.inputHandler(any())).thenReturn(Optional.empty());

        // When:
        final Exception e =
                assertThrows(
                        RuntimeException.class, () -> inputters.input(List.of(input0), testSuite));

        // Then:
        assertThat(
                e.getMessage(),
                is("No handler registered for input type: " + input0.getClass().getName()));
    }

    @Test
    void shouldProcessInputInOrder() {
        // When:
        inputters.input(List.of(input0, input1, input0), testSuite);

        // Then:
        final InOrder inOrder = inOrder(inputHandler0, inputHandler1);
        inOrder.verify(inputHandler0).process(eq(input0), any());
        inOrder.verify(inputHandler1).process(eq(input1), any());
        inOrder.verify(inputHandler0).process(eq(input0), any());
    }

    @Test
    void shouldExposeOptionsToHandlers() {
        // Given:
        inputters.input(List.of(input0), testSuite);
        verify(inputHandler0).process(eq(input0), optionsCaptor.capture());

        // When:
        optionsCaptor.getValue().get(Option.class);

        // Then:
        verify(testSuite).options(Option.class);
    }

    @Test
    void shouldFlushHandlersOnce() {
        // When:
        inputters.input(List.of(input0, input1, input0), testSuite);

        // Then:
        verify(inputHandler0, times(1)).flush();
        verify(inputHandler1, times(1)).flush();
    }

    private interface Input0 extends Input {}

    private interface Input1 extends Input {}
}

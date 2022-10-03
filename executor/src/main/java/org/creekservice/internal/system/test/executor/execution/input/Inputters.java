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

package org.creekservice.internal.system.test.executor.execution.input;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toCollection;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.api.system.test.extension.test.model.InputHandler;
import org.creekservice.api.system.test.extension.test.model.TestModelContainer;

public final class Inputters {

    private final TestModelContainer model;

    public Inputters(final TestModelContainer model) {
        this.model = requireNonNull(model, "model");
    }

    public void input(final Collection<? extends Input> inputs) {
        final Set<InputHandler<?>> usedHandlers =
                inputs.stream()
                        .map(this::input)
                        .collect(
                                toCollection(
                                        () -> Collections.newSetFromMap(new IdentityHashMap<>())));

        usedHandlers.forEach(InputHandler::flush);
    }

    @SuppressWarnings("unchecked")
    private <T extends Input> InputHandler<T> input(final T input) {
        final InputHandler<T> handler =
                model.inputHandler((Class<T>) input.getClass())
                        .orElseThrow(() -> new HandlerNotRegisteredException(input.getClass()));

        handler.process(input);
        return handler;
    }

    private static final class HandlerNotRegisteredException extends RuntimeException {
        HandlerNotRegisteredException(final Class<? extends Input> inputType) {
            super("No handler registered for input type: " + inputType.getName());
        }
    }
}

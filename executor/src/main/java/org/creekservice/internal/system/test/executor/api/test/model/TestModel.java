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

package org.creekservice.internal.system.test.executor.api.test.model;

import static java.util.Objects.requireNonNull;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.test.model.Expectation;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler;
import org.creekservice.api.system.test.extension.test.model.ExpectationRef;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.api.system.test.extension.test.model.InputHandler;
import org.creekservice.api.system.test.extension.test.model.InputRef;
import org.creekservice.api.system.test.extension.test.model.TestModelContainer;
import org.creekservice.api.system.test.parser.ModelType;

public final class TestModel implements TestModelContainer {

    private final long threadId;
    private final Map<Class<?>, ModelType<?>> types = new HashMap<>();
    private final Map<Class<? extends Input>, InputHandler<?>> inputHandlers = new HashMap<>();
    private final Map<Class<? extends Expectation>, ExpectationHandler<?>> expectationHandlers =
            new HashMap<>();

    public TestModel() {
        this(Thread.currentThread().getId());
    }

    @VisibleForTesting
    TestModel(final long threadId) {
        this.threadId = threadId;
    }

    @Override
    public <T extends InputRef & ExpectationRef> NameBuilder addRef(final Class<T> type) {
        return addType(type, ModelType::ref, ModelType::ref);
    }

    @Override
    public <T extends InputRef> NameBuilder addInputRef(final Class<T> type) {
        return addType(type, ModelType::inputRef, ModelType::inputRef);
    }

    @Override
    public <T extends ExpectationRef> NameBuilder addExpectationRef(final Class<T> type) {
        return addType(type, ModelType::expectationRef, ModelType::expectationRef);
    }

    @Override
    public <T extends Input> NameBuilder addInput(
            final Class<T> type, final InputHandler<? super T> handler) {
        requireNonNull(handler, "handler");
        final NameBuilder builder = addType(type, ModelType::input, ModelType::input);
        inputHandlers.put(type, handler);
        return builder;
    }

    @Override
    public <T extends Expectation> NameBuilder addExpectation(
            final Class<T> type, final ExpectationHandler<? super T> handler) {
        requireNonNull(handler, "handler");
        final NameBuilder builder = addType(type, ModelType::expectation, ModelType::expectation);
        expectationHandlers.put(type, handler);
        return builder;
    }

    @Override
    public boolean hasType(final Class<?> type) {
        throwIfNotOnCorrectThread();

        return types.containsKey(requireNonNull(type, "type"));
    }

    public List<ModelType<?>> modelTypes() {
        throwIfNotOnCorrectThread();

        return List.copyOf(types.values());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends Input> Optional<InputHandler<T>> inputHandler(final Class<T> type) {
        throwIfNotOnCorrectThread();

        return Optional.ofNullable((InputHandler) inputHandlers.get(requireNonNull(type, "type")));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends Expectation> Optional<ExpectationHandler<T>> expectationHandler(
            final Class<T> type) {
        throwIfNotOnCorrectThread();

        return Optional.ofNullable(
                (ExpectationHandler) expectationHandlers.get(requireNonNull(type, "type")));
    }

    private <T> NameBuilder addType(
            final Class<T> type,
            final Function<Class<T>, ModelType<T>> stdNaming,
            final BiFunction<Class<T>, String, ModelType<T>> customNaming) {
        throwIfNotOnCorrectThread();

        types.compute(
                requireNonNull(type, "type"),
                (k, existing) -> {
                    if (existing != null) {
                        throw new IllegalArgumentException("duplicate type: " + type.getName());
                    }
                    return stdNaming.apply(type);
                });

        return name -> types.put(type, customNaming.apply(type, name));
    }

    private void throwIfNotOnCorrectThread() {
        if (Thread.currentThread().getId() != threadId) {
            throw new ConcurrentModificationException("Class is not thread safe");
        }
    }
}

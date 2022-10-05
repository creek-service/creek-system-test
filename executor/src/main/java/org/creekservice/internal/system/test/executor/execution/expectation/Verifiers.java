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

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.creekservice.api.system.test.extension.test.model.Expectation;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler.Verifier;
import org.creekservice.api.system.test.extension.test.model.TestModelContainer;

public final class Verifiers {

    private final TestModelContainer model;
    private final Duration verifierTimeout;

    public Verifiers(final TestModelContainer model, final Duration verifierTimeout) {
        this.model = requireNonNull(model, "model");
        this.verifierTimeout = requireNonNull(verifierTimeout, "verifierTimeout");
    }

    public Verifier prepare(final Collection<? extends Expectation> expectations) {
        final Map<
                        ? extends ExpectationHandler<? extends Expectation>,
                        ? extends List<? extends Expectation>>
                byHandler = expectations.stream().collect(groupingBy(this::expectationHandler));

        final List<Verifier> verifiers =
                byHandler.entrySet().stream()
                        .map(e -> prepare(e.getKey(), e.getValue()))
                        .collect(Collectors.toUnmodifiableList());

        return () -> verifiers.forEach(Verifier::verify);
    }

    private ExpectationHandler<? extends Expectation> expectationHandler(final Expectation e) {
        return model.expectationHandler(e.getClass())
                .orElseThrow(() -> new HandlerNotRegisteredException(e.getClass()));
    }

    @SuppressWarnings("unchecked")
    private <T extends Expectation> Verifier prepare(
            final ExpectationHandler<T> handler, final List<? extends Expectation> expectations) {
        return handler.prepare((List<T>) expectations, () -> verifierTimeout);
    }

    private static final class HandlerNotRegisteredException extends RuntimeException {
        HandlerNotRegisteredException(final Class<? extends Expectation> expectationType) {
            super("No handler registered for expectation type: " + expectationType.getName());
        }
    }
}

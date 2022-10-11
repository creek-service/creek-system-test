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

package org.creekservice.api.system.test.test.extension;


import java.util.Collection;
import java.util.stream.Collectors;
import org.creekservice.api.system.test.extension.CreekSystemTest;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler.ExpectationOptions;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler.Verifier;
import org.creekservice.api.system.test.extension.test.model.InputHandler.InputOptions;

/** Extension used for testing */
public final class TestCreekTestExtension implements CreekTestExtension {

    @Override
    public String name() {
        return "test system test extension";
    }

    @Override
    public void initialize(final CreekSystemTest api) {
        api.extensions().ensureExtension(TestCreekExtensionProvider.class);

        api.tests().model().addInput(TestInput.class, this::pipeInput).withName("creek/test");

        api.tests()
                .model()
                .addExpectation(TestExpectation.class, (e, o) -> prepareExpectation(e, o))
                .withName("creek/test");
    }

    private void pipeInput(final TestInput input, final InputOptions options) {
        System.out.println("Piping input: " + input.value);

        if (input.value.equals("should throw")) {
            throw new RuntimeException("Failed to process input");
        }
    }

    private Verifier prepareExpectation(
            final Collection<? extends TestExpectation> expectations,
            final ExpectationOptions options) {
        final String outputs =
                expectations.stream().map(e -> e.value).collect(Collectors.joining(","));

        if (outputs.contains("should throw")) {
            throw new RuntimeException("Failed to process expectation");
        }

        if (outputs.contains("should fail")) {
            return () -> {
                throw new AssertionError("Failed because it was meant to");
            };
        }

        return () -> System.out.println("Verifying expectations: " + outputs);
    }
}

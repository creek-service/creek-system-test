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
import org.creekservice.api.system.test.extension.CreekSystemTest;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.api.system.test.extension.test.model.ExpectationHandler.Verifier;

/** Extension used for testing */
public final class TestCreekTestExtension implements CreekTestExtension {

    @Override
    public String name() {
        return "test system test extension";
    }

    @Override
    public void initialize(final CreekSystemTest api) {
        api.extensions().ensureExtension(TestCreekExtensionProvider.class);

        api.tests().model().addExpectation(TestExpectation.class, this::prepareExpectation);
    }

    private Verifier prepareExpectation(final Collection<TestExpectation> expectations) {
        return () -> System.out.println("Verifying expectations: " + expectations);
    }
}

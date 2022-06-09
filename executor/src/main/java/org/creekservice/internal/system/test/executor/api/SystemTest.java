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

package org.creekservice.internal.system.test.executor.api;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Collection;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.CreekSystemTest;
import org.creekservice.api.system.test.extension.CreekTestExtension;

public final class SystemTest implements CreekSystemTest {

    private final Model model;
    private final Tests tests;

    public SystemTest(final Collection<? extends CreekTestExtension> extensions) {
        this(extensions, new Model(), new Tests());
    }

    @VisibleForTesting
    SystemTest(
            final Collection<? extends CreekTestExtension> extensions,
            final Model model,
            final Tests tests) {
        this.model = requireNonNull(model, "model");
        this.tests = requireNonNull(tests, "test");
        extensions.forEach(ext -> ext.initialize(this));
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    @Override
    public Model model() {
        return model;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    @Override
    public Tests test() {
        return tests;
    }
}

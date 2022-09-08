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

package org.creekservice.api.system.test.test.services;

import static org.creekservice.internal.system.test.test.services.TestResources.internal;
import static org.creekservice.internal.system.test.test.services.TestResources.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.creekservice.api.platform.metadata.ComponentInput;
import org.creekservice.api.platform.metadata.ComponentInternal;
import org.creekservice.api.platform.metadata.ComponentOutput;
import org.creekservice.api.platform.metadata.ServiceDescriptor;
import org.creekservice.api.system.test.test.service.extension.TestResourceInput;
import org.creekservice.api.system.test.test.service.extension.TestResourceInternal;
import org.creekservice.api.system.test.test.service.extension.TestResourceOutput;

public final class TestServiceDescriptor implements ServiceDescriptor {
    public static final String SERVICE_NAME = "test-service";

    private static final List<ComponentInput> INPUTS = new ArrayList<>();
    private static final List<ComponentInternal> INTERNALS = new ArrayList<>();
    private static final List<ComponentOutput> OUTPUTS = new ArrayList<>();

    public static final TestResourceInput UnownedInput1 =
            register(TestAggregateDescriptor.OUTPUT.toInput());

    @SuppressWarnings("unused")
    private static final TestResourceInput UnownedInput2 =
            register(SharedResources.SHARED).toInput();

    @SuppressWarnings("unused")
    private static final TestResourceInternal UnmanagedInternal = register(internal("internal"));

    public static final TestResourceOutput OwnedOutput = register(output("output"));

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public String dockerImage() {
        return "ghcr.io/creekservice/creek-system-test-" + name();
    }

    @Override
    public Collection<ComponentInput> inputs() {
        return List.copyOf(INPUTS);
    }

    @Override
    public Collection<ComponentInternal> internals() {
        return List.copyOf(INTERNALS);
    }

    @Override
    public Collection<ComponentOutput> outputs() {
        return List.copyOf(OUTPUTS);
    }

    private static <T extends ComponentInput> T register(final T input) {
        INPUTS.add(input);
        return input;
    }

    private static <T extends ComponentInternal> T register(final T internal) {
        INTERNALS.add(internal);
        return internal;
    }

    private static <T extends ComponentOutput> T register(final T output) {
        OUTPUTS.add(output);
        return output;
    }
}

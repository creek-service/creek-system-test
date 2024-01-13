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

package org.creekservice.api.system.test.test.services;

import static org.creekservice.internal.system.test.test.services.TestResources.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.creekservice.api.platform.metadata.AggregateDescriptor;
import org.creekservice.api.platform.metadata.ComponentOutput;
import org.creekservice.api.system.test.test.service.extension.TestResourceOutput;

/** Example upstream service (which would normally live in a different jar) */
public final class TestAggregateDescriptor implements AggregateDescriptor {

    private static final List<ComponentOutput> OUTPUTS = new ArrayList<>();

    public static final TestResourceOutput OUTPUT = register(output("upstream"));

    public TestAggregateDescriptor() {}

    @Override
    public String name() {
        return "test-agg";
    }

    @Override
    public Collection<ComponentOutput> outputs() {
        return List.copyOf(OUTPUTS);
    }

    private static <T extends ComponentOutput> T register(final T output) {
        OUTPUTS.add(output);
        return output;
    }
}

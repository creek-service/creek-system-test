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

package org.creekservice.internal.system.test.executor.components;


import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import org.creekservice.api.platform.metadata.ComponentDescriptor;

public final class ComponentDescriptors {

    private ComponentDescriptors() {}

    /** Instantiate any extensions available at runtime. */
    public static List<ComponentDescriptor> load() {
        return ServiceLoader.load(ComponentDescriptor.class).stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toUnmodifiableList());
    }
}

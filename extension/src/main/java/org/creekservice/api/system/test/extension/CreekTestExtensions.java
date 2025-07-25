/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.system.test.extension;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/** Factory class for loading system test extensions. */
public final class CreekTestExtensions {

    private CreekTestExtensions() {}

    /**
     * Instantiate any test extensions available at runtime.
     *
     * @return the list of system test extensions found on the model and class paths.
     */
    public static List<CreekTestExtension> load() {
        return ServiceLoader.load(CreekTestExtension.class).stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toUnmodifiableList());
    }
}

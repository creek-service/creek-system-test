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

package org.creekservice.internal.system.test.parser;

import org.creekservice.api.system.test.extension.test.model.LocationAware;
import org.creekservice.api.system.test.extension.test.model.Ref;

/** Indicates a test dependency, i.e. a test input or expectation file, was missing. */
public class MissingDependencyException extends TestLoadFailedException {

    /**
     * @param ref the reference to the missing file.
     */
    public MissingDependencyException(final Ref ref) {
        super("Missing dependency: " + ref.id() + maybeLocation(ref));
    }

    private static String maybeLocation(final Ref ref) {
        if (ref instanceof LocationAware) {
            return ", referenced: " + ((LocationAware<?>) ref).location();
        }
        return "";
    }
}

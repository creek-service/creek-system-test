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

package org.creekservice.internal.system.test.parser;


import org.creekservice.api.system.test.extension.model.BaseRef;
import org.creekservice.api.system.test.model.LocationAware;

public class MissingDependencyException extends TestLoadFailedException {

    public MissingDependencyException(final BaseRef ref) {
        super("Missing dependency: " + ref.id() + maybeLocation(ref));
    }

    private static String maybeLocation(final BaseRef ref) {
        if (ref instanceof LocationAware) {
            return ", referenced: " + ((LocationAware<?>) ref).location();
        }
        return "";
    }
}
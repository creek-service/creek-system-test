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

package org.creekservice.api.system.test.parser;


import java.util.Collection;
import org.creekservice.api.system.test.extension.model.ModelType;
import org.creekservice.internal.system.test.parser.YamlTestPackageParser;

/** Factory class for test package parsing. */
public final class TestPackageParsers {

    private TestPackageParsers() {}

    /**
     * Parser of YAML test packages.
     *
     * @param modelExtensions the test model extensions know to the system
     * @param observer an observer to call with information while parsing.
     * @return the parser.
     */
    public static TestPackageParser yaml(
            final Collection<ModelType<?>> modelExtensions,
            final TestPackageParser.Observer observer) {
        return new YamlTestPackageParser(modelExtensions, observer);
    }
}

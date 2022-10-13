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

import static org.creekservice.api.system.test.parser.TestPackageParsers.yamlParser;
import static org.creekservice.api.system.test.parser.TestPackagesLoader.testPackagesLoader;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.creekservice.api.system.test.model.TestPackage;
import org.creekservice.api.system.test.model.TestSuite;
import org.creekservice.api.system.test.test.extension.TestExpectation;
import org.creekservice.api.test.util.TestPaths;
import org.junit.jupiter.api.Test;

class TestPackagesLoaderFunctionalTest {

    private static final Path TESTS_DIR =
            TestPaths.moduleRoot("parser").resolve("src/test/resources/tests");

    @Test
    void shouldLoadFromDirTree() {
        // Given:
        final TestPackagesLoader loader =
                testPackagesLoader(
                        TESTS_DIR,
                        yamlParser(
                                List.of(ModelType.expectation(TestExpectation.class, "creek/test")),
                                new TestPackageParser.Observer() {}),
                        path -> true);

        // When:
        final List<String> suiteNames =
                loader.stream()
                        .map(TestPackage::suites)
                        .flatMap(Collection::stream)
                        .map(TestSuite::name)
                        .collect(Collectors.toList());

        // Then:
        assertThat(suiteNames, containsInAnyOrder("p1", "p2", "p1.sub", "p1.sub.sub"));
    }
}

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

package org.creekservice.api.system.test.executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExecutorOptionsTest {

    private ExecutorOptions options;

    @BeforeEach
    void setUp() {
        options =
                new ExecutorOptions() {
                    @Override
                    public Path testDirectory() {
                        return null;
                    }

                    @Override
                    public Path resultDirectory() {
                        return null;
                    }
                };
    }

    @Test
    void shouldDefaultToNoVerifierTimeout() {
        assertThat(options.verifierTimeout(), is(Optional.empty()));
    }

    @Test
    void shouldDefaultToAllTestSuites() {
        assertThat(options.suitesFilter().test(Paths.get("any/old/path")), is(true));
    }

    @Test
    void shouldDefaultToNotEchoingOnly() {
        assertThat(options.echoOnly(), is(false));
    }
}

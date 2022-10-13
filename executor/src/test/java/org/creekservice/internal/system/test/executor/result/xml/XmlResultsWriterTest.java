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

package org.creekservice.internal.system.test.executor.result.xml;

import static org.creekservice.api.test.hamcrest.PathMatchers.fileContains;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.List;
import org.creekservice.api.system.test.extension.test.model.TestExecutionResult;
import org.creekservice.api.test.util.TestPaths;
import org.creekservice.internal.system.test.executor.result.SuiteResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class XmlResultsWriterTest {

    @Mock private ObjectWriter objectWriter;
    @Mock private TestExecutionResult result;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SuiteResult suite0;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SuiteResult suite1;

    @TempDir Path testDir;
    private Path outputDir;
    private XmlResultsWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        outputDir = testDir.resolve("output");
        writer = new XmlResultsWriter(outputDir, objectWriter);

        when(suite0.testSuite().name()).thenReturn("suite0");
        when(suite1.testSuite().name()).thenReturn("suite1");

        when(objectWriter.writeValueAsString(any()))
                .thenReturn("some xml")
                .thenReturn("some more xml");

        doReturn(List.of(suite0, suite1)).when(result).results();
    }

    @Test
    void shouldThrowIfFailedToCreateDirectory() {
        // Given:
        TestPaths.write(outputDir, "if file exists, dir creation will fail...");

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> writer.write(result));

        // Then:
        assertThat(e.getMessage(), is("Failed to create output directory: " + outputDir));
        assertThat(e.getCause(), is(instanceOf(FileAlreadyExistsException.class)));
    }

    @Test
    void shouldThrowOnJsonProcessingException() throws Exception {
        // Given:
        final TestJsonProcessingException expected = new TestJsonProcessingException("msg");
        when(objectWriter.writeValueAsString(any())).thenThrow(expected);

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> writer.write(result));

        // Then:
        assertThat(e.getMessage(), is("Failed to serialize result"));
        assertThat(e.getCause(), is(expected));
    }

    @Test
    void shouldThrowOnIOException() throws Exception {
        // Given:
        final Path suite0Path = outputDir.resolve("TEST-suite0.xml");
        when(objectWriter.writeValueAsString(any()))
                .thenAnswer(
                        inv -> {
                            TestPaths.ensureDirectories(suite0Path);
                            return "some xml";
                        });

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> writer.write(result));

        // Then:
        assertThat(e.getMessage(), is("Failed to write result to: " + suite0Path));
        assertThat(e.getCause(), is(instanceOf(FileSystemException.class)));
    }

    @Test
    void shouldWriteSuitesToFiles() {
        // When:
        writer.write(result);

        // Then:
        assertThat(outputDir.resolve("TEST-suite0.xml"), fileContains("some xml"));
        assertThat(outputDir.resolve("TEST-suite1.xml"), fileContains("some more xml"));
    }

    @Test
    void shouldOverwriteExistingFiles() {
        // Given:
        TestPaths.write(outputDir.resolve("TEST-suite0.xml"), "old results");
        TestPaths.write(outputDir.resolve("TEST-suite1.xml"), "old results");

        // When:
        writer.write(result);

        // Then:
        assertThat(outputDir.resolve("TEST-suite0.xml"), fileContains("some xml"));
        assertThat(outputDir.resolve("TEST-suite1.xml"), fileContains("some more xml"));
    }

    @Test
    void shouldSanitizeSuiteNameForUseInFileName() {
        // Given:
        when(suite0.testSuite().name()).thenReturn("!some $$ Weird --___ !£$%£^&* Name");

        // When:
        writer.write(result);

        // Then:
        assertThat(
                outputDir.resolve("TEST-_some____Weird_--_____________Name.xml"),
                fileContains("some xml"));
    }

    private static final class TestJsonProcessingException extends JsonProcessingException {

        TestJsonProcessingException(final String msg) {
            super(msg);
        }
    }
}

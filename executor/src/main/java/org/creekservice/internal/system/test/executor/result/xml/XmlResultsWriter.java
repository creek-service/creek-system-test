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

package org.creekservice.internal.system.test.executor.result.xml;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.test.model.TestExecutionResult;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;
import org.creekservice.internal.system.test.executor.result.ResultsWriter;

/** A writer of XML results */
public final class XmlResultsWriter implements ResultsWriter {

    private final Path outputDirectory;
    private final ObjectWriter writer;

    /**
     * @param outputDirectory the directory in which to persist results
     */
    public XmlResultsWriter(final Path outputDirectory) {
        this(outputDirectory, XmlResultMapper.INSTANCE.get().writerWithDefaultPrettyPrinter());
    }

    @VisibleForTesting
    XmlResultsWriter(final Path outputDirectory, final ObjectWriter writer) {
        this.outputDirectory = requireNonNull(outputDirectory, "outputDirectory");
        this.writer = requireNonNull(writer, "writer");
    }

    @Override
    public void write(final TestExecutionResult result) {
        ensureOutputDirectoryExists();
        result.results().forEach(this::write);
    }

    private void write(final TestSuiteResult result) {
        final XmlTestSuiteResult xmlResult = XmlTestSuiteResult.from(result);

        final Path path =
                outputDirectory.resolve("TEST-" + sanitize(result.testSuite().name()) + ".xml");

        try {
            final String xml = writer.writeValueAsString(xmlResult);
            Files.write(path, xml.getBytes(StandardCharsets.UTF_8));
        } catch (final JsonProcessingException e) {
            throw new WriteXmlResultsException("Failed to serialize result", e);
        } catch (final IOException e) {
            throw new WriteXmlResultsException("Failed to write result to: " + path, e);
        }
    }

    private void ensureOutputDirectoryExists() {
        try {
            Files.createDirectories(outputDirectory);
        } catch (final IOException e) {
            throw new WriteXmlResultsException(
                    "Failed to create output directory: " + outputDirectory, e);
        }
    }

    private static String sanitize(final String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9-_.]", "_");
    }

    private static final class WriteXmlResultsException extends RuntimeException {

        WriteXmlResultsException(final String msg, final Throwable cause) {
            super(msg, cause);
        }
    }
}

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

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.system.test.extension.test.model.TestSuiteResult;

/** Test suite result that can be serialized as part of JUnit style test XML report. */
@SuppressWarnings("unused") // Invoked via reflection
@JacksonXmlRootElement(localName = "testsuite")
public final class XmlTestSuiteResult {

    private static final String LOCAL_HOST = localHostName();

    private final TestSuiteResult result;
    private final String hostName;

    public static XmlTestSuiteResult from(final TestSuiteResult result) {
        return new XmlTestSuiteResult(result, LOCAL_HOST);
    }

    @VisibleForTesting
    XmlTestSuiteResult(final TestSuiteResult result, final String hostName) {
        this.result = requireNonNull(result, "result");
        this.hostName = requireNonNull(hostName, "hostName");
    }

    @JacksonXmlProperty(isAttribute = true)
    public String name() {
        return result.testSuite().name();
    }

    @JacksonXmlProperty(isAttribute = true)
    public long tests() {
        return result.testCases().size();
    }

    @JacksonXmlProperty(isAttribute = true)
    public long skipped() {
        return result.skipped();
    }

    @JacksonXmlProperty(isAttribute = true)
    public long failures() {
        return result.failures();
    }

    @JacksonXmlProperty(isAttribute = true)
    public long errors() {
        return result.errors();
    }

    @JacksonXmlProperty(isAttribute = true)
    public String timestamp() {
        final String timestampZ = DateTimeFormatter.ISO_INSTANT.format(result.start());
        return timestampZ.substring(0, timestampZ.length() - 1);
    }

    @JacksonXmlProperty(isAttribute = true)
    public String hostname() {
        return hostName;
    }

    @JacksonXmlProperty(isAttribute = true)
    public String time() {
        return String.format(
                "%d.%03d",
                result.duration().getSeconds(),
                TimeUnit.NANOSECONDS.toMillis(result.duration().getNano()));
    }

    @JacksonXmlProperty
    public List<XmlTestCaseResult> testcase() {
        return result.testCases().stream().map(XmlTestCaseResult::from).collect(toList());
    }

    private static String localHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            return "Unknown";
        }
    }
}

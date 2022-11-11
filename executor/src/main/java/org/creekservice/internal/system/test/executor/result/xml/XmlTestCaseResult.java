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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.creekservice.api.system.test.extension.test.model.TestCaseResult;

/** Test case result that can be serialized as part of JUnit style test XML report. */
@SuppressWarnings("unused") // Invoked via reflection
@JacksonXmlRootElement(localName = "testcase")
public final class XmlTestCaseResult {

    private final TestCaseResult result;

    /**
     * Factory method.
     *
     * @param result the test case result.
     * @return the XML equivalent.
     */
    public static XmlTestCaseResult from(final TestCaseResult result) {
        return new XmlTestCaseResult(result);
    }

    private XmlTestCaseResult(final TestCaseResult result) {
        this.result = requireNonNull(result, "result");
    }

    /**
     * @return the test name.
     */
    @JacksonXmlProperty(isAttribute = true)
    public String name() {
        return result.testCase().name();
    }

    /**
     * @return the suite name.
     */
    @JacksonXmlProperty(isAttribute = true)
    public String classname() {
        return result.testCase().suite().name();
    }

    /**
     * @return the duration of the test execution
     */
    @JacksonXmlProperty(isAttribute = true)
    public String time() {
        return String.format(
                "%d.%03d",
                result.duration().getSeconds(),
                TimeUnit.NANOSECONDS.toMillis(result.duration().getNano()));
    }

    /**
     * @return any failure details, i.e. test was not successful.
     */
    @JacksonXmlProperty
    public Optional<XmlIssue> failure() {
        return result.failure().map(XmlIssue::new);
    }

    /**
     * @return any error details, i.e. test did not run.
     */
    @JacksonXmlProperty
    public Optional<XmlIssue> error() {
        return result.error().map(XmlIssue::new);
    }

    /**
     * @return non-empty if the test was skipped.
     */
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JacksonXmlProperty
    public Optional<Object> skipped() {
        return result.skipped() ? Optional.of(new Object()) : Optional.empty();
    }
}

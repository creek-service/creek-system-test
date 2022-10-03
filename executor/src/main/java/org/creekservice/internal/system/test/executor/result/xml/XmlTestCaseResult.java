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
import static java.util.Objects.requireNonNullElse;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.creekservice.api.base.type.Throwables;
import org.creekservice.api.system.test.extension.test.model.TestCaseResult;

/** Test case result that can be serialized as part of JUnit style test XML report. */
@SuppressWarnings("unused") // Invoked via reflection
@JacksonXmlRootElement(localName = "testcase")
public final class XmlTestCaseResult {

    private final TestCaseResult result;

    public static XmlTestCaseResult from(final TestCaseResult result) {
        return new XmlTestCaseResult(result);
    }

    private XmlTestCaseResult(final TestCaseResult result) {
        this.result = requireNonNull(result, "result");
    }

    @JacksonXmlProperty(isAttribute = true)
    public String name() {
        return result.testCase().name();
    }

    @JacksonXmlProperty(isAttribute = true)
    public String classname() {
        return result.testCase().suite().name();
    }

    @JacksonXmlProperty(isAttribute = true)
    public String time() {
        return String.format(
                "%d.%03d",
                result.duration().getSeconds(),
                TimeUnit.NANOSECONDS.toMillis(result.duration().getNano()));
    }

    @JacksonXmlProperty
    public Optional<Failure> failure() {
        if (result.failure().isPresent()) {
            return result.failure().map(Failure::new);
        }

        return result.error().map(Failure::new);
    }

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    @JacksonXmlProperty
    public Optional<Object> skipped() {
        return result.skipped() ? Optional.of(new Object()) : Optional.empty();
    }

    public static final class Failure {

        private final Throwable cause;

        public Failure(final Throwable cause) {
            this.cause = requireNonNull(cause, "cause");
        }

        @JacksonXmlProperty(isAttribute = true)
        @JsonGetter("message")
        public String message() {
            return requireNonNullElse(cause.getMessage(), "");
        }

        @JacksonXmlProperty(isAttribute = true)
        @JsonGetter("type")
        public String type() {
            return cause.getClass().getCanonicalName();
        }

        @JacksonXmlText
        @JsonGetter("stack")
        public String stack() {
            return Throwables.stackTrace(cause);
        }
    }
}

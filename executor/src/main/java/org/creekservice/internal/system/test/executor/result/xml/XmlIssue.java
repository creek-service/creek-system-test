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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import org.creekservice.api.base.type.Throwables;

/** A type used to serialize a failure or error as XML. */
public final class XmlIssue {

    private final Throwable cause;

    XmlIssue(final Throwable cause) {
        this.cause = requireNonNull(cause, "cause");
    }

    /**
     * @return a descriptive message
     */
    @JacksonXmlProperty(isAttribute = true)
    @JsonGetter("message")
    public String message() {
        return requireNonNullElse(cause.getMessage(), "");
    }

    /**
     * @return the type of the issue, i.e. the exception type.
     */
    @JacksonXmlProperty(isAttribute = true)
    @JsonGetter("type")
    public String type() {
        return cause.getClass().getCanonicalName();
    }

    /**
     * @return the stack trace.
     */
    @JacksonXmlText
    @JsonGetter("stack")
    public String stack() {
        return Throwables.stackTrace(cause);
    }
}

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

package org.creek.api.system.test.model;

import static java.util.Objects.requireNonNull;
import static org.creek.api.base.type.Preconditions.requireNonBlank;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/** Used to indicate a test suite or individual case is disabled. */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class Disabled {

    private final String reason;
    private final Optional<URI> issue;

    @JsonCreator
    public static Disabled disabled(
            @JsonProperty(value = "reason", required = true) final String reason,
            @JsonProperty(value = "issue") final Optional<URI> issue) {
        return new Disabled(reason, issue);
    }

    private Disabled(final String reason, final Optional<URI> issue) {
        this.reason = requireNonBlank(reason, "reason");
        this.issue = requireNonNull(issue, "issue");
    }

    @JsonGetter("reason")
    @JsonPropertyDescription("The reason why the suite or test is disabled.")
    public String reason() {
        return reason;
    }

    @JsonGetter("issue")
    @JsonPropertyDescription("Optional link to a related issue")
    public Optional<URI> issue() {
        return issue;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Disabled disabled = (Disabled) o;
        return Objects.equals(reason, disabled.reason) && Objects.equals(issue, disabled.issue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason, issue);
    }

    @Override
    public String toString() {
        return "Disabled{reason='" + reason + '\'' + ", issue=" + issue + '}';
    }
}

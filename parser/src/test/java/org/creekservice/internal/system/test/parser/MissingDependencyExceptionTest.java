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

package org.creekservice.internal.system.test.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.creekservice.api.system.test.extension.model.Ref;
import org.creekservice.api.system.test.model.LocationAware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MissingDependencyExceptionTest {

    private static final URI LOCATION = URI.create("file://some.location:78");
    private static final String ID = "some.id";

    @Mock(extraInterfaces = LocationAware.class)
    private Ref locationAware;

    @Mock private Ref dumbRef;

    @Test
    void shouldWorkOnLocationAwareRef() {
        // Given:
        when(((LocationAware<?>) locationAware).location()).thenReturn(LOCATION);
        when(locationAware.id()).thenReturn(ID);

        // When:
        final Exception e = new MissingDependencyException(locationAware);

        // Then:
        assertThat(
                e.getMessage(),
                is("Missing dependency: some.id, " + "referenced: file://some.location:78"));
    }

    @Test
    void shouldWorkOnNonLocationAwareRef() {
        // Given:
        when(dumbRef.id()).thenReturn(ID);

        // When:
        final Exception e = new MissingDependencyException(dumbRef);

        // Then:
        assertThat(e.getMessage(), is("Missing dependency: some.id"));
    }
}

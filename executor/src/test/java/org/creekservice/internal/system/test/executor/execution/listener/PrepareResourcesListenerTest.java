/*
 * Copyright 2023-2024 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.internal.system.test.executor.execution.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.creekservice.api.platform.metadata.ComponentDescriptor;
import org.creekservice.api.platform.metadata.ResourceDescriptor;
import org.creekservice.api.service.extension.component.model.ResourceHandler;
import org.creekservice.api.system.test.extension.component.definition.ComponentDefinition;
import org.creekservice.internal.system.test.executor.api.SystemTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrepareResourcesListenerTest {

    private static final ResourceA RES_A_0 = new ResourceA(URI.create("id://A-0"));
    private static final ResourceA RES_A_1 = new ResourceA(URI.create("id://A-1"));
    private static final ResourceB RES_B_0 = new ResourceB(URI.create("id://B-0"));
    private static final ResourceB RES_B_1 = new ResourceB(URI.create("id://B-1"));

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SystemTest api;

    @Mock private ComponentDefinition def0;
    @Mock private ComponentDefinition def1;

    @Mock private ComponentDescriptor desc0;
    @Mock private ComponentDescriptor desc1;
    @Mock private ResourceHandler<ResourceA> handlerA;
    @Mock private ResourceHandler<ResourceB> handlerB;
    private PrepareResourcesListener listener;

    @SuppressWarnings({"unchecked", "rawtypes"})
    @BeforeEach
    void setUp() {
        listener = new PrepareResourcesListener(api);

        when(api.components().definitions().stream()).thenAnswer(inv -> Stream.of(def0, def1));

        when(def0.descriptor()).thenReturn((Optional) Optional.of(desc0));
        when(def1.descriptor()).thenReturn((Optional) Optional.of(desc1));

        when(api.extensions().model().resourceHandler(ResourceA.class)).thenReturn(handlerA);
        when(api.extensions().model().resourceHandler(ResourceB.class)).thenReturn(handlerB);

        when(desc0.resources()).thenAnswer(inv -> Stream.of(RES_A_0, RES_B_0));
    }

    @Test
    void shouldIgnoreDefinitionsWithNoDescriptor() {
        // Given:
        when(def0.descriptor()).thenReturn(Optional.empty());
        when(def1.descriptor()).thenReturn(Optional.empty());

        // When:
        listener.beforeSuite(null);

        // Then:
        verify(api.extensions().model(), never()).resourceHandler(any());
    }

    @Test
    void shouldPrepareAllOfTheSameTypeInOneCall() {
        // Given:
        when(desc0.resources()).thenAnswer(inv -> Stream.of(RES_A_0, RES_A_1));
        when(desc1.resources()).thenAnswer(inv -> Stream.of(RES_B_0, RES_B_1));

        // When:
        listener.beforeSuite(null);

        // Then:
        verify(handlerA).prepare(List.of(RES_A_0, RES_A_1));
        verify(handlerB).prepare(List.of(RES_B_0, RES_B_1));
    }

    @Test
    void shouldSupportNested() {
        // Given:
        final ResourceDescriptor res = mock();
        when(res.id()).thenReturn(URI.create("other"));
        when(res.resources()).thenAnswer(inv -> Stream.of(RES_A_1));

        when(desc0.resources()).thenAnswer(inv -> Stream.of(res));

        // When:
        listener.beforeSuite(null);

        // Then:
        verify(handlerA).prepare(List.of(RES_A_1));
    }

    @Test
    void shouldPrepareOnlyFirstById() {
        // Given:
        when(desc0.resources())
                .thenAnswer(inv -> Stream.of(RES_A_0, RES_A_1, new ResourceA(RES_A_1.id())));
        when(desc1.resources()).thenAnswer(inv -> Stream.of(RES_A_0, new ResourceA(RES_A_0.id())));

        // When:
        listener.beforeSuite(null);

        // Then:
        verify(handlerA).prepare(List.of(RES_A_0, RES_A_1));
    }

    @Test
    void shouldThrowIfNoResourceHandlerRegistered() {
        // Given:
        final Exception expected = new RuntimeException("Boom");
        when(api.extensions().model().resourceHandler(any())).thenThrow(expected);

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> listener.beforeSuite(null));

        // Then:
        assertThat(e, is(expected));
    }

    @Test
    void shouldThrowIfPrepareFails() {
        // Given:
        final Exception expected = new RuntimeException("Boom");
        doThrow(expected).when(handlerA).prepare(any());

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> listener.beforeSuite(null));

        // Then:
        assertThat(e, is(expected));
    }

    private static final class ResourceA implements ResourceDescriptor {
        private final URI id;

        private ResourceA(final URI id) {
            this.id = id;
        }

        @Override
        public URI id() {
            return id;
        }
    }

    private static final class ResourceB implements ResourceDescriptor {
        private final URI id;

        private ResourceB(final URI id) {
            this.id = id;
        }

        @Override
        public URI id() {
            return id;
        }
    }
}

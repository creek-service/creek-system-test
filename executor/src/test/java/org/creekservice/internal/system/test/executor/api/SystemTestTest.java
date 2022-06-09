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

package org.creekservice.internal.system.test.executor.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.creekservice.api.system.test.extension.CreekSystemTest;
import org.creekservice.api.system.test.extension.CreekTestExtension;
import org.creekservice.api.system.test.extension.model.InputRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SystemTestTest {

    @Mock private CreekTestExtension ext1;
    @Mock private CreekTestExtension ext2;
    @Mock private Model model;
    @Mock private Tests tests;
    private SystemTest api;
    @Captor private ArgumentCaptor<SystemTest> apiCapture;
    private final Class<? extends InputRef> refType = mock(InputRef.class).getClass();

    @Test
    void shouldExposeModel() {
        // Given:
        api = new SystemTest(List.of(ext1, ext2), model, tests);

        // Then:
        assertThat(api.model(), is(sameInstance(model)));
    }

    @Test
    void shouldExposeModelToExtensions() {
        // When:
        api = new SystemTest(List.of(ext1, ext2), model, tests);

        // Then:
        verify(ext1).initialize(apiCapture.capture());
        assertThat(apiCapture.getValue().model(), is(sameInstance(model)));

        verify(ext2).initialize(apiCapture.capture());
        assertThat(apiCapture.getValue().model(), is(sameInstance(model)));
    }

    @Test
    void shouldExposeTests() {
        // Given:
        api = new SystemTest(List.of(ext1, ext2), model, tests);

        // Then:
        assertThat(api.test(), is(sameInstance(tests)));
    }

    @Test
    void shouldExposeTestsToExtensions() {
        // When:
        api = new SystemTest(List.of(ext1, ext2), model, tests);

        // Then:
        verify(ext1).initialize(apiCapture.capture());
        assertThat(apiCapture.getValue().test(), is(sameInstance(tests)));

        verify(ext2).initialize(apiCapture.capture());
        assertThat(apiCapture.getValue().test(), is(sameInstance(tests)));
    }

    @Test
    void shouldGetModelTypes() {
        // Given:
        doAnswer(
                        inv -> {
                            final CreekSystemTest api = inv.getArgument(0);
                            api.model().addInputRef(refType);
                            return null;
                        })
                .when(ext1)
                .initialize(any());

        doAnswer(
                        inv -> {
                            final CreekSystemTest api = inv.getArgument(0);
                            api.model().addInputRef(refType);
                            return null;
                        })
                .when(ext2)
                .initialize(any());

        // When:
        api = new SystemTest(List.of(ext1, ext2), model, tests);

        // Then:
        verify(model, times(2)).addInputRef(refType);
    }
}

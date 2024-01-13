/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.system.test.extension.test.env.listener;

/** Container of test package listeners */
public interface TestListenerContainer extends TestListenerCollection {

    /**
     * Append the supplied {@code listener} to the end of the collection.
     *
     * <p>Listeners are invoked in order for {@code beforeXXXX} methods and in reverse order for
     * {@code afterXXXX} methods.
     *
     * @param listener the listener to append.
     */
    void append(TestEnvironmentListener listener);
}

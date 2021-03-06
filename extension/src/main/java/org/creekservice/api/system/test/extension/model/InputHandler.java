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

package org.creekservice.api.system.test.extension.model;

/**
 * Handler of {@link Input}'s.
 *
 * <p>Extensions implement this interface to handle processing of their custom {@link Input} types.
 *
 * @param <T> the input type.
 */
public interface InputHandler<T extends Input> {

    /**
     * Process the supplied {@code input}.
     *
     * <p>The implementation will attempt to send or act upon the supplied {@code input} data. The
     * implementation can choose whether this is done synchronously or asynchronously. {@link
     * #flush()} will block until any outstanding asynchronous operations have completed.
     *
     * @param input the input to process.
     */
    void process(T input);

    /**
     * Block until any asynchronous operations started during previous calls to {@link #process}
     * have completed.
     *
     * <p>The implementation will ensure the call does not block indefinitely.
     */
    default void flush() {}
}

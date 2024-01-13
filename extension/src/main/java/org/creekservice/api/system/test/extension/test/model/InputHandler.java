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

package org.creekservice.api.system.test.extension.test.model;

import java.util.List;

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
     * @param options the customisation options of how to process the input.
     */
    void process(T input, InputOptions options);

    /**
     * Block until any asynchronous operations started during previous calls to {@link #process}
     * have completed.
     *
     * <p>The implementation will ensure the call does not block indefinitely.
     */
    default void flush() {}

    /** Customisation options for handling inputs. */
    interface InputOptions {

        /**
         * Get user supplied options.
         *
         * <p>Test extensions can register custom {@link Option} subtypes when initializing. Users
         * can then define options within the test suite files.
         *
         * @param type the type of the option to get.
         * @param <T> the type of the option to get.
         * @return the options of the requested type supplied in the current test suite
         */
        <T extends Option> List<T> get(Class<T> type);
    }
}

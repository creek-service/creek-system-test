/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

import java.time.Duration;
import java.util.Collection;
import java.util.List;

/**
 * Handler of {@link Expectation}'s.
 *
 * <p>Extensions implement this interface to handle processing of their custom {@link Expectation}
 * types.
 *
 * @param <T> the expectation type.
 */
public interface ExpectationHandler<T extends Expectation> {

    /**
     * Prepare expectations.
     *
     * <p>Called before inputs to the current test case are processed, to allow the verifiers to
     * know the initial state of the system.
     *
     * @param expectations the expectations to prepare to verify.
     * @param options the customisation options of how to process the expectation.
     * @return the verifier that will be called to verify the supplied {@code expectations}.
     */
    Verifier prepare(Collection<? extends T> expectations, ExpectationOptions options);

    /** A type that can be verified once all input is fed into the system. */
    interface Verifier {

        /**
         * Verify the expectations.
         *
         * @throws AssertionError on unmet expectations. Will include details of <i>which</i>
         *     expectation is not met.
         */
        void verify();
    }

    /** Customisation options for expectation handling. */
    interface ExpectationOptions {

        /**
         * The default timeout to use when verifying expectations
         *
         * <p>Test extensions may choose to provide a way for the user to override this default,
         * i.e. via an {@link Option option extensions}
         *
         * @return the default timeout to use when verifying expectations
         */
        Duration timeout();

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

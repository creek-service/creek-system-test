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


import java.util.Collection;

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
     * @return the verifier that will be called to verify the supplied {@code expectations}.
     */
    Verifier prepare(Collection<T> expectations);

    interface Verifier {

        /**
         * Verify the expectations.
         *
         * @throws AssertionError on unmet expectations. Will include details of <i>which</i>
         *     expectation is not met.
         */
        void verify();
    }
}

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

package org.creekservice.api.system.test.test.util;

import java.io.IOException;
import org.creekservice.api.system.test.extension.test.model.Expectation;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.api.system.test.extension.test.model.Ref;

/** Deserializer that can be used to test model types can be deserialized from text. */
public interface ModelParser {
    /**
     * Parse a {@link Ref} type.
     *
     * @param text the text to parse.
     * @param type the ref subtype.
     * @param <T> the ref subtype.
     * @return the parsed instance.
     * @throws IOException on parsing error.
     */
    default <T extends Ref> T parseRef(final String text, final Class<T> type) throws IOException {
        return parseOther(text, Ref.class, type);
    }

    /**
     * Parse a {@link Input} type.
     *
     * @param text the text to parse.
     * @param type the input subtype.
     * @param <T> the input subtype.
     * @return the parsed instance.
     * @throws IOException on parsing error.
     */
    default <T extends Input> T parseInput(final String text, final Class<T> type)
            throws IOException {
        return parseOther(text, Input.class, type);
    }

    /**
     * Parse a {@link Expectation} type.
     *
     * @param text the text to parse.
     * @param type the expectation subtype.
     * @param <T> the expectation subtype.
     * @return the parsed instance.
     * @throws IOException on parsing error.
     */
    default <T extends Expectation> T parseExpectation(final String text, final Class<T> type)
            throws IOException {
        return parseOther(text, Expectation.class, type);
    }

    /**
     * Parse any other object type.
     *
     * <p>The more specific methods above are preferable when testing that ref, input and
     * expectation types can be parsed, as they parse to the same base interface type as the system
     * tests themselves.
     *
     * <p>This method should be used to test other, non-polymorphic, classes that are used within
     * the model.
     *
     * @param text the text to parse
     * @param type the type of the object to parse
     * @param <T> the type of the object to parse
     * @return the parsed instance
     * @throws IOException on parsing error.
     */
    default <T> T parseOther(final String text, final Class<T> type) throws IOException {
        return parseOther(text, type, type);
    }

    /**
     * Parse any other object type.
     *
     * <p>The more specific methods above are preferable when testing that ref, input and
     * expectation types can be parsed, as they parse to the same base interface type as the system
     * tests themselves.
     *
     * <p>This method should be used to test other, polymorphic, classes that are used within the
     * model.
     *
     * @param text the text to parse
     * @param baseType the base of the polymorphic type, i.e. the field type used within the model,
     *     often an interface.
     * @param subType the subtype, generally the implementation of the interface.
     * @param <B> the base of the polymorphic type, i.e. the field type used within the model, often
     *     an interface.
     * @param <S> the subtype, generally the implementation of the interface.
     * @return the parsed instance
     * @throws IOException on parsing error.
     */
    <B, S extends B> S parseOther(String text, Class<B> baseType, Class<S> subType)
            throws IOException;
}

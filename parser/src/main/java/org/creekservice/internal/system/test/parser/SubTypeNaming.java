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

package org.creekservice.internal.system.test.parser;

/** Util class providing a default strategy for naming subtypes in JSON/YAML schemas. */
final class SubTypeNaming {

    private SubTypeNaming() {}

    /**
     * Get the type name of a subtype of a polymorphic type.
     *
     * <p>Given a subtype {@code FooBar} of a base type {@code Bar}, this method will return {@code
     * foo}.
     *
     * <p>Given a subtype {@code AnotherFooBar} of a base type {@code Bar}, this method will return
     * {@code another_foo}.
     *
     * <p>When the subtype does not end with base type name this method returns the subtype name in
     * lowercase, with {@code _} separating when capital letters were found
     *
     * @param subType the subtype of the {@code baseType}
     * @param baseType the base type
     * @param <T> the base type
     * @return the subtype name
     */
    public static <T> String subTypeName(
            final Class<? extends T> subType, final Class<T> baseType) {
        return subTypeName(subType, baseType.getSimpleName());
    }

    /**
     * Get the type name of a subtype of a polymorphic type.
     *
     * <p>Given a subtype {@code FooBar} of a base type {@code Bar}, this method will return {@code
     * foo}.
     *
     * <p>Given a subtype {@code AnotherFooBar} of a base type {@code Bar}, this method will return
     * {@code another_foo}.
     *
     * <p>When the subtype does not end with base type name this method returns the subtype name in
     * lowercase, with {@code _} separating when capital letters were found.
     *
     * @param subType the subtype of the {@code baseType}
     * @param baseTypeName the base type's simple name
     * @param <T> the base type
     * @return the subtype name
     */
    public static <T> String subTypeName(
            final Class<? extends T> subType, final String baseTypeName) {
        if (subType.isAnonymousClass() || subType.isSynthetic()) {
            throw new IllegalArgumentException(
                    "Anonymous/synthetic types are not supported: " + subType);
        }

        final String sub = subType.getSimpleName();
        final String prefix =
                sub.endsWith(baseTypeName)
                        ? sub.substring(0, sub.length() - baseTypeName.length())
                        : sub;
        final String name = prefix.replaceAll("([A-Z])", "_$1").toLowerCase();
        return name.startsWith("_") ? name.substring(1) : name;
    }
}

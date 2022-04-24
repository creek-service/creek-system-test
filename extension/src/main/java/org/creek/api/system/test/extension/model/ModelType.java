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

package org.creek.api.system.test.extension.model;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

/**
 * Holds a Creek system test model subtype, and it's metadata.
 *
 * @param <T> the model subtype.
 */
public final class ModelType<T> {

    private final Class<T> type;
    private final String name;

    /**
     * Create metadata about a {@link Seed} subtype, with an implicit name.
     *
     * <p>The name of the subtype will be derived from the {@code type} name. See {@link
     * #deriveTypeName} for details.
     *
     * @param type the subtype
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends Seed> ModelType<T> seed(final Class<T> type) {
        return seed(type, deriveTypeName(type, Seed.class));
    }

    /**
     * Create metadata about a {@link Seed} subtype, with an explicit name.
     *
     * @param type the subtype
     * @param name the name of the subtype.
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends Seed> ModelType<T> seed(final Class<T> type, final String name) {
        return modelType(type, name, Seed.class);
    }

    /**
     * Create metadata about a {@link Input} subtype, with an implicit name.
     *
     * <p>The name of the subtype will be derived from the {@code type} name. See {@link
     * #deriveTypeName} for details.
     *
     * @param type the subtype
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends Input> ModelType<T> input(final Class<T> type) {
        return input(type, deriveTypeName(type, Input.class));
    }

    /**
     * Create metadata about a {@link Input} subtype, with an explicit name.
     *
     * @param type the subtype
     * @param name the name of the subtype.
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends Input> ModelType<T> input(final Class<T> type, final String name) {
        return modelType(type, name, Input.class);
    }

    /**
     * Create metadata about a {@link Expectation} subtype, with an implicit name.
     *
     * <p>The name of the subtype will be derived from the {@code type} name. See {@link
     * #deriveTypeName} for details.
     *
     * @param type the subtype
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends Expectation> ModelType<T> expectation(final Class<T> type) {
        return expectation(type, deriveTypeName(type, Expectation.class));
    }

    /**
     * Create metadata about a {@link Input} subtype, with an explicit name.
     *
     * @param type the subtype
     * @param name the name of the subtype.
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends Expectation> ModelType<T> expectation(
            final Class<T> type, final String name) {
        return modelType(type, name, Expectation.class);
    }

    /**
     * The model name.
     *
     * <p>This is the name that users will use system test YAML file's in the top level {@code
     * '@type'} propeerty to indicate the file should be deserialized as {@link #type}.
     *
     * @return the model name.
     */
    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    private ModelType(final Class<T> type, final String name) {
        this.type = requireNonNull(type, "type");
        this.name = requireNonNull(name, "name").trim();

        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("name can not be blank");
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ModelType<?> modelType = (ModelType<?>) o;
        return Objects.equals(type, modelType.type) && Objects.equals(name, modelType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name);
    }

    @Override
    public String toString() {
        return "ModelType{" + "name=" + name + ", type='" + type + '\'' + '}';
    }

    /**
     * Get the name that will be used in the {@code '@type'} property of test files for this type.
     *
     * <p>Given a subtype {@code MyFirstThing} of base type {@code Thing}, this method returns
     * {@code my_first}.
     *
     * <p>If subtype name does not end with base type name, this method returns the full sub-type
     * name, for example given a subtype {@code MyFirst} of base type {@code Thing}, this method
     * returns {@code my_first}.
     *
     * @param subType the subtype
     * @param baseType the base type
     * @return the type name the subtype will be registered under.
     */
    private static <T> String deriveTypeName(
            final Class<? extends T> subType, final Class<T> baseType) {
        final String base = baseType.getSimpleName();
        final String sub = subType.getSimpleName();
        final String prefix =
                sub.endsWith(base) ? sub.substring(0, sub.length() - base.length()) : sub;
        final String name = prefix.replaceAll("([A-Z])", "_$1").toLowerCase();
        return name.startsWith("_") ? name.substring(1) : name;
    }

    private static <Base, T extends Base> ModelType<T> modelType(
            final Class<T> type, final String name, final Class<Base> baseType) {
        if (type.equals(baseType)) {
            throw new IllegalArgumentException(
                    "Not a subtype. type: " + type.getName() + ", baseType: " + baseType.getName());
        }
        return new ModelType<>(type, name);
    }
}

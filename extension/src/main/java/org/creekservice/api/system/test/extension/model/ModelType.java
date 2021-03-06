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

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Holds a Creek system test model subtype, and it's metadata.
 *
 * <p>The methods below come in variants that accept a custom type name and those that don't. Those
 * that don't, will use that default naming strategy defined in {@code SubTypeNaming.subTypeName} to
 * determine the type name. {@code SubTypeNaming} can be found in the {@code creek-base-type} jar.
 *
 * @param <T> the model subtype.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class ModelType<T> {

    private final Class<? super T> base;
    private final Class<T> type;
    private final Optional<String> name;

    /**
     * Create metadata about a {@link Ref} subtype that implements both {@link InputRef} and {@link
     * ExpectationRef}, with a standard type name.
     *
     * <p>The name of the subtype will be derived from the {@code type} name. See {@code
     * SubTypeNaming.subTypeName()} in {@code creek-base-schema} module for more info details.
     *
     * @param type the subtype
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends InputRef & ExpectationRef> ModelType<T> ref(final Class<T> type) {
        return modelType(type, Ref.class);
    }

    /**
     * Create metadata about a {@link Ref} subtype that implements both {@link InputRef} and {@link
     * ExpectationRef}, with a custom explicit name.
     *
     * @param type the subtype
     * @param name the name of the subtype.
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends InputRef & ExpectationRef> ModelType<T> ref(
            final Class<T> type, final String name) {
        return modelType(type, name, Ref.class);
    }

    /**
     * Create metadata about a {@link InputRef} subtype, with a standard type name.
     *
     * <p>The name of the subtype will be derived from the {@code type} name. See {@code
     * SubTypeNaming.subTypeName()} in {@code creek-base-schema} module for more info details.
     *
     * @param type the subtype
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends InputRef> ModelType<T> inputRef(final Class<T> type) {
        return modelType(type, InputRef.class);
    }

    /**
     * Create metadata about a {@link InputRef} subtype, with a custom explicit name.
     *
     * @param type the subtype
     * @param name the name of the subtype.
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends InputRef> ModelType<T> inputRef(
            final Class<T> type, final String name) {
        return modelType(type, name, InputRef.class);
    }

    /**
     * Create metadata about a {@link ExpectationRef} subtype, with a standard type name.
     *
     * <p>The name of the subtype will be derived from the {@code type} name. See {@code
     * SubTypeNaming.subTypeName()} in {@code creek-base-schema} module for more info details.
     *
     * @param type the subtype
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends ExpectationRef> ModelType<T> expectationRef(final Class<T> type) {
        return modelType(type, ExpectationRef.class);
    }

    /**
     * Create metadata about a {@link ExpectationRef} subtype, with a custom explicit name.
     *
     * @param type the subtype
     * @param name the name of the subtype.
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends ExpectationRef> ModelType<T> expectationRef(
            final Class<T> type, final String name) {
        return modelType(type, name, ExpectationRef.class);
    }

    /**
     * Create metadata about a {@link Input} subtype, with a standard type name.
     *
     * <p>The name of the subtype will be derived from the {@code type} name. See {@code
     * SubTypeNaming.subTypeName()} in {@code creek-base-schema} module for more info details.
     *
     * @param type the subtype
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends Input> ModelType<T> input(final Class<T> type) {
        return modelType(type, Input.class);
    }

    /**
     * Create metadata about a {@link Input} subtype, with a custom explicit name.
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
     * Create metadata about a {@link Expectation} subtype, with a standard type name.
     *
     * <p>The name of the subtype will be derived from the {@code type} name. See {@code
     * SubTypeNaming.subTypeName()} in {@code creek-base-schema} module for more info details.
     *
     * @param type the subtype
     * @param <T> the subtype
     * @return the model metadata
     */
    public static <T extends Expectation> ModelType<T> expectation(final Class<T> type) {
        return modelType(type, Expectation.class);
    }

    /**
     * Create metadata about a {@link Expectation} subtype, with a custom explicit name.
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
     * The explicit type name.
     *
     * <p>This is the name that users will use in system test YAML files for the top-level {@code
     * '@type'} or {@code <!name> } property to indicate what that a file should be deserialized
     * into {@link #type}.
     *
     * <p>Where no explicit name is provided, the system will use the default naming strategy
     * defined by {@code SubTypeNaming.subTypeName()}.
     *
     * @return any explicit model name.
     */
    public Optional<String> name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    public Class<? super T> base() {
        return base;
    }

    private ModelType(
            final Class<? super T> base, final Class<T> type, final Optional<String> name) {
        this.base = requireNonNull(base, "base");
        this.type = requireNonNull(type, "type");
        this.name = requireNonNull(name, "name").map(String::trim);

        if (type.isAnonymousClass() || type.isSynthetic()) {
            throw new IllegalArgumentException(
                    "Anonymous/synthetic types are not supported: " + type);
        }

        if (name.isPresent() && name.get().isBlank()) {
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
        return "ModelType{name=" + name + ", type='" + type + '\'' + '}';
    }

    private static <Base, T extends Base> ModelType<T> modelType(
            final Class<T> type, final Class<Base> base) {
        return modelType(type, Optional.empty(), base);
    }

    private static <Base, T extends Base> ModelType<T> modelType(
            final Class<T> type, final String name, final Class<Base> base) {
        return modelType(type, Optional.of(name), base);
    }

    private static <Base, T extends Base> ModelType<T> modelType(
            final Class<T> type, final Optional<String> name, final Class<Base> base) {
        if (type.equals(base)) {
            throw new IllegalArgumentException(
                    "Not a subtype. type: " + type.getName() + ", base: " + base.getName());
        }
        return new ModelType<>(base, type, name);
    }
}

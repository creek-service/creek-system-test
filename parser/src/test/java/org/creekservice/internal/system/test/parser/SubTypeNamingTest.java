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

package org.creekservice.internal.system.test.parser;

import static org.creekservice.api.base.schema.naming.SubTypeNaming.subTypeName;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SubTypeNamingTest {

    @Test
    void shouldReturnPrefixIfSubTypeNamedEndsInBaseType() {
        assertThat(subTypeName(SomeThing.class, Thing.class), is("some"));
        assertThat(subTypeName(SomeOtherThing.class, Thing.class), is("some_other"));
        assertThat(subTypeName(SomeOtherThing.class, "Thing"), is("some_other"));
    }

    @Test
    void shouldReturnFullNameIfSubTypeNamedDoesNotEndInBaseType() {
        assertThat(
                subTypeName(TotallyDifferentName.class, Thing.class), is("totally_different_name"));
        assertThat(subTypeName(SomeThingDifferent.class, Thing.class), is("some_thing_different"));
        assertThat(subTypeName(SomeThingDifferent.class, "Thing"), is("some_thing_different"));
    }

    @Test
    void shouldDeriveNameOfClassStartingWithLowerCaseLetter() {
        assertThat(subTypeName(lowerThing.class, Thing.class), is("lower"));
    }

    @Test
    void shouldDoSomethingSensibleWithWierdNames() {
        assertThat(
                subTypeName(_$Weird_Class_$NAme_.class, Thing.class),
                is("$_weird__class_$_n_ame_"));
    }

    @Test
    void shouldThrowOnAnonymousTypes() {
        // Given:
        final Thing anonymousType = new Thing() {};

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> subTypeName(anonymousType.getClass(), Thing.class));

        // Then:
        assertThat(
                e.getMessage(),
                is("Anonymous/synthetic types are not supported: " + anonymousType.getClass()));
    }

    @Test
    void shouldThrowOnSyntheticTypes() {
        // Given:
        final BaseFunctional lambda = () -> "name";

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> subTypeName(lambda.getClass(), BaseFunctional.class));

        // Then:
        assertThat(
                e.getMessage(),
                is("Anonymous/synthetic types are not supported: " + lambda.getClass()));
    }

    private interface BaseFunctional {
        @SuppressWarnings("unused")
        String name();
    }

    private interface Thing {}

    private interface SomeThing extends Thing {}

    private interface SomeOtherThing extends Thing {}

    private interface TotallyDifferentName extends Thing {}

    private interface SomeThingDifferent extends Thing {}

    @SuppressWarnings("checkstyle:TypeName")
    private interface lowerThing extends Thing {}

    @SuppressWarnings("checkstyle:TypeName")
    private interface _$Weird_Class_$NAme_ extends Thing {}
}

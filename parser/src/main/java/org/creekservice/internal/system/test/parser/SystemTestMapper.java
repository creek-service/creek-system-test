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

package org.creekservice.internal.system.test.parser;

import static org.creekservice.api.base.schema.naming.SubTypeNaming.subTypeName;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import java.util.Collection;
import org.creekservice.api.system.test.extension.test.model.Expectation;
import org.creekservice.api.system.test.extension.test.model.ExpectationRef;
import org.creekservice.api.system.test.extension.test.model.Input;
import org.creekservice.api.system.test.extension.test.model.InputRef;
import org.creekservice.api.system.test.extension.test.model.ModelType;
import org.creekservice.api.system.test.extension.test.model.Ref;
import org.creekservice.api.system.test.model.LocationAware;

public final class SystemTestMapper {

    private SystemTestMapper() {}

    public static ObjectMapper create(final Collection<ModelType<?>> modelTypes) {
        final SimpleModule modelModule = new SimpleModule();
        modelModule.setDeserializerModifier(new LocationAwareDeserializerModifier());

        final JsonMapper.Builder builder =
                JsonMapper.builder(new YAMLFactory().enable(YAMLGenerator.Feature.MINIMIZE_QUOTES))
                        .disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS)
                        .addModule(modelModule)
                        .addModule(new Jdk8Module())
                        .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                        .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                        .enable(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES)
                        .enable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
                        .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION)
                        .serializationInclusion(JsonInclude.Include.NON_EMPTY);

        registerModelTypes(builder, modelTypes);

        return builder.build();
    }

    private static void registerModelTypes(
            final JsonMapper.Builder builder, final Collection<ModelType<?>> modelTypes) {

        builder.addMixIn(Input.class, ModelMixin.class);
        builder.addMixIn(Expectation.class, ModelMixin.class);
        builder.addMixIn(Ref.class, RefMixin.class);
        builder.addMixIn(InputRef.class, RefMixin.class);
        builder.addMixIn(ExpectationRef.class, RefMixin.class);

        modelTypes.stream().map(SystemTestMapper::namedType).forEach(builder::registerSubtypes);
    }

    private static <T> NamedType namedType(final ModelType<T> modelType) {
        final String name =
                modelType.name().orElseGet(() -> subTypeName(modelType.type(), modelType.base()));
        return new NamedType(modelType.type(), name);
    }

    private static final class LocationAwareDeserializerModifier extends BeanDeserializerModifier {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public JsonDeserializer<?> modifyDeserializer(
                final DeserializationConfig config,
                final BeanDescription beanDesc,
                final JsonDeserializer<?> deserializer) {

            if (LocationAware.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new LocationAwareDeserializer<>(
                        (Class) beanDesc.getBeanClass(), deserializer);
            }
            return deserializer;
        }
    }
}

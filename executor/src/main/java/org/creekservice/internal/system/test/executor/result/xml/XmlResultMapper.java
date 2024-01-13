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

package org.creekservice.internal.system.test.executor.result.xml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/** Mapper for writing XML Junit compatible results. */
public enum XmlResultMapper {
    /** The one and only instance. */
    INSTANCE;

    private final ObjectMapper mapper = create();

    /**
     * @return get the mapper instance.
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intentional exposure")
    public ObjectMapper get() {
        return mapper;
    }

    private static ObjectMapper create() {
        final XmlFactory xf = new XmlFactory();

        final JacksonXmlModule xmlModule = new JacksonXmlModule();
        xmlModule.setDefaultUseWrapper(false);

        return XmlMapper.builder(xf)
                .addModules(xmlModule, new Jdk8Module())
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .serializationInclusion(JsonInclude.Include.NON_EMPTY)
                .build();
    }
}

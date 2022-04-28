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

package org.creek.internal.system.test.parser;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.creek.api.system.test.model.SimpleRef;

/**
 * {@link org.creek.api.system.test.extension.model.Ref} model types as being polymorphic, with
 * {@link SimpleRef} as its default impl.
 *
 * <p>The use of mixins avoids the need for the {@code extension} model to depend on Jackson
 * annotations.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = SimpleRef.class)
public interface RefMixin extends ModelMixin {}
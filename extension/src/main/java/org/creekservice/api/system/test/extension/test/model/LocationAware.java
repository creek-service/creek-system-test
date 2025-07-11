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

import java.net.URI;

/**
 * Marks an instance of a type as being able to track the location it was loaded from.
 *
 * <p>Any test model extensions that implement this interface will have the {@link
 * LocationAware#withLocation(URI)} method called during deserialization. The supplied URI will be
 * the file and line number the instance was encountered with the system test YAML files.
 *
 * @param <T> the type that is location aware.
 */
public interface LocationAware<T extends LocationAware<T>> extends Locatable {

    /**
     * The default location an instance can use before the deserializer calls {@link
     * #withLocation(URI)}.
     */
    URI UNKNOWN_LOCATION = URI.create("file://unknown");

    /**
     * @param location the location
     * @return a new instance with the location set.
     */
    T withLocation(URI location);
}

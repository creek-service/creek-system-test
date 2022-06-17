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

package org.creekservice.api.system.test.extension.service;


import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** A collection of service instances */
public interface ServiceCollection extends Iterable<ServiceInstance> {
    /**
     * Returns a sequential {@link Stream} with this collection as its source.
     *
     * @return a sequential {@link Stream} over the {@link ServiceInstance instance's} in this
     *     collection.
     */
    default Stream<ServiceInstance> stream() {
        return StreamSupport.stream(spliterator(), false);
    }
}

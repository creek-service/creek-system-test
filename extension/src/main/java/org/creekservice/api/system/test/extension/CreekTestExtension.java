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

package org.creekservice.api.system.test.extension;

/**
 * Base type for system test extensions to Creek.
 *
 * <p>Creek will look for extensions using {@link java.util.ServiceLoader} to load instances of this
 * type from the class & module paths. Therefore, to be loaded by Creek the extension must:
 *
 * <ul>
 *   <li>be listed in the {@code module-info.java} file as a {@code provider} of {@link
 *       CreekTestExtension}, if using Java modules, or
 *   <li>have a suitable entry in the {@code META-INFO.services} directory
 *   <li>or both
 * </ul>
 */
public interface CreekTestExtension {

    /** @return the extension name. */
    String name();

    /** Called to allow the extension to do its thing. */
    void initialize(CreekSystemTest systemTest);
}

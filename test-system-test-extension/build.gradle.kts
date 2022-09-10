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

plugins {
    `java-library`
}

val jacksonVersion : String by extra
val creekServiceVersion : String by extra

dependencies {
    api(project(":extension"))
    api("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")

    implementation(project(":test-service-extension-metadata"))
    implementation("org.creekservice:creek-service-api:$creekServiceVersion")
}

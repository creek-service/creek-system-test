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
    application
}

val creekBaseVersion : String by extra
val creekObsVersion : String by extra
val testContainersVersion : String by extra
val picocliVersion : String by extra
val log4jVersion : String by extra
val spotBugsVersion : String by extra

dependencies {
    implementation(project(":extension"))
    implementation(project(":parser"))
    implementation("org.creekservice:creek-base-type:$creekBaseVersion")
    implementation("org.creekservice:creek-platform-metadata:$creekBaseVersion")
    implementation("org.creekservice:creek-observability-lifecycle:$creekObsVersion")
    implementation("com.github.spotbugs:spotbugs-annotations:$spotBugsVersion")
    implementation("org.testcontainers:testcontainers:$testContainersVersion")
    implementation("info.picocli:picocli:$picocliVersion")
    implementation("org.apache.logging.log4j:log4j-api:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j18-impl:$log4jVersion")

    testImplementation(project(":test-extension"))
    testImplementation(project(":test-services"))
}

application {
    mainModule.set("creek.system.test.executor")
    mainClass.set("org.creekservice.api.system.test.executor.SystemTestExecutor")
}

tasks.test {
    dependsOn("installDist")
    dependsOn(":test-extension:jar")
    dependsOn(":test-services:jar")
    dependsOn(":test-service:buildAppImage")
}
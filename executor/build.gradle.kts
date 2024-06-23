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

plugins {
    `java-library`
    application
}

val creekVersion : String by extra
val testContainersVersion : String by extra
val picocliVersion : String by extra
val log4jVersion : String by extra
val spotBugsVersion : String by extra
val jacksonVersion : String by extra

dependencies {
    implementation(project(":extension"))
    implementation(project(":parser"))
    implementation("org.creekservice:creek-base-type:$creekVersion")
    implementation("org.creekservice:creek-platform-metadata:$creekVersion")
    implementation("org.creekservice:creek-platform-resource:$creekVersion")
    implementation("org.creekservice:creek-observability-lifecycle:$creekVersion")
    implementation("org.creekservice:creek-service-api:$creekVersion")
    implementation("com.github.spotbugs:spotbugs-annotations:$spotBugsVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("org.testcontainers:testcontainers:$testContainersVersion")
    implementation("info.picocli:picocli:$picocliVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")

    testImplementation(project(":test-system-test-extension"))
    testImplementation(project(":test-services"))
}

application {
    mainModule.set("creek.system.test.executor")
    mainClass.set("org.creekservice.api.system.test.executor.SystemTestExecutor")
}

tasks.test {
    dependsOn("installDist")
    dependsOn(":test-system-test-extension:jar")
    dependsOn(":test-service-extension-metadata:jar")
    dependsOn(":test-services:jar")
    dependsOn(":test-service:buildAppImage")
}

tasks.compileTestJava {
    // For some reason, since upgrading guava-testlib from 33.0.0-jre to 33.1.0-jre, without this, task fails with:
    // ../com.google.guava/guava/33.2.1-jre/818e780da2c66c63bbb6480fef1f3855eeafa3e4/guava-33.2.1-jre.jar(/com/google/common/collect/Streams.class): warning: Cannot find annotation method 'replacement()' in type 'InlineMe': class file for com.google.errorprone.annotations.InlineMe not found
    options.compilerArgs.add("-Xlint:-classfile")
}
/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
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

description = "DynamiaTools - App"

dependencies {
    compileOnly("jakarta.servlet:jakarta.servlet-api")

    api(project(":actions"))
    api(project(":commons"))
    api(project(":crud"))
    api(project(":domain"))
    api(project(":integration"))
    api(project(":io"))
    api(project(":navigation"))
    api(project(":reports"))
    api(project(":templates"))
    api(project(":viewers"))
    api(project(":web"))

    api("org.springframework.data:spring-data-rest-webmvc")
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    api("org.springframework:spring-context-support")
    compileOnly("org.slf4j:slf4j-api:${rootProject.ext["slf4jVersion"]}")

    api("org.springframework.boot:spring-boot")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework:spring-websocket")

    implementation("org.apache.velocity:velocity-engine-core:${rootProject.ext["velocityVersion"]}")
    implementation("net.sf.jasperreports:jasperreports:${rootProject.ext["jasperreportsVersion"]}")
    implementation("org.apache.velocity.tools:velocity-tools-generic:3.1")
    implementation("org.ehcache:ehcache:3.10.8")

    // Test dependencies
    testImplementation(project(":domain-jpa"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("jakarta.persistence:jakarta.persistence-api")
    testImplementation("com.h2database:h2")

}

tasks.processResources {
    filesMatching("**/*.properties") {
        filter { line ->
            line.replace("\${project.version}", version.toString())
        }
    }
}

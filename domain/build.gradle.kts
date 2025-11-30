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

description = "DynamiaTools - Domain"

dependencies {
    api(project(":commons"))
    api(project(":integration"))
    api(project(":io"))

    api("org.springframework:spring-core")
    api("org.springframework:spring-context")
    runtimeOnly("org.springframework:spring-aop")
    runtimeOnly("org.springframework:spring-aspects")
    api("org.springframework:spring-tx")
    api("org.springframework:spring-jdbc")

    compileOnly("jakarta.validation:jakarta.validation-api")
    compileOnly("jakarta.persistence:jakarta.persistence-api")

    testImplementation("org.springframework:spring-test")
    testImplementation("jakarta.validation:jakarta.validation-api")
    testImplementation("org.hibernate.validator:hibernate-validator")
    testImplementation("org.glassfish:jakarta.el:4.0.2")
}


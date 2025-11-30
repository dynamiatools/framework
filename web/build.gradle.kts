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

description = "DynamiaTools - Web - A set of common classes and interfaces for web application development"

dependencies {
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    api("org.springframework:spring-webmvc")
    api("io.swagger.core.v3:swagger-annotations:${rootProject.ext["swaggerVersion"]}")

    api(project(":crud"))
    api(project(":viewers"))
    api(project(":navigation"))
    api(project(":integration"))
    api(project(":commons"))
}

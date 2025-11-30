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

description = "DynamiaTools - ZK - Classes for ZK web application"

dependencies {
    api(project(":web"))
    api(project(":navigation"))
    api(project(":ui"))
    api(project(":domain"))
    api(project(":viewers"))
    api(project(":crud"))
    api(project(":reports"))
    api(project(":templates"))

    implementation("org.yaml:snakeyaml:${rootProject.ext["snakeyamlVersion"]}")

    api("tools.dynamia:dynamia-zk-addons:${rootProject.ext["dynamiaZkVersion"]}")
    api("org.zkoss.zk:zkplus:${rootProject.ext["zkVersion"]}")
    api("org.zkoss.zk:zkbind:${rootProject.ext["zkVersion"]}")
    api("org.zkoss.zk:zul:${rootProject.ext["zkVersion"]}")
    api("org.zkoss.zk:zhtml:${rootProject.ext["zkVersion"]}")

    api("org.springframework.boot:spring-boot")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework:spring-websocket")
    compileOnly("jakarta.servlet:jakarta.servlet-api")
}

tasks.processResources {
    exclude("**/lang-addon.xml")

    from("src/main/resources") {
        include("**/lang-addon.xml")
        filter { line ->
            line.replace("\${project.version}", version.toString())
        }
    }
}

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

description = "DynamiaTools - Domain JPA"

dependencies {
    api(project(":domain"))

    api("org.springframework.data:spring-data-jpa")
    api("org.springframework:spring-orm")
    api("org.hibernate.orm:hibernate-core")
    implementation("org.hibernate.validator:hibernate-validator")

    api("jakarta.validation:jakarta.validation-api")
    api("jakarta.persistence:jakarta.persistence-api")

    testImplementation("org.springframework:spring-test")
    testImplementation("com.h2database:h2")
}

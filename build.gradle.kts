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

plugins {
    java
    `java-library`
    `maven-publish`
    signing
    id("org.springframework.boot") version "3.5.8" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

group = "tools.dynamia"
version = "5.4.7"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven { url = uri("https://mavensync.zkoss.org/maven2") }
}

// Configuración común para todos los sub-proyectos
subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "io.spring.dependency-management")

    group = "tools.dynamia"
    version = "5.4.7"

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withJavadocJar()
        withSourcesJar()
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://mavensync.zkoss.org/maven2") }
    }

    // Gestión de dependencias de Spring Boot
    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.8")
        }
    }

    dependencies {
        // Dependencias comunes de testing
        testImplementation("junit:junit:4.13.2")
        testImplementation("org.slf4j:slf4j-jdk14:2.0.17")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
    }

    tasks.withType<Javadoc> {
        options {
            this as StandardJavadocDocletOptions
            addStringOption("Xdoclint:none", "-quiet")
            encoding = "UTF-8"
        }
    }

    tasks.named<Test>("test") {
        useJUnit()
    }

    // Configuración de publicación
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                pom {
                    name.set(project.name)
                    description.set("Fullstack web framework for java web applications")
                    url.set("https://www.dynamia.tools")
                    inceptionYear.set("2009")

                    organization {
                        name.set("Dynamia Soluciones IT SAS")
                        url.set("https://www.dynamiasoluciones.com")
                    }

                    licenses {
                        license {
                            name.set("APACHE LICENSE, VERSION 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            name.set("Mario Serrano Leones")
                            email.set("mario@dynamiasoluciones.com")
                            organization.set("Dynamia Soluciones IT")
                            organizationUrl.set("https://www.dynamiasoluciones.com")
                        }
                    }

                    scm {
                        url.set("https://github.com/dynamiatools/framework")
                    }
                }
            }
        }
    }

    // Configuración de firma (solo si performRelease está activo)
    if (project.hasProperty("performRelease")) {
        signing {
            sign(publishing.publications["mavenJava"])
        }
    }
}

// Propiedades del proyecto
ext {
    set("springbootVersion", "3.5.8")
    set("swaggerVersion", "2.2.38")
    set("mongodbVersion", "1")
    set("zkVersion", "10.1.0-jakarta")
    set("dynamiaZkVersion", "1.1.0")
    set("jasperreportsVersion", "6.21.4")
    set("poiVersion", "5.4.1")
    set("snakeyamlVersion", "2.5")
    set("zxingVersion", "3.5.3")
    set("velocityVersion", "2.4.1")
    set("slf4jVersion", "2.0.17")
}



[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia/tools.dynamia.app)](https://search.maven.org/search?q=tools.dynamia)
![Java Version Required](https://img.shields.io/badge/java-21-blue)
[![Java CI with Maven](https://github.com/dynamiatools/framework/actions/workflows/maven.yml/badge.svg)](https://github.com/dynamiatools/framework/actions/workflows/maven.yml)
[![DynamiaTools Documentation](https://img.shields.io/badge/DynamiaTools-Documentation-orange)](https://dynamia.tools/getting-started/)



<p align="center">
  <img src="https://dynamia.tools/_astro/dynamia-tools-splash.B183ReOk_Z1w9UUh.webp" alt="Dynamia Tools Logo" width="100"/>
</p>

<h1 align="center">DynamiaTools</h1>
DynamiaTools is a cutting-edge full-stack Java 17+ framework designed for building powerful enterprise web applications, harnessing the strength of Spring Boot 3 and the elegance of ZK 10. 


## With DynamiaTools you can

- Create fully functional web applications without too much knowledge about Web Development.
- Write modular applications. Yes, you could create a module (jar file)  and reuse it.
- It helps you to keep your project DRY.
- Automatic CRUD.
- Automatic REST endpoints.
- Extends the framework with new modules, actions and services.
- Use HTML5 in your application thanks to ZK framework, this means you can use bootstrap, css3, and many HTML5
  technologies
- Customize your application with themes or templates.
- Use MVC or MVVM
- Integrate with other Java framework
- Much more.

## Installation

- Create a new SpringBoot project using https://start.spring.io
- Select Web, JPA and programing language for your spring app
- Optional select another framework or jdbc driver your need
- Download and import it in your IDE
- Add DynamiaTools dependencies
- Enable DynamiaTools in you application

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tools.dynamia.app.EnableDynamiaTools;

@SpringBootApplication
@EnableDynamiaTools // <- this is all you need
class MyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }

}
```

- Run
- Done!

**Maven**

```xml

<dependencies>
    <dependency>
        <groupId>tools.dynamia</groupId>
        <artifactId>tools.dynamia.app</artifactId>
        <version>5.4.0</version>
    </dependency>

    <dependency>
        <groupId>tools.dynamia</groupId>
        <artifactId>tools.dynamia.zk</artifactId>
        <version>5.4.0</version>
    </dependency>

    <dependency>
        <groupId>tools.dynamia</groupId>
        <artifactId>tools.dynamia.domain.jpa</artifactId>
        <version>5.4.0</version>
    </dependency>
</dependencies>
```

**Gradle**

```groovy
compile 'tools.dynamia:tools.dynamia.app:5.4.0'
compile 'tools.dynamia:tools.dynamia.zk:5.4.0'
compile 'tools.dynamia:tools.dynamia.domain.jpa:5.4.0'
```

Artifacts are available in **Maven Central** repositories

## Documentation

Please visit (https://dynamia.tools) for full documentation and tutorials

## Building

- Install OpenJDK 21
- Install Maven 3.x
- Install Git
- Clone this repository
- Execute `mvn clean install` ;-)
- Done

## License

DynamiaTools is available under Apache 2 License

## History

We start developing DynamiaTools 13 years ago as an internal utility library for our projects at Dynamia Soluciones
IT (https://www.dynamiasoluciones.com). We needed that our developers could create web applications fast without too
much knowledge about specific frameworks or APIs like Spring or JPA.

### v1.x

First version was focus in many utility classes for hiding spring complexity, ZK apis, database connection, project
configuration, page navigation, services and domain code integration and reusable simple modules. We did same stuff over
and over again when we need create consistency user interfaces and actions across modules.

### v2.x

Then version 2.x focus on simplify and automatize user interface creation without the need to generate code
(that we think is easy to use but very hard to maintain). We created something called view descriptors, instead of write
UI code and layout components you just describe it using a simple YML plain file and, the framework do its best to
create the UI for you. On the other hand there are the most common thing you do when create any application that use
some data storage, the infamous CRUDs. We ended writing the same kind of code for every simple entity. DynamiaTools
create beautiful CRUDs for you.  
You just need create an entity and your done.

### v3.x

Finally, version 3.x come to life, we reorganize, repackage and optimize everything. This version has better code,
better design, better performance, new and great features, it's not backwards compatible and, most important is the
first version open source. Yes, we want to share our work with you.

### v4.x

Now, version 4.x goal is Java 11+ and beyond, better performance, integrate new frameworks and new website with much better
documentations and demos.

Please use it, extend it and help us to build a great community around it. We will continue support this project always.
Currently, we have 10 projects and more than 40 modules powered by DynamiaTools 4.  As you can see this is not
a new framework but is new in the open source world. This is not the only project we are going to open, we have more
products to share.

### v5.x

The main goal of 5.x version is to upgrade to latest version of Java 21, spring and hibernate.

## Want contribute?

Please read [Contributing Guidelines](https://github.com/dynamiatools/framework/blob/master/CONTRIBUTING.md)

## ☕ Support DynamiaTools

Hey there! DynamiaTools is built with a lot of dedication to help developers speed up their projects and make development more enjoyable. If DynamiaTools has been useful to you or your projects, consider supporting its development. Your support helps keep the project alive and evolving with new features and improvements. Every coffee counts! 😊

<a href="https://www.buymeacoffee.com/marioserrano" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 60px !important;width: 217px !important;" ></a>


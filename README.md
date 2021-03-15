[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia/tools.dynamia.zk.app)](https://search.maven.org/search?q=tools.dynamia)
![Java Version Required](https://img.shields.io/badge/java-%3E%3D11-blue)
[![Build Status](https://travis-ci.com/dynamiatools/framework.svg?branch=main)](https://travis-ci.com/dynamiatools/framework)


# Overview

DynamiaTools is a framework for building Java 11+ web applications fast and easy using standard design patterns, views
descriptors for automatic UI generation, actions, services and modules. Based in Spring 5 (https://spring.io), ZK
9 (https://www.zkoss.org) for web UI and JPA 2.

## With DynamiaTools you can

- Create fully functional web applications without too much knowledge about Web Development.
- Write modular applications. Yes, you could create a module (jar file)  and reuse it.
- It helps you to keep your project DRY.
- Automatically create CRUDs for you.
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
import tools.dynamia.domain.jpa.EnableDynamiaToolsJPA;
import tools.dynamia.zk.app.EnableDynamiaTools;

@SpringBootApplication
@EnableDynamiaTools
@EnableDynamiaToolsJPA
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
        <artifactId>tools.dynamia.zk.app</artifactId>
        <version>4.0.1</version>
    </dependency>

    <dependency>
        <groupId>tools.dynamia</groupId>
        <artifactId>tools.dynamia.domain.jpa</artifactId>
        <version>4.0.1</version>
    </dependency>    
</dependencies>
```

**Gradle**

```groovy
compile 'tools.dynamia:tools.dynamia.zk.app:4.0.1'
compile 'tools.dynamia:tools.dynamia.domain.jpa:4.0.1'
```

Artifacts are available in **Maven Central** repositories

## Documentation

Please visit (https://dynamia.tools) for full documentation and tutorials

## Building

- Install OpenJDK 11
- Install Maven 3.x
- Install Git
- Clone this repository
- Execute `mvn clean install` ;-)
- Done

## License

DynamiaTools is available under Apache 2 License

## History

We start developing DynamiaTools 12 years ago as an internal utility library for our projects at Dynamia Soluciones
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

## Want contribute?

Please read [Contributing Guidelines](https://github.com/dynamiatools/framework/blob/master/CONTRIBUTING.md)

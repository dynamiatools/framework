[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia.themes/tools.dynamia.themes.dynamical.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22tools.dynamia.themes%22%20AND%20a:%22tools.dynamia.themes.dynamical%22)
[![Maven Build](https://github.com/dynamiatools/theme-dynamical/actions/workflows/maven.yml/badge.svg)](https://github.com/dynamiatools/theme-dynamical/actions/workflows/maven.yml)
[![Release and Deploy](https://github.com/dynamiatools/theme-dynamical/actions/workflows/release.yml/badge.svg)](https://github.com/dynamiatools/theme-dynamical/actions/workflows/release.yml)
![Java Version Required](https://img.shields.io/badge/java-21-blue)

# Bootstrap theme for DynamiaTools

This template is based on https://adminlte.io/ free (MIT) admin template. Which is a fully responsive admin template. Based on Bootstrap 5.x framework. Highly customizable and easy to use. Fits many screen resolutions from small mobile devices to large desktops. 

![Screenshot](https://github.com/dynamiatools/theme-dynamical/blob/master/screenshots/screenshot1.png?raw=true)

## Features
- 10 skins available
- Support for user login and profile UI
- Multi tabbed UI
- Full Responsive
- Optimized to work with ZK 10.x
- Compact
- Support for app logo and icon


## Installation

**Maven**
```xml
<dependency>
  <groupId>tools.dynamia.themes</groupId>
  <artifactId>tools.dynamia.themes.dynamical</artifactId>
  <version>5.4.0</version>
</dependency>
```

**Gradle**
```groovy
compile 'tools.dynamia.themes:tools.dynamia.themes.dynamical:5.4.0'
```

Edit Spring Boot properties ```application.properties```
```properties
dynamia.app.template=Dynamical
dynamia.app.default-skin=Blue
```

Or application.yml
```yaml
dynamia:
   app:
     template: Dynamical
     skin: Blue
```

## Available Skins
- Blue
- Dynamia
- Red
- Orange
- Purple
- Green
- DarkOrange
- Yellow
- Olive
- Black

## License

Theme Dynamical is available under Apache 2 License

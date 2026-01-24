[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia.reports/tools.dynamia.reports.core)](https://search.maven.org/search?q=tools.dynamia.reports)
![Java Version Required](https://img.shields.io/badge/java-17-blue)
[![Maven Build](https://github.com/dynamiatools/dynamia-reports/actions/workflows/maven.yml/badge.svg)](https://github.com/dynamiatools/dynamia-reports/actions/workflows/maven.yml)
[![Release and Deploy](https://github.com/dynamiatools/dynamia-reports/actions/workflows/release.yml/badge.svg)](https://github.com/dynamiatools/dynamia-reports/actions/workflows/release.yml)

# DynamiaReports

Is a small reporting framework built using DynamiaTools for generate reports in app storing queries metadata in database and allowing users to run, edit, copy the reports. 

## Initial Features
- Create Queries using JPSQL or native SQL
- Create or use current database datasource
- Use filters
- Show reports in screen
- Exports reports to CVS, Excel and PDF
- Send reports to email
- View Reports 
- Embed reports in other apps
- Integrate as a module with any DynamiaTools modules
- View reports data as charts


## Architecture
Modules organization

### Core Module
Main module with all reporting logic, allowing to run, store, filter and generate report data. 

### UI Module
- View report data in table view
- Edit reports
- Show report filters
- Export to CSV, Excel and PDF actions
- Custom report actions for extensions

### Datasources Modules
Additional module for  create and connect to external datasources. Mainly SQL databases, later NoSQL and plain files

### Boot module
A spring boot application to run DynamiaReports as a standalone app.

# License
Opensource project using Apache 2.0 license





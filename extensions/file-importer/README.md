[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia.modules/tools.dynamia.modules.importer)](https://search.maven.org/search?q=tools.dynamia.modules.importer)
![Java Version Required](https://img.shields.io/badge/java-25-blue)


# Importer Module

This [DynamiaTools](https://dynamia.tools) extension allow you easily import and process Excel files. It provides async
upload and processing, progress monitor, allow user to stop importing process and more.

You can add your own `ImportAction` to process other file types

## Modules

- Core: Entities, Services and API implementation
- UI: Actions and views for user interface integration.

## Installation

Add the following dependencies to project classpath

**Maven**

```xml

<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.importer</artifactId>
    <version>26.2.2</version>
</dependency>
```

```xml

<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.importer.ui</artifactId>
    <version>26.2.2</version>
</dependency>

```

**Gradle**

```groovy
compile 'tools.dynamia.modules:tools.dynamia.modules.importer:26.2.2'
compile 'tools.dynamia.modules:tools.dynamia.modules.importer.ui:26.2.2'
```

## Usage

The `Importer` class is a ZK `Window` to show user the Excel format they need to import and show actions to process the
file. You can map columns to POJO properties

```java
import tools.dynamia.actions.ActionEvent;
import tools.dynamia.crud.AbstractCrudAction;
import tools.dynamia.modules.importer.ui.Importer;
import tools.dynamia.modules.importer.ImportExcelAction;
import myproject.Person;

@InstallAction
class ImportPeopleAction extends AbstractCrudAction {

    public ImportPeopleAction() {
        setName("Import");
        setApplicableClass(Person.class);
    }

    public void actionPerformed(ActionEvent evt) {

        var importer = new Importer();
        importer.addColumn("Code");
        importer.addColumn("Name");
        importer.addColumn("Last Name", "name2"); //map to property name2
        importer.addColumn("Start Date", "dateOfStart");
        importer.addAction(new ImportPeopleAction()); //custom import action

        importer.show("Import People"); // show the import window to user
    }

}

class ImportPeopleAction extends ImportExcelAction<Person> {

    @Override
    public List<Person> importFromExcel(InputStream excelFile, ProgressMonitor monitor) throws Exception {

        var result = new ArrayList<Person>();

        ImportUtils.readExcel(excelFile, monitor, row -> {

            var code = ImportUtils.getCellValue(row, 0); //get string value from column A
            var name = ImportUtils.getCellValue(row, 1); //get string value from column B
            var lastName = ImportUtils.getCellValue(row, 2); //get string value from column C
            var startDate = ImportUtils.getCellValueObject(row, 3); //get object value from column D

            var person = new Person();
            person.setCode(code);
            person.setName(name);
            person.setName2(lastName);
            if (startDate instanceof Date) {
                person.setDateOfStart((Date) startDate);
            }

            result.add(person);
        });

        return result;
    }
}
```
## License

DynamiaTools Importer is available under Apache 2 License

[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia.modules/tools.dynamia.modules.entityfiles)](https://search.maven.org/search?q=tools.dynamia.modules.entityfiles)
![Java Version Required](https://img.shields.io/badge/java-21-blue)
[![Maven Build](https://github.com/dynamiatools/module-entityfiles/actions/workflows/maven.yml/badge.svg)](https://github.com/dynamiatools/module-entityfiles/actions/workflows/maven.yml)
[![Release and Deploy](https://github.com/dynamiatools/module-entityfiles/actions/workflows/release.yml/badge.svg)](https://github.com/dynamiatools/module-entityfiles/actions/workflows/release.yml)

# EntityFiles Module

This [DynamiaTools](https://dynamia.tools) extension allow attaching files to database entities. Files are saved to locale disk or using AWS S3 and file
metadata are store in the database in table `mod_entity_files` using JPA entity `EntityFile`

## Modules

- Core: Domain and API
- UI: Actions and views for user interface integration.
- S3: Support to store files in Amazon S3 Bucket

## Installation

### Maven

```xml

<dependencies>
    <dependency>
        <groupId>tools.dynamia.modules</groupId>
        <artifactId>tools.dynamia.modules.entityfiles</artifactId>
        <version>7.4.0</version>
    </dependency>

    <dependency>
        <groupId>tools.dynamia.modules</groupId>
        <artifactId>tools.dynamia.modules.entityfiles.ui</artifactId>
        <version>7.4.0</version>
    </dependency>
</dependencies>
```

#### AWS S3 Support
```xml
    <dependency>
        <groupId>tools.dynamia.modules</groupId>
        <artifactId>tools.dynamia.modules.entityfiles.s3</artifactId>
        <version>7.4.0</version>
    </dependency>
```

### Gradle

```groovy
compile 'tools.dynamia.modules:tools.dynamia.modules.entityfiles:7.4.0'
compile 'tools.dynamia.modules:tools.dynamia.modules.entityfiles.ui:7.4.0'
compile 'tools.dynamia.modules:tools.dynamia.modules.entityfiles.s3:7.4.0'
```

## Usage

Add a field of type `tools.dynamia.modules.entityfile.domain.EntityFile` in your JPA entity with association `@OneToOne`

```Java
import tools.dynamia.modules.entityfile.domain.EntityFile;

@Entity
public class Person implements AccountAware {


    @OneToOne
    private EntityFile photo;
    @OneToOne
    private EntityFile cover;
    // other fields

    //getter and setters    
}

```

Beside entities, the UI module install a new `Action` called `FileAction` to manage files attached to entities. This
action is showed in `CrudState.READ` (tables or tree) of CrudView.

The same functionality of `FileAction` can be using anywhere in your user interface code invoking

```Java
EntityFileUtils.showFileExplorer(entity);
```

## EntityFileService

`tools.dynamia.modules.entityfile.service.EntityFileService`

This service is the central core of this module, it helps you to attach any kind of file to any JPA entity.

Examples:

```Java

//somewhere in your code
@Component
class SomeSpringCompoonent{

    @Autowired
    private EntityFileService service;

    public void attachPhoto(File file, Person entity) {
        var fileInfo = new UploadFileInfo(file);
        var photo = entityService.createEntityFile(fileInfo, entity);

        entity.setPhoto(photo);
    }
}
```

### EntityFileStorage

Internally `EntityFileService` use an instance of tools.dynamia.modules.entityfile.EntityFileStorage to process and upload
files.

- `LocalEntityFileStorage` is the default implementation and store files in local file system
- `S3EntityFileStorage` in module S3 can upload files to AWS S3 buckets

## License

EntityFiles is available under Apache 2 License

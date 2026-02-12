[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia.modules/tools.dynamia.modules.email)](https://search.maven.org/search?q=tools.dynamia.modules.email)
![Java Version Required](https://img.shields.io/badge/java-17-blue)
[![Maven Build](https://github.com/dynamiatools/module-email/actions/workflows/maven.yml/badge.svg)](https://github.com/dynamiatools/module-email/actions/workflows/maven.yml)
[![Release and Deploy](https://github.com/dynamiatools/module-email/actions/workflows/release.yml/badge.svg)](https://github.com/dynamiatools/module-email/actions/workflows/release.yml)

# Email and SMS Module

This [DynamiaTools](https://dynamia.tools) extension allow you to send email and SMS messages using JavaMail and AWS.
This library install a ew DynamiaTools module called `Email` with CRUDs to set up emails and sms accounts, email
templates and query sending logs

## Modules

- Core: Entities, Services and API implementation
  - JPA Entities:
    - `EmailAccount`
    - `EmailTemplate`
    - `EmailAddress`
    - `SMSMessageLog`
- UI: Actions and views for user interface integration.

## Installation

Add the following dependencies to project classpath

**Maven**

```xml

<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.email</artifactId>
    <version>3.1.1</version>
</dependency>
```

```xml

<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.email.ui</artifactId>
    <version>3.1.1</version>
</dependency>

```

**Gradle**

```groovy
compile 'tools.dynamia.modules:tools.dynamia.modules.email:3.1.1'
compile 'tools.dynamia.modules:tools.dynamia.modules.email.ui:3.1.1'
```


## Usage

Main services are `EmailService` to send `EmailMessage` and `SMSService` to send `SMSMessage`

```java

@Service
class SomeService { //spring service

    @Autowired
    private EmailService emailService;

    @Autowired
    private SMSService smsService;

    public void sendNotification(Person person, String message) {

        var email = new EmailMessage(person.getEmail(), "Notification", message); 
        var sms = new SMSMessage(person.getMobileNumber(), message);

        //sending messages is async
        emailService.send(email); //return a Future
        smsService.send(sms); //return sms uuid

    }
}
```
`EmailService` and `SMSService` require at least one preferred `EmailAccount` created. These service 
will get the preferred account automatically. On the other hand, you can set up the `EmailAccount` in the 
`EmailMessage` and AWS Credentials in the `SMSMessage` directly.

If the `EmailMessage` has a `EmailTemplate` and this template has configured an SMS message, when you send 
the email the SMS will be sent too.

## License

DynamiaTools Email is available under Apache 2 License

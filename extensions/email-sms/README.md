[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia.modules/tools.dynamia.modules.email)](https://search.maven.org/search?q=tools.dynamia.modules.email)
![Java Version Required](https://img.shields.io/badge/java-25-blue)


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
    <version>26.2.2</version>
</dependency>
```

```xml

<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.email.ui</artifactId>
    <version>26.2.2</version>
</dependency>

```

**Gradle**

```groovy
compile 'tools.dynamia.modules:tools.dynamia.modules.email:26.2.2'
compile 'tools.dynamia.modules:tools.dynamia.modules.email.ui:26.2.2'
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

## Email templates and the block editor

An `EmailTemplate` is written in one of two languages, chosen per template (`engine`):

| Engine | What it is |
|---|---|
| `VELOCITY` (default, also when empty) | Apache Velocity through the platform `TemplateEngine`. Every template created before the block editor is one, and keeps working exactly as before. |
| `DYNAMIA` | The safe `DynamiaTemplateEngine` of `tools.dynamia.templates`: `${x}`, `{{x}}`, `$a.b`, pipes (`{{ total \| currency:0 }}`), `{{#if cond}}...{{else}}...{{/if}}` and `{{#each items as item}}...{{/each}}`. It never runs code from the template. The same engine exists in TypeScript (`@dynamia-tools/template-engine`) and both run the same conformance cases. |

`EmailTemplateRenderer` picks the engine for the body, subject, `to/cc/bcc/replyTo` and the SMS number and text; a child
template and its layout (`parent`) each use their own. Applications that need the HTML themselves must call
`EmailService.render(template, model)` instead of evaluating `template.getContent()` with Velocity: a template can be
Dynamia, and a migrated template is.

Values are HTML-escaped only in templates built in the editor (`escapeValues`); print trusted HTML with the `raw` filter
(`{{ TEMPLATE_CONTENT | raw }}` in a layout). Subjects, addresses and SMS text are never escaped.

### Block editor

The editor is a Vue package, [`@dynamia-tools/email-editor-vue`](packages/email-editor-vue/README.md): blocks (header, text,
image, button, divider, spacer, columns, footer, HTML, repeat, condition), responsive output through MJML, undo/redo,
variables panel, preview on desktop and mobile, test send and the migration. A template stores the editor document in
`design` (JSON) and the HTML it compiles to in `content`.

In the ZK UI the CRUD of templates gets three actions: **Design** (opens the editor page in a new window),
**Migrate to editor** and **Revert migration**. The editor page is built and packaged by an opt-in profile, so the normal
build needs no Node:

```bash
mvn -Peditor -pl extensions/email-sms/sources/ui package   # provisions Node and pnpm in target/, builds the page
```

Without `-Peditor` the Design action says the editor is not included.

### Migrating a template from Velocity

`Migrate to editor` (or `POST /api/email/templates/{id}/migrate?dryRun=false`) converts references, `#if/#elseif/#else`,
`#foreach` and `$TEMPLATE_CONTENT` in every text field, only when everything is portable. `#set`, macros, `#parse`,
`#include`, method calls, indexes and complex conditions are reported and the template is left untouched. The HTML becomes
one block, so the email looks the same, and the original fields are kept so `Revert migration` can restore them (it asks
before losing edits made to the design afterwards). A migrated template does not escape values, as Velocity never did.

### REST API

`/api/email/templates` (get, create, `PUT {id}/design`, `variables`, `{id}/preview`, `{id}/test-send`, `{id}/migrate`,
`{id}/revert`). Every call needs an authenticated user that an `EmailTemplatePolicy` accepts and works only on templates
of the current account; the system account's templates are read-only. The default policy lets the roles listed in
`dynamia.email.editor-roles` (comma separated); **with the property empty nobody can use the API**. One policy that says
yes is enough, so to restrict access with your own `EmailTemplatePolicy` leave the property empty. Changing calls must
carry an `X-Requested-With` header (the client sends it): CSRF protection is off in the framework and the API uses the
session cookie. A test email goes only
to the address of the logged in user (their principal name when it is an email, or what your own policy returns).
Request content is rendered only by the Dynamia engine, never by Velocity.

To list variables in the editor, implement `EmailTemplateModelProvider.getVariables()`.

## License

DynamiaTools Email is available under Apache 2 License

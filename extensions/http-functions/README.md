# DynamiaTools HTTP Functions Extension

## Overview

The DynamiaTools HTTP Functions Extension provides a declarative, versioned function runtime
exposed over HTTP. It lets applications define, register, and execute reusable business
capabilities as versioned functions, without coupling callers to a specific implementation of
"how to call WhatsApp" / "how to generate this PDF" / "how to call that third-party API".

Instead of hardcoding a REST client for every external integration, you configure a
`DynamiaHttpFunction` once (method, URL, body template, headers) and call it by name from Java
code or straight over HTTP. Credentials, endpoints and payload shapes can change without touching
application code or redeploying.

It acts as an internal capability bus for Dynamia-based applications, and provides:

- Versioned function contracts (`name` + `functionVersion`)
- HTTP-based invocation (`POST /api/dynamia/fx/{functionName}`) and direct Java invocation
  (`DynamiaFunctions.call(...)`)
- Parameter validation and type coercion before execution
- JSON and binary responses (images, PDFs, CSV, etc.)
- Deterministic version resolution (highest `ACTIVE` version by default)
- A back office UI to register functions and test them without leaving the browser

------------------------------------------------------------------------

## Getting Started

### 1. Add the dependency

```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.functions.core</artifactId>
    <version>26.6.0</version>
</dependency>

<!-- Optional: back office CRUD + "Test" action (ZK-based) -->
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.functions.ui</artifactId>
    <version>26.6.0</version>
</dependency>
```

No extra configuration is required: `DynamiaHttpFunctionsServiceImpl` and the REST controller are
auto-registered as long as the module is on the classpath (standard DynamiaTools `@Service` /
component scanning).

### 2. Register a function

You can create it through the back office UI (`Http Functions` page, once the `ui` module is on
the classpath), or programmatically:

```java
DynamiaHttpFunction function = new DynamiaHttpFunction();
function.setName("WhatsApp.sendMessage");
function.setFunctionVersion(1);
function.setMethod(HttpMethod.POST);
function.setUrl("https://api.whatsapp.example.com/v1/messages");
function.setContentType("application/json");
function.setBodyTemplate("{\"to\":\"${number}\",\"text\":\"${message}\"}");
function.setHeaders("Authorization: Bearer ${apiToken}");
function.setStatus(FunctionStatus.ACTIVE);

DynamiaHttpFunctionParameter number = new DynamiaHttpFunctionParameter();
number.setName("number");
number.setRequired(true);
function.addParameter(number);

DynamiaHttpFunctionParameter message = new DynamiaHttpFunctionParameter();
message.setName("message");
message.setRequired(true);
function.addParameter(message);

crudService.create(function);
```

### 3. Call it

From Java:

```java
FunctionResult result = DynamiaFunctions.call("WhatsApp.sendMessage",
        Map.of("number", "123456789", "message", "Hello"));

if (result.isSuccess()) {
    Map<String, Object> data = result.toJson();
    // or, mapped straight into a DTO:
    // SendMessageResponse dto = result.toJson(SendMessageResponse.class);
}
```

Or over HTTP:

```http
POST /api/dynamia/fx/WhatsApp.sendMessage
Content-Type: application/json

{ "params": { "number": "123456789", "message": "Hello" } }
```

That's it — no client class, no manual `RestTemplate`/`RestClient` wiring, no hardcoded URL in
your service code.

------------------------------------------------------------------------

## Core Concepts

### 1. `DynamiaHttpFunction`

Represents a versioned function definition. Each function includes:

| Field             | Description                                                                              |
|-------------------|--------------------------------------------------------------------------------------------|
| `name`            | Function identifier, e.g. `WhatsApp.sendMessage`. Free-form, but a `Namespace.action` convention is recommended. |
| `functionVersion` | Integer, starts at 1.                                                                     |
| `method`          | HTTP method used for the outbound call (`HttpMethod`).                                    |
| `url`             | Target URL. Supports `${param}` placeholders.                                             |
| `contentType`     | Content type sent with the request body. Defaults to `application/json`.                 |
| `bodyTemplate`    | Request body template. Supports `${param}` placeholders.                                  |
| `headers`         | Optional, one `Header-Name: value` per line. Supports `${param}` placeholders — the usual way to inject API keys/tokens. |
| `status`          | `DRAFT`, `ACTIVE`, `INACTIVE`, `DELETED`. Only `ACTIVE` functions can be called.           |
| `metadata`        | Optional free-form JSON, informative/extensible only — not used by the execution engine.  |
| `parameters`      | List of `DynamiaHttpFunctionParameter` (see below).                                       |

`url`, `bodyTemplate` and `headers` are rendered with the call parameters before the request is
issued, using `${paramName}` placeholders (e.g. `{"to":"${number}","text":"${message}"}`).

Constraint: `(name, functionVersion)` is unique per account, enforced both by a database index and
by `DynamiaHttpFunctionValidator`. A function can only reach `ACTIVE` status once it has a `url`.

### 2. `DynamiaHttpFunctionParameter`

Declares one input parameter accepted by a function call:

| Field          | Description                                                            |
|----------------|--------------------------------------------------------------------------|
| `name`         | Parameter name, matched against the `params` map at call time.         |
| `type`         | `STRING` (default), `NUMBER`, `BOOLEAN`, `DATE` (`yyyy-MM-dd`), `DATETIME` (`yyyy-MM-dd HH:mm:ss`). |
| `required`     | When `true` and the caller doesn't provide a value (and there's no `defaultValue`), the call fails with `400`. |
| `defaultValue` | Used when the caller omits the parameter.                              |
| `position`     | Display order in the back office UI.                                   |

### 3. Versioning Model

Versioning is explicit and manual in the MVP.

Rules:

- Versions start at 1
- A new version must be strictly greater than the current maximum for that `name`
- No duplicate `(name, version)` combinations
- By default, the highest **`ACTIVE`** version is executed
- Specific versions can be requested explicitly, regardless of status — useful for testing a
  `DRAFT` version before promoting it to `ACTIVE`

```java
DynamiaFunctions.call("WhatsApp.sendMessage", params);      // highest ACTIVE version
DynamiaFunctions.call("WhatsApp.sendMessage", 2, params);   // version 2 explicitly
```

### 4. Parameter Validation & Type Coercion

Before invoking the function, declared parameters are validated against the call's `params` map:

- Missing values fall back to `defaultValue` when declared
- Still-missing **required** parameters raise a validation error → HTTP `400`
- Present values are coerced to the declared `type` (e.g. a `NUMBER` parameter accepts a numeric
  string and is parsed into a `BigDecimal`); a value that can't be coerced also raises `400`

------------------------------------------------------------------------

## HTTP API

### Base Endpoint

```
POST /api/dynamia/fx/{functionName}
```

### Optional Version Selection

- Header: `X-Dynamia-Version: 2`
- Or query parameter: `?v=2`

If not specified, the latest `ACTIVE` version is used.

### Request

```http
POST /api/dynamia/fx/WhatsApp.sendMessage
Content-Type: application/json

{ "params": { "number": "123456789", "message": "Hello" } }
```

### Response

JSON response:

```json
{ "success": true, "data": { ... } }
```

Binary response — when the target endpoint returns an image/PDF/CSV/etc., the raw bytes are
streamed back with the matching `Content-Type` (e.g. `image/png`), no JSON envelope, no base64
wrapping.

Error response (validation, not-found, inactive, execution error):

```json
{ "success": false, "error": "Parameter [number] is required" }
```

### Status Codes

| Code | Meaning                                                    |
|------|-------------------------------------------------------------|
| 200  | Success                                                    |
| 400  | Validation error (missing/invalid parameter)               |
| 401  | Authentication required *(delegated to the app's security layer, see below)* |
| 403  | Authorization failure *(delegated to the app's security layer, see below)* |
| 404  | Function or version not found, or function not `ACTIVE`    |
| 500  | Internal execution error (target endpoint failed, network error, etc.) |

------------------------------------------------------------------------

## Calling Functions from Java

Functions are resolved through `DynamiaHttpFunctionsService` (registry + execution engine). The
static facade `DynamiaFunctions` is the recommended entry point for application code:

```java
// Highest ACTIVE version
FunctionResult result = DynamiaFunctions.call("WhatsApp.sendMessage",
        Map.of("number", "123456789", "message", "Hello"));

// Specific version
FunctionResult v2 = DynamiaFunctions.call("WhatsApp.sendMessage", 2,
        Map.of("number", "123456789", "message", "Hello"));

// Auto-register a DRAFT placeholder instead of throwing FunctionNotFoundException
// when the function hasn't been configured yet — handy while wiring up new
// integrations, so calling code doesn't need a hard dependency on setup order.
FunctionResult draft = DynamiaFunctions.call("Invoicing.generatePdf",
        Map.of("orderId", "123"), true);

// Non-blocking (runs on a virtual thread)
DynamiaFunctions.callAsync("WhatsApp.sendMessage", Map.of("number", "123", "message", "Hello"))
        .thenAccept(r -> log.info("sent: " + r.isSuccess()));
```

### Reading the result

`FunctionResult` wraps either structured data or binary content:

```java
FunctionResult result = DynamiaFunctions.call("WhatsApp.sendMessage", params);

result.isSuccess();     // true/false
result.isBinary();      // true when the payload is raw bytes (image/PDF/CSV/...)
result.isJson();        // true when getData() is a Map/List

Map<String, Object> data = result.toJson();               // parsed as a Map
SendMessageResponse dto = result.toJson(SendMessageResponse.class); // parsed into a DTO
byte[] file = result.getBinaryData();                      // when isBinary() == true
```

### Error handling

Calls can throw:

- `FunctionNotFoundException` — no function registered for that `(name, version)`
- `FunctionInactiveException` — function exists but isn't `ACTIVE`
- `ValidationError` (`tools.dynamia.domain`) — missing/invalid parameter
- `FunctionExecutionException` — the outbound HTTP call failed (network error, non-2xx response,
  etc.); wraps the original cause

The REST controller maps these to `404`, `400` and `500` respectively (see [Status
Codes](#status-codes)). When calling from Java, catch what's relevant to your use case, or let it
bubble up if the caller should fail loudly.

------------------------------------------------------------------------

## UI Module

The `ui` submodule (`tools.dynamia.modules.functions.ui`, ZK-based) adds a back office CRUD for
`DynamiaHttpFunction` / `DynamiaHttpFunctionParameter`:

- **Navigation**: a "Http Functions" page group is contributed to the existing `saas` module (see
  `DynamiaHttpFunctionsModuleProvider`), listing all registered functions.
- **View descriptors** (`META-INF/descriptors/`): form/table/crud for the function itself, plus a
  nested `crudview` for its parameters and a form/table pair for `DynamiaHttpFunctionParameter`.
- **`TestHttpFunctionAction`**: a crud action ("Test") that opens a dialog to edit call parameters
  as JSON and immediately invokes the selected function through `DynamiaHttpFunctionsService`,
  showing the resulting `FunctionResult` (or the validation/execution error) without leaving the
  browser — the fastest way to verify a function definition before wiring it into application
  code.

------------------------------------------------------------------------

## Design Principles

- Deterministic version resolution
- Immutable version contracts (a version, once created, isn't meant to be edited — cut a new one)
- Clear separation between metadata and implementation
- HTTP-native semantics
- Extensible without modifying core controllers

------------------------------------------------------------------------

## Example Use Cases

- Send WhatsApp/SMS/email notifications through a third-party provider
- Generate PDF reports or optimize images via an internal microservice
- Export data files (CSV, XLSX)
- Integrate third-party services whose credentials/endpoints vary per environment or tenant
- Trigger internal business processes exposed as internal HTTP endpoints

------------------------------------------------------------------------

## Known Limitations / Roadmap

- **401/403 enforcement is not done at the function level** — securing who can call
  `/api/dynamia/fx/{functionName}` is currently delegated entirely to the application's security
  layer (e.g. Spring Security rules on the base path).
- `interfaceName` / `methodName` fields exist on `DynamiaHttpFunction` reserved for a future
  dynamic Java interface proxy (call a function through a regular interface method instead of
  `DynamiaFunctions.call(...)`), **not implemented yet**.
- Not yet implemented: deprecation metadata, automatic version suggestions, execution
  metrics/observability, response caching, script-based execution sandbox.

------------------------------------------------------------------------

## Philosophy

This extension provides a lightweight, versioned function runtime that enables controlled
evolution of business capabilities while maintaining contract stability. It is designed to support
long-term extensibility without introducing unnecessary architectural complexity.

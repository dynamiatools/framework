# DynamiaTools HTTP Functions Extension

## Overview

The DynamiaTools HTTP Functions Extension provides a declarative,
versioned function runtime exposed over HTTP. It allows applications to
define, register, and execute reusable business capabilities as
versioned functions without coupling callers to specific
implementations.

This extension enables:

-   Versioned function contracts
-   HTTP-based invocation
-   Parameter validation before execution
-   JSON and binary responses
-   Deterministic version resolution
-   Internal service decoupling

It acts as an internal capability bus for Dynamia-based applications.

------------------------------------------------------------------------

## Core Concepts

### 1. DynamiaHttpFunction

Represents a versioned function definition.

Each function includes:

-   name (e.g., WhatsApp.sendMessage)
-   functionVersion (integer, starting at 1)
-   method, url, contentType and bodyTemplate describing the external HTTP call to perform
-   headers (optional, one `Header-Name: value` per line, supports `${param}` placeholders)
-   status (DRAFT, ACTIVE, INACTIVE, DELETED)
-   metadata (optional free-form JSON, informative/extensible)
-   Parameter definitions

`url`, `bodyTemplate` and `headers` are rendered with the call parameters before the request is
issued, using `${paramName}` placeholders (e.g. `{"to":"${number}","text":"${message}"}`).

Constraint: (name, functionVersion) is unique per account, enforced both by a database index and by
`DynamiaHttpFunctionValidator`.

------------------------------------------------------------------------

### 2. Versioning Model

Versioning is explicit and manual in the MVP.

Rules:

-   Versions start at 1
-   A new version must be greater than the current maximum
-   No duplicate (name, version) combinations
-   By default, the highest version is executed
-   Specific versions can be requested explicitly

Example resolution:

DynamiaFunctions.call("WhatsApp.sendMessage", params) -\> Calls highest
available version

DynamiaFunctions.call("WhatsApp.sendMessage", 2, params) -\> Calls
version 2 explicitly

------------------------------------------------------------------------

### 3. Parameter Validation

Each function can define parameters using a separate entity:

-   Name
-   Type
-   Required flag
-   Optional validation rules (regex, size limits, etc.)

Validation is executed before invoking the function implementation.

If validation fails: HTTP 400 is returned.

------------------------------------------------------------------------

## HTTP API

### Base Endpoint

POST /api/dynamia/fx/{functionName}

### Optional Version Selection

Version can be selected using:

Header: X-Dynamia-Version: 2

Or query parameter: ?v=2

If not specified, the latest version is used.

------------------------------------------------------------------------

## Request Example

POST /api/dynamia/fx/WhatsApp.sendMessage Content-Type: application/json

Body:

{ "params": { "number": "123456789", "message": "Hello" } }

------------------------------------------------------------------------

## Response Model

### JSON Response

{ "success": true, "data": { ... } }

### Binary Response

Functions may return:

-   Images
-   PDFs
-   CSV files
-   Any binary content

When returning binary:

-   Content-Type is set accordingly (e.g., image/png)
-   Raw bytes are streamed in the HTTP response
-   No base64 wrapping unless explicitly required

------------------------------------------------------------------------

## Status Codes

-   200 -- Success
-   400 -- Validation error
-   401 -- Authentication required
-   403 -- Authorization failure
-   404 -- Function or version not found
-   500 -- Internal execution error

------------------------------------------------------------------------

## Execution Model

Functions are resolved through `DynamiaHttpFunctionsService` (registry + execution engine), and can be
called either over HTTP or directly from Java code via `DynamiaFunctions.call(...)`:

-   Lookup by (name, version), defaulting to the highest ACTIVE version
-   Validate parameters against their declared definitions (required/type coercion)
-   Render `url`, `bodyTemplate` and `headers` templates with the call parameters
-   Issue the configured HTTP request
-   Convert the response into a `FunctionResult` (JSON payload or binary content, based on the response
    content type) and return it

```java
FunctionResult result = DynamiaFunctions.call("WhatsApp.sendMessage",
        Map.of("number", "123456789", "message", "Hello"));
```

------------------------------------------------------------------------

## Design Principles

-   Deterministic version resolution
-   Immutable version contracts
-   Clear separation between metadata and implementation
-   HTTP-native semantics
-   Extensible without modifying core controllers

------------------------------------------------------------------------

## Example Use Cases

-   Send WhatsApp messages
-   Generate PDF reports
-   Optimize images
-   Export data files
-   Integrate third-party services
-   Trigger internal business processes

------------------------------------------------------------------------

## Roadmap (Future Enhancements)

-   Dynamic Java interface proxies backed by functions (`interfaceName`/`methodName` fields are
    reserved for this, not yet implemented)
-   Deprecation metadata
-   Automatic version suggestions
-   Function observability (metrics, execution time)
-   Caching support
-   Async execution support
-   Script-based execution sandbox
-   401/403 enforcement at the function level (currently delegated to the application's security layer)

------------------------------------------------------------------------

## Philosophy

This extension provides a lightweight, versioned function runtime that
enables controlled evolution of business capabilities while maintaining
contract stability.

It is designed to support long-term extensibility without introducing
unnecessary architectural complexity.

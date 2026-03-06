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
-   executorBean or implementation reference
-   active flag
-   Metadata (optional JSON)
-   Parameter definitions

Constraint: (name, functionVersion) must be unique.

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

Functions are resolved through a registry:

-   Lookup by (name, version)
-   Resolve executor implementation
-   Validate parameters
-   Execute
-   Return structured response

Executors should be stateless and deterministic.

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

-   Draft vs Active lifecycle states
-   Deprecation metadata
-   Automatic version suggestions
-   Function observability (metrics, execution time)
-   Caching support
-   Async execution support
-   Script-based execution sandbox

------------------------------------------------------------------------

## Philosophy

This extension provides a lightweight, versioned function runtime that
enables controlled evolution of business capabilities while maintaining
contract stability.

It is designed to support long-term extensibility without introducing
unnecessary architectural complexity.

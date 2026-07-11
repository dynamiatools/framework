# Coherence & Implementation Recommendations

**Date:** March 19, 2026

This document summarizes findings from the comprehensive audit of API client patterns across the Dynamia Tools framework and provides actionable recommendations for maintaining coherence and extending the architecture.

---

## Executive Summary

✅ **All packages are coherent and compliant** with the centralized `DynamiaClient` pattern.

**Key Finding:** There is **zero inconsistency** in how backend calls are made. The pattern is uniform across:
- Core SDK (`@dynamia-tools/sdk`)
- Extension SDKs (Reports, SaaS, Files)
- UI Core (`@dynamia-tools/ui-core`)
- Vue Integration (`@dynamia-tools/vue`)

---

## 1. Current State: Strengths

### 1.1 Centralized HTTP Management

**What works well:**
- Single `HttpClient` instance manages all fetch calls
- Uniform auth handling (Bearer tokens, Basic Auth, Cookies)
- Consistent error handling via `DynamiaApiError`
- Central URL building with baseUrl + path normalization
- Single point for logging, retry logic, interceptors

**Impact:** Changes to authentication or error handling propagate automatically to all SDKs.

### 1.2 Extension SDK Pattern

**What works well:**
- All extension SDKs accept `HttpClient` in constructor
- No direct fetch, axios, or HTTP library usage
- Consistent test helpers (mockFetch, makeHttpClient)
- Proper TypeScript typing throughout
- READMEs clearly document the pattern

**Impact:** New extension SDKs can be added without risk of divergent patterns.

### 1.3 View Separation of Concerns

**What works well:**
- `@dynamia-tools/ui-core` views don't call APIs
- Data is injected via callbacks (loaders, searchers)
- Framework-agnostic design (reusable in Vue, React, Svelte, etc.)
- Views are easily testable with mock loaders

**Impact:** UI logic is decoupled from HTTP logic; both can evolve independently.

### 1.4 Vue Integration

**What works well:**
- `useDynamiaClient()` provides single injection point
- Composables accept injected client or custom callbacks
- Plugin pattern is clean and Vue-idiomatic
- Real-world demo shows working examples

**Impact:** Components are not tightly coupled to specific APIs; loaders are swappable.

---

## 2. Identified Gaps

While the current architecture is coherent, there are **opportunities for improvement**:

### 2.1 Documentation Gap: Vue Patterns

**Issue:**
- Core SDK and extension SDKs have clear READMEs
- Vue package lacks a comprehensive integration guide

**Impact:**
- Developers may not know when to use `useDynamiaClient()` vs. custom loaders
- Testing patterns for composables not well-documented
- Real-world examples limited to one demo app

**Recommendation:** ✅ **IMPLEMENTED** — Created `platform/packages/vue/GUIDE.md` (see output above)

### 2.2 Documentation Gap: Extension SDK Creation

**Issue:**
- No formal template for new extension SDKs
- Each new SDK is created "from scratch"
- Risk of pattern drift as projects add new extensions

**Impact:**
- Inconsistent package.json configurations
- Variable test coverage
- Unclear conventions for API class methods

**Recommendation:** ✅ **IMPLEMENTED** — Created `EXTENSION_SDK_TEMPLATE.md` (see output above)

### 2.3 Lack of Formal Standards Document

**Issue:**
- Best practices exist but not documented in one place
- New developers must infer patterns from existing code

**Impact:**
- Onboarding time increased
- Pattern violations possible in future contributions

**Recommendation:** ✅ **IMPLEMENTED** — Created `API_CLIENT_STANDARDS.md` (see output above)

### 2.4 Error Handling Documentation

**Issue:**
- `DynamiaApiError` exists but not all packages document error handling
- Vue composables don't show error patterns

**Impact:**
- Inconsistent error handling in applications

**Recommendation:** Add error handling examples to Vue GUIDE and extension SDK template (see templates above)

### 2.5 Testing Patterns Variation

**Issue:**
- Each extension SDK has its own test helpers (mockFetch, makeHttpClient)
- Shared test utilities not centralized

**Current:** Helpers are duplicated but identical in:
- `platform/packages/sdk/test/helpers.ts`
- `extensions/reports/packages/reports-sdk/test/helpers.ts`
- `extensions/saas/packages/saas-sdk/test/helpers.ts`
- `extensions/entity-files/packages/files-sdk/test/helpers.ts`

**Impact:** DRY principle violated; future bug fixes must be propagated manually

**Recommendation:** Consider creating a `@dynamia-tools/test-utils` package

---

## 3. Recommendations for Coherence

### 3.1 Create Shared Test Utilities Package

**Proposal:**
```
platform/packages/test-utils/
├── src/
│   ├── http-mocks.ts      # mockFetch, makeHttpClient
│   ├── client-mocks.ts    # makeMockClient
│   └── index.ts           # Exports
├── package.json
├── README.md
└── ...
```

**Benefit:**
- Single source of truth for test helpers
- Consistent across all SDKs
- Easy to add new mock utilities (interceptors, retry logic, etc.)

**Implementation:**
```typescript
// @dynamia-tools/test-utils
export { mockFetch, makeHttpClient } from './http-mocks.js';

// In any SDK test
import { mockFetch, makeHttpClient } from '@dynamia-tools/test-utils';
```

### 3.2 Add CI Integration Tests

**Proposal:** Add GitHub Actions workflow to verify:
1. No `fetch()` calls outside of `HttpClient`
2. All API classes accept `HttpClient`
3. All packages use mocks in tests
4. All extension SDKs follow naming conventions

**Example:**
```yaml
# .github/workflows/api-coherence.yml
name: API Coherence Check

on: [pull_request]

jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Check for forbidden HTTP libraries
        run: |
          grep -r "fetch(" platform/packages/*/src --include="*.ts" && exit 1
          grep -r "axios" platform/packages/*/src --include="*.ts" && exit 1
          echo "✅ No forbidden HTTP libraries found"
      - name: Check extension SDKs
        run: |
          for dir in extensions/*/packages/*-sdk; do
            if [ ! -f "$dir/src/api.ts" ]; then
              echo "❌ Missing src/api.ts in $dir"
              exit 1
            fi
          done
          echo "✅ All extension SDKs follow structure"
```

### 3.3 Enhance Error Handling Documentation

**Proposal:** Add to every SDK README a section:

```markdown
## Error Handling

All API errors are thrown as \`DynamiaApiError\` (from \`@dynamia-tools/sdk\`):

\`\`\`ts
try {
  const data = await api.getData();
} catch (err) {
  if (err instanceof DynamiaApiError) {
    console.error(\`[\${err.status}] \${err.message}\`);
    console.log(err.body); // Full error response
  } else {
    throw err; // Other error
  }
}
\`\`\`
```

### 3.4 Create Contribution Checklist

**Proposal:** `framework/CONTRIBUTING_API_CLIENTS.md`

Checklist for new extensions/SDKs:
- [ ] Uses centralized `DynamiaClient` pattern
- [ ] API class accepts `HttpClient` in constructor
- [ ] No direct `fetch()`, `axios`, or similar
- [ ] All responses are TypeScript typed
- [ ] Tests use `mockFetch` helper
- [ ] README documents usage with `DynamiaClient`
- [ ] Error handling example included
- [ ] Follows naming conventions
- [ ] ESLint passes (no forbidden imports)

### 3.5 Document Authorization Patterns

**Proposal:** Add to core SDK documentation:

```markdown
## Authorization Patterns for Extension SDKs

All extension SDKs inherit auth from the core client. Examples:

### Bearer Token (JWT)
\`\`\`ts
const client = new DynamiaClient({
  baseUrl: '...',
  token: 'eyJhbGciOiJIUzI1NiJ9...',
});
\`\`\`

### Basic Auth
\`\`\`ts
const client = new DynamiaClient({
  baseUrl: '...',
  username: 'admin',
  password: 'secret',
});
\`\`\`

### Session Cookies
\`\`\`ts
const client = new DynamiaClient({
  baseUrl: '...',
  withCredentials: true,
});
\`\`\`

Extension SDKs automatically use the configured auth:
\`\`\`ts
const api = new MyExtensionApi(client.http); // ← Uses client's auth
\`\`\`
```

---

## 4. Implementation Roadmap

### Phase 1: Documentation (COMPLETED ✅)
- [x] Create `API_CLIENT_STANDARDS.md` — Comprehensive standards
- [x] Create `API_CLIENT_AUDIT_REPORT.md` — Detailed audit findings
- [x] Create `platform/packages/vue/GUIDE.md` — Vue integration patterns
- [x] Create `EXTENSION_SDK_TEMPLATE.md` — Template for new SDKs

### Phase 2: Tooling (RECOMMENDED)
- [ ] Create `@dynamia-tools/test-utils` package
- [ ] Add API coherence CI checks
- [ ] Create `CONTRIBUTING_API_CLIENTS.md` checklist

### Phase 3: Evolution (FUTURE)
- [ ] Request interceptors/middleware in `HttpClient`
- [ ] Retry logic for failed requests
- [ ] Request/response logging configuration
- [ ] Plugin system for extending APIs

---

## 5. Pattern Consistency Checklist

Use this checklist when reviewing PRs or creating new packages:

### ✅ For Extension SDK Packages

- [ ] API class accepts `HttpClient` in constructor
- [ ] All HTTP methods use `this.http.get()`, `this.http.post()`, etc.
- [ ] No direct `fetch()` or third-party HTTP library
- [ ] All responses are TypeScript typed (DTO interfaces)
- [ ] Tests use `mockFetch` + `makeHttpClient` helpers
- [ ] README shows usage with `new MyApi(client.http)`
- [ ] README documents error handling with `DynamiaApiError`
- [ ] package.json declares peer dependency on `@dynamia-tools/sdk`
- [ ] Files in `src/`, tests in `test/` directories
- [ ] Follows `@dynamia-tools/{extension}-sdk` naming

### ✅ For Vue Packages/Composables

- [ ] Composables accept `DynamiaClient` or custom callbacks
- [ ] `useDynamiaClient()` used for client injection
- [ ] No direct `fetch()` or third-party HTTP library
- [ ] Loader/searcher callbacks are tested with mocks
- [ ] Components don't call APIs directly (use composables)
- [ ] Plugin is registered at app initialization

### ✅ For ui-core Views

- [ ] Views don't call `HttpClient` directly
- [ ] Data is injected via callbacks (loaders, searchers)
- [ ] Tests mock the callbacks, not network
- [ ] Views are framework-agnostic

---

## 6. Future Enhancements

### 6.1 Request Interceptors

**Current:** `HttpClient` doesn't support interceptors.

**Future Enhancement:**
```typescript
const client = new DynamiaClient({ ... });
client.http.interceptors.request.use(req => {
  console.log(`[HTTP] ${req.method} ${req.url}`);
  return req;
});
```

### 6.2 Retry Policy

**Current:** No automatic retries.

**Future Enhancement:**
```typescript
const client = new DynamiaClient({
  ...,
  retry: {
    maxAttempts: 3,
    backoffMultiplier: 2,
    retryableStatuses: [408, 429, 500, 502, 503, 504],
  },
});
```

### 6.3 Cache Control

**Current:** Only `MetadataApi` caches views.

**Future Enhancement:**
```typescript
const client = new DynamiaClient({
  ...,
  cache: {
    enabled: true,
    ttl: 60000, // 60 seconds
    policies: {
      'GET /api/metadata': 'long', // 1 hour
      'GET /api/crud': 'short',    // 5 minutes
    },
  },
});
```

---

## 7. Conclusion

The Dynamia Tools framework has achieved **excellent coherence** in API client patterns. All packages follow the centralized `DynamiaClient` paradigm with no deviations.

### Key Takeaways

1. **Current State:** ✅ Compliant and coherent
2. **Documentation:** ✅ Enhanced with comprehensive guides
3. **Template:** ✅ Provided for new extension SDKs
4. **Next Steps:** Consider test utilities package and CI checks

### Artifacts Delivered

| Document | Location | Purpose |
|----------|----------|---------|
| API Client Standards | `API_CLIENT_STANDARDS.md` | Comprehensive standards guide |
| Audit Report | `API_CLIENT_AUDIT_REPORT.md` | Detailed findings and status |
| Vue Guide | `platform/packages/vue/GUIDE.md` | Integration patterns & examples |
| SDK Template | `EXTENSION_SDK_TEMPLATE.md` | Boilerplate for new SDKs |
| This Document | `COHERENCE_RECOMMENDATIONS.md` | Future improvements & roadmap |

---

## Appendix: Quick Reference

### When to use what

| Scenario | Use This | Not This |
|----------|----------|----------|
| Call backend API | `new MyApi(client.http)` | Direct `fetch()` |
| Fetch navigation | `client.metadata.getNavigation()` | Direct API call |
| Load table data | Pass `loader` to `useTable()` | API call in component |
| Handle auth | `DynamiaClient` config | Manual headers |
| Test API | `mockFetch()` + `makeHttpClient()` | Network requests |
| Test composable | Mock loader callback | Network requests |
| Inject client | `useDynamiaClient()` | Props drilling |
| Extension API | Accept `HttpClient` in constructor | Direct fetch |

---

## Appendix: File Locations

```
framework/
├── API_CLIENT_STANDARDS.md              (✅ Created)
├── API_CLIENT_AUDIT_REPORT.md           (✅ Created)
├── COHERENCE_RECOMMENDATIONS.md         (This file)
├── EXTENSION_SDK_TEMPLATE.md            (✅ Created)
├── platform/
│   └── packages/
│       └── vue/
│           └── GUIDE.md                 (✅ Created)
└── extensions/
    ├── reports/packages/reports-sdk/
    │   └── README.md                    (Already exists)
    ├── saas/packages/saas-sdk/
    │   └── README.md                    (Already exists)
    └── entity-files/packages/files-sdk/
        └── README.md                    (Already exists)
```

---

**Review Status:** Ready for Implementation  
**Last Updated:** March 19, 2026


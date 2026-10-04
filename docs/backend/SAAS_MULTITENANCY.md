# SaaS multi-tenancy: account isolation with Hibernate `@TenantId`

> **Status: spike (#147), not yet adopted by applications.** This document describes what the branch implements and
> the rules it imposes. The adoption checklist is at the end.

A SaaS application keeps every account's data in the same tables, discriminated by an `accountId` column. There are two
layers that keep one account from seeing another's rows.

| Layer | What it covers | What it misses |
|---|---|---|
| `AccountAwareCrudServiceListener` (always present) | `CrudService` queries (`QueryParameters`) and `beforeCreate` | `EntityManager` JPQL/Criteria, `find` by id, lazy associations, bulk updates |
| Hibernate `@TenantId` (this spike) | Every HQL/Criteria query, load by id, association and bulk statement; assigns the tenant on persist | Native SQL (not filtered) |

The listener stays: it covers entities that do not use `@TenantId`. `AccountAware` (`getAccountId`/`setAccountId`) does
not change.

## What is a tenant

The tenant is the **account id** (`Long`). It comes from, in order: a binding made with `AccountTenants`, the request
attribute `currentAccountId`, and the `AccountSessionHolder` of the thread. `AccountTenantIdentifierResolver` (saas
core) never touches the database, because Hibernate calls it while opening a session. Hibernate fixes the tenant when
the session opens and it cannot change afterwards.

The **root tenant** (`AccountTenants.ROOT_TENANT_ID`, `0`) is not an account: it sees every account and may persist data
for any account. When there is no current account (startup, background jobs) the resolver answers root, which is the
same fail-open behaviour the listener already had. Entities persisted without an `accountId` under root get `0`.

## Making an entity tenant-aware

An entity that owns an `accountId` must declare it as the discriminator. Either:

- **Extend a SaaS base** (`BaseEntitySaaS`, `SimpleEntitySaaS`, `BaseEntityUuidSaaS`, `SimpleEntityUuidSaaS`); they
  already declare `@TenantId`. Prefer this for new entities. It adds `@NotNull` to `accountId`.
- **Annotate your own field** when the entity cannot change its superclass:

```java
@TenantId
private Long accountId;
```

Already annotated in this repository: the four SaaS bases, `AccountParameter`, and the security entities `User`,
`Profile`, `UserProfile`, `Permission` and `UserAccessToken`. Entities of other extensions that extend the bases
(reports, email-sms, http-functions) become tenant-aware through them. `EntityFile` has its own `accountId` but
entity-files does not depend on SaaS, so it is **not** annotated.

Behaviour to know (checked with Hibernate 7.4 and H2 in `HibernateTenancyTest`):

- Persisting an entity whose `accountId` differs from the session's tenant is **rejected**
  (`PropertyValueException`). Setting `accountId` on a managed entity to another value is ignored.
- `find` by id of another account's row returns `null`; HQL bulk `update`/`delete` only touch the current tenant.
- **Native SQL is not filtered.** Add the `accountId` condition yourself.
- An application without SaaS still works: when no `CurrentTenantIdentifierResolver` bean exists,
  `JpaConfigurationAdapter` registers `RootTenantIdentifierResolver`, which always answers root. An application that
  defines its own resolver bean replaces it.

## Working with another account's data: `AccountTenants`

Code that reads or writes for an account other than the current one (account initializers, administration, the license
endpoint) must say so.

| Method | Transaction | Use for |
|---|---|---|
| `runAs(accountId, work)` | New (`REQUIRES_NEW`), so a new session is opened | Short units of work for one account |
| `runAsRoot(work)` | New | Short units of work across accounts |
| `with(accountId, work)`, `withRoot(work)` | None; only binds the tenant | Code that manages its own sessions/transactions, such as long jobs |
| `callWithRoot(callable)` | None | Tasks submitted to executors that throw checked exceptions |

```java
AccountTenants.runAs(newAccountId, () -> crudService.create(new Customer("default")));
```

The tenant is held in a `ScopedValue` (final in Java 25), not in a `ThreadLocal`: it is bound only while the work runs,
nested bindings shadow the outer one and are undone automatically (also when the work throws), and it can never leak
to later work on a pooled thread. A scoped value is **not inherited** by threads started inside the scope, except those
forked with `StructuredTaskScope`. Code that hands work to an executor (including virtual threads) has to bind the
tenant again inside the task, for example with `callWithRoot`. Tasks started with `SchedulerUtil` (or any
`ObjectsContext` snapshot) carry the tenant automatically: `AccountTenantContextCapturer` re-applies it inside the task, so
a task started inside `runAs(5L, ...)` runs as account 5 and not as root.

**Account migration always runs as root.** `AccountMigrationServiceImpl` binds root around export, import and clone,
and `ExportPipeline` binds it again in each of its worker threads.

## Pitfalls

- **Session binding time.** The tenant is read when the session opens. Code that opens an `EntityManager` before the
  account is known (for example open-EM-in-view before account resolution) or in another thread gets the wrong tenant.
- **Explicit `accountId` for another account.** A query with `accountId = X` under tenant `Y` returns nothing. Use
  `runAs(X, ...)` or root.
- **Writing for another account** (new-account initializers) fails with `PropertyValueException` unless done under
  `runAs`/root.
- **Cross-account reads of entities that are now tenant-aware** (for example a shared email template owned by the
  system account) need `runAs`/root.
- **Login.** `User` is tenant-aware. A login request that arrives without a resolved account resolves root and sees all
  users, as before; with a subdomain-resolved account it only sees that account's users.

## Adopting it in an application (checklist)

1. Annotate or extend a base for every entity that owns an `accountId`.
2. Move account initializers and cross-account jobs to `runAs`/`runAsRoot`/`with`.
3. Review native queries and direct `EntityManager` use.
4. Review every query that passes an explicit `accountId` for another account.
5. Review async code and where sessions are opened relative to account resolution.
6. Decide whether "no current account" should stay root (fail-open, the default) or fail closed.

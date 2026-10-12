# Web security: JWT, sessions and `CurrentUser`

The security module has two filter chains that share the browser's cookies. Knowing which one serves a request explains
most session problems of an application with a Vue shell.

| Chain | Matches | Authenticates with | State |
|---|---|---|---|
| `apiSecurityFilterChain` (order 1) | `/api/**` | `DYNAMIA_JWT` cookie or `Authorization: Bearer` (`JWTAuthenticationFilter`) | stateless |
| `webSecurityFilterChain` (order 3) | everything else (the shell at `/`, `/login`) | the HTTP session (`JSESSIONID`) | `IF_REQUIRED` |

`POST /login/json` creates both: the session (read by the shell) and the JWT cookie (read by the API).
So **a page refresh depends on the `JSESSIONID`**, not on the JWT: the web chain has no JWT filter.

## Rules the code follows

- **The API chain never rotates the `JSESSIONID`** (`sessionFixation().none()`). Spring Security's default
  (`changeSessionId`) ran on every JWT-authenticated API call; with several calls in parallel the browser could keep an
  id that was already invalid, and the next refresh went to `/login`.
- **A token request does not leave a session behind.** `CurrentUser` is session scoped, so initialising it creates a
  session. `JWTAuthenticationFilter` invalidates the sessions created while serving a request that arrived without
  one, so cookie-less clients (SDKs, mobile, scripts) do not leak one session per call. Their response still carries a
  `Set-Cookie` of that (already invalid) session.
- **`CurrentUser` is not rebuilt on every call.** `SpringSecurtyApplicationListener.fireOnUserTokenLoginListeners`
  reuses the `CurrentUser` of the session when it is the same user and was initialised less than 5 minutes ago.
  A change of permissions is therefore seen after at most 5 minutes (or on a new session).

## Known limits

- A JWT without `JSESSIONID` (cookie deleted, server restarted with in-memory sessions) still ends at `/login` for the
  shell, although the API would accept it. Making the web chain accept the JWT, or rebuild the session from it, is open.
- The JWT lasts 60 minutes (15 days with `X-Remember-Me`) while the container's session timeout is shorter by default.

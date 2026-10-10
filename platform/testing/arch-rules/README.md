# arch-rules

Executable architecture rules of Dynamia UI (`docs/next/dynamia-ui.md`, section 9). They read Java sources and `pom.xml`
files, so they need no module built, and run as plain JUnit tests.

| Rule | Class | Baseline | Meaning |
|---|---|---|---|
| R1 front end corner | `FrontendCornerRule` | `frontend-corner.baseline` | The front end (ZK) is used only in the corner paths: `import`, a fully qualified name in the body, or a Maven dependency on `org.zkoss` / `tools.dynamia.zk` / `*zk-starter` |
| R2 inventory | `ActionInventoryRule` | `frontend-bound-actions.baseline` | `@InstallAction` classes bound to the front end (the class or an ancestor uses it) |
| R3 publication | `ActionInventoryRule` | none | No action with runtime `HEADLESS`, `FLOW` or `REMOTE` may be bound to the front end, ancestors included |
| R4 declaration | `ActionInventoryRule` | `undeclared-actions.baseline` | `@InstallAction` classes whose runtime is `UNDECLARED` (a runtime is declared on the concrete class and never inherited) |
| R5 vocabulary | `VocabularyRule` | none | No identifier of the neutral core contains `ZK` / `Zk` |

A baseline lists violations that exist today and **only shrinks**: a new one fails the test, and so does a listed one that was
fixed (remove it). Regenerate with `-Dui.baseline.update=true`. Comments and string literals are ignored, and inheritance is
resolved by fully qualified name, never by simple name.

## Adopting it in another repository (dynamia-erp)

1. Add the artifact `tools.dynamia:tools.dynamia.arch-rules` with `<scope>test</scope>` to a test module of the repository.
2. Build an `ArchRulesConfig` for it: `ArchRulesConfig.forZk(baselineDir, cornerPaths)` assumes ZK and a root that holds
   `platform/` and `extensions/`; for another layout use the constructor (`root`, corner and ignored paths, front end packages,
   artifact and group ids, the paths of the neutral core and the words forbidden in it).
3. Extend the rules, one class each, returning that configuration:

```java
class ErpFrontendCornerRuleTest extends FrontendCornerRule {
    @Override protected ArchRulesConfig config() { return ErpArchRules.config(); }
}
```

4. Run once with `-Dui.baseline.update=true` to write the baselines next to the tests, review them, and commit them. From then
   on they only go down.

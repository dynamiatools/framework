# Releasing Dynamia Tools

How versions are published and why. Work status lives in GitHub Issues (#238); this file keeps the policy.

## Policy

Maven Central limits what an organization can publish each month (files, releases and size; the current thresholds and
usage are in the Central Usage Center). One full release of this repository is about 680 files, so **one full release
per month fits and two do not**. All modules share one version, so a partial release is not an option either.

| Version | Maven Central | GitHub Packages | npm | GitHub Release |
|---|---|---|---|---|
| `YY.MM.0`, the monthly release | yes | yes | yes | release |
| `YY.MM.N`, N > 0 (maintenance) | no | yes | yes | **pre-release** |
| `YY.MM.N-SNAPSHOT` (`next`) | no | yes (`publish-snapshot.yml`) | no | none |

- Consumers inside the organization (dynamia-erp and the other repos) resolve from GitHub Packages
  (`https://maven.pkg.github.com/dynamiatools/framework`). The Maven registry of GitHub Packages needs a token with
  `read:packages` even for public packages, so external users should be pointed at Maven Central.
- Central versions are immutable: a bad `YY.MM.0` can only be fixed by another version, which costs another release.
  Rehearse it first (see below).
- Exception: a critical security fix may go to Central as a normal release. It goes over the monthly limit and opens a
  30-day grace period, so do not make a habit of it.
- Versions published to GitHub Packages are immutable too. If the first job of `release.yml` fails halfway, a re-run
  fails on the modules already uploaded: delete those package versions in GitHub (or bump the version) and run it again.

## Steps

1. On `next`, commit `chore: bump version to X` (every `pom.xml` and the `package.json` files; look at the previous bump
   commit for the list). The version must not be a SNAPSHOT.
2. Rehearse: run **Release and Deploy** by hand (Actions > Run workflow) with *dry-run* checked. It checks the version,
   builds, runs the tests and signs the artifacts without deploying anything. Check *central* to rehearse the signing
   and the Central build too.
3. Create the GitHub Release with tag `vX`:
   - monthly `YY.MM.0`: a normal release;
   - maintenance `YY.MM.N`: mark it **pre-release**.
4. `release.yml` verifies that the tag matches the project version, runs the tests and deploys to GitHub Packages; for a
   normal release it then deploys to Central (`autoPublish` is on). `publish-npm.yml` publishes the npm packages for
   every release.
5. Commit `chore: bump version to <next>-SNAPSHOT` on `next`.

Before the monthly release, check the Usage Center: the release must fit in what is left of the month.

## Headroom

A release is about 16 files per jar module (jar, sources, javadoc, pom, signatures and the required checksums) plus a few
per parent pom (see the `central-publishing-maven-plugin` comment in the root `pom.xml`). A new module costs about 16
files in every release. When one release no longer fits the monthly limit, publish fewer modules to Central (for example
the testing support modules, if no external user needs them) or move to a paid Central plan.

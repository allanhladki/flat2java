# Contributing to flat2java

Bug reports, questions and pull requests are welcome. This page describes how to build the project
and what a pull request needs to be merged.

## Reporting a bug

[Open an issue](https://github.com/allanhladki/flat2java/issues/new/choose) with:

- the flat2java and Java versions;
- the mapped class, reduced to the fields involved;
- the input record or object, with any personal data replaced;
- the full exception message.

> [!IMPORTANT]
> Do not report security vulnerabilities in public issues. See [SECURITY.md](SECURITY.md).

## Development setup

| Tool | Version |
|---|---|
| JDK | 17 or later (CI runs 17 and 21) |
| Maven | 3.9 or later |

```bash
git clone https://github.com/allanhladki/flat2java.git
cd flat2java
mvn verify
```

| Command | Purpose |
|---|---|
| `mvn verify` | Builds every module, runs the tests, checks formatting, writes the JaCoCo report |
| `mvn spotless:apply` | Formats the code; `verify` fails on unformatted code |
| `mvn -pl flat2java-core -Dtest=Flat2JavaTest test` | Runs one test class |
| `mvn -Prelease -Dgpg.skip verify` | Builds the sources and Javadoc jars, as a release does |

## Project layout

```text
flat2java-core/        Flat2Java, Flat2JavaOptions, @FlatField, exceptions, codecs (internal)
flat2java-processor/   Annotation processor; reuses the core validation rules
flat2java-spring/      Spring Boot auto-configuration
flat2java-quarkus/     CDI producers for Quarkus
```

Validation rules live in `internal.MappingRules` and are shared by the runtime and the processor.
A new rule goes there, with a test in both modules, so that both report the same message.

## Pull requests

- Keep each pull request to one change, and describe the behaviour before and after.
- Add a test for every bug fix and every new rule. JUnit 5 and AssertJ; jqwik for round-trip
  properties.
- Code, Javadoc and documentation are written in English.
- Public API changes need Javadoc and an entry in [CHANGELOG.md](CHANGELOG.md).
- Never log or put field values in exception messages: records often contain personal data.
- Pull requests are squash-merged: the pull request title becomes the commit on `master`, so write
  it as a [Conventional Commit](https://www.conventionalcommits.org/), for example
  `fix(core): reject signs in numeric columns`. Use `!` for breaking changes (`feat!: ...`).
- `master` is protected: changes go through a pull request, with the `verify` and
  `release artifacts` checks passing and the branch up to date.
- [CODEOWNERS](.github/CODEOWNERS) requests a review from the maintainer on every pull request.
  Approval is not required to merge.

## Releasing

Maintainers only. A release is a pull request with the `release` label: merging it publishes the
version in the POM.

1. Open a pull request that:
   - sets the release version in every module, dropping `-SNAPSHOT`:

     ```bash
     mvn versions:set -DnewVersion=0.1.0 -DgenerateBackupPoms=false
     ```

   - dates the version's CHANGELOG section: `## [0.1.0] - 2026-10-26`;
   - has the **`release`** label.
2. Merge it. The [Release workflow](.github/workflows/release.yml) then:
   1. checks that the merged pull request has the `release` label, that the version is not a
      `-SNAPSHOT`, that the tag `v0.1.0` does not exist yet, and that the CHANGELOG section is
      dated;
   2. builds, signs and publishes the artifacts to Maven Central, and waits until they are live;
   3. creates the tag `v0.1.0` on the merge commit, signed with the release key;
   4. creates the GitHub release with the CHANGELOG section as notes.
3. Open a pull request that sets the next `-SNAPSHOT` version and adds an empty
   `## [Unreleased]` section to the CHANGELOG. Do not label it.

> [!IMPORTANT]
> The `release` label is the only switch. A merged pull request without it never publishes, even
> when the version has no `-SNAPSHOT`. Add the label only to the release pull request: Maven
> Central does not allow a published version to be removed or replaced.

If the workflow fails, fix the cause and use **Re-run failed jobs** on the run in the Actions tab.
Each step skips what is already done: a version already on Maven Central is not deployed again,
and an existing tag or release is kept. When the fix needs a code change, merge it in a new pull
request with the `release` label; the version stays the same.

## License

By contributing, you agree that your contributions are licensed under the [MIT License](LICENSE).

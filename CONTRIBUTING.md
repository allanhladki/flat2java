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
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/), for example
  `fix(core): reject signs in numeric columns`.

## License

By contributing, you agree that your contributions are licensed under the [MIT License](LICENSE).

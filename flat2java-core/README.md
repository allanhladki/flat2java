# flat2java-core

The flat2java library. Usage, record format and configuration are documented in the
[project README](../README.md).

| Package | Content |
|---|---|
| `io.github.allanhladki.flat2java` | `Flat2Java` (entry point) and `Flat2JavaOptions` |
| `io.github.allanhladki.flat2java.annotation` | `@FlatField` |
| `io.github.allanhladki.flat2java.exception` | `Flat2JavaException` and its subclasses |
| `io.github.allanhladki.flat2java.internal` | Implementation; not part of the API |

The only runtime dependency is `slf4j-api`. The library does not ship a logging configuration.
It logs at `DEBUG` when a class mapping is built and at `WARN` when a `String` is truncated. Log
messages contain field names and lengths, never field values.

# Agent Instructions

## Scope

These instructions apply to the whole repository.

## Workflow

- Preserve the existing Gradle Kotlin DSL structure and version-catalog usage. Add dependencies in `gradle/libs.versions.toml` first, then reference them from `build.gradle.kts`.
- Keep changes small and local. Do not rename packages, tasks, or plugin IDs unless the task requires it.
- Mirror the existing source layout: production code in `src/main/kotlin`, tests in `src/test/kotlin`, package `it.unibo.protelis2kotlin`.

## Formatting And Validation

- Run `./gradlew ktlintFormat` before the final verification when Kotlin or Gradle Kotlin DSL files change.
- Run `./gradlew build` as the default final validation for any non-trivial change.
- Use repository tasks instead of ad hoc alternatives. Prefer `./gradlew test`, `./gradlew detekt`, `./gradlew dokkaGenerateHtml`, or the relevant publishing task only when the task specifically needs them.

## Code Rules

- Follow `.editorconfig`
- Keep Dokka and Protelis-related behavior aligned with the current plugin design. Prefer extending existing tasks and plugin wiring over introducing parallel mechanisms.
- Treat warning suppressions as a last resort. Add a short justification next to every suppression explaining why a code fix is not practical.
- Do not re-enable disabled Dokka V1 tasks unless the task explicitly requires that migration work.

## Testing Expectations

- Add or update tests in `src/test/kotlin` when changing conversion logic, plugin wiring, or generated-output behavior.
- Prefer the narrowest validation while iterating, but finish with `./gradlew build`.
- If a change targets JVM compatibility or Gradle-plugin behavior, use the existing Gradle tasks for that area instead of custom scripts.

## Release And Commit Rules

- Follow conventional commits with header format `type(scope): summary`.
- For breaking changes, use `type(scope)!: summary` and add a `BREAKING CHANGE:` footer.
- This repository uses `semantic-release-preconfigured-conventional-commits`: use `feat` for minor releases; `fix`, `docs`, `perf`, and `revert` for patch releases; `chore(api-deps)` for minor dependency releases; `chore(core-deps)` for patch dependency releases; `test`, `ci`, `build`, `style`, `refactor`, and other `chore(...)` scopes for non-release maintenance.
- Use `chore(...)` or `ci(...)` for changes to agent instructions, skills, or automation policy.

## Updates

- Treat an update request as complete only when the version change, any required compatibility fixes, and a successful verification run are all done.

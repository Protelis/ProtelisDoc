# ProtelisDoc - GitHub Copilot Instructions

## Project Overview

ProtelisDoc is a Gradle plugin that generates documentation for Protelis code. It works by:
1. Converting Protelis code to Kotlin code (preserving documentation comments and function signatures)
2. Using Dokka (Kotlin's documentation engine) to generate the documentation

**Main Technologies:**
- Kotlin 2.3.10
- Gradle (with Kotlin DSL)
- Dokka 2.0.0 (documentation engine)
- JUnit 5 with Kotest for testing

## Repository Structure

```
/
├── .github/                    # GitHub configuration and workflows
│   └── workflows/              # CI/CD workflows
├── src/
│   ├── main/kotlin/            # Main plugin source code
│   │   └── it/unibo/protelis2kotlin/
│   │       ├── Protelis2Kotlin.kt           # Core conversion logic
│   │       └── Protelis2KotlinDocPlugin.kt  # Gradle plugin implementation
│   └── test/kotlin/            # Test files
│       └── it/unibo/protelis2kotlin/
├── build.gradle.kts            # Main build configuration
├── gradle/libs.versions.toml   # Dependency version catalog
└── settings.gradle.kts         # Gradle settings
```

## Build and Test

### Building the Project
```bash
./gradlew build
```

### Running Tests
```bash
./gradlew test
```

Tests use Kotest framework and are located in `src/test/kotlin/`.

### Plugin Tasks
The plugin provides the following tasks:
- `generateKotlinFromProtelis` - Converts Protelis code to Kotlin
- `protelisdoc` - Generates documentation using Dokka

## Code Style and Conventions

### Kotlin Code Style
- **Indentation**: 4 spaces (defined in `.editorconfig`)
- **Max line length**: 120 characters
- **Code style**: IntelliJ IDEA conventions
- **Final newline**: Always insert
- **Imports**: Use wildcard imports layout (`*`)

### File Organization
- Package: `it.unibo.protelis2kotlin`
- Test files mirror the main source structure

### Naming Conventions
- Follow Kotlin standard naming conventions
- Task names use camelCase (e.g., `generateKotlinFromProtelis`)
- Extension names use lowercase (e.g., `protelisdoc`)

## Architecture and Design Patterns

### Plugin Architecture
1. **Extension Class** (`ProtelisDocExtension`): Provides configuration options for users
   - `baseDir`: Base directory for Protelis files (default: project path)
   - `destDir`: Output directory for documentation
   - `kotlinDestDir`: Intermediate directory for generated Kotlin code
   - `debug`: Enable debug output

2. **Plugin Class** (`Protelis2KotlinDocPlugin`): Main plugin implementation
   - Applies Dokka plugin automatically
   - Registers custom Gradle tasks
   - Configures Dokka with generated Kotlin sources

3. **Conversion Logic** (`Protelis2Kotlin`): Handles Protelis to Kotlin transformation

### Convention Over Configuration
The plugin follows Gradle's convention-over-configuration principle:
- Default source location: project root
- Default output: `build/protelis-docs/`
- Default Kotlin intermediate: `build/protelis2kt/`

## Dependencies and Libraries

### Core Dependencies (from `build.gradle.kts`)
- Gradle API and Kotlin DSL
- Kotlin standard library and reflection
- Dokka bundle (core, gradle plugin, javadoc plugin)
- Gradle TestKit for testing

### Version Catalog (`gradle/libs.versions.toml`)
All dependency versions are centralized in the version catalog:
- Dokka: 2.0.0
- Kotlin: 2.3.10
- Kotest: 6.1.2

### Plugin Dependencies
- `git-sensitive-semantic-versioning`: Automatic versioning
- `gradle-kotlin-qa`: Code quality checks
- `multi-jvm-test-plugin`: Multi-JVM testing
- `publish-on-central`: Publishing to Maven Central

## CI/CD Workflows

### Build and Deploy (`build-and-deploy.yml`)
- **Build**: Runs on Windows, macOS, and Ubuntu (latest LTS)
- **Codecov**: Enabled on Linux builds only
- **Dry Deploy**: Tests deployment process
- **Release**: Automated release using semantic-release (conventional commits)
- **Multi-JVM**: Tests against multiple Java versions

### Release Process
- Uses `semantic-release-preconfigured-conventional-commits`
- Publishes to:
  - Gradle Plugin Portal
  - Maven Central Portal
  - GitHub Packages

## Common Workflows

### Making Changes
1. Edit source files in `src/main/kotlin/`
2. Update tests in `src/test/kotlin/` if needed
3. Run `./gradlew build` to verify
4. Ensure code follows style guidelines in `.editorconfig`

### Adding Dependencies
1. Update `gradle/libs.versions.toml` with version
2. Reference in `build.gradle.kts` using `libs.` notation
3. Keep versions centralized in the catalog

### Documentation Comments
When working with Protelis documentation:
- Use JavaDoc-style comments
- Parameters: `@param <name> <type>, <description>`
- Returns: `@return <type>, <description>`
- Function types: `(T) -> T` format
- Tuple types: `[A,B,C]` format

## Important Notes

### Java Version Compatibility
- Compiled with oldest Java supported by Gradle
- Tested against latest Java supported by Gradle
- Dokka javadoc format has known issues with Java > 8 (use HTML format as workaround)

### Kotlin Version Management
- A special task `copyKotlinVersion` extracts Kotlin version from `gradle/libs.versions.toml`
- The version is embedded in resources for runtime access by the plugin

### Testing
- Tests use Gradle TestKit for functional testing
- Test logging is verbose (shows all events, causes, and stack traces)
- Test execution displays standard streams for debugging

## Publishing

### Repositories
- Maven Central (primary)
- GitHub Packages (secondary)
- Gradle Plugin Portal (for plugin discovery)

### Signing
- Uses PGP signing for releases
- In-memory signing keys (configured in CI)

### Credentials Required
- `MAVEN_CENTRAL_USERNAME` and `MAVEN_CENTRAL_PASSWORD`
- `SIGNING_KEY` and `SIGNING_PASSWORD`
- `GRADLE_PUBLISH_KEY` and `GRADLE_PUBLISH_SECRET`
- `DEPLOYMENT_TOKEN` for GitHub releases

## Special Considerations

1. **Duplicate Strategy**: Set to `WARN` for Copy tasks
2. **Parallel Builds**: Enabled via `gradle.properties`
3. **Node.js**: Required for semantic-release (version 24.13)
4. **Fork Handling**: Deployment disabled for forks
5. **Protelis Language**: This is a specialized language for aggregate computing

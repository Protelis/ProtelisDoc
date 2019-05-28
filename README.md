# org.protelis.protelis-doc

## Overview

This project is a Gradle plugin which has the goal of producing documentation for Protelis code.

It generates documentation from JavaDoc-like comments that are put just above Protelis functions.

It works as follows:

- Protelis code is converted to Kotlin code (including only documentation comments and function signatures)
- Documentation is produced from Kotlin code via [dokka](https://github.com/Kotlin/dokka) (the tool for Kotlin corresponding to JavaDoc for Java)

## Usage

Simply import the plugin in Gradle:

```koltin
plugins {
    id("org.protelis.protelisdoc") version "0.1.0"
}
```

It follows the convention-over-configuration principle. So, with standard Gradle/Maven-like, you should expect it to find protelis code under the working directory and generate docs under `build/protelis-docs`.

The configuration syntax, configuration options, and default values are shown in the following listing:

```kotlin
Protelis2KotlinDoc {
  baseDir.set(".") // base dir from which recursively looking for .pt files
  destDir.set("${project.buildDir.path}/protelis-docs/") // output dir for docs
  kotlinVersion.set("+")
  protelisVersion.set("+")
  outputFormat.set("javadoc") // Dokka's output format (alternative: 'html')
  automaticDependencies.set(true) // Automatic resolution of deps (e.g., protelis-interpreter)
  debug.set(false) // Debug prints are disabled by default
}
```

## Documenting Protelis code

For the plugin to work correctly, Protelis code should be documented as follows:

```kotlin
/**
 * @param a bool, first condition
 * @param b bool, second condition
 * @return  bool, true if both the conditions are true
 */
public def and(a, b) { ... }

/**
 * Apply function to the children of the current device. Use this function if every
 * child has a single parent, see getAllChildren otherwise.
 *
 * @param potential num, potential to be followed
 * @param f         (ExecutionContext) -> T', function to be applied to the child
 * @param g         (T) -> T, function to be applied to the child value
 * @param default   T, default value for devices which are not children
 * @return          [num|T', T], children
 */
public def getChildren(potential, f, g, default) { ... }
```

I.e.,

* Parameters should be described with lines `@param <paramName> <paramType>, <paramDescription>`
* Return type should be described as follows `@return <retType>, <retDescription>`
* Function types take form `(T) -> T`, where single-letter uppercase symbols are assumed to be generic parameter types
* Tuple types are denoted as `[A,B,C]`


## Release notes

**0.1.0**

- A basically working plugin
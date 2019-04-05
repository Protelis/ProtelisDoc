import java.net.URI

plugins {
    kotlin("jvm") version "1.3.21"
    id("com.palantir.git-version") version "0.12.0-rc2"
    application
}

/*
 * 
 * Machinery for Semantic versioning. To be released as separate plugin and then imported.

group = "org.protelis"
val projectId = "$group.$name"
val fullName = "Protelis KDoc generator"
val websiteUrl = "https://github.com/Protelis/Protelis-KDoc-generator"
val projectDetails = "https://github.com/Protelis/Protelis-KDoc-generator"

val versionDetails: VersionDetails = (property("versionDetails") as? Closure<VersionDetails>)?.call()
    ?: throw IllegalStateException("Unable to fetch the git version for this repository")
fun Int.asBase(base: Int = 36, digits: Int = 2) = toString(base).let {
    if (it.length >= digits) it
    else generateSequence {"0"}.take(digits - it.length).joinToString("") + it
}
val minVer = "0.1.0"
val semVer = """^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(-(0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*)(\.(0|[1-9]\d*|\d*[a-zA-Z-][0-9a-zA-Z-]*))*)?(\+[0-9a-zA-Z-]+(\.[0-9a-zA-Z-]+)*)?${'$'}""".toRegex()
version = with(versionDetails) {
    val tag = lastTag ?.takeIf { it.matches(semVer) }
    val baseVersion = tag ?: minVer
    val appendix = tag?.let {
        "".takeIf { commitDistance == 0 } ?: "-dev${commitDistance.asBase()}+${gitHash}"
    } ?: "-archeo+${gitHash}"
    baseVersion + appendix
}.take(20)
if (!version.toString().matches(semVer)) {
    throw IllegalStateException("Version ${version} does not match Semantic Versioning requirements")
}

*/

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk6")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClassName = "it.unibo.protelis2kotlin.AppKt"
}

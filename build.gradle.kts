import java.net.URI

plugins {
    kotlin("jvm") version "1.3.21"
    id("com.palantir.git-version") version "0.12.0-rc2"
    id ("org.danilopianini.git-sensitive-semantic-versioning") version "0.1.0"
    application
}

repositories {
    mavenCentral()
}

gitSemVer {
    version = computeGitSemVer() // THIS IS MANDATORY, AND MUST BE LAST IN BLOCK
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClassName = "it.unibo.protelis2kotlin.AppKt"
}

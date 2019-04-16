import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    application
    kotlin("jvm") version "1.3.21"
    `maven-publish`
    signing
    id("com.palantir.git-version") version "0.12.0-rc2"
    id ("org.danilopianini.git-sensitive-semantic-versioning") version "0.1.0"
    id ("org.danilopianini.publish-on-central") version "0.1.1"
    id ("org.jetbrains.dokka") version "0.9.17"
}

group = "org.protelis"

repositories {
    mavenCentral()
}

gitSemVer {
    version = computeGitSemVer() // THIS IS MANDATORY, AND MUST BE LAST IN BLOCK
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClassName = "it.unibo.protelis2kotlin.AppKt"
}

tasks {
    javadocJar {
        dependsOn(dokka)
        from(dokka.get().outputDirectory)
    }
}

publishOnCentral {
    projectDescription.set("A translator from documented Protelis code to compiling Kotlin interfaces")
    projectLongName.set("Protelis KDoc generator")
    projectUrl.set("https://github.com/Protelis/Protelis-KDoc-generator")
    scmConnection.set("git@github.com:Protelis/Protelis-KDoc-generator.git")
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        name.set("Danilo Pianini")
                        email.set("danilo.pianini@unibo.it")
                        url.set("http://www.danilopianini.org/")
                    }
                    developer {
                        name.set("Roberto Casadei")
                        email.set("roby.casadei@unibo.it")
                    }
                }
            }
        }
    }
}
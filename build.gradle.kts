@file:Suppress("UnstableApiUsage")

import io.gitlab.arturbosch.detekt.Detekt
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    `maven-publish`
    signing
    `java-gradle-plugin`
    alias(libs.plugins.dokka)
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.gradlePluginPublish)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.publishOnCentral)
    alias(libs.plugins.taskTree)
}

group = "org.protelis"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gitSemVer {
    maxVersionLength.set(20)
    buildMetadataSeparator.set("-")
}

multiJvm {
    jvmVersionForCompilation.set(8)
    maximumSupportedJvmVersion.set(latestJavaSupportedByGradle)
    if (Os.isFamily(Os.FAMILY_WINDOWS)) {
        testByDefaultWith(latestJavaSupportedByGradle)
    }
}

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("gradle-plugin"))
    implementation(libs.kotlin.compiler)
    implementation(libs.bundles.dokka)

    testImplementation(gradleTestKit())
    testImplementation(libs.bundles.kotlin.testing)
}

val copyKotlinVersion by tasks.registering {
    val inputFile = file("gradle/libs.versions.toml")
    inputs.file(inputFile)
    val outputFile =
        project.layout.buildDirectory.map {
            it.asFile.resolve("resources/main/protelisdoc/kotlinversion")
        }
    outputs.file(outputFile)
    doLast {
        val kotlinVersion = Regex("""kotlin\s*=\s*"(\d+(\.\d+)+)"""")
        val version =
            inputFile.useLines { lines ->
                lines
                    .mapNotNull {
                        kotlinVersion.matchEntire(it)?.groupValues?.get(1)
                    }.first()
            }
        outputFile.get().parentFile.mkdirs()
        outputFile.get().writeText(version)
    }
}

tasks.withType<KotlinCompilationTask<*>>().configureEach { finalizedBy(copyKotlinVersion) }
tasks.withType<KotlinCompile>().configureEach { finalizedBy(copyKotlinVersion) }
tasks.withType<Detekt>().configureEach { dependsOn(copyKotlinVersion) }
tasks.withType<PluginUnderTestMetadata>().configureEach { dependsOn(copyKotlinVersion) }
tasks.withType<Test>().configureEach { dependsOn(copyKotlinVersion) }
tasks.withType<Jar>().configureEach { dependsOn(copyKotlinVersion) }

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(
            *org.gradle.api.tasks.testing.logging.TestLogEvent
                .values(),
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.WARN
}

if (System.getenv("CI") == true.toString()) {
    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}

val websiteUrl = "https://github.com/Protelis/Protelis-KDoc-generator"

publishOnCentral {
    projectDescription.set("A translator from documented Protelis code to compiling Kotlin interfaces")
    projectLongName.set("ProtelisDoc generator")
    projectUrl.set(websiteUrl)
    scmConnection.set("git:git@github.com:Protelis/ProtelisDoc.git")
    repository("https://maven.pkg.github.com/Protelis/${rootProject.name}".lowercase(), name = "github") {
        user.set("danysk")
        password.set(System.getenv("GITHUB_TOKEN"))
    }
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

gradlePlugin {
    plugins {
        website.set(websiteUrl)
        vcsUrl.set("https://github.com/Protelis/ProtelisDoc.git")
        create("ProtelisDoc") {
            id = "org.protelis.protelisdoc"
            displayName = "Protelis Documentation Engine"
            description = "A plugin that translates Protelis modules to Kotlin code, then generates the function documentation via Dokka"
            implementationClass = "it.unibo.protelis2kotlin.Protelis2KotlinDocPlugin"
            tags.set(listOf("protelis", "documentation", "api", "dokka", "javadoc", "aggregate computing"))
        }
    }
}

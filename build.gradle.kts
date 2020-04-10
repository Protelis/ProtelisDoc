import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    `maven-publish`
    signing
    id("com.gradle.plugin-publish")
    id("org.danilopianini.git-sensitive-semantic-versioning")
    id("org.danilopianini.publish-on-central")
    id("org.jetbrains.dokka")
    id("org.jlleitschuh.gradle.ktlint")
    `java-gradle-plugin`
}

group = "org.protelis"

repositories {
    mavenCentral()
    jcenter()
}

gitSemVer {
    maxVersionLength.set(20)
    version = computeGitSemVer()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(gradleApi())
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:_")
    implementation(kotlin("gradle-plugin"))

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation(gradleTestKit())
    testImplementation("io.kotlintest:kotlintest-runner-junit5:_")
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

ktlint {
    ignoreFailures.set(false)
}

val websiteUrl = "https://github.com/Protelis/Protelis-KDoc-generator"

publishOnCentral {
    projectDescription.set("A translator from documented Protelis code to compiling Kotlin interfaces")
    projectLongName.set("Protelis KDoc generator")
    projectUrl.set(websiteUrl)
    scmConnection.set("git@github.com:Protelis/Protelis-KDoc-generator.git")
}

pluginBundle {
    website = websiteUrl
    vcsUrl = websiteUrl
    tags = listOf("protelis", "javadoc", "documentation", "protelisdoc", "dokka", "kotlin")
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

tasks {
    "test"(Test::class) {
        useJUnitPlatform()
        testLogging.showStandardStreams = true
        testLogging {
            showCauses = true
            showStackTraces = true
            showStandardStreams = true
            events(*TestLogEvent.values())
        }
    }
    register("createClasspathManifest") {
        val outputDir = file("$buildDir/$name")
        inputs.files(sourceSets.main.get().runtimeClasspath)
        outputs.dir(outputDir)
        doLast {
            outputDir.mkdirs()
            file("$outputDir/plugin-classpath.txt").writeText(sourceSets.main.get().runtimeClasspath.joinToString("\n"))
        }
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.6"
    }
}

// Add the classpath file to the test runtime classpath
dependencies {
    testRuntimeOnly(files(tasks["createClasspathManifest"]))
}

gradlePlugin {
    plugins {
        create("Protelis2KotlinDoc") {
            id = "org.protelis.protelisdoc"
            displayName = "Protelis Documentation Engine"
            description = "A plugin that translates Protelis modules to Kotlin code, then generates the function documentation via Dokka"
            implementationClass = "it.unibo.protelis2kotlin.Protelis2KotlinDocPlugin"
        }
    }
}

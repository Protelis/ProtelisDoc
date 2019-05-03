import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.3.21"
    `maven-publish`
    signing
    id("com.gradle.build-scan") version "2.1"
    id("com.gradle.plugin-publish") version "0.10.1"
    id("org.danilopianini.git-sensitive-semantic-versioning") version "0.1.0"
    id("org.danilopianini.publish-on-central") version "0.1.1"
    id("org.jetbrains.dokka") version "0.9.16"
    id("org.jlleitschuh.gradle.ktlint") version "7.3.0"
    `java-gradle-plugin`
}

group = "org.protelis"

repositories {
    mavenCentral()
    jcenter()
}

gitSemVer {
    version = computeGitSemVer() // THIS IS MANDATORY, AND MUST BE LAST IN BLOCK
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7")
    implementation(kotlin("reflect"))
    implementation(gradleApi())
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:0.9.18")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
    testImplementation(gradleTestKit())
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
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
        create("Protelis2Kotlin") {
            id = "org.protelis.protelis2kotlin"
            displayName = "Protelis to Kotlin API converter"
            implementationClass = "it.unibo.protelis2kotlin.Protelis2KotlinPlugin"
        }
        create("Protelis2KotlinDoc") {
            id = "org.protelis.protelisdoc"
            displayName = "Protelis Documentation Engine"
            implementationClass = "it.unibo.protelis2kotlin.Protelis2KotlinDocPlugin"
        }
    }
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
}

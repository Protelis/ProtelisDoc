import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
    signing
    id("org.danilopianini.git-sensitive-semantic-versioning")
    id("org.danilopianini.publish-on-central")
    id("org.jetbrains.dokka")
    `java-gradle-plugin`
    alias(libs.plugins.gradlePluginPublish)
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

dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(kotlin("gradle-plugin"))
    implementation("org.jetbrains.dokka:dokka-core:_")
    implementation("org.jetbrains.dokka:javadoc-plugin:_")
    implementation("org.jetbrains.dokka:dokka-gradle-plugin:_")

    testImplementation(gradleTestKit())
    testImplementation("io.kotlintest:kotlintest-runner-junit5:_")
}

tasks {
    withType<Copy> {
        duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.WARN
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
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
}

// Add the classpath file to the test runtime classpath
dependencies {
    testRuntimeOnly(files(tasks["createClasspathManifest"]))
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
    projectDescription = "A translator from documented Protelis code to compiling Kotlin interfaces"
    projectLongName = "Protelis KDoc generator"
    projectUrl = websiteUrl
    scmConnection = "git@github.com:Protelis/Protelis-KDoc-generator.git"
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

pluginBundle {
    website = websiteUrl
    vcsUrl = websiteUrl
    tags = listOf("protelis", "javadoc", "documentation", "protelisdoc", "dokka", "kotlin")
}

gradlePlugin {
    plugins {
        create("ProtelisDoc") {
            id = "org.protelis.protelisdoc"
            displayName = "Protelis Documentation Engine"
            description = "A plugin that translates Protelis modules to Kotlin code, then generates the function documentation via Dokka"
            implementationClass = "it.unibo.protelis2kotlin.Protelis2KotlinDocPlugin"
        }
    }
}

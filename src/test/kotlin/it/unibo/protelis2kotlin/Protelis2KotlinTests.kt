package it.unibo.protelis2kotlin

import io.kotlintest.specs.StringSpec
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class Protelis2KotlinTests : StringSpec({
    fun folder(closure: TemporaryFolder.() -> Unit) = TemporaryFolder().apply {
        create()
        closure()
    }
    fun TemporaryFolder.file(name: String, content: () -> String) =
            newFile(name).writeText(content().trimIndent())

    val workingDirectory = folder {
        file("settings.gradle") { "rootProject.name = 'testproject'" }
        val srcDir = File("${this.root.absolutePath}/src/main/protelis").mkdirs()
        File("${this.root.absolutePath}/src/main/protelis/file.pt").writeText("""
module protelis:coord:accumulation
import protelis:coord:meta
import protelis:coord:spreading
import protelis:lang:utils
import protelis:state:time

module protelis:lang:utils
import java.lang.Math.pow

/**
 * @param a bool, first condition
 * @param b bool, second condition
 * @return  bool, true if both the conditions are true
 */
public def and(a, b) {
    a && b
}
        """.trimIndent()
        )

        file("build.gradle.kts") { """
        import org.jetbrains.dokka.gradle.DokkaTask

        plugins {
            kotlin("jvm") version "1.3.21"
            id("it.unibo.protelis2kotlin")
            id("org.jetbrains.dokka") version "0.9.18"
        }

        dependencies {
            implementation(kotlin("stdlib"))
            implementation("org.protelis:protelis-interpreter:11.1.0")
        }

        repositories {
            jcenter() // or maven { url 'https://dl.bintray.com/kotlin/dokka' }
        }

        Protelis2Kotlin {
            baseDir.set("${this.root.absolutePath!!}/src/main/protelis")
            destDir.set("${this.root.absolutePath!!}/src/main/kotlin")
        }

        val dokka by tasks.getting(DokkaTask::class) {
            outputFormat = "html"
            outputDirectory = "${"$"}buildDir/dokka"
            jdkVersion = 8
            reportUndocumented = true
            dependsOn("generateKotlinFromProtelis")
            dependsOn("compileKotlin")
        }
    """ }
    }
    val pluginClasspathResource = ClassLoader.getSystemClassLoader()
            .getResource("plugin-classpath.txt")
            ?: throw IllegalStateException("Did not find plugin classpath resource, run \"testClasses\" build task.") as Throwable
    val classpath = pluginClasspathResource.openStream().bufferedReader().use { reader ->
        reader.readLines().map { File(it) }
    }
    "Generation of Kotlin interfaces from Protelis sources should work" {
        println(workingDirectory.root)
        val result = GradleRunner.create()
                .withProjectDir(workingDirectory.root)
                .withPluginClasspath(classpath)
                .withArguments("generateKotlinFromProtelis", "dokka")
                .build()
        println(result.tasks)
        println(result.output)
        File(workingDirectory.root.toURI()).walkTopDown().forEach { println(it) }
    }
})

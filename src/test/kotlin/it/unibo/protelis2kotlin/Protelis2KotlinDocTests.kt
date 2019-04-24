package it.unibo.protelis2kotlin

import io.kotlintest.specs.StringSpec
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class Protelis2KotlinDocTests : StringSpec({
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
        plugins {
            kotlin("jvm") version "1.3.21"
            id("it.unibo.protelis2kotlindoc")
        }

        dependencies {
            implementation(kotlin("stdlib"))
        }

        repositories {
            jcenter() // or maven { url 'https://dl.bintray.com/kotlin/dokka' }
        }

        Protelis2KotlinDoc {
            baseDir.set("${this.root.absolutePath!!}/src/main/protelis")
            destDir.set("${this.root.absolutePath!!}/docs")
        }

    """ }
    }
    val pluginClasspathResource = ClassLoader.getSystemClassLoader()
            .getResource("plugin-classpath.txt")
            ?: throw IllegalStateException("Did not find plugin classpath resource, run \"testClasses\" build task.") as Throwable
    val classpath = pluginClasspathResource.openStream().bufferedReader().use { reader ->
        reader.readLines().map { File(it) }
    }
    "Generation of Kotlin docs from Protelis sources should work" {
        println(workingDirectory.root)
        val result = GradleRunner.create()
                .withProjectDir(workingDirectory.root)
                .withPluginClasspath(classpath)
                .withArguments("generateProtelisDoc")
                .build()
        println(result.tasks)
        println(result.output)
        File(workingDirectory.root.toURI()).walkTopDown().forEach { println(it) }
    }
})

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
        File("${this.root.absolutePath}/src/main/protelis").mkdirs()
        File("${this.root.absolutePath}/src/main/protelis/file2.pt").writeText("""
            unformed protelis file
            def prova
            /* ..
            hello
            """)
        File("${this.root.absolutePath}/src/main/protelis/file.java").writeText("""
            /** prova
            */
            public static void main(String[] args){ }
        """.trimIndent())
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

/**
 * Find the parent of the current device following the maximum decrease in
 * potential.
 *
 * @param potential num, potential
 * @param f         (ExecutionContext) -> T, what to do
 *                   with the parent
 * @param g         (T') -> T', what to do with the value
 * @param local     T', local value
 * @return          [num|T,T'], [imRoot()|noParent()|f(parent), g(value)]
 * @see imRoot, noParent
 */
public def getParentExtended(potential, f, g, local) {
    getParentsExtended(potential, v -> { minHood(v) }, f, g, local, local)
}

/**
 * Find the parents of the current device following the decrease in
 * potential.
 *
 * @param potential num, potential
 * @param f         (ExecutionContext) -> T, what to do with the parent
 * @param g         (T') -> T', what to do with the value
 * @param local     T', local value
 * @return          [[num|T,T']], [[imRoot()|noParent()|f(parent), g(value)]]
 * @see imRoot, noParent
 */
public def getParents(potential, f, g, local, default) {
    getParentsExtended(potential, identity, f, g, local, default)
}

/**
 * Aggregation of local information.
 *
 * @param local  T, local information
 * @param reduce (T, T) -> T, how to aggregate information
 * @param        T, aggregated information
 */
public def aggregation(local, reduce) {
    hood((a, b) -> { reduce.apply(a, b) }, local, nbr(local))
}

        """.trimIndent()
        )

        file("build.gradle.kts") { """
        plugins {
            id("it.unibo.protelis2kotlindoc")
        }

        dependencies {
            implementation("org.protelis:protelis-interpreter:11.1.0")
            implementation(kotlin("stdlib"))
        }

        // repositories {
        //    jcenter() // or maven { url 'https://dl.bintray.com/kotlin/dokka' }
        // }

        Protelis2KotlinDoc {
            baseDir.set("${this.root.absolutePath!!}/src/main/protelis")
            // destDir.set("${this.root.absolutePath!!}/docs")
            // kotlinVersion.set("+")
            // protelisVersion.set("+")
            debug.set(true)
        }
    """ }
    }
    val pluginClasspathResource = ClassLoader.getSystemClassLoader()
            .getResource("plugin-classpath.txt")
            ?: throw IllegalStateException("Did not find plugin classpath resource, run \"testClasses\" build task.")
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

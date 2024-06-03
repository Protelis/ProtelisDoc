package it.unibo.protelis2kotlin

import io.kotest.core.spec.style.scopes.StringSpecScope
import io.kotest.matchers.shouldBe
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

internal object TestUtil {

    fun projectRoot() = File(".").findProjectRoot()

    fun catalog() = File(File(projectRoot(), "gradle"), "libs.versions.toml").readText()

    fun multiJvmVersion() = requireNotNull(
        Regex("\"org\\.danilopianini\\.multi-jvm-test-plugin:(.*?)\"").find(catalog()),
    ).groupValues[1]

    fun folder(closure: TemporaryFolder.() -> Unit) = TemporaryFolder().apply {
        create()
        makeSettingsFile()
        File("${root.absoluteFile.absolutePath}${File.separator}src${File.separator}main${File.separator}protelis")
            .mkdirs()
        closure()
    }

    fun TemporaryFolder.file(name: String, content: () -> String) =
        newFile(name).writeText(content().trimIndent())

    fun StringSpecScope.runGradleTask(workingDirectory: TemporaryFolder, task: String) {
        val result = GradleRunner.create()
            .withProjectDir(workingDirectory.root)
            .withPluginClasspath()
            .withArguments(task, "--stacktrace")
            .build()
        println(result.output)
        result.task(":$task")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    fun File.isProjectRoot(): Boolean = exists() &&
        listFiles().orEmpty().map { it.name }.count { it == "build.gradle.kts" || it == "settings.gradle.kts" } == 2

    tailrec fun File.findProjectRoot(): File = when {
        isProjectRoot() -> this
        parentFile == null -> error("No project root found")
        else -> parentFile.findProjectRoot()
    }

    fun TemporaryFolder.makeSettingsFile() = file("settings.gradle.kts") {
        File(projectRoot(), "settings.gradle.kts")
            .readText()
            .replace(Regex("^\\s*rootProject\\s*\\.\\s*name.*$"), "rootProject.name = \"protelisdoc-test")
            .replace("createHooks()", "")
    }
}

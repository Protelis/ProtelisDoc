package it.unibo.protelis2kotlin

import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createFile
import kotlin.io.path.createTempDirectory
import kotlin.io.path.writeText

internal object TestUtil {
    private fun projectRoot() = File(".").findProjectRoot()

    fun folder(closure: Path.() -> Unit) =
        createTempDirectory("protelisdoc-test").apply {
            makeSettingsFile()
            File("${absolutePathString()}${File.separator}src${File.separator}main${File.separator}protelis")
                .mkdirs()
            closure()
        }

    fun Path.file(
        name: String,
        content: () -> String,
    ) = resolve(name).createFile().writeText(content().trimIndent())

    fun runGradleTask(
        workingDirectory: Path,
        task: String,
    ) {
        val result =
            GradleRunner
                .create()
                .withProjectDir(workingDirectory.toFile())
                .withPluginClasspath()
                .withGradleVersion(GradleVersion.current().version)
                .withArguments(task, "--stacktrace")
                .build()
        println(result.output)
        result.task(":$task")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    private fun File.isProjectRoot(): Boolean =
        exists() &&
            listFiles().orEmpty().map { it.name }.count { it == "build.gradle.kts" || it == "settings.gradle.kts" } == 2

    private tailrec fun File.findProjectRoot(): File =
        when {
            isProjectRoot() -> this
            parentFile == null -> error("No project root found")
            else -> parentFile.findProjectRoot()
        }

    private fun Path.makeSettingsFile() =
        file("settings.gradle.kts") {
            File(projectRoot(), "settings.gradle.kts")
                .readText()
                .replace(
                    Regex("^\\s*rootProject\\s*\\.\\s*name.*$", RegexOption.MULTILINE),
                    "rootProject.name = \"protelisdoc-test\"",
                ).replace("createHooks()", "")
        }
}

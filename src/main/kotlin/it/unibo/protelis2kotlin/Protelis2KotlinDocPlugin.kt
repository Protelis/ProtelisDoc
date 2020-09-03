package it.unibo.protelis2kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.dokka.DokkaVersion
import org.jetbrains.dokka.gradle.DokkaTask
import java.io.File
import java.io.File.separator as SEP

/**
 * Extension for the Protelis2KotlinDoc plugin.
 * @param baseDir The base directory from which looking for Protelis files
 * @param destDir The directory that will contain the generated docs
 * @param kotlinDestDir destubatuib directirt for the intermediate Kotlin kode
 * @param
 */
open class ProtelisDocExtension @JvmOverloads constructor(
    private val project: Project,
    val baseDir: Property<String> = project.propertyWithDefault(project.path),
    val destDir: Property<String> = project.propertyWithDefault(project.buildDir.path + "${SEP}protelis-docs$SEP"),
    val kotlinDestDir: Property<String> = project.propertyWithDefault(project.buildDir.path + "${SEP}kotlin-for-protelis$SEP"),
    val debug: Property<Boolean> = project.propertyWithDefault(false)
)

/**
 * Protelis2KotlinDoc Gradle Plugin: reuses the Protelis2Kotlin and Dokka plugins to generate Kotlin docs from Protelis code.
 */
class Protelis2KotlinDocPlugin : Plugin<Project> {
    private val generateProtelisDocTaskName = "generateProtelisDoc"
    private val generateKotlinFromProtelisTaskName = "generateKotlinFromProtelis"
    private val protelis2KotlinPluginConfig = "protelisdoc"
    private val dokkaPluginName = "org.jetbrains.dokka"

    override fun apply(project: Project) {
        val extension = project.extensions.create(protelis2KotlinPluginConfig, ProtelisDocExtension::class.java, project)
        project.logger.debug(
            """
                Applying plugin ProtelisDoc.
                Default configuration:
                - debug = ${extension.debug.get()}
                - baseDir = ${extension.baseDir.get()}
                - destDir = ${extension.destDir.get()}
                - kotlinDestDir = ${extension.kotlinDestDir.get()}
            """.trimIndent()
        )
        if (!project.pluginManager.hasPlugin(dokkaPluginName)) {
            project.pluginManager.apply(dokkaPluginName)
        }
        val config = project.configurations.create(protelis2KotlinPluginConfig) { configuration ->
            project.configurations.findByName("implementation")?.let {
                configuration.extendsFrom(it)
            }
        }
        // Kotlin generation task
        val genKotlinTask = project.task(generateKotlinFromProtelisTaskName) {
            it.doLast {
                main(arrayOf(extension.baseDir.get(), extension.kotlinDestDir.get(), if (extension.debug.get()) "1" else "0"))
            }
            Log.log("[${it.name}]\nInputs: ${it.inputs.files.files}\nOutputs: ${it.outputs.files.files}")
        }
        // ProtelisDoc task, based on Dokka
        project.tasks.register(generateProtelisDocTaskName, DokkaTask::class.java) { dokkaTask ->
            dokkaTask.plugins.dependencies.add(
                project.dependencies.create("org.jetbrains.dokka:javadoc-plugin:${ DokkaVersion.version}")
            )
            dokkaTask.dependsOn(genKotlinTask)
            dokkaTask.outputDirectory.set(extension.destDir.map { File(it) })
            dokkaTask.dokkaSourceSets {
                create("protelisdoc") { sourceSet ->
                    sourceSet.classpath.setFrom(config.resolve())
                    sourceSet.sourceRoots.setFrom(extension.kotlinDestDir.get())
                }
            }
            dokkaTask.outputDirectory.set(extension.destDir.map { File(it) })
        }
    }
}

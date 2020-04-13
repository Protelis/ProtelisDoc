package it.unibo.protelis2kotlin

import org.gradle.api.Action
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.dokka.gradle.GradlePassConfigurationImpl
import org.jetbrains.dokka.gradle.GradleSourceRootImpl
import java.io.File.separator as SEP

/**
 * Extension for the Protelis2KotlinDoc plugin.
 * @param baseDir The base directory from which looking for Protelis files
 * @param destDir The directory that will contain the generated docs
 * @param kotlinVersion
 * @param protelisVersion
 */
open class Protelis2KotlinDocPluginExtension @JvmOverloads constructor(
    private val project: Project,
    val baseDir: Property<String> = project.propertyWithDefault("."),
    val destDir: Property<String> = project.propertyWithDefault(project.buildDir.path + "${SEP}protelis-docs$SEP"),
    val kotlinDestDir: Property<String> = project.propertyWithDefault(project.buildDir.path + "${SEP}kotlin-for-protelis$SEP"),
    val kotlinVersion: Property<String> = project.propertyWithDefault("+"),
    val protelisVersion: Property<String> = project.propertyWithDefault("+"),
    val outputFormat: Property<String> = project.propertyWithDefault("javadoc"),
    val debug: Property<Boolean> = project.propertyWithDefault(false)
)

/**
 * Protelis2KotlinDoc Gradle Plugin: reuses the Protelis2Kotlin and Dokka plugins to generate Kotlin docs from Protelis code.
 */
class Protelis2KotlinDocPlugin : Plugin<Project> {
    private val generateProtelisDocTaskName = "generateProtelisDoc"
    private val generateKotlinFromProtelisTaskName = "generateKotlinFromProtelis"

    private val dokkaPluginName = "org.jetbrains.dokka"
    private val protelis2KotlinDocPlugin = "Protelis2KotlinDoc"

    private val protelisGroup = "org.protelis"
    private val protelisInterpreterDepName = "protelis-interpreter"
    private val kotlinGroup = "org.jetbrains.kotlin"
    private val kotlinStdlibDepName = "kotlin-stdlib"
    private val protelis2KotlinPluginConfig = "protelisdoc"

    override fun apply(project: Project) {
        val extension = project.extensions.create(protelis2KotlinDocPlugin, Protelis2KotlinDocPluginExtension::class.java, project)
        project.logger.debug("""Applying plugin $protelis2KotlinDocPlugin.
            Default configuration:
            - debug = ${extension.debug.get()}
            - baseDir = ${extension.baseDir.get()}
            - destDir = ${extension.destDir.get()}
            - protelisVersion = ${extension.protelisVersion.get()}
            - outputFormat = ${extension.outputFormat.get()}
            - kotlinDestDir = ${extension.kotlinDestDir.get()}
            """.trimIndent())

        if (JavaVersion.current() > JavaVersion.VERSION_1_8) extension.outputFormat.set("html")

//        if (!project.repositories.contains(project.repositories.jcenter())) {
//            project.repositories.add(project.repositories.jcenter())
//        }
//        if (!project.repositories.contains(project.repositories.mavenCentral())) {
//            project.repositories.add(project.repositories.mavenCentral())
//        }
//        if (!project.pluginManager.hasPlugin(kotlinPluginName)) {
//            project.pluginManager.apply(kotlinPluginName)
//        }

        val config = project.configurations.create(protelis2KotlinPluginConfig) { configuration ->
            configuration.extendsFrom(project.configurations.getByName("implementation"))
            val kotliStdlibDependency = "$kotlinGroup:$kotlinStdlibDepName"
            val protelisInterpreter = "$protelisGroup:$protelisInterpreterDepName"
            val allDependencies = configuration.dependencies.map { "${it.group}:${it.name}" }
            if (kotliStdlibDependency !in allDependencies) {
                project.dependencies.add(configuration.name, "$kotliStdlibDependency:${extension.kotlinVersion.get()}")
            }
            if (protelisInterpreter !in allDependencies) {
                project.dependencies.add(configuration.name, "$protelisInterpreter:${extension.protelisVersion.get()}")
            }
        }

        if (!project.pluginManager.hasPlugin(dokkaPluginName)) {
            project.pluginManager.apply(dokkaPluginName)
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
            dokkaTask.dependsOn(genKotlinTask)
            dokkaTask.outputDirectory = extension.destDir.get()
            dokkaTask.outputFormat = extension.outputFormat.get()
            dokkaTask.configuration(Action<GradlePassConfigurationImpl> { dokkaConf ->
                dokkaConf.sourceRoot(Action<GradleSourceRootImpl> { t -> t.path = extension.kotlinDestDir.get() })
                dokkaTask.doFirst {
                    dokkaConf.classpath = config.resolve().map { it.absolutePath }
                }
            })
            dokkaTask.outputDirectory = extension.destDir.get()
            dokkaTask.outputFormat = extension.outputFormat.get()
        }
    }
}
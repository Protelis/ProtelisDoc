package it.unibo.protelis2kotlin

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
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
    val baseDir: Property<String> = project.propertyWithDefault(project.rootDir.path),
    val destDir: Property<String> = project.propertyWithDefault(project.buildDir.path + "${SEP}protelis-docs$SEP"),
    val kotlinDestDir: Property<String> = project.propertyWithDefault(project.buildDir.path + "${SEP}kotlin-for-protelis$SEP"),
    val kotlinVersion: Property<String> = project.propertyWithDefault("+"),
    val protelisVersion: Property<String> = project.propertyWithDefault("+"),
    val outputFormat: Property<String> = project.propertyWithDefault("javadoc"),
    val automaticDependencies: Property<Boolean> = project.propertyWithDefault(false),
    val debug: Property<Boolean> = project.propertyWithDefault(false)
)

/**
 * Protelis2KotlinDoc Gradle Plugin: reuses the Protelis2Kotlin and Dokka plugins to generate Kotlin docs from Protelis code.
 */
class Protelis2KotlinDocPlugin : Plugin<Project> {
    private val generateProtelisDocTaskName = "generateProtelisDoc"
    private val generateKotlinFromProtelisTaskName = "generateKotlinFromProtelis"
    private val compileKotlinTaskName = "compileKotlin"

    private val dokkaTaskName = "dokka"
    private val dokkaPluginName = "org.jetbrains.dokka"
    private val kotlinPluginName = "org.jetbrains.kotlin.jvm"
    private val protelis2KotlinDocPlugin = "Protelis2KotlinDoc"

    private val protelisGroup = "org.protelis"
    private val protelisInterpreterDepName = "protelis-interpreter"
    private val kotlinGroup = "org.jetbrains.kotlin"
    private val kotlinStdlibDepName = "kotlin-stdlib"

    private val implConfiguration = "implementation"
    private val protelis2KotlinPluginConfig = "protelis2KotlinPluginConfig"

    override fun apply(project: Project) {
        val extension = project.extensions.create(protelis2KotlinDocPlugin, Protelis2KotlinDocPluginExtension::class.java, project)
        Log.log("""Applying plugin $protelis2KotlinDocPlugin.
            Default configuration:
            ${pluginProperties(extension)}
            """.trimIndent())

        if (JavaVersion.current() > JavaVersion.VERSION_1_8) extension.outputFormat.set("html")

        if (!project.repositories.contains(project.repositories.jcenter())) {
            project.repositories.add(project.repositories.jcenter())
        }
        if (!project.repositories.contains(project.repositories.mavenCentral())) {
            project.repositories.add(project.repositories.mavenCentral())
        }

        if (!project.pluginManager.hasPlugin(kotlinPluginName)) {
            project.pluginManager.apply(kotlinPluginName)
        }

        // Add dependency to Kotlin stdlib for TODO()s and Protelis
        val config = project.configurations.create(protelis2KotlinPluginConfig)
        assert(project.configurations.add(config))
        project.dependencies.add(config.name, "$kotlinGroup:$kotlinStdlibDepName:${extension.kotlinVersion.get()}")
        project.dependencies.add(config.name, "$protelisGroup:$protelisInterpreterDepName:${extension.protelisVersion.get()}")
        // project.configurations.getByName(implConfiguration).extendsFrom(config) // We want them to be decoupled

        if (!project.pluginManager.hasPlugin(dokkaPluginName)) {
            project.pluginManager.apply(dokkaPluginName)
        }

        val compileKotlin = project.tasks.getByPath(compileKotlinTaskName) as KotlinCompile
        compileKotlin.dependsOn(generateKotlinFromProtelisTaskName)
        // compileKotlin.taskDependencies.getDependencies(compileKotlin).addAll(config.dependencies)

        // Configure Dokka plugin
        val dokkaTask = project.tasks.getByName(dokkaTaskName)
        dokkaTask.setProperty("jdkVersion", 8)
        dokkaTask.setProperty("reportUndocumented", true)
        dokkaTask.dependsOn(compileKotlinTaskName)
        dokkaTask.setProperty("outputDirectory", extension.destDir.get())
        dokkaTask.setProperty("outputFormat", extension.outputFormat.get())

        // Configure Kotlin plugin
        val kotlinPluginExt = project.extensions.getByName("kotlin") as KotlinJvmProjectExtension
        val mainKotlinSrcset = kotlinPluginExt.sourceSets.getByName("main")
        // This doesn't work: mainKotlinSrcset.kotlin.srcDirs.add(File(*))
        // This also doesn't work: mainKotlinSrcset.resources.srcDirs.add(File(*))
        // This doesn't work as well: mainKotlinSrcset.kotlin.sourceDirectories.files.add(File(*))
        mainKotlinSrcset.kotlin.setSrcDirs(setOf(File(extension.kotlinDestDir.get())))

        val genKotlinTask = project.task(generateKotlinFromProtelisTaskName) {
            it.doLast {
                main(arrayOf(extension.baseDir.get(), extension.kotlinDestDir.get(), if (extension.debug.get()) "1" else "0"))
            }
            Log.log("[${it.name}]\nInputs: ${it.inputs.files.files}\nOutputs: ${it.outputs.files.files}")
        }

        val genDocTask = project.task(generateProtelisDocTaskName) {
            it.dependsOn(dokkaTaskName)
            Log.log("[${it.name}]\nInputs: ${it.inputs.files.files}\nOutputs: ${it.outputs.files.files}")
        }

        project.task("configureProtelis2KotlinPluginTasks") {
            genKotlinTask.dependsOn(it.name)
            it.doLast {
                if (extension.automaticDependencies.get()) {
                    val deps = project.configurations.getByName(implConfiguration).dependencies
                    if (!deps.any { it.group==kotlinGroup && it.name==kotlinStdlibDepName }) {
                        project.dependencies.add(implConfiguration, "$kotlinGroup:$kotlinStdlibDepName:${extension.kotlinVersion.get()}")
                    }

                    if (!deps.any { it.group==protelisGroup && it.name==protelisInterpreterDepName }) {
                        project.dependencies.add(implConfiguration, "$protelisGroup:$protelisInterpreterDepName:${extension.protelisVersion.get()}")
                    }
                }

                genKotlinTask.inputs.files(project.fileTree(extension.baseDir.get()))
                genKotlinTask.outputs.files(project.fileTree(extension.kotlinDestDir.get()))
                genDocTask.inputs.files(project.fileTree(extension.baseDir.get()))
                genDocTask.outputs.files(project.fileTree(extension.destDir.get()))

                Log.log("[${it.name}] Protelis2KotlinDoc plugin configured\n${pluginProperties(extension)}")
            }
        }
    }

    fun pluginProperties(extension: Protelis2KotlinDocPluginExtension) {
        """
            - debug = ${extension.debug.get()}
            - baseDir = ${extension.baseDir.get()}
            - destDir = ${extension.destDir.get()}
            - protelisVersion = ${extension.protelisVersion.get()}
            - outputFormat = ${extension.outputFormat.get()}
            - automaticDependencies = ${extension.automaticDependencies.get()}
            - kotlinDestDir = ${extension.kotlinDestDir.get()}
        """.trimIndent()
    }
}
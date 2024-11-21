package it.unibo.protelis2kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.property
import org.jetbrains.dokka.DokkaVersion
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.io.File.separator as SEP

/**
 * Extension for the Protelis2KotlinDoc plugin.
 * @param baseDir The base directory from which looking for Protelis files
 * @param destDir The directory that will contain the generated docs
 * @param kotlinDestDir destubatuib directirt for the intermediate Kotlin kode
 * @param debug enables debug output
 * @param
 */
open class ProtelisDocExtension @JvmOverloads constructor(
    private val project: Project,
    val baseDir: Property<String> = project.propertyWithDefault(project.path),
    val destDir: Property<String> = project.propertyWithDefault(
        project.buildDirectory.map { "$it${SEP}kotlin-for-protelis$SEP" },
    ),
    val kotlinDestDir: Property<String> = project.objects.property(String::class).convention(
        project.layout.buildDirectory.dir("protelis2kt").map { it.asFile.absolutePath },
    ),
    val debug: Property<Boolean> = project.propertyWithDefault(false),
) {
    private companion object {
        private val Project.buildDirectory: Provider<String> get() =
            project.layout.buildDirectory.asFile.map { it.absolutePath }
    }
}

/**
 * Protelis2KotlinDoc Gradle Plugin:
 * reuses the Protelis2Kotlin and Dokka plugins to generate Kotlin docs from Protelis code.
 */
class Protelis2KotlinDocPlugin : Plugin<Project> {
    private val protelisDocTaskName = "protelisdoc"
    private val generateKotlinFromProtelisTaskName = "generateKotlinFromProtelis"
    private val extensionName = "protelisdoc"
    private val dokkaPluginName = "org.jetbrains.dokka"

    override fun apply(project: Project) {
        val extension = project.extensions
            .create(extensionName, ProtelisDocExtension::class.java, project)
        if (!project.pluginManager.hasPlugin(dokkaPluginName)) {
            project.pluginManager.apply(dokkaPluginName)
        }
        val config = project.configurations.create(extensionName) { configuration ->
            configuration.dependencies.add(
                project.dependencies.create(
                    "org.jetbrains.kotlin:kotlin-stdlib:${KotlinCompilerVersion.VERSION}",
                ),
            )
        }
        // Kotlin generation task
        val genKotlinTask = project.task(generateKotlinFromProtelisTaskName) {
            it.doLast {
                project.logger.debug(
                    """
                    Applying plugin ProtelisDoc. Configuration:
                    - debug = ${extension.debug.get()}
                    - baseDir = ${extension.baseDir.get()}
                    - destDir = ${extension.destDir.get()}
                    - kotlinDestDir = ${extension.kotlinDestDir.get()}
                    """.trimIndent(),
                )
                project.protelis2Kt(extension.baseDir.get(), extension.kotlinDestDir.get())
            }
        }
        // ProtelisDoc task, based on Dokka
        val protelisdoc = project.tasks.register(protelisDocTaskName, DokkaTask::class.java) { dokkaTask ->
            dokkaTask.plugins.dependencies.add(
                project.dependencies.create("org.jetbrains.dokka:javadoc-plugin:${DokkaVersion.version}"),
            )
            dokkaTask.dependsOn(genKotlinTask)
        }
        project.afterEvaluate {
            protelisdoc.get().apply {
                dokkaSourceSets { sourceSetContainer ->
                    sourceSetContainer.create("protelisdoc") { sourceSet ->
                        sourceSet.sourceRoots.setFrom(extension.kotlinDestDir.get())
                        val resolvedConfiguration = config.resolvedConfiguration
                        if (resolvedConfiguration.hasError()) {
                            kotlin.runCatching { resolvedConfiguration.rethrowFailure() }.onFailure {
                                logger.warn("ProtelisDoc failed dependecy resolution!", it)
                            }
                        } else {
                            sourceSet.classpath.setFrom(resolvedConfiguration.resolvedArtifacts.map { it.file })
                        }
                    }
                }
                outputDirectory.set(extension.destDir.map { project.file(it) }.get())
            }
        }
    }
}

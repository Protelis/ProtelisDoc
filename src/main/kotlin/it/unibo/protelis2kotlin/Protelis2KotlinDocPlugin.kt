package it.unibo.protelis2kotlin

import java.io.File.separator as SEP
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.property
import org.jetbrains.dokka.DokkaVersion
import org.jetbrains.dokka.gradle.DokkaExtension

/**
 * Extension for the Protelis2KotlinDoc plugin.
 * @param baseDir The base directory from which looking for Protelis files
 * @param destDir The directory that will contain the generated docs
 * @param kotlinDestDir destubatuib directirt for the intermediate Kotlin kode
 * @param debug enables debug output
 * @param
 */
open class ProtelisDocExtension
@JvmOverloads
constructor(
    private val project: Project,
    val baseDir: Property<String> = project.propertyWithDefault(project.path),
    val destDir: Property<String> = project.propertyWithDefault(
        project.buildDirectory.map { "$it${SEP}kotlin-for-protelis$SEP" },
    ),
    val kotlinDestDir: Property<String> = project.objects.property(String::class).convention(
        project.layout.buildDirectory
            .dir("protelis2kt")
            .map { it.asFile.absolutePath },
    ),
    val debug: Property<Boolean> = project.propertyWithDefault(false),
) {
    private companion object {
        private val Project.buildDirectory: Provider<String>
            get() = project.layout.buildDirectory.asFile.map { it.absolutePath }
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
        val extension = project.extensions.create(extensionName, ProtelisDocExtension::class.java, project)
        if (!project.pluginManager.hasPlugin(dokkaPluginName)) {
            project.pluginManager.apply(dokkaPluginName)
        }
        val kotlinVersion = checkNotNull(
            Thread.currentThread().contextClassLoader.getResource("protelisdoc/kotlinversion"),
        ) {
            "Kotlin version not found, bug in protelisdoc"
        }.readText().trim()
        val config = project.configurations.create(extensionName) { configuration ->
            configuration.dependencies.add(
                project.dependencies.create(
                    "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion",
                ),
            )
        }
        // Kotlin generation task
        val genKotlinTask = project.tasks.register(generateKotlinFromProtelisTaskName) {
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

        // Configure Dokka V2 extension
        val dokkaExtension = project.extensions.getByType(DokkaExtension::class.java)

        project.afterEvaluate {
            dokkaExtension.apply {
                // Configure the module name
                moduleName.set("protelisdoc")

                // Configure source sets
                dokkaSourceSets.create("protelisdoc") { sourceSet ->
                    sourceSet.sourceRoots.from(extension.kotlinDestDir.get())
                    val resolvedConfiguration = config.resolvedConfiguration
                    if (resolvedConfiguration.hasError()) {
                        kotlin.runCatching { resolvedConfiguration.rethrowFailure() }.onFailure {
                            project.logger.warn("ProtelisDoc failed dependency resolution!", it)
                        }
                    } else {
                        sourceSet.classpath.from(resolvedConfiguration.resolvedArtifacts.map { it.file })
                    }
                }

                // Configure the default HTML publication
                dokkaPublications.named("html") { publication ->
                    publication.outputDirectory.set(project.file(extension.destDir.get()))
                }
            }

            // Add javadoc plugin dependency to the dokkaPlugin configuration
            project.dependencies.add(
                "dokkaPlugin",
                "org.jetbrains.dokka:javadoc-plugin:${DokkaVersion.version}",
            )

            // Create a wrapper task that depends on the generated Dokka task
            val dokkaGenerateTask = project.tasks.named("dokkaGeneratePublicationHtml")
            dokkaGenerateTask.configure { task ->
                task.dependsOn(genKotlinTask)
            }

            // Register the protelisdoc task as an alias
            project.tasks.register(protelisDocTaskName) { task ->
                task.dependsOn(dokkaGenerateTask)
            }
        }
    }
}

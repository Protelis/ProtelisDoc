package it.unibo.protelis2kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property

inline fun <reified T> Project.propertyWithDefault(default: T): Property<T> =
        objects.property(T::class.java).apply { convention(default) }

inline fun <reified T> Project.propertyWithDefault(noinline default: () -> T): Property<T> =
    objects.property(T::class.java).apply { convention(default()) }

/**
 *  Extension for the Protelis2Kotlin plugin.
 *  @param baseDir The base directory from which looking for Protelis files
 *  @param destDir The directory that will contain the generated Kotlin code
 */
open class Protelis2KotlinPluginExtension @JvmOverloads constructor(
    private val project: Project,
    val baseDir: Property<String> = project.propertyWithDefault("."),
    val destDir: Property<String> = project.propertyWithDefault("."),
    val debug: Property<Boolean> = project.propertyWithDefault(false)
)

/**
 * Protelis2Kotlin Gradle Plugin: generates Kotlin code (interface) from Protelis code.
 */
class Protelis2KotlinPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("Protelis2Kotlin", Protelis2KotlinPluginExtension::class.java, project)
        project.task("generateKotlinFromProtelis") {
            it.inputs.files(extension.baseDir.get())
            it.doLast {
                main(arrayOf(extension.baseDir.get(), extension.destDir.get(), if (extension.debug.get()) "1" else "0"))
            }
            it.outputs.files(project.fileTree(extension.destDir.get()))
        }
    }
}
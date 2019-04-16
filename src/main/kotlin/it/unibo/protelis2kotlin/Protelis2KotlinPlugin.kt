package it.unibo.protelis2kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project

open class Protelis2KotlinPluginExtension {
    var baseDir: String = ""
    var destDir: String = ""
}

class Protelis2KotlinPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("Protelis2Kotlin", Protelis2KotlinPluginExtension::class.java)
        project.task("generate") {
            main(arrayOf(extension.baseDir, extension.destDir))
        }
    }
}
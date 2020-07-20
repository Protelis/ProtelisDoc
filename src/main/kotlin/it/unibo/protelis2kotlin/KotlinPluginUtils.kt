package it.unibo.protelis2kotlin

import org.gradle.api.Project
import org.gradle.api.provider.Property

inline fun <reified T> Project.propertyWithDefault(default: T): Property<T> =
    objects.property(T::class.java).apply { convention(default) }

inline fun <reified T> Project.propertyWithDefault(noinline default: () -> T): Property<T> =
    objects.property(T::class.java).apply { convention(default()) }

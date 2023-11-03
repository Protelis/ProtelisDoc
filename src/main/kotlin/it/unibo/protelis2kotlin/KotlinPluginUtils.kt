package it.unibo.protelis2kotlin

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider

internal inline fun <reified T> Project.propertyWithDefault(default: T): Property<T> =
    objects.property(T::class.java).apply { convention(default) }

internal inline fun <reified T> Project.propertyWithDefault(noinline default: () -> T): Property<T> =
    objects.property(T::class.java).apply { convention(default()) }

internal inline fun <reified T> Project.propertyWithDefault(default: Provider<T>): Property<T> =
    objects.property(T::class.java).apply { set(default) }

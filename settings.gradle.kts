import de.fayard.dependencies.bootstrapRefreshVersionsAndDependencies
import org.danilopianini.VersionAliases.justAdditionalAliases

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    dependencies {
        classpath("de.fayard:dependencies:+")
        classpath("org.danilopianini:refreshversions-aliases:+")
    }
}

bootstrapRefreshVersionsAndDependencies(justAdditionalAliases)

rootProject.name = "protelis-kdoc-generator"

plugins {
    id("com.gradle.enterprise") version "3.2"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}

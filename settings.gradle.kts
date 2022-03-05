plugins {
    id("com.gradle.enterprise") version "3.6.3"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.0.4"
}

rootProject.name = "protelisdoc"

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        publishOnFailure()
    }
}

gitHooks {
    commitMsg { conventionalCommits() }
    preCommit {
        tasks("ktlintCheck")
    }
    createHooks()
}

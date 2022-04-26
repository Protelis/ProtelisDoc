plugins {
    id("com.gradle.enterprise") version "3.10"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "1.0.10"
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

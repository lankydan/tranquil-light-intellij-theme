import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    // Java support
    id("java")
    // Kotlin support
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
    // gradle-intellij-plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
    id("org.jetbrains.intellij.platform") version "2.2.0"
    // gradle-changelog-plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
    id("org.jetbrains.changelog") version "1.3.1"
    // detekt linter - read more: https://detekt.github.io/detekt/gradle.html
    id("io.gitlab.arturbosch.detekt").version("1.23.7")
    // ktlint linter - read more: https://github.com/JLLeitschuh/ktlint-gradle
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    // dependency checker
    id("com.github.ben-manes.versions") version "0.42.0"
}

// Import variables from gradle.properties file
val pluginGroup: String by project
// `pluginName_` variable ends with `_` because of the collision with Kotlin magic getter in the `intellij` closure.
// Read more about the issue: https://github.com/JetBrains/intellij-platform-plugin-template/issues/29
val pluginName_: String by project
val pluginVersion: String by project
val pluginSinceBuild: String by project
val pluginUntilBuild: String by project
val pluginVerifierIdeVersions: String by project

val platformType: String by project
val platformVersion: String by project
val platformPlugins: String by project
val platformDownloadSources: String by project

group = pluginGroup
version = pluginVersion

// Configure project's dependencies
repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.14.2")
    intellijPlatform {
        create(platformType, platformVersion)
        plugins(providers.gradleProperty("platformPlugins").map { it.split(',') })
    }
}

// Configure gradle-intellij-plugin plugin.
// Read more: https://github.com/JetBrains/gradle-intellij-plugin
intellijPlatform {
    pluginConfiguration {
        name = pluginName_
        version = pluginVersion
        ideaVersion {
            sinceBuild = pluginSinceBuild
            untilBuild = pluginUntilBuild
        }
        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = provider {
            File("./README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"

                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        }
        // Get the latest available change notes from the changelog file
        changeNotes = provider { changelog.getLatest().toHTML() }
    }

    publishing {
        token = System.getenv("PUBLISH_TOKEN")
        // pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://jetbrains.org/intellij/sdk/docs/tutorials/build_system/deployment.html#specifying-a-release-channel
        channels = listOf(pluginVersion.split('-').getOrElse(1) { "default" }.split('.').first())
    }

    pluginVerification {
        ides {
            ides(pluginVerifierIdeVersions.split(","))
        }
        freeArgs = listOf("-mute", "TemplateWordInPluginName")
    }

//    downloadSources.set(platformDownloadSources.toBoolean())
}

// Configure detekt plugin.
// Read more: https://detekt.github.io/detekt/kotlindsl.html
detekt {
    config.setFrom("./detekt-config.yml")
    buildUponDefaultConfig = true
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(false)
        xml.required.set(false)
        txt.required.set(false)
    }
}

tasks.named<KotlinJvmCompile>("compileKotlin") {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks {
    // Set the compatibility versions to 17
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }

    withType<Detekt> {
        jvmTarget = "17"
    }
}

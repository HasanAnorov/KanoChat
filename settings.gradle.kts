pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Jitpack
        maven {
            setUrl("https://jitpack.io")
            content {
                includeGroup("com.github.pyamsoft.pydroid")
                includeGroup("com.github.pyamsoft")
            }
        }
    }
}

rootProject.name = "Andro Chat"
include(":app")
 
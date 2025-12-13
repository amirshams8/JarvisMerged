pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "JarvisMerged"

include(":app")
include(":modules:core")
include(":modules:automation")
include(":modules:smart")
include(":modules:ui")
include(":modules:engine")

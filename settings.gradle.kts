pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "JarvisMerged"

include(":app")
include(":modules:core")
include(":modules:engine")
include(":modules:automation")
include(":modules:smart")
include(":modules:ui")

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "ViewBindingOwner"
include(":viewbindingowner")
include(":viewbindingowner-fragment")
include(":viewbindingowner-activity")

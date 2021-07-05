import com.android.build.api.dsl.LibraryExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import com.vanniktech.maven.publish.MavenPublishPluginExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0-beta05")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.32")
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.15.1")
    }
}

plugins.apply("org.jetbrains.dokka")

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.withType<DokkaMultiModuleTask>().configureEach {
    outputDirectory.set(rootProject.file("docs/api"))
    failOnWarning.set(true)
}

subprojects {
    afterEvaluate {
        configureDefaultAndroid()
    }
    // Must be afterEvaluate or else com.vanniktech.maven.publish will overwrite our
    // dokka and version configuration.
    afterEvaluate {
        if (tasks.findByName("dokkaHtmlPartial") == null) {
            // If dokka isn't enabled on this module, skip
            return@afterEvaluate
        }

        tasks.named<DokkaTaskPartial>("dokkaHtmlPartial").configure {
            dokkaSourceSets.configureEach {
                reportUndocumented.set(true)
                skipEmptyPackages.set(true)
                skipDeprecated.set(true)
                jdkVersion.set(8)

                // Add Android SDK packages
                noAndroidSdkLink.set(false)

                // AndroidX docs
                externalDocumentationLink {
                    url.set(java.net.URL("https://developer.android.com/reference/"))
                    packageListUrl.set(java.net.URL("https://developer.android.com/reference/androidx/package-list"))
                }
                externalDocumentationLink {
                    url.set(java.net.URL("https://developer.android.com/reference/kotlin/"))
                    packageListUrl.set(java.net.URL("https://developer.android.com/reference/kotlin/androidx/package-list"))
                }
            }
        }
    }
}

allprojects {
    // Always publish to the new Sonatype S01 server
    plugins.withType<MavenPublishPlugin> {
        extensions.configure<MavenPublishPluginExtension> {
            sonatypeHost = SonatypeHost.S01
        }
    }
}

fun Project.configureDefaultAndroid() {
    extensions.configure<LibraryExtension>("android") {
        compileSdk = 30
        buildToolsVersion = "30.0.3"
        defaultConfig {
            minSdk = 21
            targetSdk = 30

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }
        buildFeatures {
            viewBinding = true
        }
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
            // Export shared module to Xcode as a framework
            binaryOption("bundleId", "com.ytmd.mobile.shared")
        }
    }

    // Register a task to copy the framework to a known location for Xcode
    val xcframeworkDir = project.layout.buildDirectory.dir("xcode-frameworks")
    tasks.register("buildXcodeFramework") {
        dependsOn("linkDebugFrameworkIosSimulatorArm64")
        doLast {
            val from = layout.buildDirectory
                .dir("bin/iosSimulatorArm64/debugFramework").get().asFile
            val to = xcframeworkDir.get().asFile
            to.mkdirs()
            from.copyRecursively(to, overwrite = true)
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // SQLDelight
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)

            // Koin
            implementation(libs.koin.core)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.kotlinx.coroutines.android)

            // Media3
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.session)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.native.driver)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "com.ytmd.mobile.shared"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("YtmdDatabase") {
            packageName.set("com.ytmd.mobile.data.local")
        }
    }
}

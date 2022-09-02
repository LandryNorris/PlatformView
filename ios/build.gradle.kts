import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "me.landrynorris"
version = "1.0"

kotlin {
    val iosTargets = listOf(iosX64("uikitX64"), iosArm64("uikitArm64"))

    iosTargets.forEach {
        it.binaries {
            executable {
                entryPoint = "main"
                freeCompilerArgs += listOf(
                    "-linker-option", "-framework", "-linker-option", "Metal",
                    "-linker-option", "-framework", "-linker-option", "CoreText",
                    "-linker-option", "-framework", "-linker-option", "CoreGraphics"
                )
            }
        }
    }
    sourceSets {

        val iosMain by creating {
            dependencies {
                implementation(project(":common"))
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)
            }
        }
        val iosTest by creating

        val uikitX64Main by getting { dependsOn(iosMain) }
        val uikitArm64Main by getting { dependsOn(iosMain) }

    }
}

kotlin {
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.all {
            // TODO: the current compose binary surprises LLVM, so disable checks for now.
            freeCompilerArgs += "-Xdisable-phases=VerifyBitcode"
        }
    }
}

compose.experimental {
    web.application {}
    uikit.application {
        bundleIdPrefix = "io.github.landrynorris"
        projectName = "PlatformViewExample"
        deployConfigurations {
            simulator("IPhone11") {
                //Usage: ./gradlew iosDeployIPhone11Debug
                device = org.jetbrains.compose.experimental.dsl.IOSDevices.IPHONE_11
            }
            simulator("IPad") {
                //Usage: ./gradlew iosDeployIPadDebug
                device = org.jetbrains.compose.experimental.dsl.IOSDevices.IPAD_MINI_6th_Gen
            }
        }
    }
}
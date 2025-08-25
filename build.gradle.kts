plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
}

version = "0.0.1"

val executableName = "gates-opener-server-ktor"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

kotlin {
    macosArm64 {
        binaries.executable {
            entryPoint = "main"
            baseName = executableName
        }
    }

    linuxArm64 {
        binaries.executable {
            entryPoint = "main"
            baseName = executableName
        }
    }

    linuxX64 {
        binaries.executable {
            entryPoint = "main"
            baseName = executableName
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.cio)
            implementation(libs.ktor.server.auth)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
        }

        nativeTest.dependencies {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.server.test.host)
            }
        }
    }
}

tasks.register("linkLinuxBinaries") {
    group = "build"
    description = "Links all binaries for linux targets"

    val linuxArm64LinkTask = tasks.named("linkReleaseExecutableLinuxArm64")
    val linuxX64LinkTask = tasks.named("linkReleaseExecutableLinuxX64")

    dependsOn(linuxArm64LinkTask, linuxX64LinkTask)

    doLast {
        val outputDir = file("$projectDir/gatesopener/bin")
        outputDir.mkdirs()

        // Paths to the executables
        val linuxArm64Executable = file("build/bin/linuxArm64/releaseExecutable/$executableName.kexe")
        val linuxX64Executable = file("build/bin/linuxX64/releaseExecutable/$executableName.kexe")

        // Copy executables to the output directory
        if (linuxArm64Executable.exists()) {
            copy {
                from(linuxArm64Executable)
                into(outputDir)
                rename { "$executableName-aarch64" }
            }
        } else {
            println("Executable not found: $linuxArm64Executable")
        }

        if (linuxX64Executable.exists()) {
            copy {
                from(linuxX64Executable)
                into(outputDir)
                rename { "$executableName-amd64" }
            }
        } else {
            println("Executable not found: $linuxX64Executable")
        }
    }
}
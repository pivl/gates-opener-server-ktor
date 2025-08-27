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
    // Только x86_64 target для Docker сборки
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
            implementation(libs.ktor.server.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.content.negotiation)
        }
        
        nativeMain.dependencies {
            implementation(libs.ktor.client.curl)
        }

        nativeTest.dependencies {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.ktor.server.test.host)
            }
        }
    }
}

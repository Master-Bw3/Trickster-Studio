plugins {
    val kotlinVersion: String by System.getProperties()
    kotlin("plugin.serialization") version kotlinVersion
    kotlin("multiplatform") version kotlinVersion
    val kvisionVersion: String by System.getProperties()
    id("io.kvision") version kvisionVersion
}

version = "1.0.0-SNAPSHOT"
group = "me.maplesyrum"

repositories {
    mavenCentral()
    mavenLocal()
    flatDir {
        dirs("KEndec/build/libs")
    }
}

// Versions
val kvisionVersion: String by System.getProperties()

kotlin {
    js(IR) {
        browser {
            useEsModules()
            commonWebpackConfig {
                outputFileName = "main.bundle.js"
                sourceMaps = false
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
        compilerOptions {
            target.set("es2015")
        }
    }
    sourceSets["jsMain"].dependencies {
        implementation("io.kvision:kvision:$kvisionVersion")
        implementation("io.kvision:kvision-tailwindcss:$kvisionVersion")
        implementation("io.kvision:kvision-i18n:$kvisionVersion")
        implementation("io.kvision:kvision-state:$kvisionVersion")
        implementation(npm("pixi.js", "8.6.6"))
        implementation(files("KEndec/build/libs/KEndec-js-1.0-SNAPSHOT.klib"))
        implementation(("com.benasher44:uuid:0.8.4"))
        implementation("com.squareup.okio:okio:3.10.2")
    }
    sourceSets["jsTest"].dependencies {
        implementation(kotlin("test-js"))
        implementation("io.kvision:kvision-testutils:$kvisionVersion")
    }
}

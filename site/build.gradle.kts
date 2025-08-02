import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec
import kotlin.jvm.java

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kobweb.application)
//    alias(libs.plugins.kobwebx.markdown)
}

group = "me.maplesyrum.tricksterStudio"
version = "1.0-SNAPSHOT"

kobweb {
    app {
        index {
            description.set("Powered by Kobweb")
        }
    }
}

kotlin {
    // This example is frontend only. However, for a fullstack app, you can uncomment the includeServer parameter
    // and the `jvmMain` source set below.
    configAsKobwebApplication("tricksterStudio" /*, includeServer = true*/)

    sourceSets {
//        commonMain.dependencies {
//          // Add shared dependencies between JS and JVM here if building a fullstack app
//        }


        jsMain.dependencies {
            implementation(project.dependencies.platform(libs.kotlin.wrappers.bom))
            implementation(libs.compose.runtime)
            implementation(libs.compose.html.core)
            implementation(libs.kobweb.core)
            implementation(libs.kobweb.silk)
            // This default template uses built-in SVG icons, but what's available is limited.
            // Uncomment the following if you want access to a large set of font-awesome icons:
            // implementation(libs.silk.icons.fa)
            implementation(libs.kobwebx.markdown)
            implementation(libs.kendec)
            implementation(npm("pixi.js", "8.6.6"))
            implementation(("com.benasher44:uuid:0.8.4"))
            implementation("com.squareup.okio:okio:3.10.2")
            implementation(npm("pako", "2.1.0"))
            implementation("org.jetbrains.kotlin-wrappers:kotlin-browser")
            implementation(libs.kotlin.coroutines.core)
        }

        // Uncomment the following if you pass `includeServer = true` into the `configAsKobwebApplication` call.
//        jvmMain.dependencies {
//            compileOnly(libs.kobweb.api) // Provided by Kobweb backend at runtime
//        }
    }
}


tasks.withType<KotlinJsCompile>().configureEach {
    compilerOptions {
        moduleKind.set(JsModuleKind.MODULE_ES)
        useEsClasses.set(true)
    }
}
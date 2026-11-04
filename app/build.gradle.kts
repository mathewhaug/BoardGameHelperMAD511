import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    // ksp runs Room's annotation processor, and the room3 plugin is what wires schemaDirectory into it
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room3)
    // hilt generates the component code at build time, through the same ksp that Room already uses
    alias(libs.plugins.hilt)
}

// local.properties is gitignored, so each teammate keeps their own BGG token in it and it never
// lands in a commit. a missing file or a missing key just means an empty token, which the app
// treats as "use the sample data", so a fresh clone still builds and runs
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val bggToken: String = localProperties.getProperty("BGG_TOKEN", "")

android {
    namespace = "com.mathewhaug.boardgamehelper"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.mathewhaug.boardgamehelper"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BGG_TOKEN", "\"$bggToken\"")
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        // off by default since AGP 8, and BGG_TOKEN above needs it to exist
        buildConfig = true
    }
}

// every version of the schema gets written here as JSON. it is checked in so a migration can be
// tested against the exact shape a real device has, not against what we remember it looked like
room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // viewmodel-compose gives us viewModel(), runtime-compose gives us collectAsStateWithLifecycle,
    // and viewmodel-navigation3 is the bridge that scopes a ViewModel to a Nav3 back stack entry
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.kotlinx.serialization.core)
    // retrofit turns an interface into HTTP calls, and okhttp underneath it does the actual sending.
    // the BOM pins every okhttp artifact to one version so logging-interceptor needs no version of
    // its own and cannot drift away from the okhttp that retrofit brought in
    implementation(libs.retrofit)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp.logging.interceptor)
    // BGG answers in XML, so these two are not used by it. they are the usual setup for a JSON API
    // and sit here so the wiring is there to look at
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    // coil loads the thumbnails, and coil-network-okhttp is how it fetches them over http at all
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    // the runtime is what the app calls, the compiler only runs at build time to generate the DAO
    // and database classes, and the bundled driver ships its own SQLite so every device runs the
    // same version instead of whatever the phone happens to have
    implementation(libs.androidx.room3.runtime)
    implementation(libs.androidx.sqlite.bundled)
    ksp(libs.androidx.room3.compiler)
    // hilt-android is the runtime, the compiler writes the graph, and viewmodel-compose gives us
    // hiltViewModel() without pulling in Navigation 2 the way hilt-navigation-compose would
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.viewmodel.compose)
    testImplementation(libs.junit)
    // runTest and StandardTestDispatcher, so a ViewModel that launches coroutines can be driven
    // from a plain JVM test with no emulator involved
    testImplementation(libs.kotlinx.coroutines.test)
    // on the JVM the android.jar every unit test sees is only stubs, and XmlPullParserFactory
    // throws "not mocked". kxml2 is the parser Android itself ships, so the tests run the same
    // pull parser API on a real implementation
    testImplementation(libs.kxml2)
    // a real HTTP server on localhost, so a test can see the exact request our client sends. no
    // version here, the okhttp BOM above pins it to the same 4.12.0 as the client it is testing
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

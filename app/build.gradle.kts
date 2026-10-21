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
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

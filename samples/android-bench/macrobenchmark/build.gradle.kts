plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.sanamhub.nepdate.sample.bench.macro"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    androidTestImplementation(libs.benchmark.macro.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}

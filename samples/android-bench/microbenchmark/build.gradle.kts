plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "io.github.sanamhub.nepdate.sample.bench.micro"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        testInstrumentationRunner = "androidx.benchmark.junit4.BenchmarkRunner"
    }
}

dependencies {
    implementation("io.github.sanamhub:nepdate")
    implementation("io.github.sanamhub:nepdate-patro")
    androidTestImplementation(libs.benchmark.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
}

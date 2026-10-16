plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.sanamhub.nepdate.sample.bench"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.sanamhub.nepdate.sample.bench"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("io.github.sanamhub:nepdate")
    implementation("io.github.sanamhub:nepdate-patro")
}

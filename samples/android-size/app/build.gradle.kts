plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "io.github.sanamhub.nepdate.sample.size"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.sanamhub.nepdate.sample.size"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions += "library"
    productFlavors {
        create("bare") {
            dimension = "library"
        }
        create("core") {
            dimension = "library"
        }
        create("patro") {
            dimension = "library"
        }
    }
}

dependencies {
    // Each flavor references exactly the artifact it measures; R8 keeps only what the flavor calls.
    "coreImplementation"("io.github.sanamhub:nepdate")
    "patroImplementation"("io.github.sanamhub:nepdate-patro")
}

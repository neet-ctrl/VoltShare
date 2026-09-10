@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.twofasAndroidLibrary)
    alias(libs.plugins.twofasCompose)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.twofasapp.feature.secrets"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":data:services"))
    implementation(libs.core)
    implementation(project(":core:designsystem"))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.viewModel)
    implementation(libs.securityCrypto)
    implementation(libs.kotlinSerialization)
}
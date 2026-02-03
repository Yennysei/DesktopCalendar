plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            api(libs.slf4j.api)
            runtimeOnly(libs.log4j.slf4j.impl)
            runtimeOnly(libs.jackson.dataformat.yaml)
        }
    }
}

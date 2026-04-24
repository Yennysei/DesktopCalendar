plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":sources:common:di"))
        }

        jvmMain.dependencies {
            api(libs.exposed.core)
            api(libs.exposed.jdbc)
            implementation(libs.h2)
            implementation(libs.java.keyring)
        }
    }
}

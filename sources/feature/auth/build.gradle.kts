plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(libs.java.jwt)
            implementation(libs.java.keyring)
            implementation(libs.jwks.rsa)
            implementation(libs.kotlinx.serialization.core)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.server.call.logging)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)

            implementation(project(":sources:common:cryptography"))
            implementation(project(":sources:common:di"))
            implementation(project(":sources:core:database"))
            implementation(project(":sources:core:http-client"))

            runtimeOnly(project(":sources:common:logging"))
        }
    }
}

val generateBuildConfig by tasks.registering {
    val outputDir = project.layout.buildDirectory.dir("generated/buildConfig/src").get().asFile
    outputs.dir(outputDir) // Mark the output directory so Gradle can track it

    val packageName = "dev.yenny.calendar.generated"
    val directory = packageName.replace('.', '/')

    val clientIdKey = "cred.client.id"
    val clientSecretKey = "cred.client.secret"

    val clientId = project.ext.get(clientIdKey) as? String ?: ""
    val clientSecret = project.ext.get(clientSecretKey) as? String ?: ""

    inputs.property(clientIdKey, clientId)
    inputs.property(clientSecretKey, clientSecret)

    doLast {
        val generatedFile = File(outputDir, "$directory/Credentials.kt")
        generatedFile.parentFile.mkdirs()

        generatedFile.writeText(
            """
            package $packageName

            /**
             * Setup your credentials in gradle.properties
             */
            object Credentials {
                const val CLIENT_ID: String = "$clientId"
                const val CLIENT_SECRET: String = "$clientSecret"
            }
            """.trimIndent()
        )
    }
}

kotlin.sourceSets.getByName("commonMain")
    .kotlin.srcDir(
        generateBuildConfig.map { it.outputs.files.singleFile }
    )

tasks.named("build").configure {
    dependsOn(generateBuildConfig)
}

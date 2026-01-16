dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }

    versionCatalogs {
        fileTree("gradle") { include("*.libs.versions.toml") }.forEach {
            val catalogName = it.name.substringBefore(".")
            create(catalogName) {
                from(files(it))
            }
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

/**
 * 引用子模块
 */
fun installSubmodule(dir: String, parentNode: String? = null) {
    val fileDir = File(dir).takeIf { it.isAbsolute } ?: File(rootDir, dir)
    val buildFiles = listOf("build.gradle.kts", "build.gradle")
    val itemList = fileDir.listFiles { d: File ->
        if (d.name.startsWith(".") || d.isFile) return@listFiles false
        buildFiles.any { b -> File(d, b).exists() }
    } ?: return
    itemList.forEach {
        val parentName = parentNode ?: fileDir.name
        val projectName = ":$parentName:${it.name}"
        include(projectName)
        project(projectName).projectDir = it
    }
}

include(":app")
rootProject.name = "Catalog"
installSubmodule("catalog")
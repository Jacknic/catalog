/**
 * 库发布配置统一处理
 */
fun Project.mavenPublish() {
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/Jacknic/hmios-core")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
        publications {
            create<MavenPublication>("main") {
                val groupIdBuilder = StringBuilder(libs.catalog.get().group)
                groupId = groupIdBuilder.toString()
                var targetComponent: SoftwareComponent? = components.findByName("release")
                if (targetComponent == null) {
                    targetComponent = components.first {
                        it.name in listOf("java", "versionCatalog")
                    }
                    tasks.firstOrNull {
                        it.name in listOf("releaseSourcesJar", "kotlinSourcesJar")
                    }?.let { artifact(it) }
                }
                from(targetComponent!!)
                pom {
                    version = version
                    description.set(project.description)
                }
            }
        }
    }
}


subprojects {
    pluginManager.withPlugin("maven-publish") {
        afterEvaluate { mavenPublish() }
    }

    val tomlFile: File = rootProject.file("gradle/${project.name}.libs.versions.toml")

    gradle.projectsEvaluated {
        extensions.findByType<CatalogPluginExtension>()?.apply {
            versionCatalog {
                from(files(tomlFile))
            }
        }
        // 手动拷贝替换为原始 toml 文件
        tasks.findByName("generateCatalogAsToml")?.doLast {
            val targetFile = file("build/version-catalog/libs.versions.toml")
            tomlFile.copyTo(targetFile, true)
        }
    }
}
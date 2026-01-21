import org.gradle.internal.extensions.stdlib.capitalized

plugins {
    alias(libs.plugins.publish)
}

/**
 * 库发布配置统一处理
 */
fun Project.mavenPublish() {
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/Jacknic/catalog")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
            maven {
                name = "CNB"
                url = uri("https://maven.cnb.cool/jacknic/maven-public/-/packages/")
                credentials {
                    username = "cnb"
                    password = System.getenv("CNB_TOKEN")
                }
            }
        }
    }
}


subprojects {
    pluginManager.withPlugin("maven-publish") {
        version = libs.versions.version.get()
        afterEvaluate {
            /**
             * 属性配置 [com.vanniktech.maven.publish.MavenPublishBaseExtension.pomFromGradleProperties]
             */
            val pomName = name.capitalized()
            project.ext.set("POM_NAME", pomName)
            project.ext.set("POM_DESCRIPTION", "$pomName Catalog")
            pluginManager.apply(libs.plugins.publish.get().pluginId)
            mavenPublish()
        }
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
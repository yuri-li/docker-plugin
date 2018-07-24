package org.authx.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.task

open class DockerPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = project.run {
        val extension = extensions.create("dockerfile", DockerfileExtension::class.java, project)

        task("build-dockerfile", DockerfileTask::class) {
            group = "docker"

            doFirst {
                localDir = extension.localDir
                dockerfiles = extension.dockerfiles
            }
            doLast {
                dockerfiles.forEach { dockerfile ->
                    println("""
                        host: ${dockerfile.host}
                        username: ${dockerfile.username}
                        password: ${dockerfile.password}
                        serverDir: ${dockerfile.serverDir}
                    """.trimIndent())
                }
            }
        }
        task("push-dockerfile", PushDockerfileTask::class) {
            group = "docker"

            doFirst {
                localDir = extension.localDir
                dockerfiles = extension.dockerfiles
            }

        }
    }
}
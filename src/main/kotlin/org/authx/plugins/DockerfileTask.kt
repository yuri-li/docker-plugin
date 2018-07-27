package org.authx.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DockerfileTask : DefaultTask() {
    lateinit var localDir: String
    lateinit var dockerfiles: NamedDomainObjectContainer<Dockerfile>

    @TaskAction
    fun run(): Unit {
        val directory = File(localDir)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        dockerfiles.forEach { dockerfile ->
            File(localDir,"${dockerfile.name}-Dockerfile").writeText(dockerfile.context.trimIndent())
        }
    }
}

open class DockerfileExtension(val project: Project) {
    var localDir: String = "${project.buildDir}/docker"

    val dockerfiles: NamedDomainObjectContainer<Dockerfile> = project.container(Dockerfile::class.java)
}

open class Dockerfile(val name: String) {
    lateinit var host: String
    lateinit var username: String
    lateinit var password: String
    var serverDir: String = ""
        set(value) {
            field = if (!value.startsWith("/")) {
                throw IllegalArgumentException("serverDir:${value}。请使用绝对路径")
            } else if (!value.endsWith("/")) {
                "${value}/"
            } else {
                value
            }
        }
    lateinit var context: String
}
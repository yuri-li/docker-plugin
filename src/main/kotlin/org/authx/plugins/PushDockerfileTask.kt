package org.authx.plugins

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskAction
import java.io.File

open class PushDockerfileTask : DefaultTask() {
    lateinit var localDir: String
    lateinit var dockerfiles: NamedDomainObjectContainer<Dockerfile>

    @TaskAction
    fun run(): Unit {
        val directory = File(localDir)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val dockerfile = dockerfiles.first()
        var serverDir = dockerfile.serverDir
        if (!serverDir.startsWith("/")) {
            throw IllegalArgumentException("serverDir:${serverDir}。请使用绝对路径")
        }
        if (!serverDir.endsWith("/")) {
            serverDir = "${serverDir}/"
        }

        val jsch = JSch()
        val session = jsch.getSession(dockerfile.username, dockerfile.host)
        session.setPassword(dockerfile.password)
        val config = java.util.Properties()
        config.put("StrictHostKeyChecking", "no")
        session.setConfig(config)
        session.connect()
        val channel = session.openChannel("sftp") as ChannelSftp
        channel.connect()

        try {
            channel.stat(serverDir)
            channel.rm("${serverDir}*")
        } catch (e: Exception) {
            channel.mkdir(serverDir)
        }
        channel.put("${localDir}/${dockerfile.name}-Dockerfile", "${serverDir}/Dockerfile")
        channel.disconnect()
        session.disconnect()
    }
}

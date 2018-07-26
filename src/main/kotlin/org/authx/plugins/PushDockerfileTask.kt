package org.authx.plugins

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.*

open class PushDockerfileTask : DefaultTask() {
    lateinit var localDir: String
    lateinit var dockerfiles: NamedDomainObjectContainer<Dockerfile>

    @TaskAction
    fun run(): Unit {
        val directory = File(localDir)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        dockerfiles.forEach { dockerfile ->
            val serverDir = dockerfile.serverDir

            val session = buildSession(dockerfile)
            val sftp = buildSftpChannel(session)

            cleanServerDir(sftp, serverDir)
            sftp.put("${localDir}/${dockerfile.name}-Dockerfile", "${serverDir}/Dockerfile")

            sftp.disconnect()
            session.disconnect()
        }
    }

    private fun cleanServerDir(sftp: ChannelSftp, serverDir: String) {
        try {
            sftp.stat(serverDir)
            sftp.rm("${serverDir}*")
        } catch (e: Exception) {
            sftp.mkdir(serverDir)
        }
    }

    private fun buildSession(dockerfile: Dockerfile): Session {
        val jsch = JSch()
        println("尝试连接服务器${dockerfile.name},参数：[username= ${dockerfile.username} password = ${dockerfile.password} host = ${dockerfile.host}]")
        val session = jsch.getSession(dockerfile.username, dockerfile.host)
        session.setPassword(dockerfile.password)
        val config = Properties()
        config.put("StrictHostKeyChecking", "no")
        session.setConfig(config)
        session.connect()
        println("成功连接服务器${dockerfile.name}")
        return session
    }

    private fun buildSftpChannel(session: Session): ChannelSftp {
        val channel = session.openChannel("sftp") as ChannelSftp
        channel.connect()
        return channel
    }
}

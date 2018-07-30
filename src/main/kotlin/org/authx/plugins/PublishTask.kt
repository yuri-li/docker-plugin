package org.authx.plugins

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskAction
import java.io.BufferedReader
import java.io.File
import java.util.*

open class PublishTask : DefaultTask() {
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
            sftp.put("${localDir}/${dockerfile.name}-Dockerfile", "${serverDir}Dockerfile")
            sftp.put("${project.buildDir}/libs/${project.name}-${project.version}.jar", serverDir)
            sftp.disconnect()

            buildDockerContainer(session, serverDir)
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

    private fun buildDockerContainer(session: Session, serverDir: String) {
        execCommand(session,
                "docker stop ${project.name}", //停止容器
                "docker rm ${project.name}", //删除容器
                "docker image rm $(docker image ls --filter=reference='springboot-kotlin:*' -q)", //删除镜像
                "cd ${serverDir} && docker image build --no-cache -t ${project.name}:${project.version} ./ && docker image ls --filter name=${project.name}",
                "docker run -d --name ${project.name} -p 8080:8080 --mount source=app,target=/app ${project.name}:${project.version}")
    }

    private fun execCommand(session: Session, vararg commands: String) {
        commands.forEach {
            val channel = session.openChannel("exec") as ChannelExec
            channel.setCommand(it)
            channel.connect()
            println(channel.inputStream.bufferedReader().use(BufferedReader::readText))
            channel.disconnect()
        }
    }
}

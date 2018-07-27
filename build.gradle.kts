plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "org.authx.plugins"
version = "0.0.1"

task<Wrapper>("local-wrapper") { //修改gradle的版本
    gradleVersion = "4.9"
}
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    compile("com.jcraft:jsch:0.1.54")
}

gradlePlugin{
    (plugins) {
        "docker-plugin" {
            id = "org.authx.plugins.docker-plugin"
            implementationClass = "org.authx.plugins.DockerPlugin"
        }
    }
}
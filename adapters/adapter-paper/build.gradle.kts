plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.4.2"
}

dependencies {
    implementation(project(":pyjama-api"))
    implementation(project(":pyjama-core"))
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.+")
}

val platformId = "paper"

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "platformId" to platformId
    )
    inputs.properties(props)
    filesMatching(listOf("plugin.yml", "pyjama-platform.properties")) {
        expand(props)
    }
}

tasks.shadowJar {
    archiveBaseName.set("${rootProject.name}-$platformId")
    archiveClassifier.set("")
}

tasks.jar {
    archiveClassifier.set("plain")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

val defaultServerPluginsDir = rootProject.layout.projectDirectory.dir("run/plugins").asFile.absolutePath
val serverPluginsDir: String = (project.findProperty("pyjamaServerPluginsDir") as String?) ?: defaultServerPluginsDir

tasks.register<Copy>("copyPluginJar") {
    group = "pyjama"
    description = "Copies the shadow jar into the minecraft server"
    dependsOn(tasks.shadowJar)
    from(tasks.shadowJar.flatMap { it.archiveFile })
    into(serverPluginsDir)
}

tasks.shadowJar {
    finalizedBy("copyPluginJar")
}
plugins {
    `java-library`
}

dependencies {
    api(project(":pyjama-api"))
    implementation("org.yaml:snakeyaml:2.3")
    api("net.kyori:adventure-api:4.17.0")
    api("net.kyori:adventure-text-minimessage:4.17.0")
    compileOnly("io.github.miniplaceholders:miniplaceholders-api:3.2.0")
}

tasks.processResources {
    val props = mapOf(
        "version" to project.version,
        "projectName" to rootProject.name
    )
    inputs.properties(props)
    filesMatching("pyjama-build.properties") {
        expand(props)
    }
}
plugins {
    `java-library`
}

dependencies {
    api(project(":pyjama-api"))
    implementation("org.yaml:snakeyaml:2.3")
    api("net.kyori:adventure-api:4.17.0")
    api("net.kyori:adventure-text-minimessage:4.17.0")
}
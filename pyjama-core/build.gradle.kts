plugins {
    `java-library`
}

dependencies {
    api(project(":pyjama-api"))
    implementation("org.yaml:snakeyaml:2.3")
}
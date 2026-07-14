rootProject.name = "pyjama"

include("pyjama-api")
include("pyjama-core")
include("adapters:adapter-paper")

project(":adapters:adapter-paper").projectDir = file("adapters/adapter-paper")
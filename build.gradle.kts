plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.0.0"
}

group = "com.kruthers"
version = "1.1.2"

repositories {
    mavenCentral()
    maven { url = uri("https://libraries.minecraft.net/") }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    compileOnly(kotlin("stdlib"))

    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0") {isTransitive = false}

    val cloudVersion = "1.8.4"
    compileOnly("cloud.commandframework:cloud-core:${cloudVersion}")
    compileOnly("cloud.commandframework:cloud-paper:${cloudVersion}")
    compileOnly("cloud.commandframework:cloud-minecraft-extras:${cloudVersion}")

    implementation("org.incendo.interfaces:interfaces-paper:1.0.0-SNAPSHOT")
    implementation("org.incendo.interfaces:interfaces-kotlin:1.0.0-SNAPSHOT")

    val adventureVersion = "4.15.0"
    compileOnly("net.kyori","adventure-api",adventureVersion)
    compileOnly("net.kyori","adventure-platform-bukkit","4.3.0")
    compileOnly("net.kyori","adventure-text-minimessage",adventureVersion)
    implementation("net.kyori","adventure-text-serializer-gson",adventureVersion)
}


tasks {
    shadowJar {
        archiveClassifier.set("")

//        dependencies {
//            exclude(dependency("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT"))
//            exclude(dependency("com.comphenix.protocol:ProtocolLib:4.7.0"))
//        }

        minimize()
    }
    build {
        dependsOn(shadowJar)
    }
    processResources {
        expand("name" to project.name, "description" to project.description, "version" to project.version)
    }
    runServer {
        minecraftVersion("1.20.1")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
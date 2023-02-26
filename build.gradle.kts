plugins {
    kotlin("jvm") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.kruthers"
version = "1.0.0"

repositories {
    mavenCentral()
    maven { url = uri("https://libraries.minecraft.net/") }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    implementation(kotlin("stdlib"))

    compileOnly("io.papermc.paper:paper-api:1.19.2-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0") {isTransitive = false}

    implementation("cloud.commandframework:cloud-core:1.7.1")
    implementation("cloud.commandframework:cloud-paper:1.7.1")
    implementation("cloud.commandframework:cloud-minecraft-extras:1.7.1")

    implementation("org.incendo.interfaces:interfaces-paper:1.0.0-SNAPSHOT")
    implementation("org.incendo.interfaces:interfaces-kotlin:1.0.0-SNAPSHOT")

    implementation("net.kyori:adventure-api:4.12.0")
    implementation("net.kyori:adventure-platform-bukkit:4.1.2")
    implementation("net.kyori:adventure-text-minimessage:4.12.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.12.0")
}


tasks {
    shadowJar {
        destinationDirectory.set(file("build"))
        archiveClassifier.set("")

        dependencies {
            exclude(dependency("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT"))
            exclude(dependency("com.comphenix.protocol:ProtocolLib:4.7.0"))
        }

        minimize()
    }
    build {
        dependsOn(shadowJar)
    }
    processResources {
        expand("name" to project.name, "description" to project.description, "version" to project.version)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}
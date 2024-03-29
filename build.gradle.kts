version = "1.0.0-SNAPSHOT"

plugins {
  kotlin("jvm") version "1.4.20"
  `java-library`
  kotlin("plugin.serialization") version "1.4.20"
  id("com.github.johnrengelman.shadow") version "6.1.0"
}

dependencies {
  implementation("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
  implementation("com.comphenix.protocol:ProtocolLib:4.6.0")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

repositories {
  jcenter()
  maven("https://hub.spigotmc.org/nexus/content/repositories/public")
  maven("https://repo.dmulloy2.net/repository/public")
}

tasks {
  build {
    dependsOn(shadowJar)
  }

  jar {
    enabled = false
  }

  shadowJar {
    archiveClassifier.set("")

    dependencies {
      exclude(dependency("org.spigotmc:spigot-api"))
      exclude(dependency("com.comphenix.protocol:ProtocolLib"))
    }
  }

  processResources {
    expand("version" to version)
  }
}

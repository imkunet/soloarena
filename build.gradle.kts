import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.kunet"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://repo.hpfxd.com/releases/")
    maven("https://jitpack.io/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("com.hpfxd.pandaspigot:pandaspigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")

    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.16.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.16.0")

    implementation("fr.mrmicky:fastboard:2.1.2")
    implementation("com.github.retrooper.packetevents:spigot:2.3.0")

    implementation("org.lz4:lz4-java:1.8.0")
}

kotlin {
    jvmToolchain(11)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    withType<ShadowJar> {
        exclude("META-INF/**")
        exclude("DebugProbesKt.bin")
    }
}

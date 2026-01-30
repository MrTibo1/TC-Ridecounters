plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.0"
}

group = "be.mrtibo"
version = "1.2"

val javaVersion: JavaLanguageVersion = JavaLanguageVersion.of(21)

repositories {
    mavenCentral()

    maven ("https://repo.papermc.io/repository/maven-public/")
    maven ("https://oss.sonatype.org/content/groups/public/")
    maven("https://ci.mg-dev.eu/plugin/repository/everything/")
}

dependencies {
    implementation(kotlin("stdlib"))

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    compileOnly("com.bergerkiller.bukkit:TrainCarts:1.21.11-v1-SNAPSHOT")
    compileOnly("com.bergerkiller.bukkit:BKCommonLib:1.21.11-v1-SNAPSHOT")

//    implementation("cloud.commandframework", "cloud-paper", "1.8.4")
    compileOnly("org.incendo:cloud-paper")
    compileOnly("org.incendo:cloud-annotations")
    compileOnly("org.incendo:cloud-minecraft-extras")
//    implementation("org.incendo:cloud-kotlin-coroutines-annotations:2.0.0")
//    implementation("org.incendo:cloud-kotlin-coroutines:2.0.0")

    implementation("com.zaxxer", "HikariCP", "7.0.2")

}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        val commonPrefix = "com.bergerkiller.bukkit.common.dep"
        relocate("org.incendo.cloud", "$commonPrefix.cloud")
        relocate("io.leangen.geantyref", "$commonPrefix.typetoken")
        relocate("me.lucko.commodore", "$commonPrefix.me.lucko.commodore")
//        relocate("net.kyori", "$commonPrefix.net.kyori")
    }

}

java {
    toolchain.languageVersion.set(javaVersion)
}
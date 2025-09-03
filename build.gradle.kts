plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "9.0.0"
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

    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    compileOnly("com.bergerkiller.bukkit:TrainCarts:1.21.8-v1-SNAPSHOT")
    compileOnly("com.bergerkiller.bukkit:BKCommonLib:1.21.8-v1-SNAPSHOT")

//    implementation("cloud.commandframework", "cloud-paper", "1.8.4")
    compileOnly("org.incendo:cloud-paper")
    compileOnly("org.incendo:cloud-annotations")
    compileOnly("org.incendo:cloud-minecraft-extras")

    implementation("com.zaxxer", "HikariCP", "7.0.2")

}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    compileKotlin {
        kotlinOptions.jvmTarget = javaVersion.toString()
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
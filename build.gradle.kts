plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.0"
}

group = "be.mrtibo"
version = "2.0-SNAPSHOT"

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

    implementation("org.incendo:cloud-paper:2.0.0+")
    implementation("org.incendo:cloud-annotations:2.0.0+")
    implementation("org.incendo", "cloud-minecraft-extras", "2.0.0-beta.10")
    implementation("org.incendo:cloud-kotlin-coroutines-annotations:2.0.0")
//    implementation("org.incendo:cloud-kotlin-coroutines:2.0.0")

    implementation("com.zaxxer", "HikariCP", "7.0.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    implementation("org.mariadb.jdbc:mariadb-java-client:3.5.7")
    implementation("org.bstats:bstats-bukkit:3.1.0")

}

tasks {
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        relocate("kotlinx.coroutines", "be.mrtibo.lib.kotlinx.coroutines")
        relocate("org.incendo.cloud", "be.mrtibo.lib.cloud")
        relocate("org.bstats", "be.mrtibo.lib.bstats")

        val commonPrefix = "com.bergerkiller.bukkit.common.dep"
        relocate("io.leangen.geantyref", "$commonPrefix.typetoken")
        relocate("me.lucko.commodore", "$commonPrefix.me.lucko.commodore")
//        relocate("net.kyori", "$commonPrefix.net.kyori")
    }

}

java {
    toolchain.languageVersion.set(javaVersion)
}
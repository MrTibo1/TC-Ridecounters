plugins {
    kotlin("jvm") version "1.9.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "be.mrtibo"
version = "1.0"

val javaVersion: JavaLanguageVersion = JavaLanguageVersion.of(17)

repositories {
    mavenCentral()

    maven ("https://repo.papermc.io/repository/maven-public/")
    maven ("https://oss.sonatype.org/content/groups/public/")
    maven("https://ci.mg-dev.eu/plugin/repository/everything/")
}

dependencies {
    implementation(kotlin("stdlib"))

    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")

    compileOnly("com.bergerkiller.bukkit:TrainCarts:1.20.4-v2-SNAPSHOT")
//    compileOnly("com.bergerkiller.bukkit:TCCoasters:1.20.4-v2-SNAPSHOT")
    compileOnly("com.bergerkiller.bukkit:BKCommonLib:1.20.4-v2-SNAPSHOT")

    implementation("cloud.commandframework", "cloud-paper", "1.8.4")

    implementation("com.zaxxer", "HikariCP", "5.1.0")

}

tasks {
    assemble {
        dependsOn(shadowJar)
    }

    compileKotlin {
        kotlinOptions.jvmTarget = javaVersion.toString()
    }

    shadowJar {

    }

}

java {
    toolchain.languageVersion.set(javaVersion)
}
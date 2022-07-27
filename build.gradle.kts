plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        url = uri("https://nexuslite.gcnt.net/repos/gcnt/")
    }

    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://nexuslite.gcnt.net/repos/other/")
    }

    maven {
        url = uri("https://ci.nametagedit.com/plugin/repository/everything/")
    }

    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }

    maven {
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }

    maven {
        url = uri("https://repo.viaversion.com")
    }

    maven {
        url = uri("https://jitpack.io")
    }

    maven {
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.onarandombox.com/content/groups/public/")
    }

    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://simonsator.de/repo/")
    }

    maven {
        url = uri("https://repo.alessiodp.com/releases/")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
    maven {
        url = uri("https://libraries.minecraft.net/")
    }
}

dependencies {
    implementation("org.mongodb:mongodb-driver:3.12.11")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("io.papermc:paperlib:1.0.8-SNAPSHOT")
    shadow("io.papermc:paperlib:1.0.8-SNAPSHOT")
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("de.simonsator:Party-and-Friends-MySQL-Edition-Spigot-API:1.5.3")
    compileOnly("de.simonsator:Spigot-Party-API-For-RedisBungee:1.0.3-SNAPSHOT")
    compileOnly("com.alessiodp.parties:parties-api:3.2.6")
    compileOnly("de.simonsator:DevelopmentPAFSpigot:1.0.67")
    compileOnly("com.onarandombox.multiversecore:Multiverse-Core:4.3.1")
    compileOnly("org.popcraft:chunky-bukkit:1.2.217")
    compileOnly("org.popcraft:chunky-common:1.2.217")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.10")
    compileOnly("me.clip:placeholderapi:2.11.2")
    compileOnly("com.nametagedit:nametagedit:4.5.8")
    compileOnly("com.viaversion:viaversion-api:4.3.1")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("net.gcnt:additionsplus:1.0.3")
    compileOnly("com.mojang:authlib:1.5.21")
}

group = "me.gaagjescraft.network.team"
version = "1.3.2"
description = "Manhunt"
java.sourceCompatibility = JavaVersion.VERSION_16

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

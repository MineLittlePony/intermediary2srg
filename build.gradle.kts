@file:Suppress("UnstableApiUsage")

plugins {
    `java-gradle-plugin`
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version ("1.3.72")
}

val release: String by project

gradlePlugin {
    plugins {
        create("remapperPlugin") {
            id = "intermediary2srg"
            implementationClass = "com.minelittlepony.intermediary2srg.Intermediary2SrgPlugin"
        }
    }
}

repositories {
    jcenter()
    mavenCentral()
    maven("https://maven.fabricmc.net") {
        name = "fabricmc"
    }
}

dependencies {
    implementation("net.fabricmc:fabric-loom:0.4-SNAPSHOT")
    implementation("net.fabricmc:tiny-remapper:0.3.1.71")
    implementation("net.fabricmc:tiny-mappings-parser:0.3.0+build.17")
    implementation("com.google.code.gson:gson:2.8.5")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.13")
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks {
    named<Jar>("jar") {
        from("LICENSE")
    }
    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
        from("LICENSE")
    }
}

artifacts {
    archives(tasks["sourcesJar"])
}

publishing {
    publications {
        // java-gradle-plugin creates a maven publication automatically
        create<MavenPublication>("pluginMaven") {
            artifact(tasks["sourcesJar"])
            pom {
                name.set("intermediary2srg")
                description.set("Translates intermediary mods to searge names")
                url.set("https://github.com/MineLittlePony/intermediary2srg")

                licenses {
                    license {
                        name.set("MIT Public License")
                        url.set("https://tlo.mit.edu/learn-about-intellectual-property/software-and-open-source-licensing")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/MineLittlePony/intermediary2srg.git")
                    developerConnection.set("scm:git:ssh://github.com/MineLittlePony/intermediary2srg.git")
                    url.set("https://github.com/MineLittlePony/intermediary2srg")
                }
            }
        }
    }
    repositories {
        mavenLocal()

        val s3AccessKey: String? = System.getenv("ACCESS_KEY")
        val s3SecretKey: String? = System.getenv("SECRET_KEY")

        if (s3AccessKey != null && s3SecretKey != null) {
            maven {
                name = "MineLittlePony"
                url = uri("s3://repo.minelittlepony-mod.com/maven/" +
                        if (release == "SNAPSHOT") "snapshot" else "release")

                credentials(AwsCredentials::class.java) {
                    this.accessKey = s3AccessKey
                    this.secretKey = s3SecretKey
                }
            }
        }
    }
}

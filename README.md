# intermediary2srg

intermediary2srg will remap your built intermediary-jar to srg at build time.
This can allow modders to target both Fabric and Forge with minimal code
duplication.

**Note**: Mixin remapping is not (yet?) supported

When applied, a task will run on build that will create a jar containing srg
names. It will have the `srg` classifier. You can customize this in the
`remapSrgJar` block.

## Usage

To use, add the plugin to your root project's buildscript block, then apply the
plugin after loom. I suggest using a common project that doesn't use fabric.

```gradle
buildscript {
    repositories {
        mavenLocal()
        jcenter()
        mavenCentral()
        maven {
            name = "MineLittlePony"
            url = "https://repo.minelittlepony-mod.com/maven/snapshot"
        }   

        maven {
            name = "Fabric"
            url = "https://maven.fabricmc.net/"
        }
    }
    dependencies {
        classpath "net.fabricmc:fabric-loom:0.2.7-SNAPSHOT"
        classpath "com.minelittlepony:intermediary2srg:0.1-SNAPSHOT"
    }
}

apply plugin: 'java-library'
apply plugin: 'fabric-loom'
apply plugin: 'intermediary2srg'
```

If you're using subprojects, you can put them all in a `plugins` block.

```gradle
plugins {
    id 'java-library'
    id 'fabric-loom'
    id 'intermediary2srg'
}
```

## Configuring

The only thing that needs to be configured is what mappings
to use. These can be set via dependencies.

For example, this is the minimal required configuration for 1.15.2

```gradle
plugins {
    id 'java-library'
    id 'fabric-loom'
    id 'intermediary2srg'
}

repositories {
    maven {
        name = "Forge"
        url = "https://files.minecraftforge.net/maven"
    }
}

dependencies {
    // required for loom
    minecraft "com.mojang:minecraft:1.15.2"
    mappings "net.fabricmc:yarn:1.15.2+build.9:v2"

    // required for intermediary2srg
    intermediary "net.fabricmc:intermediary:1.15.2:v2"
    mcpconfig "de.oceanlabs.mcp:mcp_config:1.15.2@zip"
}
```

Additionally, the `remapSrgJar` task inherits from `Jar`, so the output jar can
be customized the same way as other jars tasks. The input jar can be changed by
setting the `jarTask` property. It should be an instance of `RemapJarTask`.

```gradle
remapSrgJar {
    archiveClassifier = "" // Set to default
    archiveVersion = "1.0-forge"
}
```
@file:Suppress("UnstableApiUsage")

package com.minelittlepony.intermediary2srg

import org.gradle.api.Plugin
import org.gradle.api.Project

const val OFFICIAL = "official"
const val INTERMEDIARY = "intermediary"
const val SEARGE = "searge"

/**
 * The main plugin. It requires loom.
 */
class Intermediary2SrgPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.configurations.create("intermediary")
        project.configurations.create("mcpconfig")
        project.tasks.register("generateInt2SrgMappings", GenerateSrgMappingsTask::class.java)
        val remapSrgJar = project.tasks.register("remapSrgJar", RemapSrgJarTask::class.java) { task ->

            task.archiveClassifier.set("srg")
            task.dependsOn("generateInt2SrgMappings")

            project.artifacts.add("archives", task)
        }
        project.afterEvaluate {
            remapSrgJar.configure {
                it.dependsOn(it.jarTask)
            }
        }
    }
}
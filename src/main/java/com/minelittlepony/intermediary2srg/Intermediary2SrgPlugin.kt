@file:Suppress("UnstableApiUsage")

package com.minelittlepony.intermediary2srg

import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar

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

        project.afterEvaluate {

            reconfigureJarTasks(project)

            val remapSourcesJar = project.tasks.named("remapSourcesJar", RemapSourcesJarTask::class.java)
            if (remapSourcesJar.isPresent) {
                val remapSrgSourcesJar = project.tasks.register("remapSrgSourcesJar", RemapSrgSourcesJarTask::class.java) { task ->
                    task.archiveAppendix.set(SEARGE)
                    task.archiveClassifier.set("sources")

                    task.dependsOn(remapSourcesJar)
                    task.dependsOn("generateInt2SrgMappings")
                }
                project.artifacts.add("archives", remapSrgSourcesJar)
//                project.tasks.getByName("build").dependsOn(remapSrgSourcesJar)
            }
        }
    }

    private fun reconfigureJarTasks(project: Project) {
        val jar = project.tasks.named("jar", AbstractArchiveTask::class.java) { task ->
            task.archiveClassifier.set("")
        }
        project.tasks.named("remapJar", RemapJarTask::class.java) { task ->
            task.archiveAppendix.set(INTERMEDIARY)
            task.input.set(jar.map { it.archiveFile.get() })
        }

        // dummy task used to generate jar file path
        // to be removed right away
        val dummyJarTask = project.tasks.create("__remapSourcesJar__", Jar::class.java) { task ->
            task.archiveClassifier.set("sources")
            task.archiveAppendix.set(INTERMEDIARY)
            task.doFirst {
                throw GradleException("Do not run this task!")
            }
        }
        project.tasks.remove(dummyJarTask)

        project.tasks.named("remapSourcesJar", RemapSourcesJarTask::class.java) { task ->
            task.setOutput(dummyJarTask.archiveFile.get())
        }

        val remapSrgJar = project.tasks.register("remapSrgJar", RemapSrgJarTask::class.java) { task ->
            task.archiveAppendix.set(SEARGE)

            task.dependsOn("generateInt2SrgMappings")
            task.dependsOn(task.jarTask)
        }
        project.artifacts.add("archives", remapSrgJar)
    }
}
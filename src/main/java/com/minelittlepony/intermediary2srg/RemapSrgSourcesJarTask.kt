@file:Suppress("UnstableApiUsage")

package com.minelittlepony.intermediary2srg

import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.providers.MappingsCache
import net.fabricmc.loom.task.RemapSourcesJarTask
import net.fabricmc.loom.util.Constants
import net.fabricmc.lorenztiny.TinyMappingsReader
import org.cadixdev.mercury.Mercury
import org.cadixdev.mercury.remapper.MercuryRemapper
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import java.nio.file.Files

open class RemapSrgSourcesJarTask : Jar() {

    @InputFiles
    var sourcesJarTask: TaskProvider<RemapSourcesJarTask> = project.tasks.named("remapSourcesJar", RemapSourcesJarTask::class.java)

    @TaskAction
    fun remapSources() {
        val sourceRemapper = SourceRemapper(project, createMercury())
        val input = sourcesJarTask.get().output
        val output = archiveFile.get().asFile
        sourceRemapper.scheduleRemapSources(input, output)
        sourceRemapper.remapAll()
    }

    private fun createMercury(): Mercury = run {
        val extension = project.extensions.getByType(LoomGradleExtension::class.java)
        val mappingsTask = project.tasks.named("generateInt2SrgMappings", GenerateSrgMappingsTask::class.java).get()
        val mappingsPath = mappingsTask.outputFile.toPath()
        val srgTree = MappingsCache.INSTANCE.get(mappingsPath)
        val mappings = TinyMappingsReader(srgTree, INTERMEDIARY, SEARGE).read()

        createMercuryWithClassPath().also {
            for (file in extension.unmappedMods) {
                if (Files.isRegularFile(file)) {
                    it.classPath.add(file)
                }
            }
            it.classPath.add(extension.minecraftMappedProvider.mappedJar.toPath())
            it.classPath.add(extension.minecraftMappedProvider.intermediaryJar.toPath())
            it.processors.add(MercuryRemapper.create(mappings))
        }
    }

    private fun createMercuryWithClassPath(): Mercury {
        return Mercury().also {
            for (file in project.configurations.getByName(Constants.MINECRAFT_DEPENDENCIES).files) {
                it.classPath.add(file.toPath())
            }
            for (entry in Constants.MOD_COMPILE_ENTRIES) {
                for (inputFile in project.configurations.getByName(entry.sourceConfiguration).files) {
                    it.classPath.add(inputFile.toPath())
                }
            }
        }
    }
}
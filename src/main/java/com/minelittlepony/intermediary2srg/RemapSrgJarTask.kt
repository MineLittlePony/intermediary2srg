@file:Suppress("UnstableApiUsage")

package com.minelittlepony.intermediary2srg

import net.fabricmc.loom.LoomGradleExtension
import net.fabricmc.loom.providers.MappingsCache
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.util.TinyRemapperMappingsHelper
import net.fabricmc.tinyremapper.OutputConsumerPath
import net.fabricmc.tinyremapper.TinyRemapper
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.jvm.tasks.Jar
import java.io.FileNotFoundException
import java.nio.file.Files

open class RemapSrgJarTask : Jar() {

    @InputFiles
    var jarTask: TaskProvider<RemapJarTask> = project.tasks.named("remapJar", RemapJarTask::class.java)

    @TaskAction
    @Throws(Throwable::class)
    fun doTask() {
        val extension = project.extensions.getByType(LoomGradleExtension::class.java)
        val intermediaryJar = extension.mappingsProvider.mappedProvider.intermediaryJar

        val input = jarTask.get().archiveFile.get().asFile.toPath()
        val output = archiveFile.get().asFile.toPath()

        if (Files.notExists(input)) {
            throw FileNotFoundException(input.toString())
        }

        val classpathFiles = project.configurations.getByName("minecraftLibraries").files + intermediaryJar
        val classpath = classpathFiles.filter { input != it && it.exists() }
                .map { it.toPath() }
                .toTypedArray()

        val mappingsTask = project.tasks.getByName("generateInt2SrgMappings") as GenerateSrgMappingsTask
        val mappingsPath = mappingsTask.outputFile.toPath()
        val mappings = MappingsCache.INSTANCE.get(mappingsPath)

        val remapper = TinyRemapper.newRemapper()
                .withMappings(TinyRemapperMappingsHelper.create(mappings, INTERMEDIARY, SEARGE, false))
                .build()

        project.logger.lifecycle(":remapping " + input.fileName)
        val rc = StringBuilder("Remap classpath: ")
        for (p in classpath) {
            rc.append("\n - ").append(p.toString())
        }
        project.logger.debug(rc.toString())

        try {
            OutputConsumerPath.Builder(output).build().use { outputConsumer ->
                outputConsumer.addNonClassFiles(input)
                remapper.readClassPath(*classpath)
                remapper.readInputs(input)
                remapper.apply(outputConsumer)
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to remap $input to $output", e)
        } finally {
            remapper.finish()
        }
        if (Files.notExists(output)) {
            throw RuntimeException("Failed to remap $input to $output - file missing!")
        }
    }
}
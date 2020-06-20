package com.minelittlepony.intermediary2srg

import net.fabricmc.loom.util.DeletingFileVisitor
import net.fabricmc.loom.util.progress.ProgressLogger
import net.fabricmc.stitch.util.StitchUtil
import org.cadixdev.mercury.Mercury
import org.gradle.api.Project
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class SourceRemapper(private val project: Project, private val mercury: Mercury) {

    private val remapTasks: MutableList<(ProgressLogger) -> Unit> = ArrayList()

    @Throws(Exception::class)
    fun scheduleRemapSources(source: File, destination: File) {
        remapTasks.add { logger ->
            try {
                logger.progress("remapping sources - " + source.name)
                remapSourcesInner(source, destination)
            } catch (e: Exception) {
                throw RuntimeException("Failed to remap sources for $source", e)
            }
        }
    }

    fun remapAll() {
        if (remapTasks.isNotEmpty()) {
            project.logger.lifecycle(":remapping sources")
        }
        val progressLogger = ProgressLogger.getProgressFactory(project, SourceRemapper::class.java.name)
        progressLogger.start("Remapping dependency sources", "sources")
        remapTasks.forEach {
            it(progressLogger)
        }
        progressLogger.completed()

        // TODO: FIXME - WORKAROUND https://github.com/FabricMC/fabric-loom/issues/45
        System.gc()
    }

    @Throws(Exception::class)
    private fun remapSourcesInner(source: File, destination: File) {
        project.logger.info(":remapping source jar")
        val mercury: Mercury = mercury
        var srcPath = source.toPath()
        var isSrcTmp = false
        if (!source.isDirectory) {
            // create tmp directory
            isSrcTmp = true
            srcPath = Files.createTempDirectory("fabric-loom-src")
            ZipUtil.unpack(source, srcPath.toFile())
        }
        if (!destination.isDirectory && destination.exists()) {
            if (!destination.delete()) {
                throw RuntimeException("Could not delete ${destination.name}!")
            }
        }
        val dstFs = if (destination.isDirectory) null else StitchUtil.getJarFileSystem(destination, true)
        val dstPath = if (dstFs != null) dstFs.get().getPath("/") else destination.toPath()
        try {
            mercury.rewrite(srcPath, dstPath)
        } catch (e: Exception) {
            project.logger.warn("Could not remap {} fully!", source.name, e)
        }
        copyNonJavaFiles(srcPath, dstPath, source)
        dstFs?.close()
        if (isSrcTmp) {
            Files.walkFileTree(srcPath, DeletingFileVisitor())
        }
    }

    @Throws(IOException::class)
    private fun copyNonJavaFiles(from: Path, to: Path, source: File) {
        Files.walk(from).forEach { path: Path ->
            val targetPath = to.resolve(from.relativize(path).toString())
            if (!isJavaFile(path) && !Files.exists(targetPath)) {
                try {
                    Files.copy(path, targetPath)
                } catch (e: IOException) {
                    project.logger.warn("Could not copy non-java sources '${source.name}' fully!", e)
                }
            }
        }
    }

    private fun isJavaFile(path: Path): Boolean {
        val name = path.fileName.toString()
        // ".java" is not a valid java file
        return name.endsWith(".java") && name.length != 5
    }
}
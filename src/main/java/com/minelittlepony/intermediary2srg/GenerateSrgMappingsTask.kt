package com.minelittlepony.intermediary2srg

import com.minelittlepony.intermediary2srg.MappingUtil.loadIntermediary
import com.minelittlepony.intermediary2srg.MappingUtil.loadMcpConfig
import com.minelittlepony.intermediary2srg.mapping.TinyWriter
import com.minelittlepony.intermediary2srg.mapping.tree.TsrgTree
import com.minelittlepony.intermediary2srg.mapping.tree.TsrgVisitor
import net.fabricmc.mapping.reader.v2.MappingGetter
import net.fabricmc.mapping.reader.v2.TinyMetadata
import net.fabricmc.mapping.reader.v2.TinyVisitor
import net.fabricmc.mapping.tree.*
import net.fabricmc.tinyremapper.IMappingProvider
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.*
import java.io.File
import java.net.URI
import java.nio.file.FileSystems

open class GenerateSrgMappingsTask : DefaultTask() {

    @InputFiles
    var intermediary = project.configurations.getByName("intermediary")

    @InputFiles
    var mcpconfig = project.configurations.getByName("mcpconfig")

    @OutputFile
    var outputFile = File(temporaryDir, "intermediary-tsrg.tiny")

    @TaskAction
    fun generateMappings() {
        val errors = ArrayList<InvalidUserDataException>()
        if (intermediary.files.size != 1) {
            errors.add(InvalidUserDataException("intermediary configuration size is not 1"))
        }
        if (mcpconfig.files.size != 1) {
            errors.add(InvalidUserDataException("mcpconfig configuration size is not 1"))
        }

        if (errors.isNotEmpty()) {
            throw TaskValidationException("Failed to validate configurations", errors)
        }

        val intrJar = URI("jar:" + intermediary.singleFile.toURI().toString())
        val mcpcZip = URI("jar:" + mcpconfig.singleFile.toURI().toString())
        val intrMappings = FileSystems.newFileSystem(intrJar, emptyMap<String, Any>()).use {
            loadIntermediary(it!!.getPath("/"))
        }
        val tsrgMappings = FileSystems.newFileSystem(mcpcZip, emptyMap<String, Any>()).use {
            loadMcpConfig(it.getPath("/"))
        }

        val merged = merge(intrMappings, tsrgMappings)

        val tiny = writeTiny(merged)
        outputFile.parentFile.mkdirs()
        outputFile.writeText(tiny)
    }

    private fun writeTiny(merged: TinyTree): String {
        val (source, target) = merged.metadata.namespaces
        val writer = TinyWriter(source, target)
        for (cl in merged.classes) {
            writer.acceptClass(cl.intName, cl.srgName)

            for (m in cl.methods) {
                writer.acceptMethod(IMappingProvider.Member(cl.intName, m.intName, m.intDesc), m.srgName)
            }
            for (f in cl.fields) {
                writer.acceptField(IMappingProvider.Member(cl.intName, f.intName, f.intDesc), f.srgName)
            }
        }
        return writer.toString()
    }

    private fun merge(intTree: TinyTree, srgTree: TinyTree): TinyTree {

        val visitor = TsrgVisitor()

        val namespaces = listOf(INTERMEDIARY, SEARGE)
        visitor.start(object : TinyMetadata {
            override fun getMajorVersion() = 0
            override fun getMinorVersion() = 0
            override fun getNamespaces() = namespaces
            override fun getProperties() = emptyMap<String, String?>()
        })

        val tsrgClasses = srgTree.defaultNamespaceClassMap.toMutableMap()
        for (intermediaryClass in intTree.classes) {
            val obfName = intermediaryClass.obfName
            val intName = intermediaryClass.intName

            val tsrgClass = tsrgClasses.remove(obfName)
            if (tsrgClass != null) {
                mergeClass(visitor, intermediaryClass, tsrgClass)
            } else {
                logger.warn("Intermediate class $intName (obf: $obfName) doesn't have a tsrg match")
            }
        }
        return TsrgTree(visitor.metadata, visitor.classNames, visitor.classes)
    }


    private fun mergeClass(visitor: TinyVisitor, intClass: ClassDef, srgClass: ClassDef) {
        val intName = intClass.intName
        val srgName = srgClass.srgName
        val classNames = arrayOf(intName, srgName)
        visitor.pushClass(PartGetter(classNames))

        for (intField in intClass.fields) {
            val obfName = intField.obfName
            val srgField = srgClass.findField(obfName)
            if (srgField != null) {
                mergeField(visitor, intField, srgField)
            } else {
                logger.warn("Intermediate field ${intClass.intName}.${intField.intName}" +
                        " (obf: ${intClass.obfName}.${intField.obfName}" +
                        "doesn't have a tsrg match")
            }
        }

        for (intMethod in intClass.methods) {
            val obfName = intMethod.obfName
            val obfDesc = intMethod.obfDesc
            val srgField = srgClass.findMethod(obfName, obfDesc)
            if (srgField != null) {
                mergeMethod(visitor, intMethod, srgField)
            } else {
                logger.warn("Intermediate method ${intClass.intName}.${intMethod.intName}${intMethod.intDesc}" +
                        " (obf: ${intClass.obfName}.${intMethod.obfName}${intMethod.obfDesc}" +
                        "doesn't have a tsrg match")
            }
        }
    }

    private fun mergeField(visitor: TinyVisitor, intField: FieldDef, srgField: FieldDef) {
        val intName = intField.intName
        val srgName = srgField.srgName
        val fieldNames = arrayOf(intName, srgName)
        visitor.pushField(PartGetter(fieldNames), intField.intDesc)
    }

    private fun mergeMethod(visitor: TinyVisitor, intMethod: MethodDef, srgMethod: MethodDef) {
        val intName = intMethod.intName
        val srgName = srgMethod.srgName
        val fieldNames = arrayOf(intName, srgName)
        visitor.pushMethod(PartGetter(fieldNames), intMethod.intDesc)
    }

    class PartGetter(private val parts: Array<String>) : MappingGetter {
        override fun get(namespace: Int) = getRaw(namespace)

        override fun getRawNames() = parts

        override fun getRaw(namespace: Int) = parts[namespace]

        override fun getAllNames() = rawNames
    }
}

val Mapped.obfName get() = this.getName(OFFICIAL)!!
val Mapped.intName get() = this.getName(INTERMEDIARY)!!
val Mapped.srgName get() = this.getName(SEARGE)!!
val Descriptored.obfDesc get() = this.getDescriptor(OFFICIAL)!!
val Descriptored.intDesc get() = this.getDescriptor(INTERMEDIARY)!!

fun ClassDef.findMethod(name: String, desc: String): MethodDef? {
    return this.methods.find { it.obfName == name && it.obfDesc == desc }
}

fun ClassDef.findField(name: String): FieldDef? {
    return this.fields.find { it.obfName == name }
}


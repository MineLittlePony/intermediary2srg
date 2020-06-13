package com.minelittlepony.intermediary2srg.mapping

import com.google.gson.JsonParser
import com.minelittlepony.intermediary2srg.OFFICIAL
import com.minelittlepony.intermediary2srg.SEARGE
import com.minelittlepony.intermediary2srg.mapping.tree.TsrgTree
import com.minelittlepony.intermediary2srg.mapping.tree.TsrgVisitor
import net.fabricmc.mapping.reader.v2.MappingGetter
import net.fabricmc.mapping.reader.v2.TinyMetadata
import net.fabricmc.mapping.reader.v2.TinyVisitor
import net.fabricmc.mapping.tree.TinyTree
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object TsrgFactory {
    fun load(zipRoot: Path): TinyTree {
        val visitor = TsrgVisitor()
        visit(zipRoot, visitor)
        return TsrgTree(visitor.metadata, visitor.classNames, visitor.classes)
    }

    @Throws(IOException::class)
    fun visit(zipRoot: Path, visitor: TinyVisitor) {
        val configPath = zipRoot.resolve("config.json")
        val config = Files.newBufferedReader(configPath).use {
            JsonParser().parse(it).asJsonObject
        }
        val namespaces = listOf(OFFICIAL, SEARGE)
        val meta: TinyMetadata = object : TinyMetadata {
            override fun getMajorVersion() = 0
            override fun getMinorVersion() = 0
            override fun getNamespaces() = namespaces
            override fun getProperties() = emptyMap<String, String?>()

        }
        visitor.start(meta)

        val data = config["data"].asJsonObject
        val mappingsPath: Path = zipRoot.resolve(data["mappings"].asString)
        Files.newBufferedReader(mappingsPath).useLines { lines ->
            lines.forEach { line ->
                val state = TinyState[line]
                val parts = line.trim { it <= ' ' }.split(" ".toRegex()).toTypedArray()
                state.visit(visitor, parts)
            }
        }
    }

    private enum class TinyState {
        CLASS {
            override fun visit(visitor: TinyVisitor, parts: Array<String>) {
                visitor.pushClass(PartGetter(parts))
            }
        },
        FIELD {
            override fun visit(visitor: TinyVisitor, parts: Array<String>) {
                visitor.pushField(PartGetter(parts), null)
            }
        },
        METHOD {
            override fun visit(visitor: TinyVisitor, parts: Array<String>) {
                visitor.pushMethod(PartGetter(arrayOf(parts[0], parts[2])), parts[1])
            }
        };

        abstract fun visit(visitor: TinyVisitor, parts: Array<String>)

        companion object {
            operator fun get(identifier: String): TinyState {
                if (!identifier.startsWith("\t")) {
                    return CLASS
                }
                return when (identifier.trim().split(" ").size) {
                    2 -> FIELD
                    3 -> METHOD
                    else -> throw IllegalArgumentException("Invalid identifier \"$identifier\"!")
                }
            }
        }
    }

    private class PartGetter(private val parts: Array<String>) : MappingGetter {
        override fun get(namespace: Int): String {
            return getRaw(namespace)
        }

        override fun getRaw(namespace: Int): String {
            return parts[namespace]
        }

        override fun getRawNames(): Array<String> {
            return parts.copyOf()
        }

        override fun getAllNames(): Array<String> {
            return parts
        }
    }
}
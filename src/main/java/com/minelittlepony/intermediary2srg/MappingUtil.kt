package com.minelittlepony.intermediary2srg

import com.minelittlepony.intermediary2srg.mapping.TsrgFactory
import net.fabricmc.mapping.tree.TinyMappingFactory
import net.fabricmc.mapping.tree.TinyTree
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

object MappingUtil {
    @Throws(IOException::class)
    fun loadIntermediary(root: Path): TinyTree {
        val mappings = root.resolve("/mappings/mappings.tiny")
        return Files.newBufferedReader(mappings).use { reader ->
            TinyMappingFactory.loadWithDetection(reader, true)
        }
    }

    @Throws(IOException::class)
    fun loadMcpConfig(root: Path): TinyTree {
        return TsrgFactory.load(root)
    }
}
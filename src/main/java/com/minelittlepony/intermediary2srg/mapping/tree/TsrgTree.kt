package com.minelittlepony.intermediary2srg.mapping.tree

import net.fabricmc.mapping.reader.v2.TinyMetadata
import net.fabricmc.mapping.tree.TinyTree

/**
 * A lot of the classes in this package needed to be copied from the parser
 * because it doesn't expose them or provide a way to initialize them.
 * tiny-parser is not very extensible for other formats (like tsrg).
 */
open class TsrgTree(
        private val metadata: TinyMetadata,
        private val defaultNamespaceClassMap: Map<String, ClassImpl>,
        private val classes: Collection<ClassImpl>
) : TinyTree {
    override fun getMetadata(): TinyMetadata {
        return metadata
    }

    override fun getDefaultNamespaceClassMap(): Map<String, ClassImpl> {
        return defaultNamespaceClassMap
    }

    override fun getClasses(): Collection<ClassImpl> {
        return classes
    }

}
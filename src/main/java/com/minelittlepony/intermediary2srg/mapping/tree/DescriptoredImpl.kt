package com.minelittlepony.intermediary2srg.mapping.tree

import net.fabricmc.mapping.tree.Descriptored

abstract class DescriptoredImpl(private val mapper: DescriptorMapper,
                                namespaceMapper: (String) -> Int,
                                names: Array<String>,
                                private val signature: String?)
    : MappedImpl(namespaceMapper, names), Descriptored {

    override fun getDescriptor(namespace: String): String? {
        val t: Int = namespaceMapper(namespace)
        return if (t == 0 || signature == null) signature else mapper.mapDescriptor(t, signature)
    }

}
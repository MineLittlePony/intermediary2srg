package com.minelittlepony.intermediary2srg.mapping.tree

import net.fabricmc.mapping.tree.Mapped

open class MappedImpl(
        protected val namespaceMapper: (String) -> Int,
        private val names: Array<String>) : Mapped {

    private var comment: String? = null

    override fun getName(namespace: String): String {
        val t = namespaceMapper(namespace)
        return getName(t)
    }

    override fun getRawName(namespace: String): String {
        return getName(namespace)
    }

    fun getName(namespace: Int): String {
        @Suppress("NAME_SHADOWING")
        var namespace = namespace
        if (namespace >= names.size) namespace = names.size - 1
        while (names[namespace].isEmpty()) {
            if (namespace == 0) return ""
            namespace--
        }
        return names[namespace]
    }

    override fun getComment(): String? {
        return comment
    }

    fun setComment(comment: String?) {
        this.comment = comment
    }


}
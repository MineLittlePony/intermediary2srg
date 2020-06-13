package com.minelittlepony.intermediary2srg.mapping.tree

class DescriptorMapper(private val map: Map<String, MappedImpl>) {

    private fun mapClass(namespace: Int, old: String): String {
        val got = map[old]
        return got?.getName(namespace) ?: old
    }

    fun mapDescriptor(namespace: Int, old: String): String {
        var lastL = old.indexOf("L")
        var lastSemi = -1
        if (lastL < 0) {
            return old
        }
        val builder = StringBuilder((old.length * 1.2).toInt()) // approximate
        while (lastL >= 0) {
            if (lastSemi + 1 < lastL) {
                builder.append(old, lastSemi + 1, lastL)
            }
            lastSemi = old.indexOf(";", lastL + 1)
            if (lastSemi == -1) return old // Invalid desc, nah!
            builder.append("L").append(mapClass(namespace, old.substring(lastL + 1, lastSemi))).append(";")
            lastL = old.indexOf("L", lastSemi + 1)
        }
        if (lastSemi + 1 < old.length) {
            builder.append(old, lastSemi + 1, old.length)
        }
        return builder.toString()
    }

}
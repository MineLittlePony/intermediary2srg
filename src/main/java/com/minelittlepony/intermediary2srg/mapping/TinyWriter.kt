package com.minelittlepony.intermediary2srg.mapping

import net.fabricmc.tinyremapper.IMappingProvider

class TinyWriter(srcNamespace: String, dstNamespace: String) : IMappingProvider.MappingAcceptor {
    private val tiny = StringBuilder("tiny\t2\t0\t$srcNamespace\t$dstNamespace\n")

    override fun acceptClass(srcName: String, dstName: String) {
        tiny.append("c\t$srcName\t$dstName\n")
    }

    override fun acceptMethod(method: IMappingProvider.Member, dstName: String) {
        tiny.append("\tm\t${method.desc}\t${method.name}\t$dstName\n")
    }

    override fun acceptMethodArg(method: IMappingProvider.Member, lvIndex: Int, dstName: String) {
        // tsrg does not support parameters
    }

    override fun acceptMethodVar(method: IMappingProvider.Member, lvIndex: Int, startOpIdx: Int, asmIndex: Int, dstName: String) {
        // tsrg does not support variables
    }

    override fun acceptField(field: IMappingProvider.Member, dstName: String) {
        tiny.append("\tf\t${field.desc}\t${field.name}\t$dstName\n")
    }

    override fun toString(): String {
        return tiny.toString()
    }

}
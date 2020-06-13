package com.minelittlepony.intermediary2srg.mapping.tree

import net.fabricmc.mapping.tree.LocalVariableDef
import net.fabricmc.mapping.tree.MethodDef
import net.fabricmc.mapping.tree.ParameterDef
import java.util.*

class MethodImpl(
        mapper: DescriptorMapper,
        namespaceMapper: (String) -> Int,
        names: Array<String>,
        signature: String?
) : DescriptoredImpl(mapper, namespaceMapper, names, signature),
        MethodDef {

    override fun getParameters(): Collection<ParameterDef> {
        return parameters
    }

    override fun getLocalVariables(): Collection<LocalVariableDef> {
        return localVariables
    }

    private val parameters: Collection<ParameterDef> = ArrayList()
    private val localVariables: Collection<LocalVariableDef> = ArrayList()
}
package com.minelittlepony.intermediary2srg.mapping.tree

import net.fabricmc.mapping.tree.ClassDef
import net.fabricmc.mapping.tree.FieldDef
import net.fabricmc.mapping.tree.MethodDef
import java.util.*

class ClassImpl(namespaceMapper: (String) -> Int, names: Array<String>) : MappedImpl(namespaceMapper, names), ClassDef {
    private val methods: ArrayList<MethodDef> = ArrayList()
    private val fields: ArrayList<FieldDef> = ArrayList()

    override fun getMethods(): ArrayList<MethodDef> {
        return methods
    }

    override fun getFields(): ArrayList<FieldDef> {
        return fields
    }
}
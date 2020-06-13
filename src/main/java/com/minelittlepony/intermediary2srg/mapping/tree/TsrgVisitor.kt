package com.minelittlepony.intermediary2srg.mapping.tree

import net.fabricmc.mapping.reader.v2.MappingGetter
import net.fabricmc.mapping.reader.v2.TinyMetadata
import net.fabricmc.mapping.reader.v2.TinyVisitor

class TsrgVisitor : TinyVisitor {

    val classNames: MutableMap<String, ClassImpl> = LinkedHashMap()
    val classes: MutableCollection<ClassImpl> = ArrayList()

    private lateinit var namespaceMapper: (String) -> Int
    private val descriptorMapper = DescriptorMapper(classNames)

    lateinit var metadata: TinyMetadata
    private lateinit var inClass: ClassImpl

    override fun start(metadata: TinyMetadata) {
        this.metadata = metadata
        namespaceMapper = metadata::index
    }

    override fun pushClass(name: MappingGetter) {
        val clz = ClassImpl(namespaceMapper, name.rawNames)
        classes.add(clz)
        classNames[name[0]] = clz
        inClass = clz
    }

    override fun pushField(name: MappingGetter, descriptor: String?) {
        val field = FieldImpl(descriptorMapper, namespaceMapper, name.rawNames, descriptor)
        inClass.fields.add(field)
    }

    override fun pushMethod(name: MappingGetter, descriptor: String) {
        val method = MethodImpl(descriptorMapper, namespaceMapper, name.rawNames, descriptor)
        inClass.methods.add(method)
    }
}
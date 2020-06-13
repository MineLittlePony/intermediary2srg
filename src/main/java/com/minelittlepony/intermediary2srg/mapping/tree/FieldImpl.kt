package com.minelittlepony.intermediary2srg.mapping.tree

import net.fabricmc.mapping.tree.FieldDef

class FieldImpl(
        mapper: DescriptorMapper,
        namespaceMapper: (String) -> Int,
        names: Array<String>,
        signature: String?
) : DescriptoredImpl(mapper, namespaceMapper, names, signature),
        FieldDef
package me.maplesyrum.tricksterstudio.spell.fragment

import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder


class EntityTypeFragment(val entityType: String) : Fragment() {
    override fun type(): FragmentType<*> {
        return FragmentType.ENTITY_TYPE
    }

    override fun toString(): String {
        return this.entityType;
    }

    companion object {
        val ENDEC: StructEndec<EntityTypeFragment> = StructEndecBuilder.of(
            Identifier.ENDEC
                .xmap({ id -> id.toString() }, { str -> Identifier.of(str) })
                .fieldOf("entityType", EntityTypeFragment::entityType)
        ) { entityType: String -> EntityTypeFragment(entityType) }
    }
}

package me.maplesyrum.tricksterstudio.spell.fragment

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import tree.maple.kendec.StructEndec
import tree.maple.kendec.impl.StructEndecBuilder
import tree.maple.kendec.PrimitiveEndecs
import tree.maple.kendec.impl.BuiltInEndecs


class EntityFragment(val uuid: String, val name: String) : Fragment() {
    override fun type(): FragmentType<*> {
        return FragmentType.ENTITY
    }

    override fun toString(): String {
        val match = Regex("\"([^\"]+)\"").find(name)
        val entityName = match?.groupValues?.get(1) ?: "entity"
        return entityName
    }

    companion object {
        val ENDEC: StructEndec<EntityFragment> = StructEndecBuilder.of(
            BuiltInEndecs.Uuid
                .xmap({ uuid: Uuid -> uuid.toString() }, { str: String -> uuidFrom(str) })
                .fieldOf("uuid", EntityFragment::uuid),
            PrimitiveEndecs.STRING.fieldOf("name", EntityFragment::name),
            { uuid: String, name: String -> EntityFragment(uuid, name) }
        )
    }
}

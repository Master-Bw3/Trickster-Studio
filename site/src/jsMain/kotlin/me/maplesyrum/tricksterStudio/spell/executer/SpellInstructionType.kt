package me.maplesyrum.tricksterStudio.spell.executer

enum class SpellInstructionType(val id: Int) {
    FRAGMENT(1), ENTER_SCOPE(2), EXIT_SCOPE(3);

    fun getId(): Int {
        return this.id
    }

    companion object {
        fun fromId(id: Int): SpellInstructionType {
            return when (id) {
                1 -> FRAGMENT
                2 -> ENTER_SCOPE
                3 -> EXIT_SCOPE
                else -> throw IllegalArgumentException("Unexpected spell instruction type id: " + id)
            }
        }
    }
}

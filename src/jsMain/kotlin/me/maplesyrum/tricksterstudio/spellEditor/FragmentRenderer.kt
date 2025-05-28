package me.maplesyrum.tricksterstudio.spellEditor

import Identifier
import me.maplesyrum.tricksterstudio.external.pixi.Container
import me.maplesyrum.tricksterstudio.external.pixi.Texture
import me.maplesyrum.tricksterstudio.spell.fragment.Fragment

interface FragmentRenderer<T : Fragment> {
    fun render(
        fragment: T,
        container: Container,
        x: Double,
        y: Double,
        size: Double,
        alpha: Double,
        textures: Map<String, Texture>,
        delegator: SpellCircleRenderer
    )

    fun renderRedrawDots(): Boolean
}

val fragmentRenderers: MutableMap<Identifier, FragmentRenderer<Fragment>> = mutableMapOf()


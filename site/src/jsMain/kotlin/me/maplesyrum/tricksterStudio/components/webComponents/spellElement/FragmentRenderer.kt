package me.maplesyrum.tricksterStudio.components.webComponents.spellElement

import me.maplesyrum.tricksterStudio.Identifier
import me.maplesyrum.tricksterStudio.external.pixi.Container
import me.maplesyrum.tricksterStudio.external.pixi.Texture
import me.maplesyrum.tricksterStudio.spell.fragment.Fragment

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


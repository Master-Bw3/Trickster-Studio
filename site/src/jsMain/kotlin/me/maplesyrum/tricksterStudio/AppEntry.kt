package me.maplesyrum.tricksterStudio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.varabyte.kobweb.compose.css.ScrollBehavior
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.scrollBehavior
import com.varabyte.kobweb.core.App
import com.varabyte.kobweb.silk.SilkApp
import com.varabyte.kobweb.silk.components.layout.Surface
import com.varabyte.kobweb.silk.init.InitSilk
import com.varabyte.kobweb.silk.init.InitSilkContext
import com.varabyte.kobweb.silk.init.registerStyleBase
import com.varabyte.kobweb.silk.style.common.SmoothColorStyle
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.colors.ColorMode
import com.varabyte.kobweb.silk.theme.colors.loadFromLocalStorage
import com.varabyte.kobweb.silk.theme.colors.palette.background
import com.varabyte.kobweb.silk.theme.colors.palette.color
import com.varabyte.kobweb.silk.theme.colors.saveToLocalStorage
import com.varabyte.kobweb.silk.theme.colors.systemPreference
import me.maplesyrum.tricksterStudio.components.webComponents.spellElement.SpellElement
import web.components.customElements
import web.html.HtmlTagName

private const val COLOR_MODE_KEY = "maplesyrum:colorMode"

@InitSilk
fun initColorMode(ctx: InitSilkContext) {
    ctx.config.initialColorMode = ColorMode.loadFromLocalStorage(COLOR_MODE_KEY) ?: ColorMode.systemPreference
}

@InitSilk
fun initStyles(ctx: InitSilkContext) {
    ctx.stylesheet.apply {
        registerStyleBase("html, body") { Modifier.fillMaxHeight() }
        registerStyleBase("body") { Modifier.scrollBehavior(ScrollBehavior.Smooth) }
    }
}

@InitSilk
fun overrideSilkTheme(ctx: InitSilkContext) {
    ctx.theme.palettes.light.background = Color.rgb(0xffffff)
    ctx.theme.palettes.light.color = Color.rgb(0x000000)
    ctx.theme.palettes.dark.background = Color.rgb(0x000000)
    ctx.theme.palettes.dark.color = Color.rgb(0xffffff)
}

@App
@Composable
fun AppEntry(content: @Composable () -> Unit) {
    SilkApp {
        LaunchedEffect(Unit) {
            customElements.define(HtmlTagName("spell-editor"), SpellElement::class.js)
        }

        val colorMode = ColorMode.current
        LaunchedEffect(colorMode) {
            colorMode.saveToLocalStorage(COLOR_MODE_KEY)
        }

        Surface(SmoothColorStyle.toModifier().fillMaxHeight()) {
            content()
        }
    }
}

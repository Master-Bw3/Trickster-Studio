package me.maplesyrum.tricksterstudio

import io.kvision.Application
import io.kvision.CoreModule
import io.kvision.Hot
import io.kvision.TailwindcssModule
import io.kvision.i18n.DefaultI18nManager
import io.kvision.i18n.I18n
import io.kvision.panel.root
import io.kvision.startApplication
import io.kvision.utils.useModule
import me.maplesyrum.tricksterstudio.spell.fragment.FragmentType

@JsModule("/kotlin/modules/css/kvapp.css")
external val kvappCss: dynamic

@JsModule("/kotlin/modules/i18n/messages-en.json")
external val messagesEn: dynamic

@JsModule("/kotlin/modules/i18n/messages-pl.json")
external val messagesPl: dynamic

class App : Application() {
    init {
        useModule(kvappCss)
    }

    override fun start() {
        FragmentType.register()

        I18n.manager = DefaultI18nManager(
            mapOf(
                "en" to messagesEn, "pl" to messagesPl
            )
        )

        root("kvapp") {
            editor()
        }
    }
}

fun main() {
    startApplication(::App, js("import.meta.webpackHot").unsafeCast<Hot?>(), TailwindcssModule, CoreModule)
}

@file:JsModule("pixi.js")
@file:JsNonModule

package me.maplesyrum.tricksterstudio.external.pixi

import kotlin.js.Promise

open external class Container {
    fun addChild(child: Container)
    fun removeChildren()
}

external class Graphics : Container {
    fun poly(points: Array<Point>, d: Boolean = definedExternally): Graphics
    fun fill(rgba: Array<Double>)
    fun stroke(options: dynamic)
}

external class Sprite(value: dynamic) : Container {
    var x: Double
    var y: Double
    var width: Double
    var height: Double
    var alpha: Double
}

external class Texture() {
    var source: dynamic
}

external class Point(var x: Double, var y: Double) {}

external class HTMLText(options: dynamic) : Container {
    var anchor: dynamic
    var resolution: Double
    var style: dynamic
    var alpha: Double
    var x: Double
    var y: Double
    var scale: Double
    var width: Double
    val text: String
}

external class Application() {
    fun init(settings: dynamic): Promise<Unit>
    fun queueResize()
    val canvas: dynamic
    val ticker: Ticker
    val stage: Container
}

external class Ticker {
    fun add(fn: () -> Unit)
}

external object Assets {
   fun <T> load(options: dynamic): Promise<T>
}
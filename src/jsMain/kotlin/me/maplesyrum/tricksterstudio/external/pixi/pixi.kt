@file:JsModule("pixi.js")
@file:JsNonModule

package me.maplesyrum.tricksterstudio.external.pixi

open external class Container {
    fun addChild(child: Container)

}

external class Graphics : Container {
    fun poly(points: Array<Point>, d: Boolean = definedExternally): Graphics
    fun fill(rgba: Array<Double>)
    fun stroke(options: StrokeOptions)
}

external class StrokeOptions (width: Double, color: Array<Double>)

external class Texture

external class Point(var x: Double, var y: Double) {}

external class HTMLText(options: dynamic) : Container {
    var anchor: dynamic
    var resolution: Double
    var style: dynamic
    var alpha: Double
    var x: Double
    var y: Double
    var scale: Double
}

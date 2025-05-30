@file:JsModule("pako")
@file:JsNonModule

package me.maplesyrum.tricksterstudio.external.pako

import js.typedarrays.Uint8Array

external fun gzip(data: Uint8Array<*>): Uint8Array<*>

external fun ungzip(data: Uint8Array<*>): Uint8Array<*>
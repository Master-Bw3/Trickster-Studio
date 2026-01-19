import { BitArray$BitArray } from '../../prelude.mjs';
import * as pako from "pako";

function isBunEnvironment() {
    return typeof Bun !== "undefined" && typeof Bun.version === "string";
}

export function ushr(value, bitCount) {
    return value >>> bitCount
}

export function gzip(bitArray) {
    let gzipped = pako.gzip(bitArray.rawBuffer)
    return BitArray$BitArray(gzipped)
}

export function ungzip(bitArray) {
    let gzipped = pako.ungzip(bitArray.rawBuffer)
    return BitArray$BitArray(gzipped)
}

export function to_base64(bitArray) {
    if (isBunEnvironment()) {
        const buf = Buffer.from(bitArray.rawBuffer);
        return buf.toBase64();
    } else {
        bitArray.rawBuffer.toBase64()
    }
}

export function from_base64(string) {
    if (isBunEnvironment()) {
        const buf = Buffer.fromBase64(string);
        return BitArray$BitArray(buf)
    } else {
        const arr = Uint8Array.fromBase64(string);
        return BitArray$BitArray(arr)
    }

}
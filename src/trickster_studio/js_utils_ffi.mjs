import { BitArray$BitArray, List$Empty, List$NonEmpty, Result$Ok, Result$Error } from '../../prelude.mjs';
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
    try {
        let gzipped = pako.ungzip(bitArray.rawBuffer)
        return Result$Ok(BitArray$BitArray(gzipped))
    } catch (error) {
        Result$Error(`${error}`)
    }
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
    try {
        if (isBunEnvironment()) {
            const buf = Buffer.fromBase64(string);
            return Result$Ok(BitArray$BitArray(buf))
        } else {
            const arr = Uint8Array.fromBase64(string);
            return Result$Ok(BitArray$BitArray(arr))
        }
    } catch (error) {
        Result$Error(`${error}`)
    }

}

export function get_url_search_params() {
    let list = List$Empty();

    let searchParams = new URLSearchParams(window.location.search);

    for (const [key, value] of searchParams.entries()) {
        list = List$NonEmpty([key, value], list)
    }

    return list
}

export function set_transparent_bg(renderer) {
    renderer.setClearColor( 0x000000, 0 );
}
import { BitArray$BitArray } from '../../prelude.mjs';


export function ushr(value, bitCount) {
    return value >>> bitCount
}

export function gzip(bitArray) {
    let gzipped = Bun.gzipSync(bitArray.rawBuffer)
    return BitArray$BitArray(gzipped)
}

export function ungzip(bitArray) {
    let gzipped = Bun.gunzipSync(bitArray.rawBuffer)
    return BitArray$BitArray(gzipped)
}

export function to_base64(bitArray) {
    const buf = Buffer.from(bitArray.rawBuffer);
    return buf.toBase64();
}

export function from_base64(string) {
    const buf = Buffer.fromBase64(string);
    return BitArray$BitArray(buf)
}
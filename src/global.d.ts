/// <reference types="@solidjs/start/env" />

import type { InputType, ZlibOptions } from "zlib";


interface Zlib {
    ungzip(buf: InputType, options?: ZlibOptions): Buffer;
    gzip(buf: InputType, options?: ZlibOptions): Buffer;
}

declare global {
    interface Window {
        zlib: Zlib
    }
}


export {};

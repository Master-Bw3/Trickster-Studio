import * as pako from "pako";

function decodeSpell(encodedString: string) {
    const gezipedData = atob(encodedString);
    const gzipedDataArray = Uint8Array.from(gezipedData, (c) => c.charCodeAt(0));

    const ungzipedData = pako.ungzip(gzipedDataArray);

    

}

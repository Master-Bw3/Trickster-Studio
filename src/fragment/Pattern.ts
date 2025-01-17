import { Point, Text } from "pixi.js";
import Fragment, { FragmentType, register } from "./Fragment";

//@ts-ignore
import * as wasm from "../WasmEndec-1.0-SNAPSHOT/js/endec.js";

const PATTERN = register("trickster:pattern", 0xffffff, (object: any) => {
    if (object instanceof wasm.Pattern) {
        const entries: Array<Point | null> = object.entries.map((entry: any) => {
            if (entry instanceof wasm.PatternEntry) {
                return new Point(entry.p1, entry.p2);
            } else return null;
        });

        if (entries.every((x) => x != null)) {
            return new Pattern(entries);
        }
    }
    return null;
});

export default class Pattern extends Fragment {
    entries: Array<Point>;

    constructor(entries: Array<Point>) {
        super();
        this.entries = entries;
    }

    contains(point: number): boolean {
        for (const entry of this.entries) {
            if (entry.x == point || entry.y == point) {
                return true;
            }
        }
        return false;
    }

    equals(other: Pattern): boolean {
        if (other.entries.length != this.entries.length) return false;

        let equal = true;

        for (let i = 0; i < this.entries.length; i++) {
            const a = this.entries[i];
            const b = other.entries[i];

            equal = a.x == b.x && a.y == b.y;

            if (!equal) break;
        }

        return equal;
    }

    override toString(): string {
        return "pattern";
    }

    override type(): FragmentType {
        return PATTERN;
    }
}

const possibleLines: Array<Point> = new Array(32);

let i = 0;
for (let p1 = 0; p1 < 9; p1++) {
    for (let p2 = 0; p2 < 9; p2++) {
        if (p2 > p1 && p1 + p2 != 8) {
            possibleLines[i] = new Point(p1, p2);
            i++;
        }
    }
}

function patternOf(pattern: Array<number>): Pattern | null {
    const result = patternFrom(pattern);

    for (const line of result.entries) {
        let b = false;

        for (const line2 of possibleLines) {
            if (line2.equals(line)) {
                b = true;
                break;
            }
        }

        if (!b) {
            return null;
        }
    }

    return result;
}

function patternFrom(pattern: Array<number>): Pattern {
    const list: Array<Point> = [];
    let last = null;
    for (const current of pattern) {
        if (last != null) {
            if (last < current) {
                list.push(new Point(last, current));
            } else {
                list.push(new Point(current, last));
            }
        }
        last = current;
    }
    list.sort(compareTo);
    return new Pattern([...list]);
}

function compareTo(a: Point, b: Point): number {
    const p1Compare = compareNumbers(a.x, b.x);
    return p1Compare == 0 ? compareNumbers(a.y, b.y) : p1Compare;
}

function compareNumbers(a: number, b: number): number {
    if (a < b) {
        return -1;
    } else if (a > b) {
        return 1;
    } else {
        return 0;
    }
}

export { patternOf, patternFrom };

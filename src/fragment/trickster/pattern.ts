import { Point, View } from 'pixi.js';
import { Fragment } from '~/fragment/fragment';
import { StructEndec, listOf, PrimitiveEndecs, ifAttr } from 'KEndec';
import { POINT, UBER_COMPACT_ATTRIBUTE } from '~/endecTomfoolery';
import { Identifier } from '~/util';

export type PatternData = {
    entries: ReadonlyArray<Point>;
};

export const endec = (fragmentEndecs: Map<Identifier, StructEndec<Fragment<unknown>>>) =>
    ifAttr(
        UBER_COMPACT_ATTRIBUTE,
        PrimitiveEndecs.INT.xmap(
            (int) => fromInt(int),
            (pattern) => toInt(pattern.data.entries)
        )
    ).orElse(
        POINT.listOf().xmap(
            (entries) => fromEntries(entries.asJsReadonlyArrayView()),
            (pattern) => listOf([...pattern.data.entries])
        )
    );

function displayOf(data: PatternData): () => View {
    return () => {
        throw new Error('todo');
    };
}

export function fromEntries(entries: ReadonlyArray<Point>): Fragment<PatternData> {
    const data = { entries };
    return {
        type: new Identifier('trickster', 'pattern_literal'),
        display: displayOf(data),
        data: data,
    };
}

const possibleLines: ReadonlyArray<Point> = (() => {
    const lines = new Array(32);

    let i = 0;
    for (let p1 = 0; p1 < 9; p1++) {
        for (let p2 = 0; p2 < 9; p2++) {
            if (p2 > p1 && p1 + p2 != 8) {
                lines[i] = new Point(p1, p2);
                i++;
            }
        }
    }

    return lines;
})();

export function fromInt(pattern: number) {
    var list = [];
    for (let i = 0; i < 32; i++) {
        if (((pattern >> i) & 0x1) == 1) {
            list.push(possibleLines[i]);
        }
    }
    return fromEntries(list);
}

export function toInt(entries: ReadonlyArray<Point>): number {
    let result = 0;
    for (let i = 0; i < 32; i++) {
        if (entries.includes(possibleLines[i])) {
            result |= 1 << i;
        }
    }
    return result;
}

export function compareTo(a: Point, b: Point): number {
    const p1Compare = compareNumbers(a.x, b.x);
    return p1Compare == 0 ? compareNumbers(a.y, b.y) : p1Compare;
}

export function compareNumbers(a: number, b: number): number {
    if (a < b) {
        return -1;
    } else if (a > b) {
        return 1;
    } else {
        return 0;
    }
}

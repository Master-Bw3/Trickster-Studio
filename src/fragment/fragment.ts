import {
    Endec,
    dispatchedStructEndecOf,
    ifAttr,
    PrimitiveEndecs,
    StructEndec,
    Nullable,
} from 'KEndec';
import { View } from 'pixi.js';
import { UBER_COMPACT_ATTRIBUTE } from '~/endecTomfoolery';
import { getKeyByValue, Identifier } from '~/util';

export type Fragment<Data> = {
    readonly type: Identifier
    readonly data: Data
    display(): View;
}


export const endec = (fragmentEndecs: Map<Identifier, StructEndec<Fragment<unknown>>>) => {
    const fragmentEndecIntLookup: Map<number, StructEndec<Fragment<unknown>>> = new Map();

    return dispatchedStructEndecOf(
        (endec: Nullable<StructEndec<Fragment<unknown>>>) => endec!,
        (fragment: Fragment<unknown>) => get(fragmentEndecs, fragment.type)!,
        ifAttr(
            UBER_COMPACT_ATTRIBUTE,
            PrimitiveEndecs.INT.xmap(
                (int) => fragmentEndecIntLookup.get(int)!,
                (endec) => getKeyByValue(fragmentEndecIntLookup, endec)!
            )
        ).orElse(
            Identifier.ENDEC.xmap(
                (id) => get(fragmentEndecs, id)!,
                (endec) => getKeyByValue(fragmentEndecs, endec)!
            )
        )
    );
};

function get(fragmentEndecs: Map<Identifier, StructEndec<Fragment<unknown>>>, id: Identifier) {
    return Array.from(fragmentEndecs.entries()).find(([k, v]) => k.equals(id))?.[1]
}
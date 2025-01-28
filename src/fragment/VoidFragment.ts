import { Text } from "pixi.js";
import Fragment, { FragmentType, register } from "./Fragment";
import { unit } from "~/endecTomfoolery";


export default class VoidFragment extends Fragment {
    static INSTANCE: VoidFragment

    private constructor() {
        super();
    }

    override toString(): string {
        return "Void";
    }

    override type(): FragmentType<VoidFragment> {
        return VOID;
    }
}

const VOID = register("void", 0x4400aa, unit(VoidFragment.INSTANCE));
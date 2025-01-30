import {
    Endec,
    listOf,
    Nullable,
    Optional,
    PrimitiveEndecs,
    StructEndec,
    StructEndecBuilder,
} from 'KEndec';
import { safeOptionalOf } from './endecTomfoolery';
import { Identifier } from './util';
import { Fragment } from './fragment/fragment';
import * as fragment from './fragment/fragment';

export class SerializedSpellInstruction {
    type: SpellInstructionType;
    fragment: Fragment<unknown> | null;

    constructor(type: SpellInstructionType, fragment: Fragment<unknown> | null) {
        this.type = type;
        this.fragment = fragment;
    }

    public static readonly endec = (
        fragmentEndecs: Map<Identifier, StructEndec<Fragment<unknown>>>
    ) =>
        StructEndecBuilder.of2(
            PrimitiveEndecs.INT.fieldOf('instruction_id', (s: SerializedSpellInstruction) =>
                SpellInstructionType.getId(s.type)
            ),
            safeOptionalOf(fragment.endec(fragmentEndecs).nullableOf()).optionalFieldOf(
                'fragment',
                (s: SerializedSpellInstruction) => Optional.ofNullable(s.fragment),
                Optional.empty
            ),
            (id, optionalFragment) =>
                new SerializedSpellInstruction(
                    SpellInstructionType.fromId(id),
                    optionalFragment?.orElse(null) ?? null
                )
        );

    public toDeserialized(): SpellInstruction {
        switch (this.type) {
            case SpellInstructionType.FRAGMENT:
                return this.fragment!;
            case SpellInstructionType.ENTER_SCOPE:
                return new EnterScopeInstruction();
            case SpellInstructionType.EXIT_SCOPE:
                return new ExitScopeInstruction();
        }
    }
}

export type SpellInstruction = EnterScopeInstruction | ExitScopeInstruction | Fragment<unknown>;

export function serialize(instruction: SpellInstruction) {
    if ("asSerialized" in instruction) {
        return instruction.asSerialized()
    } else {
        return new SerializedSpellInstruction(SpellInstructionType.FRAGMENT, instruction);
    }
}

export const stackEndec = (fragmentEndecs: Map<Identifier, StructEndec<Fragment<unknown>>>) =>
    SerializedSpellInstruction.endec(fragmentEndecs).listOf().xmap(
        (l) => {
            return l
                .asJsReadonlyArrayView()
                .map((instr: SerializedSpellInstruction) => instr.toDeserialized());
        },
        (s: Array<SpellInstruction>) =>
            listOf(s.map((instr: SpellInstruction) => serialize(instr)))
    );

export class EnterScopeInstruction {
    asSerialized(): SerializedSpellInstruction {
        return new SerializedSpellInstruction(SpellInstructionType.ENTER_SCOPE, null);
    }
}

export class ExitScopeInstruction {
    asSerialized(): SerializedSpellInstruction {
        return new SerializedSpellInstruction(SpellInstructionType.EXIT_SCOPE, null);
    }
}

export enum SpellInstructionType {
    FRAGMENT = 1,
    ENTER_SCOPE = 2,
    EXIT_SCOPE = 3,
}

export namespace SpellInstructionType {
    export function getId(type: SpellInstructionType): number {
        return type;
    }

    export function fromId(id: number): SpellInstructionType {
        switch (id) {
            case 1:
                return SpellInstructionType.FRAGMENT;
            case 2:
                return SpellInstructionType.ENTER_SCOPE;
            case 3:
                return SpellInstructionType.EXIT_SCOPE;
            default:
                throw new Error(`Unexpected spell instruction type id: ${id}`);
        }
    }
}

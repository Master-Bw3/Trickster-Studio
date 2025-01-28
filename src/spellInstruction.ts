import {
    Endec,
    KtList,
    Nullable,
    Optional,
    PrimitiveEndecs,
    StructEndec,
    StructEndecBuilder,
} from 'KEndec';
import Fragment from './fragment/Fragment';
import { safeOptionalOf } from './endecTomfoolery';
import { memoize } from './util';

export class SerializedSpellInstruction {
    type: SpellInstructionType;
    fragment: Fragment | null;

    constructor(type: SpellInstructionType, fragment: Fragment | null) {
        this.type = type;
        this.fragment = fragment;
    }

    public static readonly endec = memoize(() =>
        StructEndecBuilder.of2(
            PrimitiveEndecs.INT.fieldOf('instruction_id', (s: SerializedSpellInstruction) =>
                SpellInstructionType.getId(s.type)
            ),
            safeOptionalOf(Fragment.ENDEC.nullableOf()).optionalFieldOf(
                'fragment',
                (s: SerializedSpellInstruction) => Optional.ofNullable(s.fragment),
                Optional.empty
            ),
            (id, optionalFragment) =>
                new SerializedSpellInstruction(
                    SpellInstructionType.fromId(id),
                    optionalFragment?.orElse(null) ?? null
                )
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

export abstract class SpellInstruction {
    public static readonly stackEndec = memoize(() =>
        SerializedSpellInstruction.endec()
            .listOf()
            .xmap(
                (l) => {
                    return l
                        .asJsReadonlyArrayView()
                        .map((instr: SerializedSpellInstruction) => instr.toDeserialized());
                },
                (s: Array<SpellInstruction>) =>
                    KtList.getInstance().fromJsArray(
                        s.map((instr: SpellInstruction) => instr.asSerialized())
                    )
            )
    );

    abstract asSerialized(): SerializedSpellInstruction;
}

export class EnterScopeInstruction extends SpellInstruction {
    override asSerialized(): SerializedSpellInstruction {
        return new SerializedSpellInstruction(SpellInstructionType.ENTER_SCOPE, null);
    }
}

export class ExitScopeInstruction extends SpellInstruction {
    override asSerialized(): SerializedSpellInstruction {
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

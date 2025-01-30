import { Optional } from "KEndec";
import { EnterScopeInstruction, ExitScopeInstruction, SpellInstruction } from "./spellInstruction";
import { Fragment } from "./fragment/fragment";
import { SpellPartData } from "./fragment/trickster/spellPart";
import * as spellPart from "./fragment/trickster/spellPart";

export class SpellUtils {
    static decodeInstructions(instructions: Array<SpellInstruction>, scope: Array<number>, inputs: Array<Fragment<unknown>>, overrideReturnValue: Optional<Fragment>): SpellPart {
        var children: Array<Fragment<SpellPartData>> = [];
        instructions = [...instructions];
        scope = [...scope];

        for (const input of inputs) {
            children.push(spellPart.of(input, []));
        }

        while (instructions.length > 0) {
            var inst = instructions.pop();

            if (inst instanceof EnterScopeInstruction) {
                scope.push(0);
            } else if (inst instanceof ExitScopeInstruction) {
                scope.pop();

                if (scope.length != 0)
                    scope.push(scope.pop()! + 1);
            } else {
                let args;
                {
                    let _args: Array<Fragment<SpellPartData>> = [];
                    for (let i = scope[scope.length - 1]; i > 0; i--)
                        _args.push(children.pop() as Fragment<SpellPartData>);
                    args = _args.toReversed();
                }

                children.push(spellPart.of(inst as Fragment<unknown>, args));
            }
        }

        return overrideReturnValue.map(fragment => spellPart.of(fragment, children.toReversed())).orElse(children.pop())!;
    }

    static flattenNode(head: Fragment<SpellPartData>): Array<SpellInstruction> {
        const instructions: Array<SpellInstruction>  = [];
        const headStack: Array<Fragment<SpellPartData>> = [];
        const indexStack: Array<number> = [];

        headStack.push(head);
        indexStack.push(-1);

        while (headStack.length != 0) {
            let currentNode: Fragment<SpellPartData> = headStack[headStack.length - 1];
            let currentIndex = indexStack.pop()!;

            if (currentIndex == -1) {
                instructions.push(new ExitScopeInstruction());
                instructions.push(currentNode.data.glyph);
            }

            currentIndex++;

            if (currentIndex < currentNode.data.subParts.length) {
                headStack.push(currentNode.data.subParts.toReversed()[currentIndex]);
                indexStack.push(currentIndex);
                indexStack.push(-1);
            } else {
                headStack.pop();
                instructions.push(new EnterScopeInstruction());
            }
        }

        return instructions;
    }
}
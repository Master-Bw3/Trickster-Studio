import { Optional } from "KEndec";
import Fragment from "./fragment/Fragment";
import { EnterScopeInstruction, ExitScopeInstruction, SpellInstruction } from "./spellInstruction";
import SpellPart from "./fragment/SpellPart";

export class SpellUtils {
    static decodeInstructions(instructions: Array<SpellInstruction>, scope: Array<number>, inputs: Array<Fragment>, overrideReturnValue: Optional<Fragment>): SpellPart {
        var children: Array<SpellPart> = [];
        instructions = [...instructions];
        scope = [...scope];

        for (const input of inputs) {
            children.push(new SpellPart(input));
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
                    let _args: Array<SpellPart> = [];
                    for (let i = scope[scope.length - 1]; i > 0; i--)
                        _args.push(children.pop() as SpellPart);
                    args = _args.toReversed();
                }

                children.push(new SpellPart(inst as Fragment, args));
            }
        }

        return overrideReturnValue.map(fragment => new SpellPart(fragment, children.toReversed())).orElse(children.pop())!;
    }

    static flattenNode(head: SpellPart): Array<SpellInstruction> {
        const instructions: Array<SpellInstruction>  = [];
        const headStack: Array<SpellPart> = [];
        const indexStack: Array<number> = [];

        headStack.push(head);
        indexStack.push(-1);

        while (headStack.length != 0) {
            let currentNode: SpellPart = headStack[headStack.length - 1];
            let currentIndex = indexStack.pop()!;

            if (currentIndex == -1) {
                instructions.push(new ExitScopeInstruction());
                instructions.push(currentNode.glyph);
            }

            currentIndex++;

            if (currentIndex < currentNode.subParts.length) {
                headStack.push(currentNode.subParts.toReversed()[currentIndex]);
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
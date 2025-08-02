package me.maplesyrum.tricksterStudio

import me.maplesyrum.tricksterStudio.spell.executer.EnterScopeInstruction
import me.maplesyrum.tricksterStudio.spell.executer.ExitScopeInstruction
import me.maplesyrum.tricksterStudio.spell.executer.SpellInstruction
import me.maplesyrum.tricksterStudio.spell.fragment.Fragment
import me.maplesyrum.tricksterStudio.spell.fragment.SpellPart
import tree.maple.kendec.util.Optional

object SpellUtils {
    fun decodeInstructions(
        instructions: List<SpellInstruction>,
        scope: List<Int>,
        inputs: List<Fragment>,
        overrideReturnValue: Optional<Fragment>
    ): SpellPart {
        val children: ArrayDeque<SpellPart> = ArrayDeque<SpellPart>()
        val instructions = ArrayDeque(instructions)
        val scope = ArrayDeque(scope)

        for (input in inputs) {
            children.add(SpellPart(input))
        }

        while (instructions.isNotEmpty()) {
            val inst = instructions.removeLast()

            if (inst is EnterScopeInstruction) {
                scope.add(0)
            } else if (inst is ExitScopeInstruction) {
                scope.removeLast()

                if (!scope.isEmpty()) scope.add(scope.removeLast() + 1)
            } else {
                val args: List<SpellPart>
                run {
                    val _args: ArrayList<SpellPart> = ArrayList<SpellPart>()
                    for (i in scope.last() downTo 1) _args.add(children.removeLast())
                    args = _args.reversed()
                }

                children.add(SpellPart(inst as Fragment, args))
            }
        }

        return overrideReturnValue.map { fragment -> SpellPart(fragment, ArrayList(children).reversed()) }
            .orElse(children.removeLast())!!
    }

    fun flattenNode(head: SpellPart): ArrayDeque<SpellInstruction> {
        val instructions: ArrayDeque<SpellInstruction> = ArrayDeque()
        val headStack: ArrayDeque<SpellPart> = ArrayDeque()
        val indexStack: ArrayDeque<Int> = ArrayDeque()

        headStack.add(head)
        indexStack.add(-1)

        while (!headStack.isEmpty()) {
            val currentNode: SpellPart = headStack.last()
            var currentIndex: Int = indexStack.removeLast()

            if (currentIndex == -1) {
                instructions.add(ExitScopeInstruction())
                instructions.add(currentNode.glyph)
            }

            currentIndex++

            if (currentIndex < currentNode.subParts.size) {
                headStack.add(currentNode.subParts.reversed()[currentIndex])
                indexStack.add(currentIndex)
                indexStack.add(-1)
            } else {
                headStack.removeLast()
                instructions.add(EnterScopeInstruction())
            }
        }

        return instructions
    }
}

import SpellPart from "./Fragment/SpellPart";
import Pattern from "./Pattern";

export default class RevisionContext {
    updateSpell(rootSpellPart: SpellPart) {
        throw new Error("Method not implemented.");
    }
    updateSpellWithSpell(drawingPart: SpellPart | null, arg1: SpellPart) {
        throw new Error("Method not implemented.");
    }
    getMacros() : Map<Pattern, SpellPart> {
        throw new Error("Method not implemented.");
    }

}
import SpellPart from "./Fragment/SpellPart";
import Pattern from "./Pattern";

export default class RevisionContext {
    updateSpell(rootSpellPart: SpellPart) {
        
    }
    updateSpellWithSpell(drawingPart: SpellPart | null, arg1: SpellPart) {
        
    }
    getMacros() : Map<Pattern, SpellPart> {
       return new Map()
    }

}
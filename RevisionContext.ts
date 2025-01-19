import SpellPart from "./fragment/SpellPart";
import Pattern from "./fragment/Pattern";

export default class RevisionContext {
    updateSpell(rootSpellPart: SpellPart) {
        
    }
    updateSpellWithSpell(drawingPart: SpellPart | null, arg1: SpellPart) {
        
    }
    getMacros() : Map<Pattern, SpellPart> {
       return new Map()
    }

}
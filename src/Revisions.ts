import SpellPart from "./Fragment/SpellPart";
import Pattern from "./Pattern";
import RevisionContext from "./RevisionContext";

const Revisions = {
    EXECUTE_OFF_HAND: {
        pattern() {
            return new Pattern();
        },

        apply(a: RevisionContext, b: SpellPart, c: SpellPart) {},
    },

    lookup(pattern: Pattern): null | { apply(a: RevisionContext, b: SpellPart, c: SpellPart): SpellPart } {
        return null;
    },
};

export default Revisions;

import SpellPart from "./fragment/old-garbage/SpellPart";
import Pattern, { patternOf } from "./fragment/old-garbage/Pattern";
import RevisionContext from "./RevisionContext";

interface Revision {
    pattern(): Pattern;

    apply(ctx: RevisionContext, root: SpellPart, drawingPart: SpellPart): SpellPart;
}

const EXECUTE_OFF_HAND: Revision = {
    pattern() {
        return patternOf([4, 3, 0, 4, 5, 2, 4, 1])!;
    },

    apply(ctx: RevisionContext, root: SpellPart, drawingPart: SpellPart) {
        return drawingPart;
    },
};

const CREATE_SUBCIRCLE: Revision = {
    pattern(): Pattern {
        return patternOf([0, 4, 8, 7])!;
    },

    apply(ctx: RevisionContext, root: SpellPart, drawingPart: SpellPart): SpellPart {
        drawingPart.subParts = [...drawingPart.subParts, new SpellPart()];
        return root;
    },
};

const revisions: Map<Pattern, Revision> = new Map([
    [EXECUTE_OFF_HAND.pattern(), EXECUTE_OFF_HAND],
    [CREATE_SUBCIRCLE.pattern(), CREATE_SUBCIRCLE],
]);

function lookup(pattern: Pattern): Revision | null {
    for (const [key, revision] of revisions) {
        if (key.equals(pattern)) return revision
    }

    return null
}

export { EXECUTE_OFF_HAND, CREATE_SUBCIRCLE, revisions, lookup, Revision };

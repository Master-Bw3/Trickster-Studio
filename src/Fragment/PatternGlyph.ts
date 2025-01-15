import Pattern from "../Pattern";

export default class PatternGlyph{


    constructor(pattern: Array<number> | Pattern | null = null) {
        if (pattern === null) {
            console.log("Pattern is null");
        } else if (Array.isArray(pattern)) {
            console.log("Pattern is an array of numbers:", pattern);
        } else if (pattern instanceof Pattern) {
            console.log("Pattern is an instance of Pattern:", pattern);
        } else {
            throw new Error("Invalid pattern type");
        }
    }

}
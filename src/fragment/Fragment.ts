import { Text } from "pixi.js";

export default class Fragment {
    asFormattedText(): Text {
        throw new Error("Method not implemented.");
    }
    type(): FragmentType {
        throw ""
    }
}

export class FragmentType {

}

const fragmentTypes: Map<string, FragmentType> = new Map() 

function getKeyByValue<T, U>(map: Map<T, U>, value: U): T | null {
    for (let [key, val] of map.entries()) {
      if (val === value) {
        return key;
      }
    }
    return null; // Or undefined if you prefer
  }

export {
    fragmentTypes,
    getKeyByValue
}
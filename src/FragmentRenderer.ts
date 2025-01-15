import { Container, Texture } from "pixi.js"
import SpellCircleRenderer from "./SpellCircleRenderer"
import Fragment from "./Fragment/Fragment"

export default interface FragmentRenderer<T extends Fragment> {
    render(fragment: T, container: Container, x: number, y: number, size: number, alpha: number, textures: Map<string, Texture>, delegator: SpellCircleRenderer): void

    renderRedrawDots(): boolean
}

const fragmentRenderers: Map<string, FragmentRenderer<Fragment>> = new Map()

export {fragmentRenderers}


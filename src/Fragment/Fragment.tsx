import { Sprite } from 'pixi.js';
import { ReactElement } from 'react';

interface Fragment {
    renderAsGlyph(props: { x: number; y: number; size: number }): ReactElement;
}

export default Fragment;

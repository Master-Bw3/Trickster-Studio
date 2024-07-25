import React from 'react';
import Fragment from '../../Interpreter/Fragment';
import { Point } from 'pixi.js';
import { Drawing } from '../../App';

type GlyphProps<T extends Fragment> = {
    fragment: T;
    x: number;
    y: number;
    size: number;
    startingAngle: number;
    zIndex: number;
    drawing: Drawing;
    setDrawing: any;
    dotPositions: Array<Point>;
};

abstract class Glyph<T extends Fragment> extends React.Component<GlyphProps<T>> {}

export default Fragment;

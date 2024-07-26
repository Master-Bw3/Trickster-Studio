import React from 'react';
import Fragment from '../../Interpreter/Fragment';
import { Point } from 'pixi.js';
import { Drawing } from '../../App';

export type GlyphProps<T extends Fragment> = {
    fragment: T;
    x: number;
    y: number;
    size: number;
    zIndex: number;
    drawing: Drawing;
    setDrawing: any;
    dotPositions: Array<Point>;
};

export default abstract class Glyph<T extends Fragment> extends React.Component<GlyphProps<T>> {}

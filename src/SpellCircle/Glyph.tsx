import Fragment from '../Fragment/Fragment';

function Glyph(props: { glyph: Fragment; x: number; y: number; size: number }) {
    return props.glyph.renderAsGlyph(props);
}

export { Glyph };

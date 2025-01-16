import { Application, Assets, Container, Matrix, SCALE_MODES, Sprite, Texture } from "pixi.js";
import SpellPartWidget from "./SpellPartWidget";
import SpellPart from "./fragment/SpellPart";
import PatternGlyph from "./fragment/PatternGlyph";
import RevisionContext from "./RevisionContext";
import { decodeSpell } from "./serialization";

(async () => {
    const params = new URLSearchParams(window.location.search);
    const encoded = params.get("spell");

    try {
        if (encoded == null) throw new Error();
        await spellPartDisplay(decodeSpell(encoded));
    } catch (e: any) {
        createSpellForm()
    }
})();

function createSpellForm() {
    document.getElementById("pixi-content")?.remove()
    document.body.setAttribute('data-bs-theme', 'dark');


    // Create form container
    const container = document.createElement('div');
    container.classList.add('container', 'mt-5');

    // Create a row and column to center the form
    const row = document.createElement('div');
    row.classList.add('row', 'justify-content-center');
    const col = document.createElement('div');
    col.classList.add('col-md-6');
    row.appendChild(col);
    container.appendChild(row);

    // Create card for the form
    const card = document.createElement('div');
    card.classList.add('card');
    col.appendChild(card);

    // Create card header with title
    const cardHeader = document.createElement('div');
    cardHeader.classList.add('card-header', 'text-center');
    const title = document.createElement('h3');
    title.textContent = 'Trickster Spell Viewer';
    cardHeader.appendChild(title);
    card.appendChild(cardHeader);

    // Create card body with form
    const cardBody = document.createElement('div');
    cardBody.classList.add('card-body');
    card.appendChild(cardBody);

    // Create form
    const form = document.createElement('form');
    form.id = 'spellForm';
    cardBody.appendChild(form);

    // Create input field for spell
    const inputGroup = document.createElement('div');
    inputGroup.classList.add('mb-3');
    const input = document.createElement('input');
    input.type = 'text';
    input.id = 'spell';
    input.classList.add('form-control');
    input.placeholder = 'Enter spell';
    input.required = true;
    inputGroup.appendChild(input);
    form.appendChild(inputGroup);

    // Create submit button
    const submitButton = document.createElement('button');
    submitButton.type = 'submit';
    submitButton.classList.add('btn', 'btn-primary', 'w-100');
    submitButton.textContent = 'Submit';
    form.appendChild(submitButton);

    // Append the container to the body
    document.body.appendChild(container);

    // Add event listener for form submission
    form.addEventListener('submit', function (event) {
        event.preventDefault();
        const spell = (document.getElementById('spell') as HTMLInputElement).value;
        const addr = window.location.origin;
        window.location.href = `${addr}/?spell=${encodeURIComponent(spell)}`;
    });
}

async function spellPartDisplay(spellPart: SpellPart) {
    const app = new Application();
    const canvasElement = document.getElementById("pixi-canvas") as HTMLCanvasElement;

    await app.init({
        canvas: canvasElement,
        resolution: window.devicePixelRatio || 1,
        autoDensity: true,
        backgroundColor: 0x151515,
        width: window.innerWidth,
        height: window.innerHeight,
        resizeTo: window,
    });

    const textures: Map<string, Texture> = new Map();

    textures.set("circle_48", await Assets.load("./circle_48.png"));
    textures.get("circle_48")!.source.scaleMode = "nearest";

    //query
    const params = new URLSearchParams(window.location.search);

    const widget = new SpellPartWidget(spellPart, app.canvas.width / 2, app.canvas.height / 2, 64 * 5, new RevisionContext(), true);

    widget.render(app.stage, 0, 0, 0, app.canvas.height, textures);

    canvasElement.addEventListener("wheel", (e) => {
        widget.mouseScrolled(e.x, e.y, -e.deltaY / 100);

        app.stage.removeChildren();
        widget.render(app.stage, e.x, e.y, 0, app.canvas.height, textures);
    });

    let mouseDown = false;

    canvasElement.addEventListener("mousedown", (e) => {
        const button = e.button;
        widget.mouseClicked(e.x, e.y, button);
        mouseDown = true;

        app.stage.removeChildren();
        widget.render(app.stage, e.x, e.y, 0, app.canvas.height, textures);
    });

    canvasElement.addEventListener("mouseup", (e) => {
        const button = e.button;
        widget.mouseReleased(e.x, e.y, button);
        mouseDown = false;

        app.stage.removeChildren();
        widget.render(app.stage, e.x, e.y, 0, app.canvas.height, textures);
    });

    canvasElement.addEventListener("mousemove", (e) => {
        if (mouseDown) {
            const button = e.button;
            widget.mouseDragged(e.x, e.y, button, e.movementX, e.movementY);
        } else {
            widget.mouseMoved(e.x, e.y);
        }

        app.stage.removeChildren();
        widget.render(app.stage, e.x, e.y, 0, app.canvas.height, textures);
    });
}

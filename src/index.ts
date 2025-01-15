import { Application, Assets, Sprite } from 'pixi.js'

(async () => {
	const app = new Application();

	await app.init({
		canvas: document.getElementById("pixi-canvas") as HTMLCanvasElement,
		resolution: window.devicePixelRatio || 1,
		autoDensity: true,
		backgroundColor: 0x6495ed,
		width: 640,
		height: 480
	})


	const clampy: Sprite = new Sprite(await Assets.load("clampy.png"));

	clampy.anchor.set(0.5);

	clampy.x = app.screen.width / 2;
	clampy.y = app.screen.height / 2;

	app.stage.addChild(clampy);
})();

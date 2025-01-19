import topLevelAwait from "vite-plugin-top-level-await";
import { defineConfig } from 'vite'
import {ViteEjsPlugin} from "vite-plugin-ejs";
import wasm from "vite-plugin-wasm";


export default defineConfig({
  plugins: [
    wasm(),
    topLevelAwait(),
  ]
});
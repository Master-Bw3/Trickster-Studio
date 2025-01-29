import topLevelAwait from 'vite-plugin-top-level-await';
import { defineConfig } from 'vite';
import wasm from 'vite-plugin-wasm';
import tsconfigPaths from 'vite-tsconfig-paths';

export default defineConfig({
    plugins: [wasm(), topLevelAwait(), tsconfigPaths()],
    build: {
        outDir: 'dist',
    },
    test: {
        globals: true, // If you're using Vitest with globals
        setupFiles: ['./src/global.d.ts'],
        environment: 'jsdom', // or 'node', depending on your use case
    },
});

import {defineConfig} from "vite";
import react from "@vitejs/plugin-react";
import UnoCSS from 'unocss/vite'

export default defineConfig(async () => ({
    plugins: [
        UnoCSS(),
        react()
    ],
    clearScreen: false,
  
    // Tauri expects a fixed port, fail if that port is not available
    server: {
        port: 1420,
        strictPort: true,
    },
    envPrefix: ["VITE_", "TAURI_"],
}));

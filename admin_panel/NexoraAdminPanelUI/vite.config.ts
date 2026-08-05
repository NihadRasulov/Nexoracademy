import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'node:path'
import fs from 'node:fs'

function readAdminBasePath(): string {
  const settingsPath = path.resolve(
    __dirname,
    '../NexoraAdminPanel/NexoraAdminPanel/src/NexoraAcademy.AdminBff/appsettings.json',
  )
  const settings = JSON.parse(fs.readFileSync(settingsPath, 'utf8')) as {
    AdminSettings?: { SecretPath?: string }
  }
  const secretPath = process.env.AdminSettings__SecretPath ?? settings.AdminSettings?.SecretPath

  if (!secretPath || !/^[a-z0-9](?:[a-z0-9-]{10,62}[a-z0-9])$/.test(secretPath)) {
    throw new Error('AdminSettings:SecretPath is missing or invalid.')
  }

  return `/${secretPath}`
}

export default defineConfig(({ command }) => {
  const adminBasePath = command === 'serve' ? readAdminBasePath() : undefined

  return {
    base: './',
    plugins: [
      command === 'serve' && {
        name: 'admin-base-path',
        transformIndexHtml: (html: string) =>
          html.replace('<head>', `<head>\n    <base href="${adminBasePath}/" />`),
      },
      react(),
      tailwindcss(),
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      port: 5173,
      strictPort: true,
    },
  }
})

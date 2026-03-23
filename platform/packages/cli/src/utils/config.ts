import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join, resolve } from 'node:path'

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface TemplateEntry {
  label: string
  repo: string
  branch: string
  description: string
}

export interface CliConfig {
  dynamia: { version: string; docsUrl: string }
  java: { version: string; sdkmanCandidate: string }
  node: { minimumVersion: string }
  installer: { sdkmanUrl: string; fnmUrl: string }
  templates: {
    backend: Record<string, TemplateEntry>
    frontend: Record<string, TemplateEntry>
  }
  npm: Record<string, { package: string; version: string }>
  vite: { fallbackEnabled: boolean; templates: Record<string, string> }
  tokens: Record<string, string>
}

// ---------------------------------------------------------------------------
// Parser
// ---------------------------------------------------------------------------

function parseProperties(content: string): Record<string, string> {
  const result: Record<string, string> = {}
  for (const raw of content.split('\n')) {
    const line = raw.trim()
    if (!line || line.startsWith('#')) continue
    const eqIdx = line.indexOf('=')
    if (eqIdx === -1) continue
    const key = line.slice(0, eqIdx).trim()
    const value = line.slice(eqIdx + 1).trim()
    result[key] = value
  }
  return result
}

// ---------------------------------------------------------------------------
// Loader
// ---------------------------------------------------------------------------

export async function loadConfig(): Promise<CliConfig> {
  // Locate cli.properties relative to this compiled file (dist/utils/config.js)
  const __filename = fileURLToPath(import.meta.url)
  const __dirname = dirname(__filename)
  const propsPath = resolve(__dirname, '../../cli.properties')

  const content = readFileSync(propsPath, 'utf-8')
  const props = parseProperties(content)

  // --- helpers ---
  const get = (key: string, fallback = ''): string => props[key] ?? fallback

  // --- templates ---
  const backendTemplates: Record<string, TemplateEntry> = {}
  const frontendTemplates: Record<string, TemplateEntry> = {}

  for (const [key, value] of Object.entries(props)) {
    const backMatch = key.match(/^template\.backend\.(\w+)\.(label|repo|branch|description)$/)
    if (backMatch) {
      const id = backMatch[1]!
      const field = backMatch[2] as keyof TemplateEntry
      if (!backendTemplates[id]) {
        backendTemplates[id] = { label: '', repo: '', branch: 'main', description: '' }
      }
      backendTemplates[id]![field] = value
      continue
    }

    const frontMatch = key.match(/^template\.frontend\.(\w+)\.(label|repo|branch|description)$/)
    if (frontMatch) {
      const id = frontMatch[1]!
      const field = frontMatch[2] as keyof TemplateEntry
      if (!frontendTemplates[id]) {
        frontendTemplates[id] = { label: '', repo: '', branch: 'main', description: '' }
      }
      frontendTemplates[id]![field] = value
    }
  }

  // --- npm packages ---
  const npmPackages: Record<string, { package: string; version: string }> = {}
  for (const [key, value] of Object.entries(props)) {
    const m = key.match(/^npm\.(\w[\w-]*)\.(\w+)$/)
    if (m) {
      const id = m[1]!
      const field = m[2]!
      if (!npmPackages[id]) npmPackages[id] = { package: '', version: '' }
      if (field === 'package' || field === 'version') {
        npmPackages[id]![field] = value
      }
    }
  }

  // --- vite fallback templates ---
  const viteTemplates: Record<string, string> = {}
  for (const [key, value] of Object.entries(props)) {
    const m = key.match(/^vite\.templates\.(\w+)$/)
    if (m) {
      viteTemplates[m[1]!] = value
    }
  }

  // --- tokens ---
  const tokens: Record<string, string> = {}
  for (const [key, value] of Object.entries(props)) {
    if (key.startsWith('token.')) {
      // Convert "token.group.id" → "groupId" camelCase key for easy lookup
      const parts = key.slice('token.'.length).split('.').filter((p) => p.length > 0)
      const camel = parts
        .map((p, i) => (i === 0 ? p : p.charAt(0).toUpperCase() + p.slice(1)))
        .join('')
      tokens[camel] = value
    }
  }

  return {
    dynamia: {
      version: get('dynamia.version'),
      docsUrl: get('dynamia.docs.url'),
    },
    java: {
      version: get('java.version'),
      sdkmanCandidate: get('java.sdkman.candidate'),
    },
    node: {
      minimumVersion: get('node.minimum.version'),
    },
    installer: {
      sdkmanUrl: get('installer.sdkman.url'),
      fnmUrl: get('installer.fnm.url'),
    },
    templates: {
      backend: backendTemplates,
      frontend: frontendTemplates,
    },
    npm: npmPackages,
    vite: {
      fallbackEnabled: get('vite.fallback.enabled') === 'true',
      templates: viteTemplates,
    },
    tokens,
  }
}

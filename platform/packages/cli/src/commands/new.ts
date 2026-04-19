import { input, select, confirm } from '@inquirer/prompts'
import { join } from 'node:path'
import { loadConfig, type CliConfig } from '../utils/config.js'
import { runChecks } from '../utils/env.js'
import { banner, info, error, beta, errorMessage, errorWithCode } from '../utils/logger.js'
import { generateBackend } from '../generators/backend.js'
import { generateFrontend } from '../generators/frontend.js'

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** Fetch the latest Spring Boot version from Spring Initializr metadata. */
function normalizeSpringBootVersion(version: string): string {
  // Initializr can return legacy values like "4.0.5.RELEASE"; Maven/Gradle expect "4.0.5".
  return version.trim().replace(/\.RELEASE$/i, '')
}

async function fetchSpringBootVersion(_config: CliConfig): Promise<string> {
  try {
    const metaRes = await fetch('https://start.spring.io/metadata/client')
    if (metaRes.ok) {
      const meta = await metaRes.json() as Record<string, unknown>
      const bootVersion = (meta as { bootVersion?: { default?: string } }).bootVersion?.default
      if (bootVersion) return normalizeSpringBootVersion(bootVersion)
    }
  } catch {
    // ignore — use fallback
  }
  // Fallback from config (spring.boot.version kept as reference)
  return '4.0.5'
}

/** Print the final success message. */
function printSuccessMessage(opts: {
  projectName: string
  generateBackend: boolean
  generateFrontend: boolean
  language: string
  framework: string
  packageManager: string
  config: CliConfig
  springBootVersion: string
}): void {
  const {
    projectName,
    generateBackend: doBackend,
    generateFrontend: doFrontend,
    language,
    framework,
    packageManager: pm,
    config,
    springBootVersion,
  } = opts

  const displaySpringBootVersion = normalizeSpringBootVersion(springBootVersion)

  console.log('')
  console.log(`✓ Project "${projectName}" created successfully!`)
  console.log('')
  console.log('  📁 Structure:')
  console.log(`     ${projectName}/`)
  if (doBackend) {
    console.log(`     ├── backend/     (${language.charAt(0).toUpperCase() + language.slice(1)} · Spring Boot ${displaySpringBootVersion} · Dynamia Tools ${config.dynamia.version})`)
  }
  if (doFrontend) {
    const frontendLabel = config.templates.frontend[framework]?.label ?? framework
    const sdkVersion = config.npm['vue']?.version ?? config.npm['sdk']?.version ?? config.dynamia.version
    const prefix = doBackend ? '└' : '├'
    console.log(`     ${prefix}── frontend/    (${frontendLabel} · Vite · @dynamia-tools/${framework === 'vue' ? 'vue' : 'sdk'} ${sdkVersion})`)
  }
  console.log('')
  console.log('  🚀 Next steps:')
  console.log('')
  if (doBackend) {
    console.log('     Backend:')
    console.log(`       cd ${projectName}/backend`)
    console.log('       ./mvnw spring-boot:run')
    console.log('')
  }
  if (doFrontend) {
    const installCmd = pm === 'npm' ? 'npm install' : pm === 'yarn' ? 'yarn' : 'pnpm install'
    const devCmd = pm === 'npm' ? 'npm run dev' : pm === 'yarn' ? 'yarn dev' : 'pnpm dev'
    console.log('     Frontend:')
    console.log(`       cd ${projectName}/frontend`)
    console.log(`       ${installCmd} && ${devCmd}`)
    console.log('')
  }
  console.log(`  📖 Docs: ${config.dynamia.docsUrl}`)
  console.log('')
}

// ---------------------------------------------------------------------------
// Command entry point
// ---------------------------------------------------------------------------

export async function runNew(): Promise<void> {
  banner()

  // Load configuration from cli.properties
  let config: CliConfig
  try {
    config = await loadConfig()
  } catch (err) {
    const reason = err instanceof Error ? err.message : 'Unknown configuration error.'
    errorWithCode('DT-CONFIG-001', `Failed to load CLI configuration: ${reason}`)
  }

  if (config.beta.enabled && config.beta.introMessageEnabled) {
    beta('Dynamia Tools CLI is in beta. Some features may still be under active development.')
  }

  // Environment checks (git missing = hard stop)
  await runChecks()

  // --- Step 1: Project name ---
  const projectName = await input({
    message: 'Project name:',
    validate: (value: string) => {
      if (!value.trim()) return 'Project name cannot be empty'
      if (!/^[a-z0-9-]+$/.test(value.trim())) {
        return 'Only lowercase letters, numbers, and hyphens are allowed'
      }
      return true
    },
  })

  // --- Step 2: What to generate ---
  type ScaffoldChoice = 'both' | 'backend' | 'frontend'
  const scaffoldChoice = await select<ScaffoldChoice>({
    message: 'What do you want to scaffold?',
    choices: [
      { name: 'Backend + Frontend', value: 'both' },
      { name: 'Backend only', value: 'backend' },
      { name: 'Frontend only', value: 'frontend' },
    ],
  })

  const doBackend = scaffoldChoice === 'both' || scaffoldChoice === 'backend'
  const doFrontend = scaffoldChoice === 'both' || scaffoldChoice === 'frontend'

  // --- Step 3: Backend language ---
  let language = 'java'
  let groupId = 'com.example'
  let artifactId = projectName
  let version = '1.0.0-SNAPSHOT'
  let description = ''

  if (doBackend) {
    const backendTemplates = config.templates.backend
    const languageChoices = Object.entries(backendTemplates).map(([id, entry]) => ({
      name: entry.label,
      value: id,
      description: entry.enabled ? entry.description : `${entry.description} — ${entry.availabilityMessage}`,
      disabled: entry.enabled ? false : entry.availabilityMessage,
    }))

    if (!languageChoices.some((choice) => !choice.disabled)) {
      errorWithCode('DT-BACKEND-005', 'Backend templates are not available yet. Please try frontend only for now.')
    }

    language = await select({
      message: 'Backend language:',
      choices: languageChoices,
    })

    // --- Step 4: Maven coordinates ---
    groupId = await input({
      message: 'Group ID:',
      default: 'com.example',
    })

    artifactId = await input({
      message: 'Artifact ID:',
      default: projectName,
    })

    version = await input({
      message: 'Version:',
      default: '1.0.0-SNAPSHOT',
    })

    description = await input({
      message: 'Description (optional):',
      default: '',
    })
  }

  // --- Step 5: Frontend framework ---
  let framework = 'vue'
  let packageManager = 'pnpm'

  if (doFrontend) {
    const frontendTemplates = config.templates.frontend
    const frameworkChoices = Object.entries(frontendTemplates).map(([id, entry]) => ({
      name: entry.label,
      value: id,
      description: entry.enabled ? entry.description : `${entry.description} — ${entry.availabilityMessage}`,
      disabled: entry.enabled ? false : entry.availabilityMessage,
    }))

    if (!frameworkChoices.some((choice) => !choice.disabled)) {
      errorWithCode('DT-FRONTEND-005', 'Frontend templates are not available yet. Please try again soon.')
    }

    framework = await select({
      message: 'Frontend framework:',
      choices: frameworkChoices,
    })

    // --- Step 6: Package manager ---
    packageManager = await select({
      message: 'Package manager:',
      choices: [
        { name: 'pnpm', value: 'pnpm' },
        { name: 'npm', value: 'npm' },
        { name: 'yarn', value: 'yarn' },
      ],
    })
  }

  // --- Step 7: Resolve Spring Boot version for backend summary and generation ---
  let springBootVersion = ''
  if (doBackend) {
    springBootVersion = normalizeSpringBootVersion(await fetchSpringBootVersion(config))
  }

  // --- Step 8: Confirm ---
  console.log('')
  console.log('  Summary:')
  console.log(`    Project:   ${projectName}`)
  if (doBackend) {
    console.log(`    Backend:   ${language} | ${groupId}:${artifactId}:${version} | Spring Boot ${springBootVersion}`)
  }
  if (doFrontend) {
    console.log(`    Frontend:  ${framework} | ${packageManager}`)
  }
  console.log('')

  const confirmed = await confirm({
    message: 'Generate project?',
    default: true,
  })

  if (!confirmed) {
    info('Cancelled.')
    process.exit(0)
  }

  // Target directory is CWD / projectName
  const targetDir = join(process.cwd(), projectName)

  // Generate backend
  try {
    if (doBackend) {
      await generateBackend({
        projectName,
        language,
        groupId,
        artifactId,
        version,
        description,
        targetDir: join(targetDir, 'backend'),
        config,
        springBootVersion,
      })
    }

    // Generate frontend
    if (doFrontend) {
      await generateFrontend({
        projectName,
        framework,
        packageManager,
        targetDir: join(targetDir, 'frontend'),
        config,
      })
    }
  } catch (err) {
    const reason = errorMessage(err, 'DT-RUN-001')
    error(`${reason}\nIf this keeps happening, please open an issue at https://github.com/dynamiatools/framework/issues`)
  }

  printSuccessMessage({
    projectName,
    generateBackend: doBackend,
    generateFrontend: doFrontend,
    language,
    framework,
    packageManager,
    config,
    springBootVersion,
  })
}

import { input, select, confirm } from '@inquirer/prompts'
import { join } from 'node:path'
import { existsSync } from 'node:fs'
import { parseArgs } from 'node:util'
import { execa } from 'execa'
import { loadConfig, type CliConfig } from '../utils/config.js'
import { runChecks } from '../utils/env.js'
import { banner, info, error, beta, errorMessage, errorWithCode, success, warn } from '../utils/logger.js'
import { generateBackend } from '../generators/backend.js'
import { generateFrontend } from '../generators/frontend.js'

// ---------------------------------------------------------------------------
// Non-interactive flags
// ---------------------------------------------------------------------------

type ScaffoldChoice = 'both' | 'backend' | 'frontend'
type PackageManager = 'pnpm' | 'npm' | 'yarn'

export interface NewCommandFlags {
  name?: string
  scaffold?: ScaffoldChoice
  backendLang?: string
  groupId?: string
  artifactId?: string
  version?: string
  description?: string
  frontend?: string
  pm?: PackageManager
  yes?: boolean
  git?: boolean
}

/**
 * Parse CLI flags for `dynamia new`, allowing scripted/CI usage without prompts.
 * Any flag left unset falls back to its interactive prompt.
 */
export function parseNewFlags(argv: string[]): NewCommandFlags {
  const { values } = parseArgs({
    args: argv,
    options: {
      name: { type: 'string' },
      scaffold: { type: 'string' },
      'backend-lang': { type: 'string' },
      'group-id': { type: 'string' },
      'artifact-id': { type: 'string' },
      version: { type: 'string' },
      description: { type: 'string' },
      frontend: { type: 'string' },
      pm: { type: 'string' },
      yes: { type: 'boolean' },
      git: { type: 'boolean' },
      'no-git': { type: 'boolean' },
    },
    allowPositionals: true,
    strict: false,
  })

  const scaffold = values.scaffold as string | undefined
  if (scaffold !== undefined && scaffold !== 'both' && scaffold !== 'backend' && scaffold !== 'frontend') {
    errorWithCode('DT-COMMAND-003', `--scaffold must be one of: both, backend, frontend (got "${scaffold}")`)
  }

  const pm = values.pm as string | undefined
  if (pm !== undefined && pm !== 'pnpm' && pm !== 'npm' && pm !== 'yarn') {
    errorWithCode('DT-COMMAND-003', `--pm must be one of: pnpm, npm, yarn (got "${pm}")`)
  }

  return {
    name: values.name as string | undefined,
    scaffold: scaffold as ScaffoldChoice | undefined,
    backendLang: values['backend-lang'] as string | undefined,
    groupId: values['group-id'] as string | undefined,
    artifactId: values['artifact-id'] as string | undefined,
    version: values.version as string | undefined,
    description: values.description as string | undefined,
    frontend: values.frontend as string | undefined,
    pm: pm as PackageManager | undefined,
    yes: values.yes as boolean | undefined,
    git: values['no-git'] ? false : (values.git as boolean | undefined),
  }
}

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
    const metaRes = await fetch('https://start.spring.io/metadata/client', {
      signal: AbortSignal.timeout(4000),
    })
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

export async function runNew(argv: string[] = []): Promise<void> {
  banner()

  const flags = parseNewFlags(argv)

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
  await runChecks(config)

  // --- Step 1: Project name ---
  const validateProjectName = (value: string): true | string => {
    if (!value.trim()) return 'Project name cannot be empty'
    if (!/^[a-z0-9-]+$/.test(value.trim())) {
      return 'Only lowercase letters, numbers, and hyphens are allowed'
    }
    return true
  }

  let projectName: string
  if (flags.name !== undefined) {
    const validation = validateProjectName(flags.name)
    if (validation !== true) errorWithCode('DT-COMMAND-003', `--name: ${validation}`)
    projectName = flags.name.trim()
  } else {
    projectName = await input({ message: 'Project name:', validate: validateProjectName })
  }

  // --- Step 2: What to generate ---
  const scaffoldChoice: ScaffoldChoice = flags.scaffold ?? await select<ScaffoldChoice>({
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

    if (flags.backendLang !== undefined) {
      const chosen = languageChoices.find((choice) => choice.value === flags.backendLang)
      if (!chosen) errorWithCode('DT-COMMAND-003', `--backend-lang "${flags.backendLang}" is not a known backend template.`)
      if (chosen.disabled) errorWithCode('DT-COMMAND-003', `--backend-lang "${flags.backendLang}": ${chosen.disabled}`)
      language = flags.backendLang
    } else {
      language = await select({
        message: 'Backend language:',
        choices: languageChoices,
      })
    }

    // --- Step 4: Maven coordinates ---
    groupId = flags.groupId ?? await input({
      message: 'Group ID:',
      default: 'com.example',
    })

    artifactId = flags.artifactId ?? await input({
      message: 'Artifact ID:',
      default: projectName,
    })

    version = flags.version ?? await input({
      message: 'Version:',
      default: '1.0.0-SNAPSHOT',
    })

    description = flags.description ?? await input({
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

    if (flags.frontend !== undefined) {
      const chosen = frameworkChoices.find((choice) => choice.value === flags.frontend)
      if (!chosen) errorWithCode('DT-COMMAND-003', `--frontend "${flags.frontend}" is not a known frontend template.`)
      if (chosen.disabled) errorWithCode('DT-COMMAND-003', `--frontend "${flags.frontend}": ${chosen.disabled}`)
      framework = flags.frontend
    } else {
      framework = await select({
        message: 'Frontend framework:',
        choices: frameworkChoices,
      })
    }

    // --- Step 6: Package manager ---
    packageManager = flags.pm ?? await select({
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

  const confirmed = flags.yes ?? await confirm({
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

  const initGit = flags.git ?? await confirm({
    message: 'Initialize a Git repository in the project root?',
    default: true,
  })

  if (!initGit) {
    info('Git initialization skipped.')
    return
  }

  const gitDir = join(targetDir, '.git')
  if (existsSync(gitDir)) {
    info('Git repository is already initialized.')
    return
  }

  try {
    await execa('git', ['init'], { cwd: targetDir, stdout: 'pipe', stderr: 'pipe' })
    success(`Git repository initialized at ${targetDir}`)
  } catch (err) {
    warn(`Project was created, but Git initialization failed: ${errorMessage(err, 'DT-RUN-001')}`)
  }
}

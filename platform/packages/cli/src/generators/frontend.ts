import { join } from 'node:path'
import { rmSync, mkdirSync } from 'node:fs'
import { execa } from 'execa'
import { type CliConfig } from '../utils/config.js'
import { replaceTokensInDir } from '../utils/replace.js'
import { spin, warn, success } from '../utils/logger.js'

// Fallback package names used when cli.properties entries are unavailable
const DEFAULT_SDK_PACKAGE = '@dynamia-tools/sdk'
const DEFAULT_UI_CORE_PACKAGE = '@dynamia-tools/ui-core'

export interface FrontendOptions {
  projectName: string
  framework: string
  packageManager: string
  targetDir: string
  config: CliConfig
}

/**
 * Clone a frontend template from GitHub.
 */
async function cloneFrontendTemplate(
  framework: string,
  targetDir: string,
  config: CliConfig,
): Promise<void> {
  const template = config.templates.frontend[framework]
  if (!template) {
    throw new Error(`Unknown frontend framework: ${framework}`)
  }

  await execa('git', [
    'clone',
    '--depth=1',
    '--branch',
    template.branch,
    template.repo,
    targetDir,
  ])

  rmSync(join(targetDir, '.git'), { recursive: true, force: true })
}

/**
 * Fallback: scaffold frontend using Vite when the git clone fails.
 */
async function generateWithVite(
  framework: string,
  targetDir: string,
  pm: string,
  config: CliConfig,
): Promise<void> {
  const viteTemplate = config.vite.templates[framework]
  if (!viteTemplate) {
    throw new Error(`No Vite template configured for framework: ${framework}`)
  }

  await execa(pm, ['create', 'vite@latest', targetDir, '--template', viteTemplate], {
    stdio: 'inherit',
  })

  // Install Dynamia SDK packages
  const sdkPkg = config.npm['sdk']?.package ?? DEFAULT_SDK_PACKAGE
  const uiPkg = config.npm['ui-core']?.package ?? DEFAULT_UI_CORE_PACKAGE
  await execa(pm, ['install', sdkPkg, uiPkg], {
    cwd: targetDir,
    stdio: 'inherit',
  })
}

/**
 * Generate the frontend project.
 * Tries to clone the template first; falls back to Vite if clone fails (and fallback is enabled).
 */
export async function generateFrontend(options: FrontendOptions): Promise<void> {
  const { projectName, framework, packageManager, targetDir, config } = options

  const tokenMap: Record<string, string> = {
    '{{PROJECT_NAME}}': projectName,
  }

  // Map standard tokens too
  for (const [key, placeholder] of Object.entries(config.tokens)) {
    if (key === 'projectName') {
      tokenMap[placeholder] = projectName
    }
  }

  mkdirSync(targetDir, { recursive: true })

  let cloneSuccess = false
  const spinner = spin(`Cloning frontend template (${framework})…`)

  try {
    await cloneFrontendTemplate(framework, targetDir, config)
    spinner.succeed('Frontend template cloned')
    cloneSuccess = true
  } catch (err) {
    spinner.warn(`Could not clone frontend template: ${(err as Error).message}`)
  }

  if (!cloneSuccess) {
    if (!config.vite.fallbackEnabled) {
      throw new Error('Frontend template clone failed and Vite fallback is disabled.')
    }
    warn('Falling back to Vite scaffold…')
    const viteSpinner = spin('Scaffolding with Vite…')
    try {
      await generateWithVite(framework, targetDir, packageManager, config)
      viteSpinner.succeed('Vite scaffold complete')
    } catch (err) {
      viteSpinner.fail('Vite scaffold failed')
      throw err
    }
  } else {
    // Replace tokens in cloned template
    const replaceSpinner = spin('Replacing project tokens…')
    try {
      replaceTokensInDir(targetDir, tokenMap)
      replaceSpinner.succeed('Tokens replaced')
    } catch (err) {
      replaceSpinner.fail('Token replacement failed')
      throw err
    }
  }

  success(`Frontend (${framework}) generated at ${targetDir}`)
}

import { join } from 'node:path'
import { rmSync, mkdirSync } from 'node:fs'
import { execa } from 'execa'
import { type CliConfig } from '../utils/config.js'
import { replaceTokensInDir } from '../utils/replace.js'
import { spin, warn, success, friendlyGitError, cliError, errorMessage } from '../utils/logger.js'
import { validateTemplateRepo } from '../utils/template-repo.js'

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
    throw cliError('DT-FRONTEND-001', `Unknown frontend framework: ${framework}.`)
  }
  if (!template.enabled) {
    throw cliError('DT-FRONTEND-002', template.availabilityMessage)
  }

  const validation = await validateTemplateRepo({
    repo: template.repo,
    branch: template.branch,
  })
  if (!validation.ok) {
    throw cliError(
      'DT-TEMPLATE-002',
      `Frontend template validation failed for ${template.repo}#${template.branch}. ${validation.reason ?? ''}`.trim(),
    )
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
    throw cliError('DT-FRONTEND-003', `No Vite template configured for framework: ${framework}`)
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
    spinner.warn(`Could not clone frontend template: ${friendlyGitError(err)}`)
  }

  if (!cloneSuccess) {
    if (!config.vite.fallbackEnabled) {
      throw cliError(
        'DT-FRONTEND-003',
        'Frontend template is currently not available and Vite fallback is disabled. Please try again later or enable vite.fallback.enabled in cli.properties.',
      )
    }
    warn('Template is unavailable right now, so we will use Vite fallback.')
    const viteSpinner = spin('Scaffolding with Vite…')
    try {
      await generateWithVite(framework, targetDir, packageManager, config)
      viteSpinner.succeed('Vite scaffold complete')
    } catch (err) {
      viteSpinner.fail('Vite scaffold failed')
      throw cliError('DT-FRONTEND-004', `Vite fallback failed: ${errorMessage(err)}`)
    }
  } else {
    // Replace tokens in cloned template
    const replaceSpinner = spin('Replacing project tokens…')
    try {
      replaceTokensInDir(targetDir, tokenMap)
      replaceSpinner.succeed('Tokens replaced')
    } catch (err) {
      replaceSpinner.fail('Token replacement failed')
      throw cliError('DT-FRONTEND-004', `Frontend token replacement failed: ${errorMessage(err)}`)
    }
  }

  success(`Frontend (${framework}) generated at ${targetDir}`)
}

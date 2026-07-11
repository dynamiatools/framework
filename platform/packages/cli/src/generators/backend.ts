import { join } from 'node:path'
import { execa } from 'execa'
import { rmSync, mkdirSync } from 'node:fs'
import { type CliConfig } from '../utils/config.js'
import { renameJavaPackages } from '../utils/replace.js'
import { spin, success, friendlyGitError, notAvailableYet, cliError, errorMessage } from '../utils/logger.js'
import { validateTemplateRepo } from '../utils/template-repo.js'

export interface BackendOptions {
  projectName: string
  language: string
  groupId: string
  artifactId: string
  version: string
  description: string
  targetDir: string
  config: CliConfig
  springBootVersion: string
}

/**
 * Clone a backend template from GitHub and rename packages/tokens.
 */
async function cloneBackendTemplate(
  language: string,
  targetDir: string,
  config: CliConfig,
): Promise<void> {
  const template = config.templates.backend[language]
  if (!template) {
    throw cliError('DT-BACKEND-001', `Unknown backend language: ${language}.`)
  }
  if (!template.enabled) {
    throw cliError('DT-BACKEND-002', template.availabilityMessage)
  }

  const validation = await validateTemplateRepo({
    repo: template.repo,
    branch: template.branch,
  })
  if (!validation.ok) {
    throw cliError(
      'DT-TEMPLATE-001',
      `Backend template validation failed for ${template.repo}#${template.branch}. ${validation.reason ?? ''}`.trim(),
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

  // Remove .git directory so the project starts fresh
  rmSync(join(targetDir, '.git'), { recursive: true, force: true })
}

/**
 * Generate the backend project by cloning a template and performing token replacement.
 */
export async function generateBackend(options: BackendOptions): Promise<void> {
  const {
    projectName,
    language,
    groupId,
    artifactId,
    version,
    description,
    targetDir,
    config,
    springBootVersion,
  } = options

  const spinner = spin(`Cloning backend template (${language})…`)

  try {
    mkdirSync(targetDir, { recursive: true })
    await cloneBackendTemplate(language, targetDir, config)
    spinner.succeed('Backend template cloned')
  } catch (err) {
    spinner.fail('Could not prepare backend template')
    const reason = friendlyGitError(err)
    if (config.beta.notAvailableMessageEnabled) {
      notAvailableYet('Automatic backend generation for this template setup', 'DT-BACKEND-003')
    }
    throw cliError('DT-BACKEND-003', `${reason}\nTip: Please verify template.backend.${language}.* entries in cli.properties.`)
  }

  const renameSpinner = spin('Renaming packages and replacing tokens…')
  try {
    await renameJavaPackages({
      projectDir: targetDir,
      projectName,
      groupId,
      artifactId,
      version,
      description,
      dynamiaVersion: config.dynamia.version,
      springBootVersion,
      language: language as 'java' | 'kotlin' | 'groovy',
      tokens: config.tokens,
    })
    renameSpinner.succeed('Packages renamed')
  } catch (err) {
    renameSpinner.fail('Failed to rename packages')
    throw cliError('DT-BACKEND-004', `Backend token/package replacement failed: ${errorMessage(err)}`)
  }

  success(`Backend (${language}) generated at ${targetDir}`)
}

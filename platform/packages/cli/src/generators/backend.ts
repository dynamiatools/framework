import { join } from 'node:path'
import { execa } from 'execa'
import { rmSync, mkdirSync } from 'node:fs'
import { type CliConfig } from '../utils/config.js'
import { renameJavaPackages } from '../utils/replace.js'
import { spin, success } from '../utils/logger.js'

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
    throw new Error(`Unknown backend language: ${language}`)
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
    spinner.fail('Failed to clone backend template')
    throw err
  }

  const renameSpinner = spin('Renaming packages and replacing tokens…')
  try {
    await renameJavaPackages({
      projectDir: targetDir,
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
    throw err
  }

  success(`Backend (${language}) generated at ${targetDir}`)
}

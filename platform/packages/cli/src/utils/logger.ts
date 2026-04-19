import chalk from 'chalk'
import ora, { type Ora } from 'ora'

export type CliErrorCode =
  | 'DT-CONFIG-001'
  | 'DT-COMMAND-001'
  | 'DT-COMMAND-002'
  | 'DT-RUN-001'
  | 'DT-BACKEND-001'
  | 'DT-BACKEND-002'
  | 'DT-BACKEND-003'
  | 'DT-BACKEND-004'
  | 'DT-BACKEND-005'
  | 'DT-FRONTEND-001'
  | 'DT-FRONTEND-002'
  | 'DT-FRONTEND-003'
  | 'DT-FRONTEND-004'
  | 'DT-FRONTEND-005'
  | 'DT-TEMPLATE-001'
  | 'DT-TEMPLATE-002'
  | 'DT-UNKNOWN-001'

export class CliError extends Error {
  constructor(
    public readonly code: CliErrorCode,
    message: string,
  ) {
    super(message)
    this.name = 'CliError'
  }
}

export function cliError(code: CliErrorCode, message: string): CliError {
  return new CliError(code, message)
}

export function withErrorCode(code: CliErrorCode, message: string): string {
  return `[${code}] ${message}`
}

/** Start a spinner and return the Ora instance so callers can stop it. */
export function spin(text: string): Ora {
  return ora(text).start()
}

/** Print a green success message. */
export function success(msg: string): void {
  console.log(chalk.green(`✓ ${msg}`))
}

/** Print a yellow warning message. */
export function warn(msg: string): void {
  console.warn(chalk.yellow(`⚠ ${msg}`))
}

/** Print a red error message and exit with code 1. */
export function error(msg: string): never {
  console.error(chalk.red(`✗ ${msg}`))
  process.exit(1)
}

export function errorWithCode(code: CliErrorCode, message: string): never {
  return error(withErrorCode(code, message))
}

/** Print a cyan informational message. */
export function info(msg: string): void {
  console.log(chalk.cyan(`ℹ ${msg}`))
}

/** Print a beta notice in a friendly tone. */
export function beta(msg: string): void {
  console.log(chalk.magenta(`β ${msg}`))
}

/** Print a standard message for features not available in beta yet. */
export function notAvailableYet(feature: string, code?: CliErrorCode): void {
  const message = `${feature} is not available yet in this beta release. We're actively working on it.`
  warn(code ? withErrorCode(code, message) : message)
}

/** Convert unknown errors into user-friendly text. */
export function errorMessage(err: unknown, fallbackCode: CliErrorCode = 'DT-UNKNOWN-001'): string {
  if (err instanceof CliError) {
    return withErrorCode(err.code, err.message)
  }
  if (err instanceof Error) {
    const maybeShort = err.message.trim()
    if (maybeShort.length > 0) return withErrorCode(fallbackCode, maybeShort)
    return withErrorCode(fallbackCode, 'Unexpected error.')
  }
  return withErrorCode(fallbackCode, 'Unexpected error.')
}

/** Improve common git-related raw errors for end users. */
export function friendlyGitError(err: unknown): string {
  const raw = errorMessage(err)
  const normalized = raw.toLowerCase()

  if (normalized.includes('repository not found')) {
    return 'Template repository not found. Please verify the template repository URL in cli.properties.'
  }
  if (normalized.includes('could not resolve host')) {
    return 'Could not reach the template repository host. Please check your internet connection and DNS settings.'
  }
  if (normalized.includes('remote branch') && normalized.includes('not found')) {
    return 'Template branch was not found in the repository. Please verify the configured branch in cli.properties.'
  }
  if (normalized.includes('authentication failed') || normalized.includes('permission denied')) {
    return 'Could not access the template repository. Check repository visibility and access permissions.'
  }

  return raw
}

/** Print the Dynamia Tools ASCII banner. */
export function banner(): void {
  const teal = chalk.hex('#00BCD4')
  console.log(teal('  ____                              _       _____           _     '))
  console.log(teal(' |  _ \\ _   _ _ __   __ _ _ __ ___ (_) __ |_   _|__   ___ | |___ '))
  console.log(teal(' | | | | | | | \'_ \\ / _` | \'_ ` _ \\| |/ _` || |/ _ \\ / _ \\| / __|'))
  console.log(teal(' | |_| | |_| | | | | (_| | | | | | | | (_| || | (_) | (_) | \\__ \\'))
  console.log(teal(' |____/ \\__, |_| |_|\\__,_|_| |_| |_|_|\\__,_||_|\\___/ \\___/|_|___/'))
  console.log(teal('         |___/                                                     '))
  console.log('')
  console.log(chalk.bold('  Dynamia Tools CLI'))
  console.log('')
}

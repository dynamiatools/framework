import chalk from 'chalk'
import ora, { type Ora } from 'ora'

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

/** Print a cyan informational message. */
export function info(msg: string): void {
  console.log(chalk.cyan(`ℹ ${msg}`))
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

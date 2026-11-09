#!/usr/bin/env node
import { runNew } from './commands/new.js'
import { errorWithCode, notAvailableYet } from './utils/logger.js'

const [, , ...args] = process.argv
const command = args[0] ?? 'new'

try {
  switch (command) {
    case 'new':
      await runNew(args.slice(1))
      break
    default:
      notAvailableYet(`Command "${command}"`, 'DT-COMMAND-001')
      errorWithCode('DT-COMMAND-002', 'Usage: dynamia new')
  }
} catch (err) {
  // Ctrl+C / closed prompt (@inquirer ExitPromptError): exit quietly, no stack trace.
  if (err instanceof Error && err.name === 'ExitPromptError') {
    console.log('\nCancelled.')
    process.exit(130)
  }
  throw err
}

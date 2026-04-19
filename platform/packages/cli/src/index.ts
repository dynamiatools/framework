#!/usr/bin/env node
import { runNew } from './commands/new.js'
import { errorWithCode, notAvailableYet } from './utils/logger.js'

const [, , ...args] = process.argv
const command = args[0] ?? 'new'

switch (command) {
  case 'new':
    await runNew()
    break
  default:
    notAvailableYet(`Command "${command}"`, 'DT-COMMAND-001')
    errorWithCode('DT-COMMAND-002', 'Usage: dynamia new')
}

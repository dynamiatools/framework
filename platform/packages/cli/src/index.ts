#!/usr/bin/env node
import { runNew } from './commands/new.js'

const [, , ...args] = process.argv
const command = args[0] ?? 'new'

switch (command) {
  case 'new':
    await runNew()
    break
  default:
    console.error(`Unknown command: ${command}`)
    console.error('Usage: dynamia new')
    process.exit(1)
}

import { describe, it, expect, vi } from 'vitest'
import { parseNewFlags } from '../src/commands/new.js'

describe('parseNewFlags', () => {
  it('returns all-undefined flags when no args are passed', () => {
    const flags = parseNewFlags([])
    expect(flags.name).toBeUndefined()
    expect(flags.scaffold).toBeUndefined()
    expect(flags.yes).toBeUndefined()
    expect(flags.git).toBeUndefined()
  })

  it('parses a full non-interactive flag set', () => {
    const flags = parseNewFlags([
      '--name', 'my-erp',
      '--scaffold', 'both',
      '--backend-lang', 'java',
      '--group-id', 'com.acme',
      '--artifact-id', 'my-erp',
      '--version', '1.0.0-SNAPSHOT',
      '--description', 'A demo app',
      '--frontend', 'vue',
      '--pm', 'pnpm',
      '--yes',
    ])

    expect(flags).toMatchObject({
      name: 'my-erp',
      scaffold: 'both',
      backendLang: 'java',
      groupId: 'com.acme',
      artifactId: 'my-erp',
      version: '1.0.0-SNAPSHOT',
      description: 'A demo app',
      frontend: 'vue',
      pm: 'pnpm',
      yes: true,
    })
  })

  it('--no-git forces git to false even if --git is also passed', () => {
    const flags = parseNewFlags(['--git', '--no-git'])
    expect(flags.git).toBe(false)
  })

  it('--git alone sets git to true', () => {
    const flags = parseNewFlags(['--git'])
    expect(flags.git).toBe(true)
  })

  it('rejects an invalid --scaffold value', () => {
    const exitSpy = vi.spyOn(process, 'exit').mockImplementation(() => {
      throw new Error('process.exit called')
    })
    expect(() => parseNewFlags(['--scaffold', 'nope'])).toThrow()
    exitSpy.mockRestore()
  })

  it('rejects an invalid --pm value', () => {
    const exitSpy = vi.spyOn(process, 'exit').mockImplementation(() => {
      throw new Error('process.exit called')
    })
    expect(() => parseNewFlags(['--pm', 'bun'])).toThrow()
    exitSpy.mockRestore()
  })
})

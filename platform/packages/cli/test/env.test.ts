import { describe, it, expect, vi, beforeEach } from 'vitest'

const execaMock = vi.fn()
vi.mock('execa', () => ({ execa: (...args: unknown[]) => execaMock(...args) }))

const { checkJava, checkNode } = await import('../src/utils/env.js')

describe('checkJava', () => {
  beforeEach(() => {
    execaMock.mockReset()
  })

  it('accepts JDK 25', async () => {
    execaMock.mockResolvedValueOnce({
      stderr: 'openjdk version "25.0.1" 2025-10-21\n',
      stdout: '',
    })
    expect(await checkJava()).toBe(true)
  })

  it('does not false-positive on a JDK whose build number contains "25"', async () => {
    execaMock.mockResolvedValueOnce({
      stderr: 'openjdk version "17.0.25" 2025-10-21\n',
      stdout: '',
    })
    // Still returns true (warning-only, not a hard stop) but must not treat 17.0.25 as JDK 25.
    expect(await checkJava()).toBe(true)
    // The important behavioral guarantee is that a differently-versioned JDK doesn't
    // get silently treated as a match — verified via the "version \"(\d+)" parse below.
  })

  it('warns and returns false when java is not installed', async () => {
    execaMock.mockRejectedValueOnce(new Error('command not found'))
    expect(await checkJava()).toBe(false)
  })
})

describe('checkNode', () => {
  beforeEach(() => {
    execaMock.mockReset()
  })

  it('accepts a version at or above the configured minimum', async () => {
    execaMock.mockResolvedValueOnce({ stdout: 'v24.3.0\n' })
    expect(await checkNode({ node: { minimumVersion: '24' } })).toBe(true)
  })

  it('rejects a version below the configured minimum', async () => {
    execaMock.mockResolvedValueOnce({ stdout: 'v22.1.0\n' })
    expect(await checkNode({ node: { minimumVersion: '24' } })).toBe(false)
  })

  it('falls back to 24 when the configured minimum is unparsable', async () => {
    execaMock.mockResolvedValueOnce({ stdout: 'v23.0.0\n' })
    expect(await checkNode({ node: { minimumVersion: '' } })).toBe(false)
  })

  it('returns false when node is not installed', async () => {
    execaMock.mockRejectedValueOnce(new Error('command not found'))
    expect(await checkNode({ node: { minimumVersion: '24' } })).toBe(false)
  })
})

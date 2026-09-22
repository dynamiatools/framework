import { describe, it, expect } from 'vitest'
import { parseProperties, loadConfig } from '../src/utils/config.js'

describe('parseProperties', () => {
  it('parses simple key=value pairs', () => {
    const result = parseProperties('foo=bar\nbaz=qux')
    expect(result).toEqual({ foo: 'bar', baz: 'qux' })
  })

  it('ignores blank lines and comments', () => {
    const result = parseProperties('# a comment\n\nfoo=bar\n  # indented comment\nbaz=qux\n')
    expect(result).toEqual({ foo: 'bar', baz: 'qux' })
  })

  it('trims whitespace around keys and values', () => {
    const result = parseProperties('  foo  =  bar  ')
    expect(result).toEqual({ foo: 'bar' })
  })

  it('keeps only the first "=" as the separator (values may contain "=")', () => {
    const result = parseProperties('url=https://example.com?a=1&b=2')
    expect(result).toEqual({ url: 'https://example.com?a=1&b=2' })
  })

  it('skips lines without an "=" instead of throwing', () => {
    const result = parseProperties('foo=bar\nnotaproperty\nbaz=qux')
    expect(result).toEqual({ foo: 'bar', baz: 'qux' })
  })

  it('returns an empty object for empty input', () => {
    expect(parseProperties('')).toEqual({})
  })
})

describe('loadConfig', () => {
  it('loads the real cli.properties and exposes backend/frontend templates', async () => {
    const config = await loadConfig()

    expect(config.dynamia.version).toBeTruthy()
    expect(config.templates.backend.java).toBeDefined()
    expect(config.templates.backend.java?.repo).toBe(
      'https://github.com/dynamiatools/template-backend-java',
    )
    expect(config.templates.frontend.vue).toBeDefined()
  })

  it('parses boolean "enabled" fields strictly against the string "false"', async () => {
    const config = await loadConfig()

    for (const entry of Object.values(config.templates.backend)) {
      expect(typeof entry.enabled).toBe('boolean')
    }
  })

  it('builds camelCase token keys from token.* properties', async () => {
    const config = await loadConfig()

    // token.group.id -> groupId, token.base.package -> basePackage, etc.
    expect(config.tokens.groupId).toBeTruthy()
    expect(config.tokens.basePackage).toBeTruthy()
    expect(config.tokens.groupId).toMatch(/\{\{.*\}\}/)
  })
})

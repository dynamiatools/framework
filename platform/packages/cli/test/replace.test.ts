import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { mkdtempSync, mkdirSync, writeFileSync, readFileSync, rmSync, existsSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { join } from 'node:path'
import { replaceTokensInDir, renameJavaPackages } from '../src/utils/replace.js'

describe('replaceTokensInDir', () => {
  let dir: string

  beforeEach(() => {
    dir = mkdtempSync(join(tmpdir(), 'dt-cli-replace-'))
  })

  afterEach(() => {
    rmSync(dir, { recursive: true, force: true })
  })

  it('replaces tokens in text files', () => {
    writeFileSync(join(dir, 'app.md'), 'Hello {{PROJECT_NAME}}!')
    replaceTokensInDir(dir, { '{{PROJECT_NAME}}': 'my-erp' })
    expect(readFileSync(join(dir, 'app.md'), 'utf-8')).toBe('Hello my-erp!')
  })

  it('skips files with extensions outside the allow-list', () => {
    writeFileSync(join(dir, 'logo.png'), '{{PROJECT_NAME}}')
    replaceTokensInDir(dir, { '{{PROJECT_NAME}}': 'my-erp' })
    expect(readFileSync(join(dir, 'logo.png'), 'utf-8')).toBe('{{PROJECT_NAME}}')
  })

  it('recurses into subdirectories', () => {
    mkdirSync(join(dir, 'nested', 'deeper'), { recursive: true })
    writeFileSync(join(dir, 'nested', 'deeper', 'a.yml'), 'name: {{PROJECT_NAME}}')
    replaceTokensInDir(dir, { '{{PROJECT_NAME}}': 'my-erp' })
    expect(readFileSync(join(dir, 'nested', 'deeper', 'a.yml'), 'utf-8')).toBe('name: my-erp')
  })

  it('applies multiple token replacements in one pass', () => {
    writeFileSync(join(dir, 'pom.xml'), '{{GROUP_ID}}:{{ARTIFACT_ID}}')
    replaceTokensInDir(dir, { '{{GROUP_ID}}': 'com.acme', '{{ARTIFACT_ID}}': 'shop' })
    expect(readFileSync(join(dir, 'pom.xml'), 'utf-8')).toBe('com.acme:shop')
  })
})

describe('renameJavaPackages', () => {
  let projectDir: string

  beforeEach(() => {
    projectDir = mkdtempSync(join(tmpdir(), 'dt-cli-rename-'))

    const mainPkg = join(projectDir, 'src', 'main', 'java', 'com', 'example', 'demo')
    const testPkg = join(projectDir, 'src', 'test', 'java', 'com', 'example', 'demo')
    mkdirSync(mainPkg, { recursive: true })
    mkdirSync(testPkg, { recursive: true })

    writeFileSync(
      join(mainPkg, 'DemoApplication.java'),
      'package com.example.demo;\n\npublic class DemoApplication {}\n',
    )
    writeFileSync(
      join(testPkg, 'ApplicationTests.java'),
      'package com.example.demo;\n\nclass ApplicationTests {}\n',
    )
    writeFileSync(
      join(projectDir, 'pom.xml'),
      [
        '<project>',
        '  <groupId>com.example</groupId>',
        '  <artifactId>demo</artifactId>',
        '  <version>0.0.1-SNAPSHOT</version>',
        '  <name>DynamiaTools App Backend</name>',
        '  <description>placeholder</description>',
        '</project>',
      ].join('\n'),
    )
  })

  afterEach(() => {
    rmSync(projectDir, { recursive: true, force: true })
  })

  const tokens = {
    groupId: '{{GROUP_ID}}',
    artifactId: '{{ARTIFACT_ID}}',
    basePackage: '{{BASE_PACKAGE}}',
    projectName: '{{PROJECT_NAME}}',
    projectVersion: '{{PROJECT_VERSION}}',
    dynamiaVersion: '{{DYNAMIA_VERSION}}',
    springBootVersion: '{{SPRING_BOOT_VERSION}}',
  }

  it('moves sources from com/example/demo to the computed base package', async () => {
    await renameJavaPackages({
      projectDir,
      projectName: 'my-erp',
      groupId: 'com.acme',
      artifactId: 'my-erp',
      version: '1.0.0-SNAPSHOT',
      description: 'desc',
      dynamiaVersion: '26.4.1',
      springBootVersion: '4.0.5',
      language: 'java',
      tokens,
    })

    const newMainDir = join(projectDir, 'src', 'main', 'java', 'com', 'acme', 'my', 'erp')
    expect(existsSync(join(newMainDir, 'MyErpApplication.java'))).toBe(true)
    expect(existsSync(join(projectDir, 'src', 'main', 'java', 'com', 'example'))).toBe(false)
  })

  it('rewrites the package declaration and class name in the moved Application file', async () => {
    await renameJavaPackages({
      projectDir,
      projectName: 'my-erp',
      groupId: 'com.acme',
      artifactId: 'my-erp',
      version: '1.0.0-SNAPSHOT',
      description: 'desc',
      dynamiaVersion: '26.4.1',
      springBootVersion: '4.0.5',
      language: 'java',
      tokens,
    })

    const content = readFileSync(
      join(projectDir, 'src', 'main', 'java', 'com', 'acme', 'my', 'erp', 'MyErpApplication.java'),
      'utf-8',
    )
    expect(content).toContain('package com.acme.my.erp;')
    expect(content).toContain('public class MyErpApplication {}')
    expect(content).not.toContain('DemoApplication')
  })

  it('replaces groupId/artifactId/version/name in pom.xml', async () => {
    await renameJavaPackages({
      projectDir,
      projectName: 'my-erp',
      groupId: 'com.acme',
      artifactId: 'my-erp',
      version: '1.0.0-SNAPSHOT',
      description: 'desc',
      dynamiaVersion: '26.4.1',
      springBootVersion: '4.0.5',
      language: 'java',
      tokens,
    })

    const pom = readFileSync(join(projectDir, 'pom.xml'), 'utf-8')
    expect(pom).toContain('<groupId>com.acme</groupId>')
    expect(pom).toContain('<artifactId>my-erp</artifactId>')
    expect(pom).toContain('<version>1.0.0-SNAPSHOT</version>')
    expect(pom).toContain('<name>my-erp</name>')
  })

  it('updates the pom.xml <description> with the user-provided value', async () => {
    await renameJavaPackages({
      projectDir,
      projectName: 'my-erp',
      groupId: 'com.acme',
      artifactId: 'my-erp',
      version: '1.0.0-SNAPSHOT',
      description: 'A custom shop backend',
      dynamiaVersion: '26.4.1',
      springBootVersion: '4.0.5',
      language: 'java',
      tokens,
    })

    const pom = readFileSync(join(projectDir, 'pom.xml'), 'utf-8')
    expect(pom).toContain('<description>A custom shop backend</description>')
  })

  it('computes the base package from groupId + hyphen-normalized artifactId', async () => {
    await renameJavaPackages({
      projectDir,
      projectName: 'my-shop',
      groupId: 'com.acme',
      artifactId: 'my-shop-app',
      version: '1.0.0-SNAPSHOT',
      description: 'desc',
      dynamiaVersion: '26.4.1',
      springBootVersion: '4.0.5',
      language: 'java',
      tokens,
    })

    expect(
      existsSync(
        join(projectDir, 'src', 'main', 'java', 'com', 'acme', 'my', 'shop', 'app', 'MyShopAppApplication.java'),
      ),
    ).toBe(true)
  })
})

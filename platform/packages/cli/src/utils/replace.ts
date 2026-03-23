import { readFileSync, writeFileSync, readdirSync, statSync, mkdirSync, renameSync, rmdirSync } from 'node:fs'
import { join, extname } from 'node:path'

// File extensions that will have token replacement applied
const TEXT_EXTENSIONS = new Set([
  '.java', '.kt', '.groovy',
  '.xml', '.yml', '.yaml', '.properties',
  '.md', '.txt', '.json', '.gradle', '.kts',
])

/**
 * Replace all token occurrences in a single text file.
 * Non-text files (by extension) are silently skipped.
 */
function replaceInFile(filePath: string, tokenMap: Record<string, string>): void {
  const ext = extname(filePath).toLowerCase()
  if (!TEXT_EXTENSIONS.has(ext)) return

  let content: string
  try {
    content = readFileSync(filePath, 'utf-8')
  } catch {
    return // skip unreadable files
  }

  let updated = content
  for (const [token, replacement] of Object.entries(tokenMap)) {
    updated = updated.split(token).join(replacement)
  }

  if (updated !== content) {
    writeFileSync(filePath, updated, 'utf-8')
  }
}

/**
 * Recursively walk a directory and apply token replacement to all text files.
 */
export function replaceTokensInDir(
  dir: string,
  tokenMap: Record<string, string>,
): void {
  const entries = readdirSync(dir)
  for (const entry of entries) {
    const fullPath = join(dir, entry)
    const stat = statSync(fullPath)
    if (stat.isDirectory()) {
      replaceTokensInDir(fullPath, tokenMap)
    } else {
      replaceInFile(fullPath, tokenMap)
    }
  }
}

export interface RenameOptions {
  projectDir: string
  groupId: string
  artifactId: string
  version: string
  description: string
  dynamiaVersion: string
  springBootVersion: string
  language: 'java' | 'kotlin' | 'groovy'
  /** Token map from cli.properties (e.g. { groupId: '{{GROUP_ID}}', ... }) */
  tokens: Record<string, string>
}

/**
 * Perform full package renaming and token replacement after cloning a backend template.
 *
 * Steps:
 * 1. Compute the base package from groupId + artifactId
 * 2. Rename the source/test directories to match the new package
 * 3. Replace all tokens in all text files
 * 4. Rename the main Application class file
 */
export async function renameJavaPackages(options: RenameOptions): Promise<void> {
  const {
    projectDir,
    groupId,
    artifactId,
    version,
    dynamiaVersion,
    springBootVersion,
    language,
    tokens,
  } = options

  // 1. Compute base package: "com.mycompany" + "my-erp" → "com.mycompany.my.erp"
  const basePackage = `${groupId}.${artifactId.replace(/-/g, '.')}`
  const basePackagePath = basePackage.replace(/\./g, '/')

  // 2. Rename source directories
  const srcExtension = language === 'kotlin' ? 'kotlin' : language === 'groovy' ? 'groovy' : 'java'
  const oldPackagePath = 'com/example/demo'
  const srcDirs = [
    join(projectDir, 'src', 'main', srcExtension),
    join(projectDir, 'src', 'test', srcExtension),
  ]

  for (const srcBase of srcDirs) {
    const oldDir = join(srcBase, oldPackagePath)
    const newDir = join(srcBase, basePackagePath)

    try {
      statSync(oldDir)
    } catch {
      continue // directory doesn't exist — skip
    }

    mkdirSync(newDir, { recursive: true })

    // Move all files from oldDir to newDir recursively
    moveDirectoryContents(oldDir, newDir)

    // Clean up old (now empty) package directories
    removeEmptyDirs(join(srcBase, 'com'))
  }

  // 3. Build token map: map cli.properties token values to user values
  const replacements: Record<string, string> = {}

  for (const [key, placeholder] of Object.entries(tokens)) {
    switch (key) {
      case 'groupId':           replacements[placeholder] = groupId; break
      case 'artifactId':        replacements[placeholder] = artifactId; break
      case 'basePackage':       replacements[placeholder] = basePackage; break
      case 'projectName':       replacements[placeholder] = artifactId; break
      case 'projectVersion':    replacements[placeholder] = version; break
      case 'dynamiaVersion':    replacements[placeholder] = dynamiaVersion; break
      case 'springBootVersion': replacements[placeholder] = springBootVersion; break
    }
  }

  // Always replace the placeholder package string itself in source files
  replacements['com.example.demo'] = basePackage
  replacements['com/example/demo'] = basePackagePath

  // 4. Replace tokens in all text files
  replaceTokensInDir(projectDir, replacements)

  // 5. Rename the main Application class file
  renameApplicationClass(projectDir, artifactId, basePackagePath, srcExtension)
}

/**
 * Move all files and subdirectories from src into dest.
 */
function moveDirectoryContents(src: string, dest: string): void {
  const entries = readdirSync(src)
  for (const entry of entries) {
    const srcPath = join(src, entry)
    const destPath = join(dest, entry)
    const stat = statSync(srcPath)
    if (stat.isDirectory()) {
      mkdirSync(destPath, { recursive: true })
      moveDirectoryContents(srcPath, destPath)
      try { rmdirSync(srcPath) } catch { /* not empty — skip */ }
    } else {
      renameSync(srcPath, destPath)
    }
  }
}

/**
 * Remove empty directories recursively (best-effort).
 */
function removeEmptyDirs(dir: string): void {
  try {
    const entries = readdirSync(dir)
    for (const entry of entries) {
      const full = join(dir, entry)
      if (statSync(full).isDirectory()) {
        removeEmptyDirs(full)
      }
    }
    try { rmdirSync(dir) } catch { /* not empty or doesn't exist */ }
  } catch { /* ignore */ }
}

/**
 * Rename DemoApplication.java → MyErpApplication.java (or .kt / .groovy).
 */
function renameApplicationClass(
  projectDir: string,
  artifactId: string,
  basePackagePath: string,
  srcExtension: string,
): void {
  // Build new class name: "my-erp" → "MyErp" → "MyErpApplication"
  const newClassName =
    artifactId
      .split('-')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join('') + 'Application'

  const fileExtMap: Record<string, string> = {
    kotlin: '.kt',
    groovy: '.groovy',
    java: '.java',
  }
  const fileExt = fileExtMap[srcExtension] ?? '.java'
  const oldFileName = `DemoApplication${fileExt}`
  const newFileName = `${newClassName}${fileExt}`

  const searchDir = join(projectDir, 'src', 'main', srcExtension, basePackagePath)
  try {
    const files = readdirSync(searchDir)
    for (const file of files) {
      if (file === oldFileName) {
        renameSync(join(searchDir, file), join(searchDir, newFileName))
        break
      }
    }
  } catch {
    // Directory doesn't exist or file not found — skip silently
  }
}

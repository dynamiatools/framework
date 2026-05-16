import path from 'node:path'
import fs from 'node:fs'
import fsp from 'node:fs/promises'
import type { SFSConfig } from '../types/index.js'

// Default data directory relative to CWD
const DEFAULT_DATA_DIR = path.join(process.cwd(), '.sfs')

export function getDataDir(): string {
  return process.env.SFS_DATA_DIR ?? DEFAULT_DATA_DIR
}

export function getDefaultConfig(): SFSConfig {
  return {
    dataDir: getDataDir(),
    host: process.env.SFS_HOST ?? '0.0.0.0',
    port: parseInt(process.env.SFS_PORT ?? '8080', 10),
    logLevel: process.env.SFS_LOG_LEVEL ?? 'info',
  }
}

export function resolveDataPaths(dataDir: string) {
  return {
    dataDir,
    configDir: path.join(dataDir, 'config'),
    identitiesDir: path.join(dataDir, 'identities'),
    bucketsDir: path.join(dataDir, 'buckets'),
    logsDir: path.join(dataDir, 'logs'),
    runtimeDir: path.join(dataDir, 'runtime'),
    cacheDir: path.join(dataDir, 'cache'),
  }
}

export async function ensureDataDirs(dataDir: string): Promise<void> {
  const paths = resolveDataPaths(dataDir)
  for (const dir of Object.values(paths)) {
    await fsp.mkdir(dir, { recursive: true })
  }
}

export function ensureDataDirsSync(dataDir: string): void {
  const paths = resolveDataPaths(dataDir)
  for (const dir of Object.values(paths)) {
    fs.mkdirSync(dir, { recursive: true })
  }
}

export async function readJsonFile<T>(filePath: string): Promise<T | null> {
  try {
    const content = await fsp.readFile(filePath, 'utf-8')
    return JSON.parse(content) as T
  } catch {
    return null
  }
}

export async function writeJsonFile<T>(filePath: string, data: T): Promise<void> {
  const dir = path.dirname(filePath)
  await fsp.mkdir(dir, { recursive: true })
  await fsp.writeFile(filePath, JSON.stringify(data, null, 2), 'utf-8')
}

export function readJsonFileSync<T>(filePath: string): T | null {
  try {
    const content = fs.readFileSync(filePath, 'utf-8')
    return JSON.parse(content) as T
  } catch {
    return null
  }
}

export function writeJsonFileSync<T>(filePath: string, data: T): void {
  const dir = path.dirname(filePath)
  fs.mkdirSync(dir, { recursive: true })
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2), 'utf-8')
}


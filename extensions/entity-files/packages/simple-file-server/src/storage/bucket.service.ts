import path from 'node:path'
import fsp from 'node:fs/promises'
import fs from 'node:fs'
import { resolveDataPaths, readJsonFile, writeJsonFile } from '../config/config.service.js'
import type { Bucket } from '../types/index.js'
import { SFSError, SFSErrorCode, notFound } from '../errors/index.js'

const SFS_INTERNAL_DIR = '.sfs'

function conflict(message: string, code: SFSErrorCode): SFSError {
  return new SFSError(code, message, 409)
}

export class BucketService {
  private bucketsDir: string

  constructor(dataDir: string) {
    this.bucketsDir = resolveDataPaths(dataDir).bucketsDir
  }

  private bucketFilePath(name: string): string {
    const safe = name.replace(/[^a-zA-Z0-9_\-\.]/g, '_')
    return path.join(this.bucketsDir, `${safe}.json`)
  }

  async create(name: string, absolutePath: string): Promise<Bucket> {
    if (!path.isAbsolute(absolutePath)) {
      throw new SFSError(SFSErrorCode.BUCKET_PATH_INVALID, `Bucket path must be absolute: ${absolutePath}`, 400)
    }

    const existing = await this.find(name)
    if (existing) {
      throw conflict(`Bucket '${name}' already exists`, SFSErrorCode.BUCKET_ALREADY_EXISTS)
    }

    // Ensure bucket dir exists
    await fsp.mkdir(absolutePath, { recursive: true })

    // Validate writable
    try {
      await fsp.access(absolutePath, fs.constants.W_OK)
    } catch {
      throw new SFSError(SFSErrorCode.BUCKET_PATH_NOT_WRITABLE, `Bucket path is not writable: ${absolutePath}`, 400)
    }

    // Create internal .sfs directory
    await this.ensureBucketInternals(absolutePath)

    const bucket: Bucket = {
      name,
      path: absolutePath,
      createdAt: new Date().toISOString(),
    }

    await writeJsonFile(this.bucketFilePath(name), bucket)
    return bucket
  }

  async find(name: string): Promise<Bucket | null> {
    return readJsonFile<Bucket>(this.bucketFilePath(name))
  }

  async get(name: string): Promise<Bucket> {
    const bucket = await this.find(name)
    if (!bucket) {
      throw notFound(`Bucket '${name}'`, SFSErrorCode.BUCKET_NOT_FOUND)
    }
    return bucket
  }

  async list(): Promise<Bucket[]> {
    try {
      const files = await fsp.readdir(this.bucketsDir)
      const buckets: Bucket[] = []
      for (const file of files) {
        if (!file.endsWith('.json')) continue
        const b = await readJsonFile<Bucket>(path.join(this.bucketsDir, file))
        if (b) buckets.push(b)
      }
      return buckets
    } catch {
      return []
    }
  }

  async delete(name: string): Promise<void> {
    const filePath = this.bucketFilePath(name)
    try {
      await fsp.unlink(filePath)
    } catch {
      throw notFound(`Bucket '${name}'`, SFSErrorCode.BUCKET_NOT_FOUND)
    }
  }

  /**
   * Resolve a key to an absolute filesystem path within the bucket.
   * Validates against path traversal attacks.
   */
  resolveKey(bucket: Bucket, key: string): string {
    // Normalize to remove double slashes, . and ..
    const normalized = path.normalize(key).replace(/\\/g, '/')

    // Block access to internal .sfs directory
    const parts = normalized.split('/')
    if (parts.some(p => p === SFS_INTERNAL_DIR)) {
      throw new SFSError(SFSErrorCode.PATH_RESERVED, `Access to internal '${SFS_INTERNAL_DIR}' path is forbidden`, 403)
    }

    const resolved = path.resolve(bucket.path, normalized.replace(/^\//, ''))

    // Ensure resolved path is within bucket root
    if (!resolved.startsWith(bucket.path + path.sep) && resolved !== bucket.path) {
      throw new SFSError(SFSErrorCode.PATH_TRAVERSAL, 'Path traversal detected', 400)
    }

    return resolved
  }

  /**
   * Validate that a key does not point outside bucket boundaries.
   */
  validateKey(bucket: Bucket, key: string): string {
    return this.resolveKey(bucket, key)
  }

  async ensureBucketInternals(bucketPath: string): Promise<void> {
    const sfsDir = path.join(bucketPath, SFS_INTERNAL_DIR)
    await fsp.mkdir(path.join(sfsDir, 'staging'), { recursive: true })
    await fsp.mkdir(path.join(sfsDir, 'thumbs'), { recursive: true })
    await fsp.mkdir(path.join(sfsDir, 'runtime'), { recursive: true })
  }

  getStagingDir(bucket: Bucket): string {
    return path.join(bucket.path, SFS_INTERNAL_DIR, 'staging')
  }

  getThumbsDir(bucket: Bucket): string {
    return path.join(bucket.path, SFS_INTERNAL_DIR, 'thumbs')
  }

  /**
   * Validate startup: check all buckets are accessible and writable, clean orphan staging files.
   */
  async validateStartup(): Promise<void> {
    const buckets = await this.list()
    for (const bucket of buckets) {
      try {
        await fsp.access(bucket.path, fs.constants.W_OK)
        await this.ensureBucketInternals(bucket.path)
        await this.cleanOrphanStagingFiles(bucket)
      } catch (err) {
        console.warn(`[SFS] Warning: bucket '${bucket.name}' at '${bucket.path}' is not accessible:`, err)
      }
    }
  }

  private async cleanOrphanStagingFiles(bucket: Bucket): Promise<void> {
    const stagingDir = this.getStagingDir(bucket)
    try {
      const files = await fsp.readdir(stagingDir)
      const cutoff = Date.now() - 60 * 60 * 1000 // older than 1 hour
      for (const file of files) {
        const filePath = path.join(stagingDir, file)
        const stat = await fsp.stat(filePath)
        if (stat.mtimeMs < cutoff) {
          await fsp.unlink(filePath).catch(() => {})
        }
      }
    } catch {
      // Ignore cleanup errors
    }
  }
}


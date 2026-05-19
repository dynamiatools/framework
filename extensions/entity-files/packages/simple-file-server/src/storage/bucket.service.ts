import path from 'node:path'
import fsp from 'node:fs/promises'
import fs from 'node:fs'
import { resolveDataPaths, readJsonFile, writeJsonFile } from '../config/config.service.js'
import type { Bucket, StorageTarget, SftpConfig } from '../types/index.js'
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

  /**
   * Create a new bucket.
   *
   * @param name        Unique bucket name.
   * @param bucketPath  Absolute path to the storage root.
   *                    For `'local'` this is a local filesystem path.
   *                    For `'sftp'` this is the remote base path on the SFTP server.
   * @param storageTarget  Storage backend (default: `'local'`).
   * @param sftpConfig     Required when `storageTarget` is `'sftp'`.
   */
  async create(
    name: string,
    bucketPath: string,
    storageTarget: StorageTarget = 'local',
    sftpConfig?: SftpConfig,
  ): Promise<Bucket> {
    if (!path.isAbsolute(bucketPath)) {
      throw new SFSError(SFSErrorCode.BUCKET_PATH_INVALID, `Bucket path must be absolute: ${bucketPath}`, 400)
    }

    const existing = await this.find(name)
    if (existing) {
      throw conflict(`Bucket '${name}' already exists`, SFSErrorCode.BUCKET_ALREADY_EXISTS)
    }

    if (storageTarget === 'sftp') {
      if (!sftpConfig) {
        throw new SFSError(
          SFSErrorCode.BUCKET_PATH_INVALID,
          `sftpConfig is required when storageTarget is 'sftp'`,
          400,
        )
      }

      const bucket: Bucket = {
        name,
        path: bucketPath,
        createdAt: new Date().toISOString(),
        storageTarget: 'sftp',
        sftpConfig,
      }

      await writeJsonFile(this.bucketFilePath(name), bucket)
      return bucket
    }

    // ── local storage ──────────────────────────────────────────────────────

    // Ensure bucket dir exists
    await fsp.mkdir(bucketPath, { recursive: true })

    // Validate writable
    try {
      await fsp.access(bucketPath, fs.constants.W_OK)
    } catch {
      throw new SFSError(SFSErrorCode.BUCKET_PATH_NOT_WRITABLE, `Bucket path is not writable: ${bucketPath}`, 400)
    }

    // Create internal .sfs directory
    await this.ensureBucketInternals(bucketPath)

    const bucket: Bucket = {
      name,
      path: bucketPath,
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
   * Only valid for `'local'` storage target buckets.
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

    // Normalize bucket root to remove any trailing separator
    const bucketRoot = path.resolve(bucket.path)
    const resolved = path.resolve(bucketRoot, normalized.replace(/^\//, ''))

    // Ensure resolved path is within bucket root
    if (!resolved.startsWith(bucketRoot + path.sep) && resolved !== bucketRoot) {
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
}


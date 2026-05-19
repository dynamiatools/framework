import path from 'node:path'
import fsp from 'node:fs/promises'
import fs from 'node:fs'
import crypto from 'node:crypto'
import { pipeline } from 'node:stream/promises'
import { PassThrough } from 'node:stream'
import type { Readable } from 'node:stream'
import SftpClient from 'ssh2-sftp-client'
import type { Bucket, ListEntry, ListResult, SftpConfig } from '../types/index.js'
import { SFSError, SFSErrorCode, notFound } from '../errors/index.js'
import type { StorageProvider, DownloadResult, UploadOptions } from './storage.provider.js'

/** Remove trailing forward-slashes without using a backtracking regex. */
function trimTrailingSlashes(s: string): string {
  let end = s.length
  while (end > 0 && s[end - 1] === '/') end--
  return s.slice(0, end)
}

/**
 * Storage provider that stores files on a remote SFTP server.
 *
 * One connection is maintained per bucket (keyed by bucket name) and
 * automatically re-established when a connection is lost.
 *
 * Files are staged locally before being pushed to the SFTP server so that
 * partial uploads are never visible at the final destination.
 *
 * Thumbnails are **not** supported for SFTP buckets — the {@link ThumbnailService}
 * will skip such buckets automatically.
 *
 * Example registration:
 * <pre>{@code
 * registry.register('sftp', new SftpStorageProvider('/var/sfs/sftp-staging'))
 * }</pre>
 */
export class SftpStorageProvider implements StorageProvider {
  private readonly connections = new Map<string, SftpClient>()

  /**
   * @param localCacheDir Base directory for local staging files.
   *   Typically `{dataDir}/sftp-staging`.
   */
  constructor(private readonly localCacheDir: string) {}

  // ── Connection management ─────────────────────────────────────────────────

  private async getClient(bucket: Bucket): Promise<SftpClient> {
    const config = this.requireSftpConfig(bucket)
    const key = bucket.name

    const existing = this.connections.get(key)
    if (existing) {
      // Perform a lightweight liveness check before reusing
      try {
        await existing.cwd()
        return existing
      } catch {
        // Dead connection — fall through to reconnect
        this.connections.delete(key)
      }
    }

    const client = new SftpClient()
    const connectOptions: SftpClient.ConnectOptions = {
      host: config.host,
      port: config.port ?? 22,
      username: config.username,
    }
    if (config.password) connectOptions.password = config.password
    if (config.privateKey) connectOptions.privateKey = config.privateKey
    if (config.passphrase) connectOptions.passphrase = config.passphrase

    try {
      await client.connect(connectOptions)
    } catch (err) {
      throw new SFSError(
        SFSErrorCode.SFTP_CONNECTION_FAILED,
        `Failed to connect to SFTP server for bucket '${bucket.name}': ${err instanceof Error ? err.message : String(err)}`,
        503,
      )
    }

    this.connections.set(key, client)
    return client
  }

  private requireSftpConfig(bucket: Bucket): SftpConfig {
    if (!bucket.sftpConfig) {
      throw new SFSError(
        SFSErrorCode.BUCKET_PATH_INVALID,
        `Bucket '${bucket.name}' has storageTarget 'sftp' but no sftpConfig`,
        500,
      )
    }
    return bucket.sftpConfig
  }

  // ── Path helpers ──────────────────────────────────────────────────────────

  private remotePath(bucket: Bucket, key: string): string {
    const normalized = key.replace(/\\/g, '/').replace(/^\/+/, '')

    // Prevent path traversal
    const parts = normalized.split('/')
    if (parts.some(p => p === '..' || p === '.')) {
      throw new SFSError(SFSErrorCode.PATH_TRAVERSAL, 'Path traversal detected in SFTP key', 400)
    }

    const base = trimTrailingSlashes(bucket.path)
    return normalized ? `${base}/${normalized}` : base
  }

  private localStagingDir(bucket: Bucket): string {
    return path.join(this.localCacheDir, bucket.name, 'staging')
  }

  // ── StorageProvider implementation ───────────────────────────────────────

  async initBucket(bucket: Bucket): Promise<void> {
    // Ensure local staging directory exists
    await fsp.mkdir(this.localStagingDir(bucket), { recursive: true })

    // Ensure remote base directory exists
    const client = await this.getClient(bucket)
    await client.mkdir(bucket.path, true).catch(() => {
      // mkdir may fail if directory already exists on some servers — safe to ignore
    })
  }

  async validateBucket(bucket: Bucket): Promise<void> {
    await fsp.mkdir(this.localStagingDir(bucket), { recursive: true })
    const client = await this.getClient(bucket)
    try {
      await client.stat(bucket.path)
    } catch {
      console.warn(`[SFS] Warning: SFTP bucket '${bucket.name}' remote path '${bucket.path}' is not accessible`)
    }
    await this.cleanOrphanStagingFiles(bucket)
  }

  async download(bucket: Bucket, key: string): Promise<DownloadResult> {
    const remote = this.remotePath(bucket, key)
    const client = await this.getClient(bucket)

    let remoteStat: SftpClient.FileStats
    try {
      remoteStat = await client.stat(remote)
    } catch {
      throw notFound(`Object '${key}' in bucket '${bucket.name}'`, SFSErrorCode.OBJECT_NOT_FOUND)
    }

    if (remoteStat.isDirectory) {
      throw new SFSError(SFSErrorCode.OBJECT_IS_DIRECTORY, `'${key}' is a directory, not a file`, 400)
    }

    const size = remoteStat.size ?? 0
    const modifyTime = remoteStat.modifyTime ?? Date.now()
    const lastModified = new Date(modifyTime)
    const etag = `"${size}-${modifyTime}"`
    const mimeType = this.mimeFromKey(key)

    // Pipe the SFTP download into a PassThrough so the caller gets a Readable
    const passThrough = new PassThrough()
    client.get(remote, passThrough).catch((err: unknown) => {
      passThrough.destroy(err instanceof Error ? err : new Error(String(err)))
    })

    return { stream: passThrough, size, mimeType, lastModified, etag }
  }

  async upload(
    bucket: Bucket,
    key: string,
    source: Readable,
    _options: UploadOptions = {},
  ): Promise<{ size: number; etag: string }> {
    const remote = this.remotePath(bucket, key)
    const stagingDir = this.localStagingDir(bucket)
    const stagingFile = path.join(stagingDir, `${crypto.randomUUID()}.tmp`)

    await fsp.mkdir(stagingDir, { recursive: true })

    try {
      // Stage the upload to a local temporary file
      const writeStream = fs.createWriteStream(stagingFile)
      await pipeline(source, writeStream)

      // fsync to ensure data is persisted before the SFTP push
      const fd = await fsp.open(stagingFile, 'r')
      try {
        await fd.sync()
      } finally {
        await fd.close()
      }

      const stat = await fsp.stat(stagingFile)
      const bytesWritten = stat.size

      // Ensure the remote parent directory exists
      const remoteDir = remote.split('/').slice(0, -1).join('/')
      const client = await this.getClient(bucket)
      if (remoteDir && remoteDir !== trimTrailingSlashes(bucket.path)) {
        await client.mkdir(remoteDir, true).catch(() => {})
      }

      // Push staged file to SFTP
      await client.put(stagingFile, remote)

      const etag = `"${bytesWritten}-${Date.now()}"`
      return { size: bytesWritten, etag }
    } finally {
      await fsp.unlink(stagingFile).catch(() => {})
    }
  }

  async delete(bucket: Bucket, key: string): Promise<void> {
    const remote = this.remotePath(bucket, key)
    const client = await this.getClient(bucket)
    try {
      await client.delete(remote)
    } catch {
      throw notFound(`Object '${key}' in bucket '${bucket.name}'`, SFSErrorCode.OBJECT_NOT_FOUND)
    }
  }

  async listDirectory(bucket: Bucket, keyPrefix: string): Promise<ListResult> {
    const remoteDir = this.remotePath(bucket, keyPrefix || '/')
    const client = await this.getClient(bucket)

    let entries: SftpClient.FileInfo[]
    try {
      entries = await client.list(remoteDir)
    } catch {
      throw notFound(`Directory '${keyPrefix}' in bucket '${bucket.name}'`, SFSErrorCode.OBJECT_NOT_FOUND)
    }

    const result: ListEntry[] = entries.map(entry => {
      const prefix = keyPrefix ? `${keyPrefix.replace(/\/$/, '')}/` : ''
      const isDir = entry.type === 'd'
      const key = isDir ? `${prefix}${entry.name}/` : `${prefix}${entry.name}`
      return {
        key,
        size: entry.size ?? 0,
        lastModified: new Date(entry.modifyTime ?? Date.now()).toISOString(),
        isDirectory: isDir,
      }
    })

    return {
      bucket: bucket.name,
      prefix: keyPrefix || '/',
      entries: result,
      hasMore: false,
      total: result.length,
    }
  }

  async listBucket(bucket: Bucket, limit: number = 100, cursor?: string): Promise<ListResult> {
    const allEntries = await this.collectAllEntries(bucket, bucket.path, '')

    let startIdx = 0
    if (cursor) {
      const cursorIdx = allEntries.findIndex(e => e.key === cursor)
      if (cursorIdx >= 0) startIdx = cursorIdx + 1
    }

    const page = allEntries.slice(startIdx, startIdx + limit)
    const hasMore = startIdx + limit < allEntries.length
    const nextCursor = hasMore ? page[page.length - 1]?.key : undefined

    return {
      bucket: bucket.name,
      prefix: '/',
      entries: page,
      cursor: nextCursor,
      hasMore,
      total: allEntries.length,
    }
  }

  async close(): Promise<void> {
    for (const client of this.connections.values()) {
      await client.end().catch(() => {})
    }
    this.connections.clear()
  }

  // ── Private helpers ────────────────────────────────────────────────────────

  private async collectAllEntries(bucket: Bucket, dirPath: string, prefix: string): Promise<ListEntry[]> {
    const client = await this.getClient(bucket)
    const result: ListEntry[] = []

    try {
      const entries = await client.list(dirPath)
      for (const entry of entries) {
        const relKey = prefix ? `${prefix}/${entry.name}` : entry.name
        if (entry.type === 'd') {
          const subDir = `${trimTrailingSlashes(dirPath)}/${entry.name}`
          const subEntries = await this.collectAllEntries(bucket, subDir, relKey)
          result.push(...subEntries)
        } else {
          result.push({
            key: relKey,
            size: entry.size ?? 0,
            lastModified: new Date(entry.modifyTime ?? Date.now()).toISOString(),
            isDirectory: false,
          })
        }
      }
    } catch {
      // empty or inaccessible directory
    }

    return result
  }

  private mimeFromKey(key: string): string {
    const ext = path.extname(key).toLowerCase()
    const mimeMap: Record<string, string> = {
      '.jpg': 'image/jpeg',
      '.jpeg': 'image/jpeg',
      '.png': 'image/png',
      '.gif': 'image/gif',
      '.webp': 'image/webp',
      '.svg': 'image/svg+xml',
      '.pdf': 'application/pdf',
      '.json': 'application/json',
      '.txt': 'text/plain',
      '.html': 'text/html',
      '.css': 'text/css',
      '.js': 'application/javascript',
      '.ts': 'application/typescript',
      '.xml': 'application/xml',
      '.zip': 'application/zip',
      '.tar': 'application/x-tar',
      '.gz': 'application/gzip',
      '.mp4': 'video/mp4',
      '.mp3': 'audio/mpeg',
      '.wav': 'audio/wav',
    }
    return mimeMap[ext] ?? 'application/octet-stream'
  }

  private async cleanOrphanStagingFiles(bucket: Bucket): Promise<void> {
    const stagingDir = this.localStagingDir(bucket)
    try {
      const files = await fsp.readdir(stagingDir)
      const cutoff = Date.now() - 60 * 60 * 1000
      for (const file of files) {
        const filePath = path.join(stagingDir, file)
        const stat = await fsp.stat(filePath)
        if (stat.mtimeMs < cutoff) {
          await fsp.unlink(filePath).catch(() => {})
        }
      }
    } catch {
      // ignore cleanup errors
    }
  }
}

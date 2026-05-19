import path from 'node:path'
import fsp from 'node:fs/promises'
import fs from 'node:fs'
import crypto from 'node:crypto'
import { pipeline } from 'node:stream/promises'
import type { Readable } from 'node:stream'
import { fileTypeFromFile } from 'file-type'
import type { Bucket, ListResult, ListEntry } from '../types/index.js'
import { SFSError, SFSErrorCode, notFound } from '../errors/index.js'
import type { StorageProvider, DownloadResult, UploadOptions } from './storage.provider.js'
import type { BucketService } from './bucket.service.js'

/**
 * Storage provider that persists files on the local filesystem.
 *
 * This is the default backend. Files are stored under the bucket's `path`
 * directory, uploads are staged atomically, and internal state lives inside a
 * reserved `.sfs` sub-directory.
 */
export class LocalStorageProvider implements StorageProvider {
  constructor(private readonly bucketService: BucketService) {}

  async initBucket(bucket: Bucket): Promise<void> {
    await this.bucketService.ensureBucketInternals(bucket.path)
  }

  async validateBucket(bucket: Bucket): Promise<void> {
    try {
      await fsp.access(bucket.path, fs.constants.W_OK)
    } catch {
      console.warn(`[SFS] Warning: bucket '${bucket.name}' at '${bucket.path}' is not accessible`)
      return
    }
    await this.bucketService.ensureBucketInternals(bucket.path)
    await this.cleanOrphanStagingFiles(bucket)
  }

  async download(bucket: Bucket, key: string): Promise<DownloadResult> {
    const filePath = this.bucketService.resolveKey(bucket, key)

    let stat: fs.Stats
    try {
      stat = await fsp.stat(filePath)
    } catch {
      throw notFound(`Object '${key}' in bucket '${bucket.name}'`, SFSErrorCode.OBJECT_NOT_FOUND)
    }

    if (stat.isDirectory()) {
      throw new SFSError(SFSErrorCode.OBJECT_IS_DIRECTORY, `'${key}' is a directory, not a file`, 400)
    }

    const mimeType = await this.detectMimeType(filePath)
    const etag = `"${stat.size}-${stat.mtimeMs}"`

    return {
      stream: fs.createReadStream(filePath),
      size: stat.size,
      mimeType,
      lastModified: stat.mtime,
      etag,
    }
  }

  async upload(
    bucket: Bucket,
    key: string,
    source: Readable,
    options: UploadOptions = {},
  ): Promise<{ size: number; etag: string }> {
    const finalPath = this.bucketService.resolveKey(bucket, key)
    const stagingDir = this.bucketService.getStagingDir(bucket)
    const stagingFile = path.join(stagingDir, `${crypto.randomUUID()}.tmp`)

    // Ensure parent directory exists
    const parentDir = path.dirname(finalPath)
    await fsp.mkdir(parentDir, { recursive: true })

    let bytesWritten = 0

    try {
      // Stream upload to staging
      const writeStream = fs.createWriteStream(stagingFile)
      writeStream.on('pipe', () => {})

      await pipeline(source, writeStream)

      // fsync to ensure data is on disk
      const fd = await fsp.open(stagingFile, 'r')
      try {
        await fd.sync()
      } finally {
        await fd.close()
      }

      const stat = await fsp.stat(stagingFile)
      bytesWritten = stat.size

      // Atomic move to final destination
      try {
        await fsp.rename(stagingFile, finalPath)
      } catch (err: unknown) {
        // Cross-device rename: fallback to copy + unlink
        const error = err as NodeJS.ErrnoException
        if (error.code === 'EXDEV') {
          await pipeline(fs.createReadStream(stagingFile), fs.createWriteStream(finalPath))
          await fsp.unlink(stagingFile)
        } else {
          throw err
        }
      }

      const etag = `"${bytesWritten}-${Date.now()}"`
      return { size: bytesWritten, etag }
    } catch (err) {
      // Cleanup staging file on error
      await fsp.unlink(stagingFile).catch(() => {})
      throw err
    }
  }

  async delete(bucket: Bucket, key: string): Promise<void> {
    const filePath = this.bucketService.resolveKey(bucket, key)

    let stat: fs.Stats
    try {
      stat = await fsp.stat(filePath)
    } catch {
      throw notFound(`Object '${key}' in bucket '${bucket.name}'`, SFSErrorCode.OBJECT_NOT_FOUND)
    }

    if (stat.isDirectory()) {
      throw new SFSError(SFSErrorCode.OBJECT_IS_DIRECTORY, `'${key}' is a directory and cannot be deleted`, 400)
    }

    await fsp.unlink(filePath)
  }

  async listDirectory(bucket: Bucket, keyPrefix: string): Promise<ListResult> {
    const dirPath = this.bucketService.resolveKey(bucket, keyPrefix || '/')

    let stat: fs.Stats
    try {
      stat = await fsp.stat(dirPath)
    } catch {
      throw notFound(`Directory '${keyPrefix}' in bucket '${bucket.name}'`, SFSErrorCode.OBJECT_NOT_FOUND)
    }

    if (!stat.isDirectory()) {
      throw new SFSError(SFSErrorCode.OBJECT_IS_DIRECTORY, `'${keyPrefix}' is not a directory`, 404)
    }

    const entries = await fsp.readdir(dirPath, { withFileTypes: true })
    const result: ListEntry[] = []

    for (const entry of entries) {
      // Skip internal .sfs directory
      if (entry.name === '.sfs') continue

      const entryPath = path.join(dirPath, entry.name)
      try {
        const entryStat = await fsp.stat(entryPath)
        const relativeKey = path.join(keyPrefix || '', entry.name).replace(/\\/g, '/')

        result.push({
          key: entry.isDirectory() ? `${relativeKey}/` : relativeKey,
          size: entryStat.size,
          lastModified: entryStat.mtime.toISOString(),
          isDirectory: entry.isDirectory(),
        })
      } catch {
        // Skip entries we can't stat
      }
    }

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

  private async collectAllEntries(bucket: Bucket, dirPath: string, prefix: string): Promise<ListEntry[]> {
    const result: ListEntry[] = []
    try {
      const entries = await fsp.readdir(dirPath, { withFileTypes: true })
      for (const entry of entries) {
        if (entry.name === '.sfs') continue

        const entryPath = path.join(dirPath, entry.name)
        const relKey = prefix ? `${prefix}/${entry.name}` : entry.name

        try {
          const stat = await fsp.stat(entryPath)
          if (entry.isDirectory()) {
            const subEntries = await this.collectAllEntries(bucket, entryPath, relKey)
            result.push(...subEntries)
          } else {
            result.push({
              key: relKey,
              size: stat.size,
              lastModified: stat.mtime.toISOString(),
              isDirectory: false,
            })
          }
        } catch {
          // skip
        }
      }
    } catch {
      // empty
    }
    return result
  }

  private async detectMimeType(filePath: string): Promise<string> {
    try {
      const result = await fileTypeFromFile(filePath)
      if (result) return result.mime
    } catch {
      // fallback
    }
    return this.mimeFromExtension(filePath)
  }

  private mimeFromExtension(filePath: string): string {
    const ext = path.extname(filePath).toLowerCase()
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
    const stagingDir = this.bucketService.getStagingDir(bucket)
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

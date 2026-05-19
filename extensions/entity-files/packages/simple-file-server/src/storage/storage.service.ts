import path from 'node:path'
import { fileTypeFromBuffer } from 'file-type'
import type { Readable } from 'node:stream'
import type { Bucket, ListResult } from '../types/index.js'
import { StorageProviderRegistry } from './storage.provider.js'
import type { StorageProvider, DownloadResult, UploadOptions } from './storage.provider.js'
import { LocalStorageProvider } from './local.provider.js'
import type { BucketService } from './bucket.service.js'

export type { DownloadResult, UploadOptions }

/**
 * Facade over the {@link StorageProviderRegistry} that routes file operations
 * to the correct backend based on each bucket's `storageTarget`.
 *
 * By default, a `LocalStorageProvider` is registered for the `'local'` target.
 * Additional providers (e.g., `SftpStorageProvider`) can be registered via
 * {@link registerProvider}.
 *
 * Example:
 * <pre>{@code
 * const storageService = new StorageService(bucketService)
 * storageService.registerProvider('sftp', new SftpStorageProvider(cacheDir))
 * }</pre>
 */
export class StorageService {
  private readonly registry: StorageProviderRegistry

  constructor(private readonly bucketService: BucketService, registry?: StorageProviderRegistry) {
    this.registry = registry ?? new StorageProviderRegistry()
    // Register the built-in local provider
    this.registry.register('local', new LocalStorageProvider(bucketService))
  }

  /**
   * Register an additional storage provider.
   * @param target Storage target identifier (e.g., `'sftp'`).
   * @param provider The provider implementation.
   */
  registerProvider(target: string, provider: StorageProvider): void {
    this.registry.register(target, provider)
  }

  private getProvider(bucket: Bucket): StorageProvider {
    return this.registry.get(bucket.storageTarget ?? 'local')
  }

  /**
   * Initialise a newly-created bucket (creates directories, remote paths, etc.).
   */
  async initBucket(bucket: Bucket): Promise<void> {
    await this.getProvider(bucket).initBucket(bucket)
  }

  /**
   * Validate all registered buckets on startup.
   * For local buckets this checks write access and cleans orphan staging files.
   * For remote buckets (e.g., SFTP) this verifies connectivity.
   */
  async validateStartup(): Promise<void> {
    const buckets = await this.bucketService.list()
    for (const bucket of buckets) {
      try {
        await this.getProvider(bucket).validateBucket(bucket)
      } catch (err) {
        console.warn(`[SFS] Warning: could not validate bucket '${bucket.name}':`, err)
      }
    }
  }

  /**
   * Close all provider connections (called on graceful shutdown).
   */
  async closeProviders(): Promise<void> {
    for (const provider of this.registry.getAll().values()) {
      await provider.close?.()
    }
  }

  // ── Delegated operations ──────────────────────────────────────────────────

  /**
   * Download a file from a bucket. Returns a readable stream.
   */
  async download(bucket: Bucket, key: string): Promise<DownloadResult> {
    return this.getProvider(bucket).download(bucket, key)
  }

  /**
   * Upload a file to a bucket using staging + atomic commit.
   */
  async upload(
    bucket: Bucket,
    key: string,
    source: Readable,
    options: UploadOptions = {},
  ): Promise<{ size: number; etag: string }> {
    return this.getProvider(bucket).upload(bucket, key, source, options)
  }

  /**
   * Delete a file from a bucket.
   */
  async delete(bucket: Bucket, key: string): Promise<void> {
    return this.getProvider(bucket).delete(bucket, key)
  }

  /**
   * List directory contents.
   */
  async listDirectory(bucket: Bucket, keyPrefix: string): Promise<ListResult> {
    return this.getProvider(bucket).listDirectory(bucket, keyPrefix)
  }

  /**
   * List bucket contents with pagination.
   */
  async listBucket(bucket: Bucket, limit: number = 100, cursor?: string): Promise<ListResult> {
    return this.getProvider(bucket).listBucket(bucket, limit, cursor)
  }

  // ── MIME helpers (independent of storage backend) ─────────────────────────

  /**
   * Detect MIME type using magic bytes, with fallback to extension.
   * This method is only reliable for locally-accessible file paths.
   */
  async detectMimeType(filePath: string): Promise<string> {
    try {
      const { fileTypeFromFile } = await import('file-type')
      const result = await fileTypeFromFile(filePath)
      if (result) return result.mime
    } catch {
      // fallback
    }
    return this.mimeFromExtension(filePath)
  }

  /**
   * Detect MIME type from an in-memory buffer using magic bytes.
   */
  async detectMimeTypeFromBuffer(buffer: Buffer): Promise<string> {
    try {
      const result = await fileTypeFromBuffer(buffer)
      if (result) return result.mime
    } catch {
      // fallback
    }
    return 'application/octet-stream'
  }

  /**
   * Returns true when the MIME type represents a raster image that can be
   * processed by Sharp (excludes SVG).
   */
  isImageMime(mimeType: string): boolean {
    return mimeType.startsWith('image/') && mimeType !== 'image/svg+xml'
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
}


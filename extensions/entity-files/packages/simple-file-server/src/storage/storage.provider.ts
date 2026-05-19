import type { Readable } from 'node:stream'
import type { Bucket, ListResult } from '../types/index.js'
import { SFSError, SFSErrorCode } from '../errors/index.js'

export interface DownloadResult {
  stream: Readable
  size: number
  mimeType: string
  lastModified: Date
  etag: string
}

export interface UploadOptions {
  overwrite?: boolean
}

/**
 * Abstraction over a storage backend.
 * Implementations must be registered with a {@link StorageProviderRegistry} so that
 * {@link StorageService} can dispatch operations to the correct backend.
 */
export interface StorageProvider {
  /**
   * Called once after a bucket is created to initialise any remote or local
   * resources (e.g., create the remote directory and local staging area).
   */
  initBucket(bucket: Bucket): Promise<void>

  /**
   * Called on server startup to verify that the bucket is accessible and
   * healthy. Implementations may also perform cleanup (e.g., orphan staging
   * files) inside this method.
   */
  validateBucket(bucket: Bucket): Promise<void>

  /** Download a file from the bucket. Returns a readable stream. */
  download(bucket: Bucket, key: string): Promise<DownloadResult>

  /** Upload a file to the bucket using staging + atomic commit. */
  upload(bucket: Bucket, key: string, source: Readable, options?: UploadOptions): Promise<{ size: number; etag: string }>

  /** Delete a single file from the bucket. */
  delete(bucket: Bucket, key: string): Promise<void>

  /** List the immediate contents of a directory inside the bucket. */
  listDirectory(bucket: Bucket, keyPrefix: string): Promise<ListResult>

  /** List all objects inside the bucket with pagination. */
  listBucket(bucket: Bucket, limit: number, cursor?: string): Promise<ListResult>

  /**
   * Release any persistent resources held by this provider (e.g., open
   * network connections). Called on server shutdown.
   */
  close?(): Promise<void>
}

/**
 * Registry that maps storage target names to {@link StorageProvider} instances.
 * New storage backends can be added at runtime by calling {@link register}.
 *
 * Example:
 * <pre>{@code
 * const registry = new StorageProviderRegistry()
 * registry.register('local', new LocalStorageProvider(bucketService))
 * registry.register('sftp', new SftpStorageProvider(cacheDir))
 * }</pre>
 */
export class StorageProviderRegistry {
  private readonly providers = new Map<string, StorageProvider>()

  /**
   * Register a storage provider under a given target name.
   * @param target Storage target identifier (e.g., `'local'`, `'sftp'`).
   * @param provider Provider implementation.
   */
  register(target: string, provider: StorageProvider): void {
    this.providers.set(target, provider)
  }

  /**
   * Retrieve the provider for a given target.
   * @param target Storage target identifier.
   * @throws {SFSError} when no provider has been registered for `target`.
   */
  get(target: string): StorageProvider {
    const provider = this.providers.get(target)
    if (!provider) {
      throw new SFSError(
        SFSErrorCode.NOT_IMPLEMENTED,
        `No storage provider registered for target '${target}'`,
        500,
      )
    }
    return provider
  }

  /** Returns all registered providers keyed by target name. */
  getAll(): Map<string, StorageProvider> {
    return new Map(this.providers)
  }
}

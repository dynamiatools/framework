// Shared types for simple-file-server

export type Permission = 'read' | 'write' | 'delete'

export type StorageTarget = 'local' | 'sftp'

export interface Grant {
  bucket: string
  prefixes: string[]
  permissions: Permission[]
}

export interface Identity {
  identity: string
  hashedSecret: string
  grants: Grant[]
  createdAt: string
  updatedAt: string
}

/**
 * SFTP connection configuration for a bucket backed by an SFTP server.
 */
export interface SftpConfig {
  host: string
  port: number
  username: string
  /** Plain-text password (mutually exclusive with privateKey) */
  password?: string
  /** PEM-encoded private key content (mutually exclusive with password) */
  privateKey?: string
  /** Passphrase for an encrypted private key */
  passphrase?: string
}

export interface Bucket {
  name: string
  /**
   * Absolute path to the storage root.
   * - `local` target: absolute path on the local filesystem.
   * - `sftp` target: absolute path on the remote SFTP server.
   */
  path: string
  createdAt: string
  /** Storage backend for this bucket. Defaults to `'local'` when absent. */
  storageTarget?: StorageTarget
  /** Required when storageTarget is `'sftp'`. */
  sftpConfig?: SftpConfig
}

export interface SFSConfig {
  dataDir: string
  host: string
  port: number
  logLevel: string
}

export interface ListEntry {
  key: string
  size: number
  lastModified: string
  isDirectory: boolean
}

export interface ListResult {
  bucket: string
  prefix: string
  entries: ListEntry[]
  cursor?: string
  hasMore: boolean
  total: number
}

export interface AuthContext {
  identity: string
  grants: Grant[]
}


// Shared types for simple-file-server

export type Permission = 'read' | 'write' | 'delete'

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

export interface Bucket {
  name: string
  path: string
  createdAt: string
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


import path from 'node:path'
import fsp from 'node:fs/promises'
import * as argon2 from 'argon2'
import { resolveDataPaths, readJsonFile, writeJsonFile } from '../config/config.service.js'
import type { Identity, Grant, Permission } from '../types/index.js'
import { SFSError, SFSErrorCode, notFound } from '../errors/index.js'

function conflict(message: string, code: SFSErrorCode): SFSError {
  return new SFSError(code, message, 409)
}

export class IdentityService {
  private identitiesDir: string

  constructor(dataDir: string) {
    this.identitiesDir = resolveDataPaths(dataDir).identitiesDir
  }

  private identityPath(identity: string): string {
    const safe = identity.replace(/[^a-zA-Z0-9_\-\.]/g, '_')
    return path.join(this.identitiesDir, `${safe}.json`)
  }

  async create(identity: string, secret: string): Promise<Identity> {
    const existing = await this.find(identity)
    if (existing) {
      throw conflict(`Identity '${identity}' already exists`, SFSErrorCode.IDENTITY_ALREADY_EXISTS)
    }

    const hashedSecret = await argon2.hash(secret, {
      type: argon2.argon2id,
      memoryCost: 65536,
      timeCost: 3,
      parallelism: 4,
    })

    const now = new Date().toISOString()
    const id: Identity = {
      identity,
      hashedSecret,
      grants: [],
      createdAt: now,
      updatedAt: now,
    }

    await writeJsonFile(this.identityPath(identity), id)
    return id
  }

  async find(identity: string): Promise<Identity | null> {
    return readJsonFile<Identity>(this.identityPath(identity))
  }

  async get(identity: string): Promise<Identity> {
    const id = await this.find(identity)
    if (!id) {
      throw notFound(`Identity '${identity}'`, SFSErrorCode.IDENTITY_NOT_FOUND)
    }
    return id
  }

  async list(): Promise<Identity[]> {
    try {
      const files = await fsp.readdir(this.identitiesDir)
      const identities: Identity[] = []
      for (const file of files) {
        if (!file.endsWith('.json')) continue
        const id = await readJsonFile<Identity>(path.join(this.identitiesDir, file))
        if (id) identities.push(id)
      }
      return identities
    } catch {
      return []
    }
  }

  async verify(identity: string, secret: string): Promise<Identity | null> {
    const id = await this.find(identity)
    if (!id) return null
    try {
      const valid = await argon2.verify(id.hashedSecret, secret)
      return valid ? id : null
    } catch {
      return null
    }
  }

  async grant(identity: string, grant: Grant): Promise<Identity> {
    const id = await this.get(identity)
    id.grants = id.grants.filter(g => g.bucket !== grant.bucket)
    id.grants.push(grant)
    id.updatedAt = new Date().toISOString()
    await writeJsonFile(this.identityPath(identity), id)
    return id
  }

  async revoke(identity: string, bucket: string): Promise<Identity> {
    const id = await this.get(identity)
    id.grants = id.grants.filter(g => g.bucket !== bucket)
    id.updatedAt = new Date().toISOString()
    await writeJsonFile(this.identityPath(identity), id)
    return id
  }

  async delete(identity: string): Promise<void> {
    const filePath = this.identityPath(identity)
    try {
      await fsp.unlink(filePath)
    } catch {
      throw notFound(`Identity '${identity}'`, SFSErrorCode.IDENTITY_NOT_FOUND)
    }
  }

  checkPermission(id: Identity, bucket: string, key: string, permission: Permission): Grant | null {
    for (const grant of id.grants) {
      if (grant.bucket !== bucket) continue
      if (!grant.permissions.includes(permission)) continue

      if (grant.prefixes.length === 0) return grant

      const normalizedKey = key.startsWith('/') ? key : `/${key}`
      for (const prefix of grant.prefixes) {
        const normalizedPrefix = prefix.startsWith('/') ? prefix : `/${prefix}`
        if (normalizedKey.startsWith(normalizedPrefix) || normalizedPrefix === '/') {
          return grant
        }
      }
    }
    return null
  }
}

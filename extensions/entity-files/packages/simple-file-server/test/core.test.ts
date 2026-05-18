import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import path from 'node:path'
import os from 'node:os'
import fsp from 'node:fs/promises'
import { BucketService } from '../src/storage/bucket.service.js'
import { StorageService } from '../src/storage/storage.service.js'
import { IdentityService } from '../src/auth/identity.service.js'
import { ensureDataDirs } from '../src/config/config.service.js'
import { Readable } from 'node:stream'

let tempDir: string
let bucketService: BucketService
let storageService: StorageService
let identityService: IdentityService

beforeEach(async () => {
  tempDir = await fsp.mkdtemp(path.join(os.tmpdir(), 'sfs-test-'))
  await ensureDataDirs(tempDir)
  bucketService = new BucketService(tempDir)
  storageService = new StorageService(bucketService)
  identityService = new IdentityService(tempDir)
})

afterEach(async () => {
  await fsp.rm(tempDir, { recursive: true, force: true })
})

// ──────────────────────────────────────────────────────────
// BucketService Tests
// ──────────────────────────────────────────────────────────
describe('BucketService', () => {
  it('should create a bucket', async () => {
    const bucketPath = path.join(tempDir, 'my-bucket')
    const bucket = await bucketService.create('test', bucketPath)
    expect(bucket.name).toBe('test')
    expect(bucket.path).toBe(bucketPath)
    expect(bucket.createdAt).toBeDefined()
  })

  it('should reject non-absolute paths', async () => {
    await expect(bucketService.create('bad', 'relative/path')).rejects.toThrow('must be absolute')
  })

  it('should prevent duplicate buckets', async () => {
    const bucketPath = path.join(tempDir, 'dup-bucket')
    await bucketService.create('dup', bucketPath)
    await expect(bucketService.create('dup', bucketPath)).rejects.toThrow('already exists')
  })

  it('should list buckets', async () => {
    await bucketService.create('b1', path.join(tempDir, 'b1'))
    await bucketService.create('b2', path.join(tempDir, 'b2'))
    const list = await bucketService.list()
    expect(list.length).toBe(2)
  })

  it('should delete a bucket', async () => {
    await bucketService.create('del', path.join(tempDir, 'del'))
    await bucketService.delete('del')
    const bucket = await bucketService.find('del')
    expect(bucket).toBeNull()
  })

  it('should detect path traversal attacks', async () => {
    const bucketPath = path.join(tempDir, 'safe-bucket')
    const bucket = await bucketService.create('safe', bucketPath)
    expect(() => bucketService.resolveKey(bucket, '../../../etc/passwd')).toThrow()
  })

  it('should block access to .sfs directory', async () => {
    const bucketPath = path.join(tempDir, 'internal-bucket')
    const bucket = await bucketService.create('internal', bucketPath)
    expect(() => bucketService.resolveKey(bucket, '.sfs/staging/file.txt')).toThrow()
  })
})

// ──────────────────────────────────────────────────────────
// IdentityService Tests
// ──────────────────────────────────────────────────────────
describe('IdentityService', () => {
  it('should create an identity', async () => {
    const identity = await identityService.create('test-user', 'secret123')
    expect(identity.identity).toBe('test-user')
    expect(identity.hashedSecret).not.toBe('secret123')
    expect(identity.grants).toEqual([])
  })

  it('should verify correct credentials', async () => {
    await identityService.create('verifiable', 'my-secret')
    const result = await identityService.verify('verifiable', 'my-secret')
    expect(result).not.toBeNull()
    expect(result?.identity).toBe('verifiable')
  })

  it('should reject incorrect credentials', async () => {
    await identityService.create('user1', 'correct-pass')
    const result = await identityService.verify('user1', 'wrong-pass')
    expect(result).toBeNull()
  })

  it('should return null for unknown identity', async () => {
    const result = await identityService.verify('unknown', 'any')
    expect(result).toBeNull()
  })

  it('should grant and check permissions', async () => {
    const id = await identityService.create('perm-user', 'pass')
    await identityService.grant('perm-user', {
      bucket: 'docs',
      prefixes: ['/tenant-a/'],
      permissions: ['read', 'write'],
    })

    const updated = await identityService.get('perm-user')
    const grant = identityService.checkPermission(updated, 'docs', '/tenant-a/file.pdf', 'read')
    expect(grant).not.toBeNull()

    const denied = identityService.checkPermission(updated, 'docs', '/tenant-b/file.pdf', 'read')
    expect(denied).toBeNull()
  })

  it('should revoke permissions', async () => {
    await identityService.create('rev-user', 'pass')
    await identityService.grant('rev-user', {
      bucket: 'docs',
      prefixes: [],
      permissions: ['read'],
    })
    await identityService.revoke('rev-user', 'docs')

    const id = await identityService.get('rev-user')
    expect(id.grants).toHaveLength(0)
  })

  it('should delete an identity', async () => {
    await identityService.create('deletable', 'pass')
    await identityService.delete('deletable')
    const found = await identityService.find('deletable')
    expect(found).toBeNull()
  })
})

// ──────────────────────────────────────────────────────────
// StorageService Tests
// ──────────────────────────────────────────────────────────
describe('StorageService', () => {
  let bucket: Awaited<ReturnType<typeof bucketService.create>>

  beforeEach(async () => {
    bucket = await bucketService.create('store', path.join(tempDir, 'store'))
  })

  it('should upload and download a file', async () => {
    const content = 'Hello, SFS!'
    const source = Readable.from([content])
    await storageService.upload(bucket, 'test/hello.txt', source)

    const download = await storageService.download(bucket, 'test/hello.txt')
    const chunks: Buffer[] = []
    for await (const chunk of download.stream) {
      chunks.push(Buffer.from(chunk))
    }
    expect(Buffer.concat(chunks).toString('utf-8')).toBe(content)
    expect(download.size).toBe(content.length)
  })

  it('should delete a file', async () => {
    const source = Readable.from(['delete me'])
    await storageService.upload(bucket, 'to-delete.txt', source)
    await storageService.delete(bucket, 'to-delete.txt')
    await expect(storageService.download(bucket, 'to-delete.txt')).rejects.toThrow()
  })

  it('should throw on missing file download', async () => {
    await expect(storageService.download(bucket, 'nonexistent.txt')).rejects.toThrow()
  })

  it('should list directory contents', async () => {
    await storageService.upload(bucket, 'dir/a.txt', Readable.from(['a']))
    await storageService.upload(bucket, 'dir/b.txt', Readable.from(['b']))

    const result = await storageService.listDirectory(bucket, 'dir')
    expect(result.entries.length).toBe(2)
    const keys = result.entries.map(e => e.key)
    expect(keys).toContain('dir/a.txt')
    expect(keys).toContain('dir/b.txt')
  })

  it('should list a bucket with pagination', async () => {
    for (let i = 0; i < 5; i++) {
      await storageService.upload(bucket, `file-${i}.txt`, Readable.from([`content-${i}`]))
    }

    const page1 = await storageService.listBucket(bucket, 3)
    expect(page1.entries.length).toBe(3)
    expect(page1.hasMore).toBe(true)

    const page2 = await storageService.listBucket(bucket, 3, page1.cursor)
    expect(page2.entries.length).toBe(2)
    expect(page2.hasMore).toBe(false)
  })
})


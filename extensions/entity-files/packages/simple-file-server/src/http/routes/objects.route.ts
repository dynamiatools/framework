import type { FastifyInstance, FastifyRequest, FastifyReply } from 'fastify'
import type { BucketService } from '../../storage/bucket.service.js'
import type { StorageService } from '../../storage/storage.service.js'
import type { ThumbnailService } from '../../thumbnail/thumbnail.service.js'
import type { IdentityService } from '../../auth/identity.service.js'
import type { OperationalLogger } from '../../logging/logger.js'
import { SFSError, SFSErrorCode, forbidden, successResponse } from '../../errors/index.js'
import type { Permission } from '../../types/index.js'

interface ObjectRoutesOptions {
  bucketService: BucketService
  storageService: StorageService
  thumbnailService: ThumbnailService
  identityService: IdentityService
  logger: OperationalLogger
}

export async function objectRoutes(fastify: FastifyInstance, options: ObjectRoutesOptions): Promise<void> {
  const { bucketService, storageService, thumbnailService, identityService, logger } = options

  /**
   * Helper: resolve bucket and check permission
   */
  async function resolveBucketAndCheck(
    request: FastifyRequest,
    reply: FastifyReply,
    bucketName: string,
    key: string,
    permission: Permission,
  ) {
    const bucket = await bucketService.get(bucketName)
    const identity = request.authIdentity

    const grant = identityService.checkPermission(identity, bucketName, key, permission)
    if (!grant) {
      logger.logError('permission_denied', `Identity '${identity.identity}' lacks '${permission}' on '${bucketName}/${key}'`, {
        identity: identity.identity,
        bucket: bucketName,
        key,
        ip: request.ip,
      })
      throw forbidden(`Permission '${permission}' denied on bucket '${bucketName}' for path '${key}'`)
    }

    return bucket
  }

  /**
   * GET /:bucket - Bucket listing with pagination
   */
  fastify.get<{
    Params: { bucket: string }
    Querystring: { limit?: string; cursor?: string }
  }>('/:bucket', async (request, reply) => {
    const { bucket: bucketName } = request.params
    const limit = Math.min(parseInt(request.query.limit ?? '100', 10), 1000)
    const cursor = request.query.cursor

    const bucket = await resolveBucketAndCheck(request, reply, bucketName, '/', 'read')
    const start = Date.now()

    const result = await storageService.listBucket(bucket, limit, cursor)

    logger.log({
      event: 'list',
      identity: request.authIdentity.identity,
      bucket: bucketName,
      elapsedMs: Date.now() - start,
      ip: request.ip,
    })

    return reply.send(successResponse(result))
  })

  /**
   * GET /:bucket/*key - Download file or list directory
   */
  fastify.get<{
    Params: { bucket: string; '*': string }
    Querystring: { w?: string; h?: string; fit?: string; format?: string }
  }>('/:bucket/*', async (request, reply) => {
    const { bucket: bucketName } = request.params
    const key = request.params['*'] ?? ''

    const bucket = await resolveBucketAndCheck(request, reply, bucketName, key, 'read')
    const start = Date.now()

    // If key ends with '/', it's a directory listing
    if (key.endsWith('/') || key === '') {
      const result = await storageService.listDirectory(bucket, key)
      logger.log({
        event: 'list',
        identity: request.authIdentity.identity,
        bucket: bucketName,
        key,
        elapsedMs: Date.now() - start,
        ip: request.ip,
      })
      return reply.send(successResponse(result))
    }

    // Check for thumbnail request
    const { w, h, fit, format } = request.query
    if (w || h) {
      const thumbOptions = {
        width: w ? parseInt(w, 10) : undefined,
        height: h ? parseInt(h, 10) : undefined,
        fit: (fit as 'cover' | 'contain' | 'fill' | 'inside' | 'outside') ?? 'cover',
        format: (format as 'jpeg' | 'png' | 'webp') ?? 'webp',
      }

      const thumb = await thumbnailService.getThumbnail(bucket, key, thumbOptions)
      if (thumb) {
        reply.header('Content-Type', thumb.mimeType)
        reply.header('Content-Length', thumb.size.toString())
        reply.header('Cache-Control', 'public, max-age=86400')
        logger.log({
          event: 'download',
          identity: request.authIdentity.identity,
          bucket: bucketName,
          key: `${key}?thumbnail`,
          size: thumb.size,
          elapsedMs: Date.now() - start,
          ip: request.ip,
        })
        return reply.send(thumb.stream)
      }
    }

    // Regular file download
    const download = await storageService.download(bucket, key)

    reply.header('Content-Type', download.mimeType)
    reply.header('Content-Length', download.size.toString())
    reply.header('ETag', download.etag)
    reply.header('Last-Modified', download.lastModified.toUTCString())
    reply.header('Cache-Control', 'private, max-age=0')

    logger.log({
      event: 'download',
      identity: request.authIdentity.identity,
      bucket: bucketName,
      key,
      size: download.size,
      elapsedMs: Date.now() - start,
      ip: request.ip,
    })

    return reply.send(download.stream)
  })

  /**
   * PUT /:bucket/*key - Upload file
   */
  fastify.put<{
    Params: { bucket: string; '*': string }
  }>('/:bucket/*', async (request, reply) => {
    const { bucket: bucketName } = request.params
    const key = request.params['*'] ?? ''

    if (!key) {
      return reply.status(400).send({
        ok: false,
        error: { code: SFSErrorCode.PATH_INVALID, message: 'Key is required for upload' },
      })
    }

    const bucket = await resolveBucketAndCheck(request, reply, bucketName, key, 'write')
    const start = Date.now()

    logger.log({
      event: 'upload_started',
      identity: request.authIdentity.identity,
      bucket: bucketName,
      key,
      ip: request.ip,
    })

    try {
      const result = await storageService.upload(bucket, key, request.raw as any)

      logger.log({
        event: 'upload_committed',
        identity: request.authIdentity.identity,
        bucket: bucketName,
        key,
        size: result.size,
        elapsedMs: Date.now() - start,
        ip: request.ip,
      })

      return reply.status(201).send(successResponse({
        bucket: bucketName,
        key,
        size: result.size,
        etag: result.etag,
      }))
    } catch (err) {
      logger.logError('upload_failed', err instanceof Error ? err : String(err), {
        identity: request.authIdentity.identity,
        bucket: bucketName,
        key,
        elapsedMs: Date.now() - start,
        ip: request.ip,
      })
      throw err
    }
  })

  /**
   * DELETE /:bucket/*key - Delete file
   */
  fastify.delete<{
    Params: { bucket: string; '*': string }
  }>('/:bucket/*', async (request, reply) => {
    const { bucket: bucketName } = request.params
    const key = request.params['*'] ?? ''

    if (!key) {
      return reply.status(400).send({
        ok: false,
        error: { code: SFSErrorCode.PATH_INVALID, message: 'Key is required for delete' },
      })
    }

    const bucket = await resolveBucketAndCheck(request, reply, bucketName, key, 'delete')
    const start = Date.now()

    await storageService.delete(bucket, key)

    logger.log({
      event: 'delete',
      identity: request.authIdentity.identity,
      bucket: bucketName,
      key,
      elapsedMs: Date.now() - start,
      ip: request.ip,
    })

    return reply.send(successResponse({ bucket: bucketName, key, deleted: true }))
  })
}


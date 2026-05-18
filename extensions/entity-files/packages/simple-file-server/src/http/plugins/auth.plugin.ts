import type { FastifyInstance, FastifyRequest, FastifyReply } from 'fastify'
import fp from 'fastify-plugin'
import { IdentityService } from '../../auth/identity.service.js'
import type { Identity } from '../../types/index.js'
import { SFSError, SFSErrorCode, unauthorized } from '../../errors/index.js'
import type { OperationalLogger } from '../../logging/logger.js'

declare module 'fastify' {
  interface FastifyRequest {
    authIdentity: Identity
  }
}

interface AuthPluginOptions {
  identityService: IdentityService
  logger: OperationalLogger
}

/**
 * Authentication plugin wrapped with fastify-plugin so that:
 *  - The `authIdentity` request decoration is visible in all sibling/child plugins.
 *  - The `onRequest` hook applies to ALL routes registered on the root instance.
 *
 * Without fastify-plugin, Fastify's encapsulation would isolate the decoration
 * and hook to routes within this plugin's scope only.
 */
export const authPlugin = fp(async function authPlugin(
  fastify: FastifyInstance,
  options: AuthPluginOptions,
): Promise<void> {
  const { identityService, logger } = options

  // Use a plain null initial value — NOT { getter: () => null }.
  // In Fastify v5 the getter-object form defines the property as read-only
  // (getter-only, no setter), which causes:
  //   "Cannot set property authIdentity of #<_Request> which has only a getter"
  // when the onRequest hook does `request.authIdentity = id`.
  fastify.decorateRequest<Identity | null>('authIdentity', null)

  fastify.addHook('onRequest', async (request: FastifyRequest, reply: FastifyReply) => {
    // Skip health check
    if (request.url === '/health') return

    const identity = extractIdentity(request)
    const secret = extractSecret(request)

    if (!identity || !secret) {
      logger.logError('auth_failed', 'Missing credentials', { ip: request.ip })
      const err = unauthorized('Authentication required. Provide X-SFS-Identity and X-SFS-Secret headers.')
      return reply.status(err.statusCode).send(err.toJSON())
    }

    const id = await identityService.verify(identity, secret)
    if (!id) {
      logger.logError('auth_failed', 'Invalid credentials', { identity, ip: request.ip })
      const err = new SFSError(SFSErrorCode.AUTH_INVALID, 'Invalid credentials', 401)
      return reply.status(err.statusCode).send(err.toJSON())
    }

    request.authIdentity = id
  })
}, { name: 'sfs-auth' })

function extractIdentity(request: FastifyRequest): string | undefined {
  const headerIdentity = request.headers['x-sfs-identity']
  if (typeof headerIdentity === 'string' && headerIdentity) return headerIdentity

  const auth = request.headers.authorization
  if (auth?.startsWith('Basic ')) {
    const decoded = Buffer.from(auth.slice(6), 'base64').toString('utf-8')
    const colonIdx = decoded.indexOf(':')
    if (colonIdx > 0) return decoded.slice(0, colonIdx)
  }

  return undefined
}

function extractSecret(request: FastifyRequest): string | undefined {
  const headerSecret = request.headers['x-sfs-secret']
  if (typeof headerSecret === 'string' && headerSecret) return headerSecret

  const auth = request.headers.authorization
  if (auth?.startsWith('Basic ')) {
    const decoded = Buffer.from(auth.slice(6), 'base64').toString('utf-8')
    const colonIdx = decoded.indexOf(':')
    if (colonIdx >= 0) return decoded.slice(colonIdx + 1)
  }

  return undefined
}


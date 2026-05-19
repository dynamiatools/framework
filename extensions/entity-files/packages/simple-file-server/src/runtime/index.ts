import path from 'node:path'
import { ensureDataDirs, getDefaultConfig, resolveDataPaths } from '../config/config.service.js'
import { IdentityService } from '../auth/identity.service.js'
import { BucketService } from '../storage/bucket.service.js'
import { StorageService } from '../storage/storage.service.js'
import { SftpStorageProvider } from '../storage/sftp.provider.js'
import { ThumbnailService } from '../thumbnail/thumbnail.service.js'
import { OperationalLogger } from '../logging/logger.js'
import { createServer } from '../http/server.js'
import type { SFSConfig } from '../types/index.js'

export interface SFSRuntime {
  identityService: IdentityService
  bucketService: BucketService
  storageService: StorageService
  thumbnailService: ThumbnailService
  operationalLogger: OperationalLogger
  config: SFSConfig
}

export async function createRuntime(overrides?: Partial<SFSConfig>): Promise<SFSRuntime> {
  const config: SFSConfig = { ...getDefaultConfig(), ...overrides }

  // Ensure all data directories exist
  await ensureDataDirs(config.dataDir)

  // Create services
  const operationalLogger = new OperationalLogger(config.dataDir)
  await operationalLogger.open()

  const identityService = new IdentityService(config.dataDir)
  const bucketService = new BucketService(config.dataDir)
  const storageService = new StorageService(bucketService)

  // Register the SFTP provider so SFTP buckets are supported out-of-the-box
  const sftpStagingDir = path.join(resolveDataPaths(config.dataDir).cacheDir, 'sftp-staging')
  storageService.registerProvider('sftp', new SftpStorageProvider(sftpStagingDir))

  const thumbnailService = new ThumbnailService(bucketService, storageService)

  return {
    config,
    identityService,
    bucketService,
    storageService,
    thumbnailService,
    operationalLogger,
  }
}

export async function startServer(overrides?: Partial<SFSConfig>): Promise<SFSRuntime> {
  const runtime = await createRuntime(overrides)
  const { config, bucketService, storageService, thumbnailService, identityService, operationalLogger } = runtime

  // Validate buckets on startup (uses StorageService to dispatch to correct provider)
  await storageService.validateStartup()

  const server = await createServer({
    config,
    bucketService,
    storageService,
    thumbnailService,
    identityService,
    operationalLogger,
  })

  operationalLogger.log({ event: 'server_start', message: `Starting SFS on ${config.host}:${config.port}` })

  // Graceful shutdown
  const shutdown = async (signal: string) => {
    server.log.info(`Received ${signal}, gracefully shutting down...`)
    operationalLogger.log({ event: 'server_stop', message: `Shutting down (${signal})` })
    await server.close()
    await storageService.closeProviders()
    await operationalLogger.close()
    process.exit(0)
  }

  process.on('SIGTERM', () => shutdown('SIGTERM'))
  process.on('SIGINT', () => shutdown('SIGINT'))

  try {
    await server.listen({ host: config.host, port: config.port })
    server.log.info(`SFS server listening on http://${config.host}:${config.port}`)
  } catch (err) {
    server.log.error(err)
    await operationalLogger.close()
    process.exit(1)
  }

  return runtime
}


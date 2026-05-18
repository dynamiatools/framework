import path from 'node:path'
import fsp from 'node:fs/promises'
import fs from 'node:fs'
import crypto from 'node:crypto'
import sharp from 'sharp'
import type { Bucket } from '../types/index.js'
import { BucketService } from '../storage/bucket.service.js'
import { StorageService } from '../storage/storage.service.js'

export interface ThumbnailOptions {
  width?: number
  height?: number
  fit?: 'cover' | 'contain' | 'fill' | 'inside' | 'outside'
  format?: 'jpeg' | 'png' | 'webp'
}

export interface ThumbnailResult {
  stream: fs.ReadStream
  mimeType: string
  size: number
}

export class ThumbnailService {
  constructor(
    private readonly bucketService: BucketService,
    private readonly storageService: StorageService,
  ) {}

  /**
   * Generate or retrieve a cached thumbnail.
   */
  async getThumbnail(bucket: Bucket, key: string, options: ThumbnailOptions): Promise<ThumbnailResult | null> {
    const filePath = this.bucketService.resolveKey(bucket, key)

    // Check if file exists
    try {
      await fsp.stat(filePath)
    } catch {
      return null
    }

    // Detect mime type
    const mimeType = await this.storageService.detectMimeType(filePath)
    if (!this.storageService.isImageMime(mimeType)) {
      return null
    }

    const cacheKey = this.buildCacheKey(key, options)
    const thumbsDir = this.bucketService.getThumbsDir(bucket)
    const thumbPath = path.join(thumbsDir, cacheKey)
    const outputFormat = options.format ?? 'webp'

    // Check if cached thumbnail exists
    try {
      const stat = await fsp.stat(thumbPath)
      return {
        stream: fs.createReadStream(thumbPath),
        mimeType: `image/${outputFormat}`,
        size: stat.size,
      }
    } catch {
      // Generate thumbnail
    }

    // Generate thumbnail
    await fsp.mkdir(path.dirname(thumbPath), { recursive: true })

    try {
      let pipeline = sharp(filePath)

      if (options.width || options.height) {
        pipeline = pipeline.resize({
          width: options.width,
          height: options.height,
          fit: options.fit ?? 'contain',
          withoutEnlargement: true,
        })
      }

      if (outputFormat === 'webp') {
        pipeline = pipeline.webp({ quality: 85 })
      } else if (outputFormat === 'png') {
        pipeline = pipeline.png({ compressionLevel: 6 })
      } else {
        pipeline = pipeline.jpeg({ quality: 85 })
      }

      await pipeline.toFile(thumbPath)

      const stat = await fsp.stat(thumbPath)
      return {
        stream: fs.createReadStream(thumbPath),
        mimeType: `image/${outputFormat}`,
        size: stat.size,
      }
    } catch (err) {
      // Cleanup failed thumb
      await fsp.unlink(thumbPath).catch(() => {})
      return null
    }
  }

  private buildCacheKey(key: string, options: ThumbnailOptions): string {
    const normalized = key.replace(/\//g, '_').replace(/\s/g, '_')
    const optStr = `${options.width ?? 0}x${options.height ?? 0}-${options.fit ?? 'contain'}-${options.format ?? 'webp'}`
    const hash = crypto.createHash('sha1').update(`${normalized}:${optStr}`).digest('hex').slice(0, 12)
    const ext = options.format ?? 'webp'
    return `${hash}.${ext}`
  }
}


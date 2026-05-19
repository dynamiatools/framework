import path from 'node:path'
import fsp from 'node:fs/promises'
import fs from 'node:fs'
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
   * Generate or retrieve a thumbnail.
   * Thumbnails are stored inside the bucket alongside the original file:
   *   {original_dir}/{WxH}/{original_filename}
   * Example:
   *   Request:  account1/producto.jpg?w=200&h=200
   *   Stored:   account1/200x200/producto.jpg
   */
  async getThumbnail(bucket: Bucket, key: string, options: ThumbnailOptions): Promise<ThumbnailResult | null> {
    // Thumbnails are only supported for local-storage buckets
    if (bucket.storageTarget && bucket.storageTarget !== 'local') {
      return null
    }

    const filePath = this.bucketService.resolveKey(bucket, key)

    // Check if original file exists
    try {
      await fsp.stat(filePath)
    } catch {
      return null
    }

    // Detect mime type — only process images
    const mimeType = await this.storageService.detectMimeType(filePath)
    if (!this.storageService.isImageMime(mimeType)) {
      return null
    }

    // Resolve output format: explicit param > inferred from original mime
    const outputFormat = options.format ?? this.formatFromMime(mimeType)

    // Build the thumbnail key and resolve its absolute path inside the bucket
    const thumbKey = this.buildThumbKey(key, options)
    const thumbPath = this.bucketService.resolveKey(bucket, thumbKey)

    // Serve existing thumbnail if already cached
    try {
      const stat = await fsp.stat(thumbPath)
      return {
        stream: fs.createReadStream(thumbPath),
        mimeType: `image/${outputFormat}`,
        size: stat.size,
      }
    } catch {
      // Not cached yet — generate below
    }

    // Ensure the {WxH} sub-directory exists
    await fsp.mkdir(path.dirname(thumbPath), { recursive: true })

    try {
      let pipeline = sharp(filePath)

      if (options.width || options.height) {
        pipeline = pipeline.resize({
          width: options.width,
          height: options.height,
          fit: options.fit ?? 'cover',
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
      // Clean up any partial file on failure
      await fsp.unlink(thumbPath).catch(() => {})
      return null
    }
  }

  /**
   * Build the bucket-relative key for a thumbnail.
   * Structure: {original_dir}/{WxH}/{original_filename}
   * Examples:
   *   "account1/producto.jpg"  + 200x200  →  "account1/200x200/producto.jpg"
   *   "photo.png"              + 100x100  →  "100x100/photo.png"
   */
  buildThumbKey(key: string, options: Pick<ThumbnailOptions, 'width' | 'height'>): string {
    const dir = path.dirname(key)       // "account1" | "."
    const filename = path.basename(key) // "producto.jpg"
    const w = options.width ?? 0
    const h = options.height ?? 0
    const dimensionFolder = `${w}x${h}`
    const baseDir = dir === '.' ? '' : dir
    return baseDir ? `${baseDir}/${dimensionFolder}/${filename}` : `${dimensionFolder}/${filename}`
  }

  /**
   * Infer a sharp-compatible output format from a MIME type.
   */
  private formatFromMime(mimeType: string): 'jpeg' | 'png' | 'webp' {
    if (mimeType === 'image/png') return 'png'
    if (mimeType === 'image/webp') return 'webp'
    return 'jpeg'
  }
}


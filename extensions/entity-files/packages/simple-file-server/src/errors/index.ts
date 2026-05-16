// SFS Error codes and helpers

export enum SFSErrorCode {
  // Auth
  AUTH_REQUIRED = 'SFS_AUTH_REQUIRED',
  AUTH_INVALID = 'SFS_AUTH_INVALID',
  PERMISSION_DENIED = 'SFS_PERMISSION_DENIED',

  // Objects
  OBJECT_NOT_FOUND = 'SFS_OBJECT_NOT_FOUND',
  OBJECT_IS_DIRECTORY = 'SFS_OBJECT_IS_DIRECTORY',
  PATH_TRAVERSAL = 'SFS_PATH_TRAVERSAL',
  PATH_INVALID = 'SFS_PATH_INVALID',
  PATH_RESERVED = 'SFS_PATH_RESERVED',

  // Buckets
  BUCKET_NOT_FOUND = 'SFS_BUCKET_NOT_FOUND',
  BUCKET_ALREADY_EXISTS = 'SFS_BUCKET_ALREADY_EXISTS',
  BUCKET_PATH_INVALID = 'SFS_BUCKET_PATH_INVALID',
  BUCKET_PATH_NOT_WRITABLE = 'SFS_BUCKET_PATH_NOT_WRITABLE',

  // Identities
  IDENTITY_NOT_FOUND = 'SFS_IDENTITY_NOT_FOUND',
  IDENTITY_ALREADY_EXISTS = 'SFS_IDENTITY_ALREADY_EXISTS',

  // Uploads
  UPLOAD_FAILED = 'SFS_UPLOAD_FAILED',
  UPLOAD_STAGING_FAILED = 'SFS_UPLOAD_STAGING_FAILED',

  // General
  INTERNAL_ERROR = 'SFS_INTERNAL_ERROR',
  NOT_IMPLEMENTED = 'SFS_NOT_IMPLEMENTED',
}

export interface SFSErrorDetails {
  code: SFSErrorCode
  message: string
  details?: Record<string, unknown>
}

export class SFSError extends Error {
  public readonly code: SFSErrorCode
  public readonly statusCode: number
  public readonly details?: Record<string, unknown>

  constructor(code: SFSErrorCode, message: string, statusCode: number = 500, details?: Record<string, unknown>) {
    super(message)
    this.name = 'SFSError'
    this.code = code
    this.statusCode = statusCode
    this.details = details
  }

  toJSON(): { ok: false; error: SFSErrorDetails } {
    return {
      ok: false,
      error: {
        code: this.code,
        message: this.message,
        details: this.details,
      },
    }
  }
}

export function notFound(resource: string, code: SFSErrorCode = SFSErrorCode.OBJECT_NOT_FOUND): SFSError {
  return new SFSError(code, `${resource} not found`, 404)
}

export function forbidden(message: string, code: SFSErrorCode = SFSErrorCode.PERMISSION_DENIED): SFSError {
  return new SFSError(code, message, 403)
}

export function unauthorized(message: string = 'Authentication required'): SFSError {
  return new SFSError(SFSErrorCode.AUTH_REQUIRED, message, 401)
}

export function badRequest(message: string, code: SFSErrorCode = SFSErrorCode.PATH_INVALID): SFSError {
  return new SFSError(code, message, 400)
}

export function conflict(message: string, code: SFSErrorCode): SFSError {
  return new SFSError(code, message, 409)
}

export function internalError(message: string, details?: Record<string, unknown>): SFSError {
  return new SFSError(SFSErrorCode.INTERNAL_ERROR, message, 500, details)
}

export function successResponse<T>(data: T): { ok: true; data: T } {
  return { ok: true, data }
}


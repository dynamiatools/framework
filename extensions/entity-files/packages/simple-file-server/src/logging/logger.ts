import path from 'node:path'
import fsp from 'node:fs/promises'
import fs from 'node:fs'
import { resolveDataPaths } from '../config/config.service.js'

export type LogEvent =
  | 'upload_started'
  | 'upload_staged'
  | 'upload_committed'
  | 'upload_failed'
  | 'download'
  | 'list'
  | 'delete'
  | 'auth_failed'
  | 'permission_denied'
  | 'server_start'
  | 'server_stop'

export interface LogEntry {
  ts: string
  event: LogEvent
  identity?: string
  bucket?: string
  key?: string
  size?: number
  elapsedMs?: number
  ip?: string
  message?: string
  error?: string
  [key: string]: unknown
}

export class OperationalLogger {
  private accessLogPath: string
  private errorLogPath: string
  private accessStream: fs.WriteStream | null = null
  private errorStream: fs.WriteStream | null = null

  constructor(dataDir: string) {
    const logsDir = resolveDataPaths(dataDir).logsDir
    this.accessLogPath = path.join(logsDir, 'access.log.jsonl')
    this.errorLogPath = path.join(logsDir, 'error.log.jsonl')
  }

  async open(): Promise<void> {
    const logsDir = path.dirname(this.accessLogPath)
    await fsp.mkdir(logsDir, { recursive: true })

    this.accessStream = fs.createWriteStream(this.accessLogPath, { flags: 'a' })
    this.errorStream = fs.createWriteStream(this.errorLogPath, { flags: 'a' })
  }

  async close(): Promise<void> {
    await new Promise<void>((resolve) => {
      if (this.accessStream) {
        this.accessStream.end(resolve)
      } else {
        resolve()
      }
    })
    await new Promise<void>((resolve) => {
      if (this.errorStream) {
        this.errorStream.end(resolve)
      } else {
        resolve()
      }
    })
  }

  log(entry: Omit<LogEntry, 'ts'> & { event: LogEvent }): void {
    const fullEntry: LogEntry = {
      ts: new Date().toISOString(),
      ...entry,
    }

    const line = JSON.stringify(fullEntry) + '\n'
    const isError = entry.event === 'auth_failed'
      || entry.event === 'permission_denied'
      || entry.event === 'upload_failed'

    if (this.accessStream) {
      this.accessStream.write(line)
    }

    if (isError && this.errorStream) {
      this.errorStream.write(line)
    }
  }

  logAccess(event: LogEvent, data: Partial<LogEntry>): void {
    this.log({ event, ...data })
  }

  logError(event: LogEvent, error: Error | string, data: Partial<LogEntry> = {}): void {
    this.log({
      event,
      error: typeof error === 'string' ? error : error.message,
      ...data,
    })
  }
}



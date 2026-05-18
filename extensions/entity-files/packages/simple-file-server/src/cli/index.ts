#!/usr/bin/env node

import { Command } from 'commander'
import { createReadStream } from 'node:fs'
import { stat } from 'node:fs/promises'
import { randomBytes } from 'node:crypto'
import os from 'node:os'
import { createRuntime, startServer } from '../runtime/index.js'
import { getDataDir } from '../config/config.service.js'
import { bashCompletion, zshCompletion, fishCompletion } from './completion.js'
import type { Permission, Bucket, Identity } from '../types/index.js'

const program = new Command()

program
  .name('sfs')
  .description('Simple File Server - filesystem-native file server with S3-style API')
  .version('26.5.1')

// ──────────────────────────────────────────────────────────
// sfs serve
// ──────────────────────────────────────────────────────────
program
  .command('serve')
  .description('Start the SFS server')
  .option('--host <host>', 'Host to listen on', process.env.SFS_HOST ?? '0.0.0.0')
  .option('--port <port>', 'Port to listen on', process.env.SFS_PORT ?? '8080')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .option('--log-level <level>', 'Log level (trace|debug|info|warn|error)', 'info')
  .action(async (opts) => {
    printEnvInfo()
    const runtime = await startServer({
      host: opts.host,
      port: parseInt(opts.port, 10),
      dataDir: opts.dataDir,
      logLevel: opts.logLevel,
    })
    await printServerSummary(runtime)
  })

// ──────────────────────────────────────────────────────────
// sfs create
// ──────────────────────────────────────────────────────────
const create = program.command('create').description('Create resources')

create
  .command('bucket <name> <absolutePath>')
  .description('Create a new bucket mapped to an absolute filesystem path')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (name: string, absolutePath: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const bucket = await runtime.bucketService.create(name, absolutePath)
      console.log(JSON.stringify({ ok: true, data: bucket }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

create
  .command('identity <identity> <secret>')
  .description('Create a new identity with the given secret')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (identity: string, secret: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const id = await runtime.identityService.create(identity, secret)
      const safeId = { ...id, hashedSecret: '[REDACTED]' }
      console.log(JSON.stringify({ ok: true, data: safeId }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// sfs list
// ──────────────────────────────────────────────────────────
const list = program.command('list').description('List resources')

list
  .command('buckets')
  .description('List all buckets')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const buckets = await runtime.bucketService.list()
      console.log(JSON.stringify({ ok: true, data: buckets }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

list
  .command('identities')
  .description('List all identities')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const ids = await runtime.identityService.list()
      const safeIds = ids.map(id => ({ ...id, hashedSecret: '[REDACTED]' }))
      console.log(JSON.stringify({ ok: true, data: safeIds }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

list
  .command('files <bucket> [path]')
  .description('List files in a bucket or directory path')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .option('--limit <limit>', 'Max entries to return (bucket root only)', '100')
  .option('--cursor <cursor>', 'Pagination cursor (bucket root only)')
  .action(async (bucketName: string, keyPath: string | undefined, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const bucket = await runtime.bucketService.get(bucketName)
      let result
      if (keyPath) {
        result = await runtime.storageService.listDirectory(bucket, keyPath)
      } else {
        result = await runtime.storageService.listBucket(bucket, parseInt(opts.limit, 10), opts.cursor)
      }
      console.log(JSON.stringify({ ok: true, data: result }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// sfs remove
// ──────────────────────────────────────────────────────────
const remove = program.command('remove').description('Remove resources')

remove
  .command('bucket <name>')
  .description('Remove a bucket definition (does not delete files)')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (name: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      await runtime.bucketService.delete(name)
      console.log(JSON.stringify({ ok: true, data: { removed: name } }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

remove
  .command('identity <identity>')
  .description('Remove an identity')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (identity: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      await runtime.identityService.delete(identity)
      console.log(JSON.stringify({ ok: true, data: { removed: identity } }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// sfs grant
// ──────────────────────────────────────────────────────────
program
  .command('grant <identity> <bucket>')
  .description('Grant permissions to an identity on a bucket')
  .option('--read', 'Grant read permission')
  .option('--write', 'Grant write permission')
  .option('--delete', 'Grant delete permission')
  .option('--prefix <prefix>', 'Restrict to path prefix (can be used multiple times)', collect, [] as string[])
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (identity: string, bucket: string, opts) => {
    const permissions: Permission[] = []
    if (opts.read) permissions.push('read')
    if (opts.write) permissions.push('write')
    if (opts.delete) permissions.push('delete')

    if (permissions.length === 0) {
      console.error(JSON.stringify({ ok: false, error: { code: 'SFS_INVALID_ARGS', message: 'At least one permission (--read, --write, --delete) must be specified' } }))
      process.exit(1)
    }

    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const id = await runtime.identityService.grant(identity, {
        bucket,
        prefixes: opts.prefix as string[],
        permissions,
      })
      const safeId = { ...id, hashedSecret: '[REDACTED]' }
      console.log(JSON.stringify({ ok: true, data: safeId }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// sfs revoke
// ──────────────────────────────────────────────────────────
program
  .command('revoke <identity> <bucket>')
  .description('Revoke all permissions from an identity on a bucket')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (identity: string, bucket: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const id = await runtime.identityService.revoke(identity, bucket)
      const safeId = { ...id, hashedSecret: '[REDACTED]' }
      console.log(JSON.stringify({ ok: true, data: safeId }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// sfs upload
// ──────────────────────────────────────────────────────────
program
  .command('upload <bucket> <key> <localFile>')
  .description('Upload a local file to a bucket')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .option('--overwrite', 'Overwrite existing file', false)
  .action(async (bucketName: string, key: string, localFile: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      await stat(localFile)
      const bucket = await runtime.bucketService.get(bucketName)
      const stream = createReadStream(localFile)
      const result = await runtime.storageService.upload(bucket, key, stream, { overwrite: opts.overwrite })
      console.log(JSON.stringify({ ok: true, data: { bucket: bucketName, key, ...result } }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// sfs copy
// ──────────────────────────────────────────────────────────
program
  .command('copy <srcBucket> <srcKey> <dstBucket> <dstKey>')
  .description('Copy a file from one bucket/key to another')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .option('--overwrite', 'Overwrite existing file', false)
  .action(async (srcBucketName: string, srcKey: string, dstBucketName: string, dstKey: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      const srcBucket = await runtime.bucketService.get(srcBucketName)
      const dstBucket = await runtime.bucketService.get(dstBucketName)
      const { stream } = await runtime.storageService.download(srcBucket, srcKey)
      const result = await runtime.storageService.upload(dstBucket, dstKey, stream, { overwrite: opts.overwrite })
      console.log(JSON.stringify({ ok: true, data: { from: `${srcBucketName}/${srcKey}`, to: `${dstBucketName}/${dstKey}`, ...result } }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// sfs provision
// ──────────────────────────────────────────────────────────
program
  .command('provision <bucket>')
  .description('Auto-generate an identity + secret, grant permissions on a bucket, and print the credentials')
  .option('--identity <name>', 'Identity name (auto-generated if omitted)')
  .option('--prefix <prefix>', 'Restrict access to a path prefix (can be repeated)', collect, [] as string[])
  .option('--read', 'Grant read permission (default: all permissions)')
  .option('--write', 'Grant write permission (default: all permissions)')
  .option('--delete', 'Grant delete permission (default: all permissions)')
  .option('--data-dir <dir>', 'Data directory', getDataDir())
  .action(async (bucketName: string, opts) => {
    const runtime = await createRuntime({ dataDir: opts.dataDir })
    try {
      // Resolve identity name
      const identity: string = opts.identity ?? `sfs-${randomBytes(4).toString('hex')}`
      // Generate a secure random secret (32 hex chars)
      const secret: string = randomBytes(24).toString('base64url')

      // Determine permissions (default to all three if none specified)
      const permissions: Permission[] = []
      if (opts.read) permissions.push('read')
      if (opts.write) permissions.push('write')
      if (opts.delete) permissions.push('delete')
      if (permissions.length === 0) permissions.push('read', 'write', 'delete')

      // Ensure bucket exists
      await runtime.bucketService.get(bucketName)

      // Create identity and grant
      await runtime.identityService.create(identity, secret)
      await runtime.identityService.grant(identity, {
        bucket: bucketName,
        prefixes: opts.prefix as string[],
        permissions,
      })

      console.log(JSON.stringify({
        ok: true,
        data: {
          identity,
          secret,
          bucket: bucketName,
          permissions,
          prefixes: opts.prefix as string[],
          note: 'Save the secret now — it will not be shown again.',
        },
      }, null, 2))
    } catch (err: unknown) {
      printError(err)
    } finally {
      await runtime.operationalLogger.close()
    }
  })

// ──────────────────────────────────────────────────────────
// sfs completion
// ──────────────────────────────────────────────────────────
program
  .command('completion <shell>')
  .description('Print shell tab-completion script (bash | zsh | fish)')
  .addHelpText('after', `
Examples:
  # Bash — add to ~/.bashrc
  eval "$(sfs completion bash)"

  # Zsh — add to ~/.zshrc  (compinit must already be loaded)
  eval "$(sfs completion zsh)"

  # Fish — save to completions dir
  sfs completion fish > ~/.config/fish/completions/sfs.fish
`)
  .action((shell: string) => {
    switch (shell.toLowerCase()) {
      case 'bash':
        console.log(bashCompletion())
        break
      case 'zsh':
        console.log(zshCompletion())
        break
      case 'fish':
        console.log(fishCompletion())
        break
      default:
        console.error(JSON.stringify({ ok: false, error: { code: 'SFS_INVALID_ARGS', message: `Unsupported shell '${shell}'. Use: bash | zsh | fish` } }))
        process.exit(1)
    }
  })

// ──────────────────────────────────────────────────────────
// Helpers
// ──────────────────────────────────────────────────────────
async function printServerSummary(runtime: Awaited<ReturnType<typeof startServer>>): Promise<void> {
  try {
    const [buckets, identities] = await Promise.all([
      runtime.bucketService.list() as Promise<Bucket[]>,
      runtime.identityService.list() as Promise<Identity[]>,
    ])

    const bucketLines = buckets.length
      ? buckets.map(b => `│    · ${b.name}  →  ${b.path}`)
      : ['│    (none)']

    const identityLines = identities.length
      ? identities.map(id => `│    · ${id.identity}  (grants: ${id.grants.map(g => g.bucket).join(', ') || 'none'})`)
      : ['│    (none)']

    const lines = [
      '┌─────────────────────────────────────────────',
      '│  Buckets',
      '├─────────────────────────────────────────────',
      ...bucketLines,
      '├─────────────────────────────────────────────',
      '│  Identities',
      '├─────────────────────────────────────────────',
      ...identityLines,
      '└─────────────────────────────────────────────',
    ]
    console.log(lines.join('\n'))
  } catch {
    // Non-fatal — server is already running
  }
}
function printEnvInfo(): void {
  const cpus = os.cpus()
  const lines = [

    'Simple File Server — Environment',
      '─────────────────────────────────────────────',
    `  OS          : ${os.type()} ${os.release()} (${os.platform()}/${os.arch()})`,
    `  Node.js     : ${process.version}`,
    `  V8          : ${process.versions.v8}`,
    `  CPUs        : ${cpus.length}x ${cpus[0]?.model ?? 'unknown'}`,
    `  Memory      : ${(os.totalmem() / 1024 / 1024 / 1024).toFixed(1)} GB total`,
    `  Hostname    : ${os.hostname()}`,
    `  User        : ${os.userInfo().username}`,
    `  PID         : ${process.pid}`
  ]
  console.log(lines.join('\n'))
}
function collect(value: string, previous: string[]): string[] {
  return previous.concat([value])
}

function printError(err: unknown): void {
  if (err && typeof err === 'object' && 'toJSON' in err && typeof (err as { toJSON: unknown }).toJSON === 'function') {
    console.error(JSON.stringify((err as { toJSON: () => unknown }).toJSON(), null, 2))
  } else {
    console.error(JSON.stringify({ ok: false, error: { code: 'SFS_ERROR', message: String(err) } }, null, 2))
  }
  process.exit(1)
}

program.parse(process.argv)


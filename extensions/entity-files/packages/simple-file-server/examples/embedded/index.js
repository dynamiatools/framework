/**
 * simple-file-server — Embedded Mode Example
 *
 * Starts the server using the configuration already present in the
 * current working directory (.sfs/ folder).  No extra setup is done
 * here — use the CLI (`sfs create bucket …`, `sfs create identity …`)
 * to provision buckets and identities beforehand.
 *
 * Configuration via environment variables (all optional):
 *   SFS_HOST       – bind host  (default: 0.0.0.0)
 *   SFS_PORT       – bind port  (default: 8500)
 *   SFS_LOG_LEVEL  – log level  (default: info)
 *
 * Usage:
 *   npm install
 *   npm start
 */
import { startServer } from '@dynamia-tools/simple-file-server'

const PORT = parseInt(process.env.SFS_PORT ?? '8500', 10)
const HOST = process.env.SFS_HOST ?? '0.0.0.0'

startServer({ port: PORT, host: HOST }).catch((err) => {
  console.error('Fatal error during startup:', err)
  process.exit(1)
})

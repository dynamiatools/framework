import { execa } from 'execa'
import { warn, error, info } from './logger.js'

/**
 * Check that JDK 25 is available.
 * Returns true if found. Logs a warning (not an error) if a different version is present.
 */
export async function checkJava(): Promise<boolean> {
  try {
    const result = await execa('java', ['-version'], { stderr: 'pipe', stdout: 'pipe' })
    // java -version writes to stderr
    const output = result.stderr + result.stdout
    if (output.includes('25')) {
      return true
    }
    warn('JDK 25 not detected. A different Java version is installed. Some features may not work as expected.')
    return true // not a hard stop — just warn
  } catch {
    warn('Java is not installed. Install JDK 25 via SDKMAN: sdk install java 25-tem')
    return false
  }
}

/**
 * Check that Node.js >= 22 is available.
 * Returns true if the requirement is satisfied.
 */
export async function checkNode(): Promise<boolean> {
  try {
    const result = await execa('node', ['--version'], { stdout: 'pipe' })
    const version = result.stdout.trim().replace(/^v/, '')
    const majorStr = version.split('.')[0]
    const major = majorStr && majorStr.length > 0 ? parseInt(majorStr, 10) : 0
    if (!isNaN(major) && major >= 22) {
      return true
    }
    warn(`Node.js ${version} found but version >=22 is required. Please upgrade Node.js.`)
    return false
  } catch {
    warn('Node.js is not installed. Install it via fnm: curl -fsSL https://fnm.vercel.app/install | bash')
    return false
  }
}

/**
 * Check that git is available.
 * Returns true if found. Exits the process if git is missing — it is REQUIRED.
 */
export async function checkGit(): Promise<boolean> {
  try {
    await execa('git', ['--version'], { stdout: 'pipe' })
    return true
  } catch {
    error(
      'git is not installed. git is required for template cloning.\n' +
        '  Install it with your package manager:\n' +
        '    Ubuntu/Debian: sudo apt-get install git\n' +
        '    macOS:         brew install git\n' +
        '    Fedora:        sudo dnf install git\n' +
        '    Arch:          sudo pacman -S git',
    )
    // error() calls process.exit(1) — this line is never reached
    return false
  }
}

/**
 * Run all environment checks at startup.
 * git missing = hard stop; other warnings are non-fatal.
 */
export async function runChecks(): Promise<void> {
  info('Checking environment...')
  await checkGit()   // exits if missing
  await checkJava()  // warning only
  await checkNode()  // warning only
}

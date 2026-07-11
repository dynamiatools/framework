import { execa } from 'execa'

export interface TemplateRef {
  repo: string
  branch: string
}

export interface ValidationResult {
  ok: boolean
  reason?: string
}

/**
 * Validate that a git repository is reachable and that a branch exists before cloning.
 */
export async function validateTemplateRepo(template: TemplateRef): Promise<ValidationResult> {
  try {
    // Fast check for repo access.
    await execa('git', ['ls-remote', '--heads', template.repo], { stdout: 'pipe', stderr: 'pipe' })
  } catch (err) {
    const message = err instanceof Error ? err.message : 'Repository is not reachable.'
    return { ok: false, reason: message }
  }

  try {
    const result = await execa(
      'git',
      ['ls-remote', '--heads', template.repo, template.branch],
      { stdout: 'pipe', stderr: 'pipe' },
    )

    if (!result.stdout.trim()) {
      return { ok: false, reason: `Branch "${template.branch}" was not found.` }
    }

    return { ok: true }
  } catch (err) {
    const message = err instanceof Error ? err.message : 'Branch validation failed.'
    return { ok: false, reason: message }
  }
}


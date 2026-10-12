/** Deterministic hash, so a title always gets the same colors. */
function hash(text: string): number {
  let h = 2166136261
  for (let i = 0; i < text.length; i++) {
    h ^= text.charCodeAt(i)
    h = Math.imul(h, 16777619)
  }
  return h >>> 0
}

/** CSS gradient used as poster when a movie has no image. */
export function posterGradient(title: string): string {
  const h = hash(title)
  const a = h % 360
  const b = (a + 40 + (h % 80)) % 360
  return `linear-gradient(155deg, hsl(${a} 55% 28%) 0%, hsl(${b} 60% 14%) 100%)`
}

export function formatVotes(votes: number): string {
  if (votes >= 1_000_000) return `${(votes / 1_000_000).toFixed(1)}M`
  if (votes >= 1_000) return `${Math.round(votes / 1_000)}K`
  return String(votes)
}

export function formatMoney(millions?: number | null): string {
  if (millions === undefined || millions === null || millions <= 0) return '—'
  if (millions >= 1000) return `$${(millions / 1000).toFixed(2)}B`
  return `$${millions.toLocaleString('en-US', { maximumFractionDigits: 1 })}M`
}

export function formatRuntime(minutes?: number | null): string {
  if (!minutes) return ''
  return `${Math.floor(minutes / 60)}h ${String(minutes % 60).padStart(2, '0')}m`
}

export function certificationLabel(code?: string): string {
  return code ? code.replace('_', '-') : ''
}

export function highlight(text: string, terms: string[]): string {
  const escape = (s: string) => s.replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c]!)
  let html = escape(text)
  for (const term of terms) {
    if (term.length < 2) continue
    const pattern = new RegExp(`(${term.replace(/[.*+?^$(){}|[\]\\]/g, '\\$&')})`, 'gi')
    html = html.replace(pattern, '<mark>$1</mark>')
  }
  return html
}

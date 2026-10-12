import { DynamiaClient } from '@dynamia-tools/sdk'

/**
 * The whole public site talks to the AUTOMATIC REST API of DynamiaTools: every CrudPage of the backend's
 * `public` module is exposed read-only at `/api/public/{page}` with filtering by field (`?year=1999`,
 * `?genresText=Drama`), sorting (`_sort`, `_order`) and paging (`page`, `size`). No custom endpoint is involved.
 */
export const client = new DynamiaClient({ baseUrl: import.meta.env.VITE_API_BASE_URL ?? '' })

export interface Movie {
  id: number
  title: string
  year: number
  runtime?: number
  certification?: string
  language?: string
  country?: string
  synopsis?: string
  posterUrl?: string
  trailerUrl?: string
  rating: number
  votes: number
  budget?: number
  boxOffice?: number
  featured: boolean
  genresText?: string
  directorName?: string
  topCast?: string
}

export interface Genre {
  id: number
  name: string
  description?: string
}

export interface Person {
  id: number
  name: string
  birthYear?: number
  nationality?: string
  biography?: string
  photoUrl?: string
}

export interface Credit {
  id: number
  role: 'DIRECTOR' | 'ACTOR' | 'WRITER' | 'COMPOSER'
  billing: number
  characterName?: string
  movie: { id: number; title?: string; year?: number; rating?: number; directorName?: string; genresText?: string }
  person: { id: number; name: string }
}

export interface Review {
  id: number
  author: string
  score: number
  comment?: string
  reviewDate?: string
}

const movies = client.crud<Movie>('public/movies')
const genres = client.crud<Genre>('public/genres')
const people = client.crud<Person>('public/people')
const credits = client.crud<Credit>('public/credits')
const reviews = client.crud<Review>('public/reviews')

/** The single-entity endpoint answers `{ response, data }`. */
function unwrap<T>(body: unknown): T {
  const envelope = body as { data?: T }
  return (envelope && typeof envelope === 'object' && 'data' in envelope ? envelope.data : body) as T
}

export type SortKey = 'rating' | 'votes' | 'year' | 'year_asc' | 'title'

const SORTS: Record<SortKey, { _sort: string; _order: 'asc' | 'desc' }> = {
  rating: { _sort: 'rating', _order: 'desc' },
  votes: { _sort: 'votes', _order: 'desc' },
  year: { _sort: 'year', _order: 'desc' },
  year_asc: { _sort: 'year', _order: 'asc' },
  title: { _sort: 'title', _order: 'asc' },
}

export interface MovieQuery {
  text?: string
  genre?: string
  year?: number
  decade?: number
  certification?: string
  featured?: boolean
  sort?: SortKey
  page?: number
  size?: number
}

export interface MovieResults {
  items: Movie[]
  total: number
  /** Milliseconds the whole search took, as the browser saw it. */
  tookMs: number
}

/** Words of a free text query, lower case. */
export function termsOf(text?: string): string[] {
  return (text ?? '').toLowerCase().split(/\s+/).filter(Boolean)
}

/**
 * Movie search. A movie has one denormalized `searchText` column (title, director, cast, genres, year, studio),
 * so the free text goes to the API as `?searchText=`. The automatic API applies one value per field, so with
 * several words the longest one is sent to the server and the others narrow the result in the browser.
 */
export async function searchMovies(q: MovieQuery): Promise<MovieResults> {
  const started = performance.now()
  const terms = termsOf(q.text)
  const main = [...terms].sort((a, b) => b.length - a.length)[0]
  const extra = terms.filter((t) => t !== main)
  const refineLocally = extra.length > 0 || q.decade !== undefined
  const size = q.size ?? 20
  const page = q.page ?? 1

  const params: Record<string, string | number | boolean | undefined> = {
    searchText: main,
    genresText: q.genre,
    year: q.year,
    certification: q.certification,
    featured: q.featured,
    ...SORTS[q.sort ?? 'rating'],
    ...(refineLocally ? { page: 1, size: 1000 } : { page, size }),
  }

  const result = await movies.findAll(params)
  let items = result.content
  let total = result.total

  if (refineLocally) {
    items = items.filter((m) => {
      if (q.decade !== undefined && (m.year < q.decade || m.year >= q.decade + 10)) return false
      const haystack = `${m.title} ${m.directorName ?? ''} ${m.topCast ?? ''} ${m.genresText ?? ''} ${m.year}`.toLowerCase()
      return extra.every((t) => haystack.includes(t))
    })
    total = items.length
    items = items.slice((page - 1) * size, page * size)
  }
  return { items, total, tookMs: performance.now() - started }
}

export async function suggestMovies(text: string): Promise<Movie[]> {
  const terms = termsOf(text)
  if (!terms.length) return []
  const { items } = await searchMovies({ text, sort: 'rating', size: 6 })
  return items
}

export async function getMovie(id: number): Promise<Movie> {
  return unwrap<Movie>(await movies.findById(id))
}

export async function getPerson(id: number): Promise<Person> {
  return unwrap<Person>(await people.findById(id))
}

export async function listGenres(): Promise<Genre[]> {
  return (await genres.findAll({ size: 100, _sort: 'name', _order: 'asc' })).content
}

export async function creditsOfMovie(movieId: number): Promise<Credit[]> {
  const { content } = await credits.findAll({ 'movie.id': movieId, size: 200, _sort: 'billing', _order: 'asc' })
  return content
}

export async function creditsOfPerson(personId: number): Promise<Credit[]> {
  const { content } = await credits.findAll({ 'person.id': personId, size: 200 })
  return content
}

export async function reviewsOfMovie(movieId: number): Promise<Review[]> {
  const { content } = await reviews.findAll({ 'movie.id': movieId, size: 50, _sort: 'reviewDate', _order: 'desc' })
  return content
}

export async function countMovies(): Promise<number> {
  return (await movies.findAll({ size: 1 })).total
}

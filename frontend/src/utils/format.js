export const dexNumber = (id) => `#${String(id).padStart(4, '0')}`

/** PokeAPI slugs ("mr-mime", "special-attack") to readable labels ("Mr Mime", "Special attack"). */
export function humanize(slug) {
  if (!slug) return ''
  const words = slug.split('-')
  return words.map((w, i) => (i === 0 ? w.charAt(0).toUpperCase() + w.slice(1) : w)).join(' ')
}

export const displayName = (slug) =>
  slug ? slug.split('-').map((w) => w.charAt(0).toUpperCase() + w.slice(1)).join(' ') : ''

export const STAT_LABELS = {
  hp: 'HP',
  attack: 'Attack',
  defense: 'Defense',
  'special-attack': 'Sp. Attack',
  'special-defense': 'Sp. Defense',
  speed: 'Speed',
}

export const formatKg = (kg) => `${Number(kg).toLocaleString(undefined, { maximumFractionDigits: 1 })} kg`
export const formatM = (m) => `${Number(m).toLocaleString(undefined, { maximumFractionDigits: 1 })} m`

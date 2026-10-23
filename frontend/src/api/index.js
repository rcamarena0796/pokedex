import { request } from './http'

export const authApi = {
  login: (username, password) =>
    request('/auth/login', { method: 'POST', body: { username, password }, auth: false }),
  register: (payload) => request('/auth/register', { method: 'POST', body: payload, auth: false }),
  me: () => request('/auth/me'),
}

export const usersApi = {
  list: () => request('/users'),
  changeRole: (id, role) => request(`/users/${id}/role`, { method: 'PUT', body: { role } }),
  remove: (id) => request(`/users/${id}`, { method: 'DELETE' }),
}

// User stories 01 and 02: public, read-only PokeAPI catalog.
export const catalogApi = {
  page: (page, size) => request('/catalog/pokemon', { query: { page, size }, auth: false }),
  detail: (idOrName) => request(`/catalog/pokemon/${encodeURIComponent(idOrName)}`, { auth: false }),
}

// User stories 03 and 04: local replica with proprietary fields.
export const pokedexApi = {
  list: (page, size, search) => request('/pokemon', { query: { page, size, search } }),
  get: (id) => request(`/pokemon/${id}`),
  update: (id, fields) => request(`/pokemon/${id}`, { method: 'PUT', body: fields }),
  remove: (id) => request(`/pokemon/${id}`, { method: 'DELETE' }),
  syncOne: (idOrName) => request(`/pokemon/sync/${encodeURIComponent(idOrName)}`, { method: 'POST' }),
  syncRange: (fromId, toId) => request('/pokemon/sync', { method: 'POST', query: { fromId, toId } }),
}

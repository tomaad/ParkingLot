import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api/v1/parking',
  headers: {
    'Content-Type': 'application/json',
  },
})

export const VEHICLE_TYPES = ['CAR', 'MOTORCYCLE', 'TRUCK', 'VAN', 'ELECTRIC']

const isRecord = (value) => value && typeof value === 'object' && !Array.isArray(value)

const firstNumber = (...values) => {
  for (const value of values) {
    const parsed = Number(value)
    if (Number.isFinite(parsed)) {
      return parsed
    }
  }

  return undefined
}

const firstText = (...values) => {
  for (const value of values) {
    if (typeof value === 'string' && value.trim()) {
      return value.trim()
    }
  }

  return ''
}

const toTitleCase = (value = '') =>
  String(value)
    .replace(/[_-]/g, ' ')
    .toLowerCase()
    .replace(/\b\w/g, (match) => match.toUpperCase())

const normalizeSlotType = (value, key) => {
  if (typeof value === 'number') {
    const typeName = firstText(key, 'Unknown')

    return {
      key: typeName.toUpperCase(),
      type: toTitleCase(typeName),
      available: value,
      occupied: 0,
      total: value,
    }
  }

  if (!isRecord(value)) {
    return null
  }

  const typeName = firstText(value.vehicleType, value.slotType, value.type, key, 'Unknown')
  const available = firstNumber(
    value.availableSlots,
    value.available,
    value.freeSlots,
    value.free,
    value.count,
    0,
  )

  let total = firstNumber(value.totalSlots, value.total, value.capacity)
  let occupied = firstNumber(value.occupiedSlots, value.occupied, value.usedSlots, value.used)

  if (total === undefined && occupied !== undefined) {
    total = available + occupied
  }

  if (occupied === undefined && total !== undefined) {
    occupied = Math.max(total - available, 0)
  }

  if (total === undefined) {
    total = available + (occupied ?? 0)
  }

  if (occupied === undefined) {
    occupied = Math.max(total - available, 0)
  }

  return {
    key: typeName.toUpperCase(),
    type: toTitleCase(typeName),
    available,
    occupied,
    total,
  }
}

const normalizeSlotTypes = (value) => {
  if (Array.isArray(value)) {
    return value
      .map((entry, index) => normalizeSlotType(entry, entry?.type ?? entry?.slotType ?? index))
      .filter(Boolean)
  }

  if (isRecord(value)) {
    return Object.entries(value)
      .map(([key, entry]) => normalizeSlotType(entry, key))
      .filter(Boolean)
  }

  return []
}

const normalizeLevel = (value, key, index) => {
  if (typeof value === 'number') {
    const name = `Level ${key ?? index + 1}`

    return {
      id: String(key ?? index + 1),
      name,
      available: value,
      occupied: 0,
      total: value,
      slotTypes: [],
    }
  }

  if (!isRecord(value)) {
    return null
  }

  const slotTypes = normalizeSlotTypes(
    value.slotTypeAvailability ??
      value.slotTypes ??
      value.availabilityByType ??
      value.slotTypeBreakdown ??
      value.slotsByType ??
      value.byType,
  )

  const slotTypeAvailable = slotTypes.reduce((sum, item) => sum + item.available, 0)
  const slotTypeTotal = slotTypes.reduce((sum, item) => sum + item.total, 0)
  const slotTypeOccupied = slotTypes.reduce((sum, item) => sum + item.occupied, 0)

  const rawName = firstText(
    value.levelName,
    value.levelLabel,
    String(value.level ?? ''),
    String(value.levelId ?? ''),
    String(key ?? ''),
    String(index + 1),
  )

  const available = firstNumber(
    value.availableSlots,
    value.available,
    value.freeSlots,
    slotTypeAvailable,
    0,
  )

  let total = firstNumber(value.totalSlots, value.total, value.capacity, slotTypeTotal)
  let occupied = firstNumber(value.occupiedSlots, value.occupied, value.usedSlots, slotTypeOccupied)

  if (total === undefined && occupied !== undefined) {
    total = available + occupied
  }

  if (occupied === undefined && total !== undefined) {
    occupied = Math.max(total - available, 0)
  }

  if (total === undefined) {
    total = available + (occupied ?? 0)
  }

  if (occupied === undefined) {
    occupied = Math.max(total - available, 0)
  }

  return {
    id: firstText(String(value.id ?? ''), String(value.levelId ?? ''), String(key ?? ''), `level-${index + 1}`),
    name: rawName.toLowerCase().includes('level') ? rawName : `Level ${rawName}`,
    available,
    occupied,
    total,
    slotTypes,
  }
}

export const normalizeAvailability = (payload) => {
  const source = payload?.availability ?? payload ?? {}
  const rawLevels =
    source.levels ??
    source.levelAvailability ??
    source.availabilityByLevel ??
    source.levelsAvailability ??
    (Array.isArray(source) ? source : undefined)

  const levels = Array.isArray(rawLevels)
    ? rawLevels.map((entry, index) => normalizeLevel(entry, entry?.level ?? entry?.levelId, index)).filter(Boolean)
    : isRecord(rawLevels)
      ? Object.entries(rawLevels).map(([key, entry], index) => normalizeLevel(entry, key, index)).filter(Boolean)
      : []

  const summedTotal = levels.reduce((sum, level) => sum + level.total, 0)
  const summedAvailable = levels.reduce((sum, level) => sum + level.available, 0)
  const summedOccupied = levels.reduce((sum, level) => sum + level.occupied, 0)

  const total = firstNumber(source.totalSlots, source.totalCapacity, source.capacity, summedTotal, 0)
  const available = firstNumber(source.availableSlots, source.available, source.freeSlots, summedAvailable, 0)
  const occupied = firstNumber(source.occupiedSlots, source.occupied, source.usedSlots, total - available, summedOccupied, 0)

  const slotTypeTotals = levels.reduce((accumulator, level) => {
    level.slotTypes.forEach((slotType) => {
      const existing = accumulator[slotType.key] ?? {
        type: slotType.type,
        available: 0,
        occupied: 0,
        total: 0,
      }

      existing.available += slotType.available
      existing.occupied += slotType.occupied
      existing.total += slotType.total
      accumulator[slotType.key] = existing
    })

    return accumulator
  }, {})

  const occupancyPercent = total > 0 ? Math.min((occupied / total) * 100, 100) : 0

  return {
    levels,
    total,
    available,
    occupied,
    occupancyPercent,
    slotTypeTotals: Object.values(slotTypeTotals),
  }
}

export const getApiErrorMessage = (error, fallback = 'Something went wrong. Please try again.') => {
  if (axios.isAxiosError(error)) {
    return (
      error.response?.data?.message ??
      error.response?.data?.error ??
      error.response?.data?.details ??
      error.message ??
      fallback
    )
  }

  return fallback
}

export const parkVehicle = async (licensePlate, vehicleType) => {
  const response = await apiClient.post('/park', {
    licensePlate: licensePlate.trim().toUpperCase(),
    vehicleType,
  })

  return response.data
}

export const unparkVehicle = async (ticketId) => {
  const response = await apiClient.post('/unpark', {
    ticketId: ticketId.trim(),
  })

  return response.data
}

export const getAvailability = async () => {
  const response = await apiClient.get('/availability')
  return response.data
}

export const lookupVehicle = async (licensePlate) => {
  const response = await apiClient.get(`/vehicles/${encodeURIComponent(licensePlate.trim().toUpperCase())}`)
  return response.data
}

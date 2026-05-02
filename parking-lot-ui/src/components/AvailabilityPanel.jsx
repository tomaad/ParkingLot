import { useEffect, useState } from 'react'
import {
  getApiErrorMessage,
  getAvailability,
  normalizeAvailability,
} from '../services/api'

const formatPercent = (value) => `${Math.round(value)}%`

const getTone = (percentage) => {
  if (percentage >= 85) {
    return 'danger'
  }

  if (percentage >= 60) {
    return 'warning'
  }

  return 'success'
}

function AvailabilityPanel() {
  const [availability, setAvailability] = useState(normalizeAvailability({}))
  const [loading, setLoading] = useState(true)
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState('')
  const [lastUpdated, setLastUpdated] = useState('')

  const loadAvailability = async ({ silent = false } = {}) => {
    try {
      if (silent) {
        setRefreshing(true)
      } else {
        setLoading(true)
      }

      setError('')
      const data = await getAvailability()
      setAvailability(normalizeAvailability(data))
      setLastUpdated(new Date().toLocaleTimeString())
    } catch (loadError) {
      setError(getApiErrorMessage(loadError, 'Unable to refresh availability data.'))
    } finally {
      setLoading(false)
      setRefreshing(false)
    }
  }

  useEffect(() => {
    loadAvailability()
    const intervalId = window.setInterval(() => {
      loadAvailability({ silent: true })
    }, 10000)

    return () => window.clearInterval(intervalId)
  }, [])

  if (loading) {
    return <div className="card loading-state">Loading detailed availability…</div>
  }

  return (
    <div className="page-shell">
      {error ? <div className="alert alert--error">{error}</div> : null}

      <div className="flex-between">
        <div>
          <h3 className="page-title">Detailed availability</h3>
          <p className="page-description">
            Auto-refreshes every 10 seconds to keep level and slot data current.
          </p>
        </div>
        <div className="button-row">
          <span className="badge">Last updated {lastUpdated || 'just now'}</span>
          <button className="button-ghost" disabled={refreshing} onClick={() => loadAvailability()} type="button">
            {refreshing ? 'Refreshing…' : 'Refresh now'}
          </button>
        </div>
      </div>

      <section className="stats-grid">
        <article className="metric-card">
          <h3>Total capacity</h3>
          <div className="metric-card__value">{availability.total}</div>
          <p className="helper-text">All parking slots across every level.</p>
        </article>
        <article className="metric-card">
          <h3>Available slots</h3>
          <div className="metric-card__value tone-success">{availability.available}</div>
          <p className="helper-text">Slots ready for new parking tickets.</p>
        </article>
        <article className="metric-card">
          <h3>Occupied slots</h3>
          <div className={`metric-card__value tone-${getTone(availability.occupancyPercent)}`}>
            {availability.occupied}
          </div>
          <p className="helper-text">Current number of active parked vehicles.</p>
        </article>
      </section>

      <section className="card capacity-card">
        <div className="capacity-card__top">
          <div>
            <h3>Overall occupancy</h3>
            <p className="helper-text">Color-coded occupancy indicator for the entire lot.</p>
          </div>
          <span className={`status-pill status-pill--${getTone(availability.occupancyPercent)}`}>
            {formatPercent(availability.occupancyPercent)} occupied
          </span>
        </div>
        <div className="capacity-meter">
          <div
            className={`capacity-meter__fill tone-${getTone(availability.occupancyPercent)}-bg`}
            style={{ width: `${availability.occupancyPercent}%` }}
          />
        </div>
      </section>

      <section className="availability-grid">
        {availability.levels.length > 0 ? (
          availability.levels.map((level) => {
            const occupancy = level.total > 0 ? (level.occupied / level.total) * 100 : 0
            const tone = getTone(occupancy)

            return (
              <article className="level-card" key={level.id}>
                <div className="level-card__top">
                  <div>
                    <h3>{level.name}</h3>
                    <p className="helper-text">
                      {level.available} available · {level.occupied} occupied · {level.total} total
                    </p>
                  </div>
                  <span className={`status-pill status-pill--${tone}`}>
                    {formatPercent(occupancy)} occupied
                  </span>
                </div>

                <div className="capacity-meter">
                  <div
                    className={`capacity-meter__fill tone-${tone}-bg`}
                    style={{ width: `${occupancy}%` }}
                  />
                </div>

                <div className="slot-type-list">
                  {level.slotTypes.length > 0 ? (
                    level.slotTypes.map((slotType) => (
                      <span className="slot-type-pill" key={`${level.id}-${slotType.key}`}>
                        {slotType.type}: {slotType.available}/{slotType.total}
                      </span>
                    ))
                  ) : (
                    <span className="helper-text">No slot-type breakdown returned for this level.</span>
                  )}
                </div>
              </article>
            )
          })
        ) : (
          <div className="card empty-state">No per-level availability data is available.</div>
        )}
      </section>

      <section className="table-card">
        <div className="flex-between">
          <div>
            <h3>Per-slot-type counts</h3>
            <p className="table-note">Aggregated across all levels currently returned by the API.</p>
          </div>
        </div>

        {availability.slotTypeTotals.length === 0 ? (
          <div className="empty-state">The backend did not return slot-type counts.</div>
        ) : (
          <div className="table-wrapper">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Slot type</th>
                  <th>Available</th>
                  <th>Occupied</th>
                  <th>Total</th>
                </tr>
              </thead>
              <tbody>
                {availability.slotTypeTotals.map((slotType) => (
                  <tr key={slotType.type}>
                    <td>{slotType.type}</td>
                    <td>{slotType.available}</td>
                    <td>{slotType.occupied}</td>
                    <td>{slotType.total}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}

export default AvailabilityPanel

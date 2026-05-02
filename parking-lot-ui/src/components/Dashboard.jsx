import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
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

function Dashboard() {
  const [availability, setAvailability] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [lastUpdated, setLastUpdated] = useState('')

  const loadAvailability = async () => {
    try {
      setLoading(true)
      setError('')
      const data = await getAvailability()
      setAvailability(normalizeAvailability(data))
      setLastUpdated(new Date().toLocaleTimeString())
    } catch (loadError) {
      setError(getApiErrorMessage(loadError, 'Unable to load parking availability.'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadAvailability()
  }, [])

  if (loading) {
    return <div className="card loading-state">Loading dashboard data…</div>
  }

  const summary = availability ?? normalizeAvailability({})
  const tone = getTone(summary.occupancyPercent)

  return (
    <div className="page-shell">
      {error ? <div className="alert alert--error">{error}</div> : null}

      <div className="dashboard-grid">
        <section className="card capacity-card">
          <div className="capacity-card__top">
            <div>
              <h3>Total capacity overview</h3>
              <p className="helper-text">Track overall occupancy across all parking levels.</p>
            </div>
            <span className={`status-pill status-pill--${tone}`}>
              {formatPercent(summary.occupancyPercent)} occupied
            </span>
          </div>

          <div className="capacity-card__values">
            <div>
              <div className="metric-card__value">{summary.occupied}</div>
              <div className="metric-card__label">Occupied slots</div>
            </div>
            <div>
              <div className="metric-card__value">{summary.total}</div>
              <div className="metric-card__label">Total capacity</div>
            </div>
            <div>
              <div className="metric-card__value">{summary.available}</div>
              <div className="metric-card__label">Available now</div>
            </div>
          </div>

          <div className="capacity-meter" aria-label="Overall occupancy">
            <div
              className={`capacity-meter__fill tone-${tone}-bg`}
              style={{ width: `${summary.occupancyPercent}%` }}
            />
          </div>

          <div className="helper-text">
            Last refreshed {lastUpdated || 'just now'}
          </div>
        </section>

        <aside className="card">
          <div className="flex-between">
            <div>
              <h3>Quick actions</h3>
              <p className="helper-text">Start common parking workflows instantly.</p>
            </div>
          </div>

          <div className="quick-actions">
            <Link className="button" to="/park">
              Park a vehicle
            </Link>
            <Link className="button-secondary" to="/unpark">
              Unpark a vehicle
            </Link>
            <Link className="button-ghost" to="/lookup">
              Find a vehicle
            </Link>
            <Link className="button-ghost" to="/availability">
              View availability
            </Link>
          </div>
        </aside>
      </div>

      <section className="stats-grid">
        <article className="metric-card">
          <h3>Occupancy</h3>
          <div className={`metric-card__value tone-${tone}`}>{formatPercent(summary.occupancyPercent)}</div>
          <p className="helper-text">Live percentage of occupied slots.</p>
        </article>
        <article className="metric-card">
          <h3>Levels monitored</h3>
          <div className="metric-card__value">{summary.levels.length}</div>
          <p className="helper-text">Per-level summaries sourced from the backend API.</p>
        </article>
        <article className="metric-card">
          <h3>Available capacity</h3>
          <div className="metric-card__value tone-success">{summary.available}</div>
          <p className="helper-text">Slots open for immediate parking assignments.</p>
        </article>
      </section>

      <section className="page-shell">
        <div className="flex-between">
          <div>
            <h3 className="page-title">Per-level availability</h3>
            <p className="page-description">A quick snapshot of each parking level and its open capacity.</p>
          </div>
          <button className="button-ghost" onClick={loadAvailability} type="button">
            Refresh dashboard
          </button>
        </div>

        {summary.levels.length === 0 ? (
          <div className="card empty-state">No availability data is currently available.</div>
        ) : (
          <div className="level-grid">
            {summary.levels.map((level) => {
              const levelTone = getTone(level.total > 0 ? (level.occupied / level.total) * 100 : 0)

              return (
                <article className="level-card" key={level.id}>
                  <div className="level-card__top">
                    <div>
                      <h3>{level.name}</h3>
                      <p className="helper-text">{level.available} available of {level.total} slots</p>
                    </div>
                    <span className={`status-pill status-pill--${levelTone}`}>
                      {level.total > 0 ? formatPercent((level.occupied / level.total) * 100) : '0%'} occupied
                    </span>
                  </div>

                  <div className="capacity-meter" aria-hidden="true">
                    <div
                      className={`capacity-meter__fill tone-${levelTone}-bg`}
                      style={{ width: `${level.total > 0 ? (level.occupied / level.total) * 100 : 0}%` }}
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
                      <span className="helper-text">No slot-type breakdown provided by the API.</span>
                    )}
                  </div>
                </article>
              )
            })}
          </div>
        )}
      </section>
    </div>
  )
}

export default Dashboard

import { useState } from 'react'
import { getApiErrorMessage, lookupVehicle } from '../services/api'

const getLocationText = (vehicle) => {
  const level = vehicle.level ?? vehicle.levelName ?? vehicle.location?.level
  const slot = vehicle.slotNumber ?? vehicle.slotId ?? vehicle.slot?.number ?? vehicle.slot?.id
  const section = vehicle.section ?? vehicle.zone ?? vehicle.slot?.section

  return [level ? `Level ${level}` : '', section, slot ? `Slot ${slot}` : '']
    .filter(Boolean)
    .join(' · ')
}

const formatTimestamp = (value) => {
  if (!value) {
    return 'Not available'
  }

  const parsed = new Date(value)
  return Number.isNaN(parsed.getTime()) ? String(value) : parsed.toLocaleString()
}

function VehicleLookup() {
  const [licensePlate, setLicensePlate] = useState('')
  const [vehicle, setVehicle] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  const licensePlateError = submitted && !licensePlate.trim()

  const handleSubmit = async (event) => {
    event.preventDefault()
    setSubmitted(true)

    if (!licensePlate.trim()) {
      return
    }

    try {
      setLoading(true)
      setError('')
      const response = await lookupVehicle(licensePlate)
      setVehicle(response)
    } catch (submitError) {
      setVehicle(null)
      setError(getApiErrorMessage(submitError, 'Vehicle lookup failed.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-shell">
      <section className="form-card">
        <div>
          <h3>Locate a vehicle</h3>
          <p className="helper-text">
            Search by license plate to view the parking location and ticket details.
          </p>
        </div>

        <form className="page-shell" onSubmit={handleSubmit}>
          <div className="field-group">
            <label htmlFor="lookupLicensePlate">License plate</label>
            <input
              id="lookupLicensePlate"
              className={`form-input${licensePlateError ? ' is-invalid' : ''}`}
              value={licensePlate}
              onChange={(event) => setLicensePlate(event.target.value.toUpperCase())}
              placeholder="ABC-123"
              autoComplete="off"
            />
            {licensePlateError ? <span className="validation-error">License plate is required.</span> : null}
          </div>

          {error ? <div className="alert alert--error">{error}</div> : null}

          <div className="button-row">
            <button className="button" disabled={loading} type="submit">
              {loading ? 'Searching…' : 'Lookup vehicle'}
            </button>
          </div>
        </form>
      </section>

      {vehicle ? (
        <section className="result-card">
          <div className="flex-between">
            <div>
              <h3>Vehicle details</h3>
              <p className="helper-text">Current location and session information for the searched vehicle.</p>
            </div>
            <div className="badge">Match found</div>
          </div>

          <dl className="definition-list">
            <div>
              <dt>License plate</dt>
              <dd>{vehicle.licensePlate ?? licensePlate}</dd>
            </div>
            <div>
              <dt>Vehicle type</dt>
              <dd>{vehicle.vehicleType ?? 'Not provided'}</dd>
            </div>
            <div>
              <dt>Location</dt>
              <dd>{getLocationText(vehicle) || 'Location unavailable'}</dd>
            </div>
            <div>
              <dt>Slot type</dt>
              <dd>{vehicle.slotType ?? vehicle.slot?.type ?? 'Not provided'}</dd>
            </div>
            <div>
              <dt>Ticket ID</dt>
              <dd>{vehicle.ticketId ?? vehicle.ticket?.ticketId ?? 'Not provided'}</dd>
            </div>
            <div>
              <dt>Parked at</dt>
              <dd>{formatTimestamp(vehicle.timestamp ?? vehicle.parkedAt ?? vehicle.entryTime)}</dd>
            </div>
          </dl>
        </section>
      ) : null}
    </div>
  )
}

export default VehicleLookup

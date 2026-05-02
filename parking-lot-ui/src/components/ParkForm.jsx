import { useState } from 'react'
import {
  VEHICLE_TYPES,
  getApiErrorMessage,
  parkVehicle,
} from '../services/api'

const getLocationText = (ticket) => {
  const level = ticket.level ?? ticket.levelName ?? ticket.location?.level
  const slot = ticket.slotNumber ?? ticket.slotId ?? ticket.slot?.number ?? ticket.slot?.id
  const section = ticket.section ?? ticket.zone ?? ticket.slot?.section

  return [level ? `Level ${level}` : '', section, slot ? `Slot ${slot}` : '']
    .filter(Boolean)
    .join(' · ')
}

const getTimestampText = (ticket) => {
  const timestamp =
    ticket.timestamp ??
    ticket.entryTime ??
    ticket.issuedAt ??
    ticket.parkedAt ??
    ticket.createdAt

  if (!timestamp) {
    return 'Captured just now'
  }

  const parsed = new Date(timestamp)
  return Number.isNaN(parsed.getTime()) ? String(timestamp) : parsed.toLocaleString()
}

function ParkForm() {
  const [licensePlate, setLicensePlate] = useState('')
  const [vehicleType, setVehicleType] = useState('CAR')
  const [ticket, setTicket] = useState(null)
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
      const response = await parkVehicle(licensePlate, vehicleType)
      setTicket(response)
    } catch (submitError) {
      setTicket(null)
      setError(getApiErrorMessage(submitError, 'Unable to park this vehicle.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-shell">
      <section className="form-card">
        <div>
          <h3>Park a vehicle</h3>
          <p className="helper-text">
            Create a parking ticket by providing a license plate and selecting the vehicle type.
          </p>
        </div>

        <form className="page-shell" onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="field-group">
              <label htmlFor="licensePlate">License plate</label>
              <input
                id="licensePlate"
                className={`form-input${licensePlateError ? ' is-invalid' : ''}`}
                value={licensePlate}
                onChange={(event) => setLicensePlate(event.target.value.toUpperCase())}
                placeholder="ABC-123"
                autoComplete="off"
              />
              {licensePlateError ? <span className="validation-error">License plate is required.</span> : null}
            </div>

            <div className="field-group">
              <label htmlFor="vehicleType">Vehicle type</label>
              <select
                id="vehicleType"
                className="form-select"
                value={vehicleType}
                onChange={(event) => setVehicleType(event.target.value)}
              >
                {VEHICLE_TYPES.map((type) => (
                  <option key={type} value={type}>
                    {type}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {error ? <div className="alert alert--error">{error}</div> : null}

          <div className="button-row">
            <button className="button" disabled={loading} type="submit">
              {loading ? 'Parking vehicle…' : 'Park vehicle'}
            </button>
            <button
              className="button-secondary"
              disabled={loading}
              type="button"
              onClick={() => {
                setLicensePlate('')
                setVehicleType('CAR')
                setTicket(null)
                setError('')
                setSubmitted(false)
              }}
            >
              Reset form
            </button>
          </div>
        </form>
      </section>

      {ticket ? (
        <section className="result-card">
          <div className="flex-between">
            <div>
              <h3>Ticket issued successfully</h3>
              <p className="helper-text">Use the ticket ID when the vehicle exits the parking lot.</p>
            </div>
            <div className="badge">Ready to unpark later</div>
          </div>

          <dl className="definition-list">
            <div>
              <dt>Ticket ID</dt>
              <dd>{ticket.ticketId ?? ticket.id ?? 'Unavailable'}</dd>
            </div>
            <div>
              <dt>Vehicle type</dt>
              <dd>{ticket.vehicleType ?? vehicleType}</dd>
            </div>
            <div>
              <dt>Assigned slot</dt>
              <dd>{getLocationText(ticket) || 'Slot information unavailable'}</dd>
            </div>
            <div>
              <dt>Timestamp</dt>
              <dd>{getTimestampText(ticket)}</dd>
            </div>
          </dl>
        </section>
      ) : null}
    </div>
  )
}

export default ParkForm

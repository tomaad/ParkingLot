import { useState } from 'react'
import { getApiErrorMessage, unparkVehicle } from '../services/api'

const getDurationText = (result) =>
  result.duration ?? result.parkingDuration ?? result.durationInMinutes ?? 'Not provided'

const getFeeText = (result) => {
  const fee = result.fee ?? result.amount ?? result.totalFee
  if (fee === undefined || fee === null || fee === '') {
    return 'Not provided'
  }

  const numericValue = Number(fee)
  return Number.isFinite(numericValue) ? `$${numericValue.toFixed(2)}` : String(fee)
}

function UnparkForm() {
  const [ticketId, setTicketId] = useState('')
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  const ticketError = submitted && !ticketId.trim()

  const handleSubmit = async (event) => {
    event.preventDefault()
    setSubmitted(true)

    if (!ticketId.trim()) {
      return
    }

    try {
      setLoading(true)
      setError('')
      const response = await unparkVehicle(ticketId)
      setResult(response)
    } catch (submitError) {
      setResult(null)
      setError(getApiErrorMessage(submitError, 'Unable to unpark this vehicle.'))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-shell">
      <section className="form-card">
        <div>
          <h3>Unpark a vehicle</h3>
          <p className="helper-text">
            Enter the parking ticket ID to calculate duration and finalize parking fees.
          </p>
        </div>

        <form className="page-shell" onSubmit={handleSubmit}>
          <div className="field-group">
            <label htmlFor="ticketId">Ticket ID</label>
            <input
              id="ticketId"
              className={`form-input${ticketError ? ' is-invalid' : ''}`}
              value={ticketId}
              onChange={(event) => setTicketId(event.target.value)}
              placeholder="ticket-uuid"
              autoComplete="off"
            />
            {ticketError ? <span className="validation-error">Ticket ID is required.</span> : null}
          </div>

          {error ? <div className="alert alert--error">{error}</div> : null}

          <div className="button-row">
            <button className="button" disabled={loading} type="submit">
              {loading ? 'Processing exit…' : 'Unpark vehicle'}
            </button>
          </div>
        </form>
      </section>

      {result ? (
        <section className="result-card">
          <div className="flex-between">
            <div>
              <h3>Vehicle unparked successfully</h3>
              <p className="helper-text">Parking duration and fees returned by the backend.</p>
            </div>
            <div className="badge">Session closed</div>
          </div>

          <dl className="definition-list">
            <div>
              <dt>Ticket ID</dt>
              <dd>{result.ticketId ?? ticketId}</dd>
            </div>
            <div>
              <dt>Duration</dt>
              <dd>{getDurationText(result)}</dd>
            </div>
            <div>
              <dt>Fee</dt>
              <dd>{getFeeText(result)}</dd>
            </div>
            <div>
              <dt>Status</dt>
              <dd>{result.status ?? 'Completed'}</dd>
            </div>
          </dl>
        </section>
      ) : null}
    </div>
  )
}

export default UnparkForm

import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import AvailabilityPanel from './components/AvailabilityPanel'
import Dashboard from './components/Dashboard'
import Layout from './components/Layout'
import ParkForm from './components/ParkForm'
import UnparkForm from './components/UnparkForm'
import VehicleLookup from './components/VehicleLookup'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route index element={<Dashboard />} />
          <Route path="park" element={<ParkForm />} />
          <Route path="unpark" element={<UnparkForm />} />
          <Route path="lookup" element={<VehicleLookup />} />
          <Route path="availability" element={<AvailabilityPanel />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default App

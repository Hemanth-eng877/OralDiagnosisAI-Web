import React from 'react'
import PatientList from './components/PatientList'

export default function App() {
  return (
    <div className="container py-4">
      <header className="d-flex justify-content-between align-items-center mb-4">
        <h1>OralDiagnosisAI - Patients</h1>
      </header>
      <PatientList />
    </div>
  )
}

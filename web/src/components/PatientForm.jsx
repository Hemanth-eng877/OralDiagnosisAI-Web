import React, { useState, useEffect } from 'react'
import { addPatient, updatePatient } from '../services/firestoreService'

const initialState = {
  patientId: '',
  name: '',
  age: '',
  gender: '',
  phone: '',
  email: '',
  address: '',
  imageUrl: '',
  diagnosis: '',
  confidence: '',
  doctorName: '',
  appointmentDate: '',
}

export default function PatientForm({ existing, onDone, setNotification }) {
  const [form, setForm] = useState(initialState)
  const [errors, setErrors] = useState({})
  const isEdit = !!existing

  useEffect(() => {
    if (existing) setForm({ ...initialState, ...existing })
    else setForm(initialState)
  }, [existing])

  function validate() {
    const e = {}
    if (!form.patientId) e.patientId = 'Patient ID is required'
    if (!form.name) e.name = 'Patient name is required'
    if (!form.age || isNaN(Number(form.age))) e.age = 'Valid age is required'
    if (!form.gender) e.gender = 'Gender is required'
    if (!form.phone) e.phone = 'Phone is required'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  async function handleSubmit(e) {
    e.preventDefault()
    if (!validate()) return
    try {
      const payload = {
        patientId: form.patientId,
        name: form.name,
        age: Number(form.age),
        gender: form.gender,
        phone: form.phone,
        email: form.email,
        address: form.address,
        imageUrl: form.imageUrl,
        diagnosis: form.diagnosis,
        confidence: form.confidence,
        doctorName: form.doctorName,
        appointmentDate: form.appointmentDate,
      }

      if (isEdit) {
        const { id } = form
        const res = await updatePatient(id, payload)
        if (res.success) {
          setNotification({ type: 'success', message: 'Patient updated successfully' })
          onDone()
        } else {
          setNotification({ type: 'danger', message: res.error || 'Update failed' })
        }
      } else {
        const res = await addPatient(payload)
        if (res.success) {
          setNotification({ type: 'success', message: `Patient saved successfully (id: ${res.id})` })
          onDone()
        } else {
          setNotification({ type: 'danger', message: res.error || 'Add failed' })
        }
      }
    } catch (err) {
      console.error('[ui] PatientForm handleSubmit error', err)
      setNotification({ type: 'danger', message: err.message || 'Operation failed' })
    }
  }

  function handleChange(e) {
    const { name, value } = e.target
    setForm((s) => ({ ...s, [name]: value }))
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="row g-2">
        <div className="col-md-4">
          <label className="form-label" htmlFor="patient-id">Patient ID</label>
          <input id="patient-id" name="patientId" value={form.patientId} onChange={handleChange} className={`form-control ${errors.patientId ? 'is-invalid' : ''}`} />
          <div className="invalid-feedback">{errors.patientId}</div>
        </div>
        <div className="col-md-8">
          <label className="form-label" htmlFor="patient-name">Name</label>
          <input id="patient-name" name="name" value={form.name} onChange={handleChange} className={`form-control ${errors.name ? 'is-invalid' : ''}`} />
          <div className="invalid-feedback">{errors.name}</div>
        </div>
        <div className="col-md-2">
          <label className="form-label" htmlFor="patient-age">Age</label>
          <input id="patient-age" name="age" value={form.age} onChange={handleChange} className={`form-control ${errors.age ? 'is-invalid' : ''}`} />
          <div className="invalid-feedback">{errors.age}</div>
        </div>
        <div className="col-md-2">
          <label className="form-label" htmlFor="patient-gender">Gender</label>
          <select id="patient-gender" name="gender" value={form.gender} onChange={handleChange} className={`form-select ${errors.gender ? 'is-invalid' : ''}`}>
            <option value="">Select</option>
            <option>Male</option>
            <option>Female</option>
            <option>Other</option>
          </select>
          <div className="invalid-feedback">{errors.gender}</div>
        </div>
        <div className="col-md-4">
          <label className="form-label" htmlFor="patient-phone">Phone</label>
          <input id="patient-phone" name="phone" value={form.phone} onChange={handleChange} className={`form-control ${errors.phone ? 'is-invalid' : ''}`} />
          <div className="invalid-feedback">{errors.phone}</div>
        </div>
        <div className="col-md-6">
          <label className="form-label" htmlFor="patient-email">Email</label>
          <input id="patient-email" name="email" value={form.email} onChange={handleChange} className="form-control" />
        </div>
        <div className="col-12">
          <label className="form-label" htmlFor="patient-address">Address</label>
          <input id="patient-address" name="address" value={form.address} onChange={handleChange} className="form-control" />
        </div>
        <div className="col-md-6">
          <label className="form-label" htmlFor="patient-diagnosis">Diagnosis</label>
          <input id="patient-diagnosis" name="diagnosis" value={form.diagnosis} onChange={handleChange} className="form-control" />
        </div>
        <div className="col-md-6">
          <label className="form-label" htmlFor="patient-confidence">Confidence</label>
          <input id="patient-confidence" name="confidence" value={form.confidence} onChange={handleChange} className="form-control" />
        </div>
        <div className="col-md-6">
          <label className="form-label" htmlFor="patient-doctor-name">Doctor Name</label>
          <input id="patient-doctor-name" name="doctorName" value={form.doctorName} onChange={handleChange} className="form-control" />
        </div>
        <div className="col-md-6">
          <label className="form-label" htmlFor="patient-appointment-date">Appointment Date</label>
          <input id="patient-appointment-date" type="date" name="appointmentDate" value={form.appointmentDate} onChange={handleChange} className="form-control" />
        </div>
      </div>

      <div className="mt-3 d-flex gap-2">
        <button type="submit" className="btn btn-primary">{isEdit ? 'Update' : 'Add'} Patient</button>
        <button type="button" className="btn btn-secondary" onClick={onDone}>Cancel</button>
      </div>
    </form>
  )
}

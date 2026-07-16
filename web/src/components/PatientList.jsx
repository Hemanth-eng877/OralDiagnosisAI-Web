import React, { useState, useEffect } from 'react'
import { subscribePatients, deletePatient } from '../services/firestoreService'
import PatientForm from './PatientForm'
import Notification from './Notification'
import Loading from './Loading'

export default function PatientList() {
  const [patients, setPatients] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState(null)
  const [notification, setNotification] = useState(null)
  const [search, setSearch] = useState('')
  const [sortDesc, setSortDesc] = useState(true)

  useEffect(() => {
    setLoading(true)
    const unsub = subscribePatients({
      onUpdate: (data, error) => {
        if (error) {
          console.error('[ui] subscribePatients error', error)
          setNotification({ type: 'danger', message: error.message || 'Failed to load patients' })
          setLoading(false)
          return
        }
        setPatients(data || [])
        setLoading(false)
      },
      search,
      sortDesc,
    })
    return () => unsub()
  }, [search, sortDesc])

  async function handleDelete(p) {
    if (!confirm(`Delete patient ${p.name} (${p.patientId})?`)) return
    const res = await deletePatient(p.id)
    if (res.success) setNotification({ type: 'success', message: 'Deleted successfully' })
    else setNotification({ type: 'danger', message: res.error || 'Delete failed' })
  }

  function openNew() { setEditing(null); setShowForm(true) }
  function openEdit(p) { setEditing(p); setShowForm(true) }

  function closeForm() { setShowForm(false); setEditing(null) }

  const sorted = [...patients].sort((a, b) => {
    const da = a.createdAt?.seconds ?? 0
    const db = b.createdAt?.seconds ?? 0
    return sortDesc ? db - da : da - db
  })

  return (
    <div>
      {notification && <Notification type={notification.type} message={notification.message} onClose={() => setNotification(null)} />}

      <div className="mb-3 d-flex gap-2">
        <button className="btn btn-success" onClick={openNew}>Add Patient</button>
        <input placeholder="Search by name or ID" className="form-control w-50" value={search} onChange={(e) => setSearch(e.target.value)} />
        <div className="form-check form-switch ms-auto">
          <input className="form-check-input" type="checkbox" id="sortDesc" checked={sortDesc} onChange={(e) => setSortDesc(e.target.checked)} />
          <label className="form-check-label" htmlFor="sortDesc">Sort by newest</label>
        </div>
      </div>

      {showForm && (
        <div className="card card-body mb-3">
          <h5>{editing ? 'Edit Patient' : 'Add Patient'}</h5>
          <PatientForm existing={editing} onDone={() => { closeForm(); }} setNotification={setNotification} />
        </div>
      )}

      {loading ? (
        <Loading />
      ) : (
        <div className="table-responsive">
          <table className="table table-striped table-hover">
            <thead>
              <tr>
                <th>Patient ID</th>
                <th>Name</th>
                <th>Age</th>
                <th>Gender</th>
                <th>Phone</th>
                <th>Doctor</th>
                <th>Appointment</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {sorted.map((p) => (
                <tr key={p.id}>
                  <td>{p.patientId}</td>
                  <td>{p.name}</td>
                  <td>{p.age}</td>
                  <td>{p.gender}</td>
                  <td>{p.phone}</td>
                  <td>{p.doctorName}</td>
                  <td>{p.appointmentDate}</td>
                  <td>
                    <button className="btn btn-sm btn-primary me-1" onClick={() => openEdit({ ...p, id: p.id })}>Edit</button>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(p)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

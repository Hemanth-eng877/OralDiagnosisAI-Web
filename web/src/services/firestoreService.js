import {
  collection,
  addDoc,
  doc,
  updateDoc,
  deleteDoc,
  onSnapshot,
  query,
  orderBy,
  serverTimestamp,
  getDocs,
} from 'firebase/firestore'
import { db } from '../firebase'

const USERS_COLLECTION = 'users'
const PATIENTS_COLLECTION = 'patients'
const SCANS_COLLECTION = 'scans'
const APPOINTMENTS_COLLECTION = 'appointments'
const REPORTS_COLLECTION = 'reports'

function buildTimestampPayload(payload) {
  return {
    ...payload,
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp(),
  }
}

async function addDocument(collectionName, payload) {
  try {
    const data = buildTimestampPayload(payload)
    console.log('[firestore] addDocument', collectionName, data)
    const ref = await addDoc(collection(db, collectionName), data)
    console.log('[firestore] addDocument success', collectionName, ref.id)
    return { success: true, id: ref.id }
  } catch (error) {
    console.error('[firestore] addDocument error', collectionName, error)
    return { success: false, error: error.message, code: error.code }
  }
}

async function updateDocument(collectionName, documentId, updates) {
  try {
    const ref = doc(db, collectionName, documentId)
    console.log('[firestore] updateDocument', collectionName, documentId, updates)
    await updateDoc(ref, { ...updates, updatedAt: serverTimestamp() })
    return { success: true }
  } catch (error) {
    console.error('[firestore] updateDocument error', collectionName, error)
    return { success: false, error: error.message, code: error.code }
  }
}

async function deleteDocument(collectionName, documentId) {
  try {
    const ref = doc(db, collectionName, documentId)
    console.log('[firestore] deleteDocument', collectionName, documentId)
    await deleteDoc(ref)
    return { success: true }
  } catch (error) {
    console.error('[firestore] deleteDocument error', collectionName, error)
    return { success: false, error: error.message, code: error.code }
  }
}

export async function addPatient(patient) {
  return addDocument(PATIENTS_COLLECTION, patient)
}

export async function updatePatient(patientId, updates) {
  return updateDocument(PATIENTS_COLLECTION, patientId, updates)
}

export async function deletePatient(patientId) {
  return deleteDocument(PATIENTS_COLLECTION, patientId)
}

export function subscribePatients({ onUpdate, search = '', sortDesc = true }) {
  try {
    console.log('[firestore] subscribePatients - start (search, sortDesc):', search, sortDesc)
    const q = query(collection(db, PATIENTS_COLLECTION), orderBy('createdAt', sortDesc ? 'desc' : 'asc'))

    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        console.log('[firestore] subscribePatients - snapshot received, size:', snapshot.size)
        const items = snapshot.docs.map((d) => ({ id: d.id, ...d.data() }))
        let filtered = items
        if (search && search.trim()) {
          const s = search.toLowerCase()
          filtered = items.filter((p) => (p.name || '').toLowerCase().includes(s) || (p.patientId || '').toLowerCase().includes(s))
        }
        onUpdate(filtered)
      },
      (error) => {
        console.error('[firestore] subscribePatients - onSnapshot error', error)
        onUpdate(null, error)
      }
    )

    return unsubscribe
  } catch (error) {
    console.error('[firestore] subscribePatients error', error)
    throw error
  }
}

export async function queryPatientsByNameOrId(term) {
  try {
    const s = term.toLowerCase()
    const snapshot = await getDocs(collection(db, PATIENTS_COLLECTION))
    const results = snapshot.docs
      .map((d) => ({ id: d.id, ...d.data() }))
      .filter((p) => (p.name || '').toLowerCase().includes(s) || (p.patientId || '').toLowerCase().includes(s))
    return { success: true, data: results }
  } catch (error) {
    console.error('[firestore] queryPatientsByNameOrId error', error)
    return { success: false, error: error.message }
  }
}

export async function addUser(user) {
  return addDocument(USERS_COLLECTION, user)
}

export async function addScan(scan) {
  return addDocument(SCANS_COLLECTION, scan)
}

export async function addAppointment(appointment) {
  return addDocument(APPOINTMENTS_COLLECTION, appointment)
}

export async function addReport(report) {
  return addDocument(REPORTS_COLLECTION, report)
}

export async function getUsers() {
  try {
    const snapshot = await getDocs(collection(db, USERS_COLLECTION))
    return { success: true, data: snapshot.docs.map((d) => ({ id: d.id, ...d.data() })) }
  } catch (error) {
    console.error('[firestore] getUsers error', error)
    return { success: false, error: error.message }
  }
}

export async function getScans() {
  try {
    const snapshot = await getDocs(collection(db, SCANS_COLLECTION))
    return { success: true, data: snapshot.docs.map((d) => ({ id: d.id, ...d.data() })) }
  } catch (error) {
    console.error('[firestore] getScans error', error)
    return { success: false, error: error.message }
  }
}

export async function getAppointments() {
  try {
    const snapshot = await getDocs(collection(db, APPOINTMENTS_COLLECTION))
    return { success: true, data: snapshot.docs.map((d) => ({ id: d.id, ...d.data() })) }
  } catch (error) {
    console.error('[firestore] getAppointments error', error)
    return { success: false, error: error.message }
  }
}

export async function getReports() {
  try {
    const snapshot = await getDocs(collection(db, REPORTS_COLLECTION))
    return { success: true, data: snapshot.docs.map((d) => ({ id: d.id, ...d.data() })) }
  } catch (error) {
    console.error('[firestore] getReports error', error)
    return { success: false, error: error.message }
  }
}

// Reusable Firebase configuration file.
// Replace values in .env.local (Vite) with your existing Firebase project's config.
import { initializeApp } from 'firebase/app'
import { getFirestore, collection, addDoc, getDoc, doc, deleteDoc, serverTimestamp } from 'firebase/firestore'

// Read configuration from Vite environment variables
const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET,
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

// Verify required Vite env variables are present
const requiredVars = [
  'VITE_FIREBASE_API_KEY',
  'VITE_FIREBASE_AUTH_DOMAIN',
  'VITE_FIREBASE_PROJECT_ID',
  'VITE_FIREBASE_STORAGE_BUCKET',
  'VITE_FIREBASE_MESSAGING_SENDER_ID',
  'VITE_FIREBASE_APP_ID',
]
const missing = requiredVars.filter((k) => !import.meta.env[k])
if (missing.length) {
  console.error('[firebase] Missing environment variables:', missing)
} else {
  console.log('[firebase] All required environment variables present')
}

if (!firebaseConfig.projectId) {
  console.error('[firebase] Firebase config missing projectId. Check your Vite env variables in .env.local')
} else {
  console.log('[firebase] Initializing Firebase for project:', firebaseConfig.projectId)
}

const app = initializeApp(firebaseConfig)
const db = getFirestore(app)

// Test connection by performing a small write/read/delete to a transient collection
export async function testFirebaseConnection() {
  try {
    if (missing.length) {
      const msg = `Missing env vars: ${missing.join(', ')}`
      console.error('[firebase test] Aborting test -', msg)
      return { success: false, error: msg }
    }

    console.log('[firebase test] Starting Firestore read/write test')
    const colRef = collection(db, 'connection_tests')
    const payload = { test: true, ts: serverTimestamp() }
    console.log('[firebase test] addDoc payload:', payload)
    const ref = await addDoc(colRef, payload)
    console.log('[firebase test] addDoc success, id:', ref.id)

    const snapshot = await getDoc(doc(db, 'connection_tests', ref.id))
    console.log('[firebase test] getDoc success, exists:', snapshot.exists(), 'data:', snapshot.data())

    await deleteDoc(doc(db, 'connection_tests', ref.id))
    console.log('[firebase test] deleteDoc success, id:', ref.id)

    return { success: true, id: ref.id }
  } catch (err) {
    console.error('[firebase test] error:', err)
    return { success: false, error: err.message, code: err.code }
  }
}

export { app, db }

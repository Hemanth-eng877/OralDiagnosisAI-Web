import React from 'react'
import { createRoot } from 'react-dom/client'
import App from './App'
import './styles.css'
import { testFirebaseConnection } from './firebase'

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)

// Run a quick Firebase connection test and log the result
(async () => {
  try {
    const res = await testFirebaseConnection()
    if (res.success) {
      console.log('[firebase test] Connection OK. Test doc id:', res.id)
    } else {
      console.error('[firebase test] Connection failed:', res.error || res)
    }
  } catch (err) {
    console.error('[firebase test] Unexpected error:', err)
  }
})()

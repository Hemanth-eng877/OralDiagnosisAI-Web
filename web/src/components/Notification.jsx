import React from 'react'

export default function Notification({ type = 'success', message, onClose }) {
  if (!message) return null
  return (
    <div className={`alert alert-${type} alert-dismissible`} role="alert">
      {message}
      <button type="button" className="btn-close" aria-label="Close" onClick={onClose}></button>
    </div>
  )
}

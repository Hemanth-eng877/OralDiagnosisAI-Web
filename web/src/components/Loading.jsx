import React from 'react'

export default function Loading({ message = 'Loading...' }) {
  return (
    <div className="d-flex align-items-center gap-2">
      <div className="spinner-border" role="status" aria-hidden="true"></div>
      <div>{message}</div>
    </div>
  )
}

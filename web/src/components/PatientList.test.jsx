import { render, screen } from '@testing-library/react'
import PatientList from './PatientList'

describe('PatientList', () => {
  it('renders the add patient button and search input', () => {
    render(<PatientList />)
    expect(screen.getByRole('button', { name: /Add Patient/i })).toBeInTheDocument()
    expect(screen.getByPlaceholderText(/Search by name or ID/i)).toBeInTheDocument()
  })
})

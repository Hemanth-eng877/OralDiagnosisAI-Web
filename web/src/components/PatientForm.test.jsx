import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import PatientForm from './PatientForm'
import { addPatient } from '../services/firestoreService'

vi.mock('../services/firestoreService', () => ({
  addPatient: vi.fn(),
  updatePatient: vi.fn(),
}))

describe('PatientForm', () => {
  it('renders the form fields and submits a valid patient payload', async () => {
    const user = userEvent.setup()
    const onDone = vi.fn()
    const setNotification = vi.fn()
    addPatient.mockResolvedValue({ success: true, id: 'patient-1' })

    render(<PatientForm existing={null} onDone={onDone} setNotification={setNotification} />)

    await user.type(screen.getByLabelText(/Patient ID/i), 'P-100')
    await user.type(screen.getByLabelText(/^Name$/i), 'Tim Carter')
    await user.type(screen.getByLabelText(/Age/i), '34')
    await user.selectOptions(screen.getByLabelText(/Gender/i), 'Male')
    await user.type(screen.getByLabelText(/Phone/i), '555-0100')
    await user.type(screen.getByLabelText(/Email/i), 'tim@example.com')
    await user.type(screen.getByLabelText(/Address/i), '123 Main St')
    await user.click(screen.getByRole('button', { name: /Add Patient/i }))

    await waitFor(() => {
      expect(setNotification).toHaveBeenCalledWith({
        type: 'success',
        message: 'Patient saved successfully (id: patient-1)',
      })
    })
  })

  it('shows validation feedback for missing required fields', async () => {
    const user = userEvent.setup()
    render(<PatientForm existing={null} onDone={vi.fn()} setNotification={vi.fn()} />)

    await user.click(screen.getByRole('button', { name: /Add Patient/i }))

    expect(screen.getByText(/Patient ID is required/i)).toBeInTheDocument()
    expect(screen.getByText(/Patient name is required/i)).toBeInTheDocument()
  })
})

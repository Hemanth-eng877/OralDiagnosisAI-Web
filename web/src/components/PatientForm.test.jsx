import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import PatientForm from './PatientForm'

describe('PatientForm', () => {
  it('renders the form fields and submits a valid patient payload', async () => {
    const user = userEvent.setup()
    const onDone = vi.fn()
    const setNotification = vi.fn()

    render(<PatientForm existing={null} onDone={onDone} setNotification={setNotification} />)

    await user.type(screen.getByLabelText(/Patient ID/i), 'P-100')
    await user.type(screen.getByLabelText(/Name/i), 'Tim Carter')
    await user.type(screen.getByLabelText(/Age/i), '34')
    await user.selectOptions(screen.getByLabelText(/Gender/i), 'Male')
    await user.type(screen.getByLabelText(/Phone/i), '555-0100')
    await user.type(screen.getByLabelText(/Email/i), 'tim@example.com')
    await user.type(screen.getByLabelText(/Address/i), '123 Main St')
    await user.click(screen.getByRole('button', { name: /Add Patient/i }))

    expect(setNotification).toHaveBeenCalled()
  })

  it('shows validation feedback for missing required fields', async () => {
    const user = userEvent.setup()
    render(<PatientForm existing={null} onDone={vi.fn()} setNotification={vi.fn()} />)

    await user.click(screen.getByRole('button', { name: /Add Patient/i }))

    expect(screen.getByText(/Patient ID is required/i)).toBeInTheDocument()
    expect(screen.getByText(/Patient name is required/i)).toBeInTheDocument()
  })
})

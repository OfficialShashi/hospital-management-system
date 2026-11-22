
import { useEffect, useState } from 'react'

import {
    getAllInvoices,
    getAllPayments,
    createInvoice,
    updateInvoiceStatus,
    createPayment,
    confirmPayment,
    failPayment,
    refundPayment,
} from '../services/billingService'

import { getAllPatients } from '../services/patientService'
import { getAppointmentsByPatient } from '../services/appointmentService'

import './Billing.css'

const initialItem = {
    itemType: 'CONSULTATION',
    description: '',
    quantity: 1,
    unitPrice: '',
    sourceId: '',
}

const initialPaymentForm = {
    invoiceId: '',
    amount: '',
    paymentMethod: 'CASH',
    transactionReference: '',
    notes: '',
}

function Billing() {
    const [invoices, setInvoices] = useState([])
    const [payments, setPayments] = useState([])
    const [patients, setPatients] = useState([])
    const [appointments, setAppointments] = useState([])

    const [issuingInvoiceId, setIssuingInvoiceId] = useState(null)
    const [confirmingPaymentId, setConfirmingPaymentId] = useState(null)
    const [processingPaymentId, setProcessingPaymentId] = useState(null)
    const [processingPaymentAction, setProcessingPaymentAction] = useState('')

    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [paymentSaving, setPaymentSaving] = useState(false)

    const [showCreateForm, setShowCreateForm] = useState(false)
    const [showPaymentForm, setShowPaymentForm] = useState(false)

    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    const [form, setForm] = useState({
        patientId: '',
        appointmentId: '',
        discount: 0,
        taxAmount: 0,
        dueDate: '',
        notes: '',
        items: [{ ...initialItem }],
    })

    const [paymentForm, setPaymentForm] = useState({
        ...initialPaymentForm,
    })

    // Load Billing Data
    const loadBillingData = async () => {
        try {
            setLoading(true)
            setError('')

            const [invoiceData, paymentData, patientData] = await Promise.all([
                getAllInvoices(),
                getAllPayments(),
                getAllPatients(),
            ])

            setInvoices(Array.isArray(invoiceData) ? invoiceData : [])
            setPayments(Array.isArray(paymentData) ? paymentData : [])
            setPatients(Array.isArray(patientData) ? patientData : [])
        } catch (err) {
            console.error('Billing loading error:', err)
            setError(
                err.response?.data?.message ||
                'Unable to load billing information.'
            )
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadBillingData()
    }, [])

    // Currency Format
    const formatCurrency = (amount) =>
        new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 2,
        }).format(amount || 0)

    // Status CSS
    const getStatusClass = (status) =>
        status?.toLowerCase().replaceAll('_', '-') || 'unknown'

    // Eligible Invoices for Payment
    const payableInvoices = invoices.filter(
        (invoice) =>
            ['ISSUED', 'PARTIALLY_PAID'].includes(invoice.invoiceStatus) &&
            Number(invoice.balanceAmount) > 0
    )

    // Selected Payment Invoice
    const selectedPaymentInvoice = payableInvoices.find(
        (invoice) => String(invoice.invoiceId) === String(paymentForm.invoiceId)
    )

    // Patient Selection
    const handlePatientChange = async (patientId) => {
        setForm((previous) => ({
            ...previous,
            patientId,
            appointmentId: '',
        }))

        setAppointments([])
        setError('')

        if (!patientId) return

        try {
            const data = await getAppointmentsByPatient(patientId)
            setAppointments(Array.isArray(data) ? data : [])
        } catch (err) {
            console.error('Appointment loading error:', err)
            setError(
                err.response?.data?.message ||
                'Unable to load patient appointments.'
            )
        }
    }

    // Invoice Item Change
    const handleItemChange = (index, field, value) => {
        setForm((previous) => ({
            ...previous,
            items: previous.items.map((item, itemIndex) =>
                itemIndex === index
                    ? { ...item, [field]: value }
                    : item
            ),
        }))
    }

    // Add Invoice Item
    const addItem = () => {
        setForm((previous) => ({
            ...previous,
            items: [...previous.items, { ...initialItem }],
        }))
    }

    // Remove Invoice Item
    const removeItem = (index) => {
        setForm((previous) => ({
            ...previous,
            items: previous.items.filter(
                (_, itemIndex) => itemIndex !== index
            ),
        }))
    }

    // Automatic Calculation
    const subtotal = form.items.reduce(
        (total, item) =>
            total +
            Number(item.quantity || 0) * Number(item.unitPrice || 0),
        0
    )

    const discount = Number(form.discount || 0)
    const taxAmount = Number(form.taxAmount || 0)

    const totalAmount = Math.max(
        0,
        subtotal - discount + taxAmount
    )

    // Reset Invoice Form
    const resetForm = () => {
        setForm({
            patientId: '',
            appointmentId: '',
            discount: 0,
            taxAmount: 0,
            dueDate: '',
            notes: '',
            items: [{ ...initialItem }],
        })

        setAppointments([])
    }

    // Create Invoice
    const handleCreateInvoice = async (event) => {
        event.preventDefault()

        setError('')
        setSuccess('')

        if (!form.patientId) {
            setError('Please select a patient.')
            return
        }

        if (!form.appointmentId) {
            setError('Please select an appointment.')
            return
        }

        if (form.items.length === 0) {
            setError('Please add at least one invoice item.')
            return
        }

        const invalidItem = form.items.some(
            (item) =>
                !item.description.trim() ||
                Number(item.quantity) <= 0 ||
                !Number.isFinite(Number(item.unitPrice)) ||
                Number(item.unitPrice) < 0 ||
                item.unitPrice === ''
        )

        if (invalidItem) {
            setError('Please enter valid details for every invoice item.')
            return
        }

        if (
            discount < 0 ||
            discount > subtotal ||
            taxAmount < 0 ||
            !Number.isFinite(discount) ||
            !Number.isFinite(taxAmount)
        ) {
            setError('Please enter a valid discount and tax amount.')
            return
        }

        const invoiceData = {
            patientId: Number(form.patientId),
            appointmentId: Number(form.appointmentId),
            discount,
            taxAmount,
            dueDate: form.dueDate || null,
            notes: form.notes.trim(),
            items: form.items.map((item) => ({
                itemType: item.itemType,
                description: item.description.trim(),
                quantity: Number(item.quantity),
                unitPrice: Number(item.unitPrice),
                ...(item.sourceId !== ''
                    ? { sourceId: Number(item.sourceId) }
                    : {}),
            })),
        }

        try {
            setSaving(true)

            const createdInvoice = await createInvoice(invoiceData)

            setSuccess(
                `Invoice ${createdInvoice.invoiceNumber || ''} created successfully.`
            )

            resetForm()
            setShowCreateForm(false)

            await loadBillingData()
        } catch (err) {
            console.error('Create invoice error:', err)
            setError(
                err.response?.data?.message ||
                'Unable to create invoice. Please check the entered details.'
            )
        } finally {
            setSaving(false)
        }
    }

    // Issue Invoice
    const handleIssueInvoice = async (invoiceId) => {
        try {
            setError('')
            setSuccess('')
            setIssuingInvoiceId(invoiceId)

            await updateInvoiceStatus(invoiceId, 'ISSUED')

            setSuccess('Invoice issued successfully.')

            await loadBillingData()
        } catch (err) {
            console.error('Issue invoice error:', err)
            setError(
                err.response?.data?.message ||
                'Unable to issue invoice.'
            )
        } finally {
            setIssuingInvoiceId(null)
        }
    }

    // Select Invoice for Payment
    const handlePaymentInvoiceChange = (invoiceId) => {
        const selectedInvoice = payableInvoices.find(
            (invoice) => String(invoice.invoiceId) === String(invoiceId)
        )

        setPaymentForm((previous) => ({
            ...previous,
            invoiceId,
            amount: selectedInvoice
                ? Number(selectedInvoice.balanceAmount).toFixed(2)
                : '',
        }))
    }

    // Reset Payment Form
    const resetPaymentForm = () => {
        setPaymentForm({ ...initialPaymentForm })
    }

    // Create Payment
    const handleCreatePayment = async (event) => {
        event.preventDefault()

        setError('')
        setSuccess('')

        if (!selectedPaymentInvoice) {
            setError('Please select a valid invoice.')
            return
        }

        const amount = Number(paymentForm.amount)
        const balance = Number(selectedPaymentInvoice.balanceAmount)

        if (!Number.isFinite(amount) || amount <= 0) {
            setError('Please enter a valid payment amount.')
            return
        }

        if (!/^\d+(\.\d{1,2})?$/.test(paymentForm.amount)) {
            setError('Payment amount can have maximum 2 decimal places.')
            return
        }

        if (amount > balance) {
            setError(`Payment cannot exceed the remaining balance of ${formatCurrency(balance)}.`)
            return
        }

        const paymentData = {
            invoiceId: Number(paymentForm.invoiceId),
            amount,
            paymentMethod: paymentForm.paymentMethod,
            transactionReference: paymentForm.transactionReference.trim(),
            notes: paymentForm.notes.trim(),
        }

        try {
            setPaymentSaving(true)

            const createdPayment = await createPayment(paymentData)

            setSuccess(
                `Payment ${createdPayment.paymentNumber || ''} created successfully. Status: PENDING.`
            )

            resetPaymentForm()
            setShowPaymentForm(false)

            await loadBillingData()
        } catch (err) {
            console.error('Create payment error:', err)
            setError(
                err.response?.data?.message ||
                'Unable to create payment. Please check the entered details.'
            )
        } finally {
            setPaymentSaving(false)
        }
    }

    // Confirm Payment
    const handleConfirmPayment = async (paymentId) => {
        try {
            setError('')
            setSuccess('')
            setConfirmingPaymentId(paymentId)

            await confirmPayment(paymentId)

            setSuccess('Payment confirmed successfully. Invoice balance updated.')

            await loadBillingData()
        } catch (err) {
            console.error('Confirm payment error:', err)
            setError(
                err.response?.data?.message ||
                'Unable to confirm payment.'
            )
        } finally {
            setConfirmingPaymentId(null)
        }
    }


    const handleFailPayment = async (paymentId) => {
        if (!window.confirm('Are you sure you want to mark this payment as FAILED?')) {
            return
        }

        try {
            setError('')
            setSuccess('')
            setProcessingPaymentId(paymentId)
            setProcessingPaymentAction('FAIL')

            await failPayment(paymentId)

            setSuccess('Payment marked as FAILED successfully.')
            await loadBillingData()
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Failed to update payment status.'
            )
        } finally {
            setProcessingPaymentId(null)
            setProcessingPaymentAction('')
        }
    }

    const handleRefundPayment = async (paymentId) => {
        if (!window.confirm('This will refund the FULL payment amount. Do you want to continue?')) {
            return
        }

        try {
            setError('')
            setSuccess('')
            setProcessingPaymentId(paymentId)
            setProcessingPaymentAction('REFUND')

            await refundPayment(paymentId)

            setSuccess('Full payment refunded successfully.')
            await loadBillingData()
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Failed to refund payment.'
            )
        } finally {
            setProcessingPaymentId(null)
            setProcessingPaymentAction('')
        }
    }


    if (loading) {
        return (
            <div className="billing-loading">
                Loading billing information...
            </div>
        )
    }

    return (
        <div className="billing-page">

            {/* Header */}
            <div className="billing-header">
                <div>
                    <h2>Billing & Payments</h2>
                    <p>Manage patient invoices and payment records.</p>
                </div>

                <div>
                    <button
                        className="billing-refresh-btn"
                        onClick={loadBillingData}
                        disabled={saving || paymentSaving || issuingInvoiceId !== null}
                    >
                        Refresh
                    </button>

                    <button
                        className="billing-create-btn"
                        onClick={() => {
                            setShowCreateForm(!showCreateForm)
                            setShowPaymentForm(false)
                            setError('')
                            setSuccess('')
                        }}
                    >
                        {showCreateForm ? 'Close Form' : '+ Create Invoice'}
                    </button>

                    <button
                        className="billing-create-btn"
                        onClick={() => {
                            setShowPaymentForm(!showPaymentForm)
                            setShowCreateForm(false)
                            setError('')
                            setSuccess('')
                        }}
                    >
                        {showPaymentForm ? 'Close Payment' : '+ Record Payment'}
                    </button>
                </div>
            </div>

            {error && <div className="billing-error">{error}</div>}
            {success && <div className="billing-success">{success}</div>}

            {/* Create Invoice Form */}
            {showCreateForm && (
                <section className="billing-section billing-create-section">
                    <h3>Create New Invoice</h3>

                    <form onSubmit={handleCreateInvoice}>
                        <div className="billing-form-grid">

                            <div className="billing-form-group">
                                <label>Patient *</label>
                                <select
                                    value={form.patientId}
                                    onChange={(event) => handlePatientChange(event.target.value)}
                                    required
                                >
                                    <option value="">Select Patient</option>
                                    {patients.map((patient) => (
                                        <option key={patient.patientId} value={patient.patientId}>
                                            {patient.fullName} - {patient.patientNumber}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="billing-form-group">
                                <label>Appointment *</label>
                                <select
                                    value={form.appointmentId}
                                    onChange={(event) =>
                                        setForm((previous) => ({
                                            ...previous,
                                            appointmentId: event.target.value,
                                        }))
                                    }
                                    disabled={!form.patientId}
                                    required
                                >
                                    <option value="">Select Appointment</option>
                                    {appointments.map((appointment) => (
                                        <option
                                            key={appointment.appointmentId || appointment.id}
                                            value={appointment.appointmentId || appointment.id}
                                        >
                                            {appointment.appointmentNumber ||
                                                `Appointment #${appointment.appointmentId || appointment.id}`}
                                            {' - '}
                                            {appointment.appointmentDate
                                                ? new Date(appointment.appointmentDate).toLocaleDateString('en-IN')
                                                : appointment.status || ''}
                                        </option>
                                    ))}
                                </select>
                            </div>

                            <div className="billing-form-group">
                                <label>Due Date</label>
                                <input
                                    type="date"
                                    value={form.dueDate}
                                    onChange={(event) =>
                                        setForm((previous) => ({
                                            ...previous,
                                            dueDate: event.target.value,
                                        }))
                                    }
                                />
                            </div>

                            <div className="billing-form-group">
                                <label>Discount (₹)</label>
                                <input
                                    type="number"
                                    min="0"
                                    step="0.01"
                                    value={form.discount}
                                    onChange={(event) =>
                                        setForm((previous) => ({
                                            ...previous,
                                            discount: event.target.value,
                                        }))
                                    }
                                />
                            </div>

                            <div className="billing-form-group">
                                <label>Tax Amount (₹)</label>
                                <input
                                    type="number"
                                    min="0"
                                    step="0.01"
                                    value={form.taxAmount}
                                    onChange={(event) =>
                                        setForm((previous) => ({
                                            ...previous,
                                            taxAmount: event.target.value,
                                        }))
                                    }
                                />
                            </div>
                        </div>

                        <div className="billing-items-header">
                            <h4>Invoice Items</h4>
                            <button
                                type="button"
                                className="billing-add-item-btn"
                                onClick={addItem}
                            >
                                + Add Item
                            </button>
                        </div>

                        {form.items.map((item, index) => (
                            <div className="billing-item-row" key={index}>

                                <div className="billing-form-group">
                                    <label>Item Type *</label>
                                    <select
                                        value={item.itemType}
                                        onChange={(event) =>
                                            handleItemChange(index, 'itemType', event.target.value)
                                        }
                                    >
                                        <option value="CONSULTATION">Consultation</option>
                                        <option value="LAB">Laboratory</option>
                                        <option value="PHARMACY">Pharmacy</option>
                                        <option value="OTHER">Other</option>
                                    </select>
                                </div>

                                <div className="billing-form-group">
                                    <label>Description *</label>
                                    <input
                                        type="text"
                                        placeholder="Enter description"
                                        value={item.description}
                                        onChange={(event) =>
                                            handleItemChange(index, 'description', event.target.value)
                                        }
                                        required
                                    />
                                </div>

                                <div className="billing-form-group">
                                    <label>Quantity *</label>
                                    <input
                                        type="number"
                                        min="1"
                                        step="1"
                                        value={item.quantity}
                                        onChange={(event) =>
                                            handleItemChange(index, 'quantity', event.target.value)
                                        }
                                        required
                                    />
                                </div>

                                <div className="billing-form-group">
                                    <label>Unit Price (₹) *</label>
                                    <input
                                        type="number"
                                        min="0"
                                        step="0.01"
                                        placeholder="0.00"
                                        value={item.unitPrice}
                                        onChange={(event) =>
                                            handleItemChange(index, 'unitPrice', event.target.value)
                                        }
                                        required
                                    />
                                </div>

                                <div className="billing-form-group">
                                    <label>Source ID</label>
                                    <input
                                        type="number"
                                        min="1"
                                        placeholder="Optional"
                                        value={item.sourceId}
                                        onChange={(event) =>
                                            handleItemChange(index, 'sourceId', event.target.value)
                                        }
                                    />
                                </div>

                                <button
                                    type="button"
                                    className="billing-remove-item-btn"
                                    onClick={() => removeItem(index)}
                                    disabled={form.items.length === 1}
                                >
                                    Remove
                                </button>
                            </div>
                        ))}

                        <div className="billing-form-group billing-notes-group">
                            <label>Notes</label>
                            <textarea
                                rows="3"
                                placeholder="Enter invoice notes (optional)"
                                value={form.notes}
                                onChange={(event) =>
                                    setForm((previous) => ({
                                        ...previous,
                                        notes: event.target.value,
                                    }))
                                }
                            />
                        </div>

                        <div className="billing-total-box">
                            <p>Subtotal: <strong>{formatCurrency(subtotal)}</strong></p>
                            <p>Discount: <strong>{formatCurrency(discount)}</strong></p>
                            <p>Tax: <strong>{formatCurrency(taxAmount)}</strong></p>
                            <h3>Total Amount: {formatCurrency(totalAmount)}</h3>
                        </div>

                        <div className="billing-form-actions">
                            <button
                                type="button"
                                className="billing-cancel-btn"
                                onClick={() => {
                                    setShowCreateForm(false)
                                    resetForm()
                                    setError('')
                                }}
                                disabled={saving}
                            >
                                Cancel
                            </button>

                            <button
                                type="submit"
                                className="billing-create-btn"
                                disabled={saving || totalAmount <= 0}
                            >
                                {saving ? 'Creating...' : 'Create Invoice'}
                            </button>
                        </div>
                    </form>
                </section>
            )}

            {/* Create Payment Form */}
            {showPaymentForm && (
                <section className="billing-section billing-create-section">
                    <h3>Record New Payment</h3>

                    {payableInvoices.length === 0 ? (
                        <p className="billing-empty">
                            No ISSUED or PARTIALLY PAID invoices with an outstanding balance.
                        </p>
                    ) : (
                        <form onSubmit={handleCreatePayment}>
                            <div className="billing-form-grid">

                                <div className="billing-form-group">
                                    <label>Select Invoice *</label>
                                    <select
                                        value={paymentForm.invoiceId}
                                        onChange={(event) =>
                                            handlePaymentInvoiceChange(event.target.value)
                                        }
                                        required
                                    >
                                        <option value="">Select Invoice</option>
                                        {payableInvoices.map((invoice) => (
                                            <option
                                                key={invoice.invoiceId}
                                                value={invoice.invoiceId}
                                            >
                                                {invoice.invoiceNumber} - {invoice.patientName}
                                                {' - Balance: '}
                                                {formatCurrency(invoice.balanceAmount)}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                <div className="billing-form-group">
                                    <label>Payment Amount (₹) *</label>
                                    <input
                                        type="number"
                                        min="0.01"
                                        max={selectedPaymentInvoice?.balanceAmount || undefined}
                                        step="0.01"
                                        placeholder="Enter payment amount"
                                        value={paymentForm.amount}
                                        onChange={(event) =>
                                            setPaymentForm((previous) => ({
                                                ...previous,
                                                amount: event.target.value,
                                            }))
                                        }
                                        required
                                    />
                                </div>

                                <div className="billing-form-group">
                                    <label>Payment Method *</label>
                                    <select
                                        value={paymentForm.paymentMethod}
                                        onChange={(event) =>
                                            setPaymentForm((previous) => ({
                                                ...previous,
                                                paymentMethod: event.target.value,
                                            }))
                                        }
                                        required
                                    >
                                        <option value="CASH">Cash</option>
                                        <option value="CARD">Card</option>
                                        <option value="UPI">UPI</option>
                                        <option value="BANK_TRANSFER">Bank Transfer</option>
                                        <option value="CHEQUE">Cheque</option>
                                    </select>
                                </div>

                                <div className="billing-form-group">
                                    <label>Transaction Reference</label>
                                    <input
                                        type="text"
                                        maxLength="100"
                                        placeholder="Optional transaction reference"
                                        value={paymentForm.transactionReference}
                                        onChange={(event) =>
                                            setPaymentForm((previous) => ({
                                                ...previous,
                                                transactionReference: event.target.value,
                                            }))
                                        }
                                    />
                                </div>
                            </div>

                            {selectedPaymentInvoice && (
                                <div className="billing-total-box">
                                    <p>
                                        Invoice Total:{' '}
                                        <strong>
                                            {formatCurrency(selectedPaymentInvoice.totalAmount)}
                                        </strong>
                                    </p>
                                    <p>
                                        Already Paid:{' '}
                                        <strong>
                                            {formatCurrency(selectedPaymentInvoice.paidAmount)}
                                        </strong>
                                    </p>
                                    <h3>
                                        Remaining Balance:{' '}
                                        {formatCurrency(selectedPaymentInvoice.balanceAmount)}
                                    </h3>
                                </div>
                            )}

                            <div className="billing-form-group billing-notes-group">
                                <label>Payment Notes</label>
                                <textarea
                                    rows="3"
                                    maxLength="2000"
                                    placeholder="Enter payment notes (optional)"
                                    value={paymentForm.notes}
                                    onChange={(event) =>
                                        setPaymentForm((previous) => ({
                                            ...previous,
                                            notes: event.target.value,
                                        }))
                                    }
                                />
                            </div>

                            <div className="billing-form-actions">
                                <button
                                    type="button"
                                    className="billing-cancel-btn"
                                    onClick={() => {
                                        setShowPaymentForm(false)
                                        resetPaymentForm()
                                        setError('')
                                    }}
                                    disabled={paymentSaving}
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className="billing-create-btn"
                                    disabled={paymentSaving || !selectedPaymentInvoice}
                                >
                                    {paymentSaving ? 'Recording...' : 'Record Payment'}
                                </button>
                            </div>
                        </form>
                    )}
                </section>
            )}


            {/* Summary Cards */}
            <div className="billing-summary">

                <div className="billing-summary-card">
                    <span>Total Invoices</span>
                    <h3>{invoices.length}</h3>
                </div>

                <div className="billing-summary-card">
                    <span>Total Payments</span>
                    <h3>{payments.length}</h3>
                </div>

                <div className="billing-summary-card">
                    <span>Total Invoiced</span>
                    <h3>
                        {formatCurrency(
                            invoices.reduce(
                                (total, invoice) =>
                                    total + Number(invoice.totalAmount || 0),
                                0
                            )
                        )}
                    </h3>
                </div>

                <div className="billing-summary-card">
                    <span>Total Failed Amount</span>
                    <h3>
                        {formatCurrency(
                            payments
                                .filter((payment) => payment.paymentStatus === 'FAILED')
                                .reduce(
                                    (total, payment) =>
                                        total + Number(payment.amount || 0),
                                    0
                                )
                        )}
                    </h3>
                </div>

                <div className="billing-summary-card">
                    <span>Total Refunded Amount</span>
                    <h3>
                        {formatCurrency(
                            payments
                                .filter((payment) => payment.paymentStatus === 'REFUNDED')
                                .reduce(
                                    (total, payment) =>
                                        total + Number(payment.amount || 0),
                                    0
                                )
                        )}
                    </h3>
                </div>

                <div className="billing-summary-card">
                    <span>Total Collected</span>
                    <h3>
                        {formatCurrency(
                            payments
                                .filter((payment) => payment.paymentStatus === 'SUCCESS')
                                .reduce(
                                    (total, payment) =>
                                        total + Number(payment.amount || 0),
                                    0
                                )
                        )}
                    </h3>
                </div>

            </div>

            {/* Invoices Table */}
            <section className="billing-section">
                <h3>Invoices</h3>

                <div className="billing-table-wrapper">
                    <table className="billing-table">
                        <thead>
                            <tr>
                                <th>Invoice No.</th>
                                <th>Patient</th>
                                <th>Date</th>
                                <th>Total Amount</th>
                                <th>Paid</th>
                                <th>Balance</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>

                        <tbody>
                            {invoices.length > 0 ? (
                                invoices.map((invoice) => (
                                    <tr key={invoice.invoiceId}>
                                        <td>{invoice.invoiceNumber}</td>
                                        <td>{invoice.patientName}</td>
                                        <td>
                                            {invoice.invoiceDate
                                                ? new Date(invoice.invoiceDate).toLocaleDateString('en-IN')
                                                : '-'}
                                        </td>
                                        <td>{formatCurrency(invoice.totalAmount)}</td>
                                        <td>{formatCurrency(invoice.paidAmount)}</td>
                                        <td>{formatCurrency(invoice.balanceAmount)}</td>
                                        <td>
                                            <span className={`billing-status ${getStatusClass(invoice.invoiceStatus)}`}>
                                                {invoice.invoiceStatus?.replaceAll('_', ' ')}
                                            </span>
                                        </td>
                                        <td>
                                            {invoice.invoiceStatus === 'DRAFT' ? (
                                                <button
                                                    className="billing-create-btn"
                                                    onClick={() => handleIssueInvoice(invoice.invoiceId)}
                                                    disabled={issuingInvoiceId !== null}
                                                >
                                                    {issuingInvoiceId === invoice.invoiceId
                                                        ? 'Issuing...'
                                                        : 'Issue Invoice'}
                                                </button>
                                            ) : (
                                                '-'
                                            )}
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="8" className="billing-empty">
                                        No invoices found.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </section>

            {/* Recent Payments Table */}
            <section className="billing-section">
                <h3>Recent Payments</h3>


                <div className="billing-table-wrapper">
                    <table className="billing-table">
                        <thead>
                            <tr>
                                <th>Payment No.</th>
                                <th>Invoice No.</th>
                                <th>Patient</th>
                                <th>Payment Date</th>
                                <th>Method</th>
                                <th>Amount</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>

                        <tbody>
                            {payments.length > 0 ? (
                                payments.map((payment) => (
                                    <tr key={payment.paymentId}>
                                        <td>{payment.paymentNumber}</td>
                                        <td>{payment.invoiceNumber}</td>
                                        <td>{payment.patientName}</td>
                                        <td>
                                            {payment.paymentDate
                                                ? new Date(payment.paymentDate).toLocaleDateString('en-IN')
                                                : '-'}
                                        </td>
                                        <td>{payment.paymentMethod?.replaceAll('_', ' ')}</td>
                                        <td>{formatCurrency(payment.amount)}</td>
                                        <td>
                                            <span className={`billing-status ${getStatusClass(payment.paymentStatus)}`}>
                                                {payment.paymentStatus}
                                            </span>
                                        </td>

                                        <td>
                                            {payment.paymentStatus === 'PENDING' ? (
                                                <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                                                    <button
                                                        className="billing-create-btn"
                                                        onClick={() => handleConfirmPayment(payment.paymentId)}
                                                        disabled={confirmingPaymentId !== null || processingPaymentId !== null}
                                                    >
                                                        {confirmingPaymentId === payment.paymentId
                                                            ? 'Confirming...'
                                                            : 'Confirm Payment'}
                                                    </button>

                                                    <button
                                                        className="billing-cancel-btn"
                                                        onClick={() => handleFailPayment(payment.paymentId)}
                                                        disabled={confirmingPaymentId !== null || processingPaymentId !== null}
                                                    >
                                                        {processingPaymentId === payment.paymentId &&
                                                            processingPaymentAction === 'FAIL'
                                                            ? 'Failing...'
                                                            : 'Fail Payment'}
                                                    </button>
                                                </div>
                                            ) : payment.paymentStatus === 'SUCCESS' ? (
                                                <button
                                                    className="billing-cancel-btn"
                                                    onClick={() => handleRefundPayment(payment.paymentId)}
                                                    disabled={confirmingPaymentId !== null || processingPaymentId !== null}
                                                >
                                                    {processingPaymentId === payment.paymentId &&
                                                        processingPaymentAction === 'REFUND'
                                                        ? 'Refunding...'
                                                        : 'Refund Payment'}
                                                </button>
                                            ) : (
                                                '-'
                                            )}
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="8" className="billing-empty">
                                        No payments found.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

            </section>

        </div>
    )
}

export default Billing

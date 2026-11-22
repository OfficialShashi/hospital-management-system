
import { useEffect, useMemo, useState } from 'react'
import './Laboratory.css'

import {
    getAllLabTests,
    createLabTest,
    updateLabTest,
    updateLabTestStatus,
    getAllLabOrders,
    getMyLabOrders,
    createLabOrder,
    createLabOrderByDoctor,
    collectLabSample,
    startLabProcessing,
    completeLabTest,
    cancelLabOrder,
} from '../services/laboratoryService'

import { getAppointmentsByPatient } from '../services/appointmentService'

import {
    getAllPatients,
    getMyDoctorPatients,
} from '../services/patientService'

import {
    getAllDoctors,
    getMyDoctorProfile,
} from '../services/doctorService'

function Laboratory() {
    const user = JSON.parse(localStorage.getItem('user') || '{}')

    // Support roles, role, userRole and authorities formats
    const roleValues = [
        ...(Array.isArray(user.roles) ? user.roles : [user.roles]),
        user.role,
        user.userRole,
        ...(Array.isArray(user.authorities) ? user.authorities : []),
    ]

    const roles = roleValues
        .filter(Boolean)
        .map((role) => {
            const value =
                typeof role === 'string'
                    ? role
                    : role.authority || role.name || ''

            return value.replace(/^ROLE_/, '').toUpperCase()
        })

    const isAdmin = roles.includes('ADMIN')
    const isLabTechnician = roles.includes('LAB_TECHNICIAN')
    const isDoctor = roles.includes('DOCTOR')

    const canViewOrders = isAdmin || isLabTechnician || isDoctor

    const [activeTab, setActiveTab] = useState('tests')
    const [doctorProfile, setDoctorProfile] = useState(null)

    const [tests, setTests] = useState([])
    const [orders, setOrders] = useState([])
    const [patients, setPatients] = useState([])
    const [doctors, setDoctors] = useState([])

    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)

    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    const [testSearch, setTestSearch] = useState('')
    const [testStatusFilter, setTestStatusFilter] = useState('ALL')

    const [orderSearch, setOrderSearch] = useState('')
    const [orderStatusFilter, setOrderStatusFilter] = useState('ALL')

    const [showTestForm, setShowTestForm] = useState(false)
    const [editingTestId, setEditingTestId] = useState(null)

    const [appointments, setAppointments] = useState([])
    const [loadingAppointments, setLoadingAppointments] = useState(false)

    const [testForm, setTestForm] = useState({
        testCode: '',
        testName: '',
        category: '',
        description: '',
        price: '',
        sampleType: '',
        normalRange: '',
        unit: '',
        preparationInstructions: '',
    })

    const [showOrderForm, setShowOrderForm] = useState(false)

    const [orderForm, setOrderForm] = useState({
        patientId: '',
        doctorId: '',
        appointmentId: '',
        priority: 'ROUTINE',
        clinicalNotes: '',
        testIds: [],
    })

    const [resultForms, setResultForms] = useState({})
    const [activeResultItem, setActiveResultItem] = useState(null)

    const getErrorMessage = (err, fallback) => {
        return (
            err.response?.data?.message ||
            err.response?.data?.error ||
            fallback
        )
    }

    // Load laboratory data according to logged-in role
    const loadData = async () => {
        setLoading(true)
        setError('')

        try {
            const testPromise = getAllLabTests()

            let orderPromise = Promise.resolve([])

            if (isAdmin || isLabTechnician) {
                orderPromise = getAllLabOrders()
            } else if (isDoctor) {
                orderPromise = getMyLabOrders()
            }

            const patientPromise = isDoctor
                ? getMyDoctorPatients()
                : isAdmin
                    ? getAllPatients()
                    : Promise.resolve([])

            const doctorPromise = isDoctor
                ? getMyDoctorProfile()
                : isAdmin
                    ? getAllDoctors()
                    : Promise.resolve(null)

            const [
                testData,
                orderData,
                patientData,
                doctorData,
            ] = await Promise.all([
                testPromise,
                orderPromise,
                patientPromise,
                doctorPromise,
            ])

            setTests(Array.isArray(testData) ? testData : [])
            setOrders(Array.isArray(orderData) ? orderData : [])
            setPatients(Array.isArray(patientData) ? patientData : [])

            if (isDoctor) {
                setDoctorProfile(doctorData)
                setDoctors([])
            } else {
                setDoctors(Array.isArray(doctorData) ? doctorData : [])
            }

        } catch (err) {
            console.error('Laboratory loading error:', err)

            setError(
                getErrorMessage(err, 'Unable to load laboratory data.')
            )
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadData()
    }, [])

    const filteredTests = useMemo(() => {
        return tests.filter((test) => {
            const search = testSearch.toLowerCase()

            const matchesSearch =
                !search ||
                test.testName?.toLowerCase().includes(search) ||
                test.testCode?.toLowerCase().includes(search) ||
                test.category?.toLowerCase().includes(search)

            const matchesStatus =
                testStatusFilter === 'ALL' ||
                test.status === testStatusFilter

            return matchesSearch && matchesStatus
        })
    }, [tests, testSearch, testStatusFilter])

    const filteredOrders = useMemo(() => {
        return orders.filter((order) => {
            const search = orderSearch.toLowerCase()

            const matchesSearch =
                !search ||
                order.orderNumber?.toLowerCase().includes(search) ||
                order.patientName?.toLowerCase().includes(search) ||
                order.doctorName?.toLowerCase().includes(search)

            const matchesStatus =
                orderStatusFilter === 'ALL' ||
                order.status === orderStatusFilter

            return matchesSearch && matchesStatus
        })
    }, [orders, orderSearch, orderStatusFilter])

    const resetTestForm = () => {
        setTestForm({
            testCode: '',
            testName: '',
            category: '',
            description: '',
            price: '',
            sampleType: '',
            normalRange: '',
            unit: '',
            preparationInstructions: '',
        })

        setEditingTestId(null)
        setShowTestForm(false)
    }

    const handleAddNewTest = async () => {
        setError('')
        setSuccess('')
        setEditingTestId(null)

        try {
            const latestTests = await getAllLabTests()

            const highestNumber = (
                Array.isArray(latestTests) ? latestTests : []
            ).reduce((max, test) => {
                const match = String(test.testCode || '')
                    .trim()
                    .match(/^LAB-(\d+)$/i)

                return match
                    ? Math.max(max, Number(match[1]))
                    : max
            }, 0)

            const nextTestCode =
                `LAB-${String(highestNumber + 1).padStart(3, '0')}`

            setTestForm({
                testCode: nextTestCode,
                testName: '',
                category: '',
                description: '',
                price: '',
                sampleType: '',
                normalRange: '',
                unit: '',
                preparationInstructions: '',
            })

            setShowTestForm(true)
        } catch (err) {
            setError(
                getErrorMessage(err, 'Unable to generate test code.')
            )
        }
    }

    const handleTestInput = (e) => {
        const { name, value } = e.target

        setTestForm((previous) => ({
            ...previous,
            [name]: value,
        }))
    }

    const handleEditTest = (test) => {
        setError('')
        setSuccess('')

        setEditingTestId(test.testId)

        setTestForm({
            testCode: test.testCode || '',
            testName: test.testName || '',
            category: test.category || '',
            description: test.description || '',
            price: test.price ?? '',
            sampleType: test.sampleType || '',
            normalRange: test.normalRange || '',
            unit: test.unit || '',
            preparationInstructions: test.preparationInstructions || '',
        })

        setShowTestForm(true)
        window.scrollTo({ top: 0, behavior: 'smooth' })
    }

    const handleTestSubmit = async (e) => {
        e.preventDefault()

        setError('')
        setSuccess('')

        if (!testForm.testCode.trim() || !testForm.testName.trim()) {
            setError('Test code and test name are required.')
            return
        }

        if (
            testForm.price === '' ||
            !Number.isFinite(Number(testForm.price)) ||
            Number(testForm.price) < 0
        ) {
            setError('Please enter a valid test price.')
            return
        }

        const payload = {
            testCode: testForm.testCode.trim(),
            testName: testForm.testName.trim(),
            category: testForm.category.trim(),
            description: testForm.description.trim(),
            price: Number(testForm.price),
            sampleType: testForm.sampleType.trim(),
            normalRange: testForm.normalRange.trim(),
            unit: testForm.unit.trim(),
            preparationInstructions:
                testForm.preparationInstructions.trim(),
        }

        setSaving(true)

        try {
            if (editingTestId) {
                await updateLabTest(editingTestId, payload)
                setSuccess('Lab test updated successfully.')
            } else {
                await createLabTest(payload)
                setSuccess('New lab test created successfully.')
            }

            resetTestForm()
            await loadData()
        } catch (err) {
            setError(
                getErrorMessage(err, 'Unable to save lab test.')
            )
        } finally {
            setSaving(false)
        }
    }

    const handleTestStatus = async (test) => {
        const newStatus =
            test.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'

        const confirmed = window.confirm(
            `Are you sure you want to change this test status to ${newStatus}?`
        )

        if (!confirmed) return

        setError('')
        setSuccess('')
        setSaving(true)

        try {
            await updateLabTestStatus(test.testId, newStatus)

            setSuccess(
                `Lab test status changed to ${newStatus}.`
            )

            await loadData()
        } catch (err) {
            setError(
                getErrorMessage(err, 'Unable to update test status.')
            )
        } finally {
            setSaving(false)
        }
    }

    // Patient selection par completed appointments load karna
    const handleOrderInput = async (e) => {
        const { name, value } = e.target

        if (name === 'patientId') {
            setOrderForm((previous) => ({
                ...previous,
                patientId: value,
                appointmentId: '',
            }))

            setAppointments([])
            setError('')

            if (!value) {
                return
            }

            setLoadingAppointments(true)

            try {
                const data = await getAppointmentsByPatient(value)

                const eligibleAppointments = (
                    Array.isArray(data) ? data : []
                ).filter(
                    (appointment) =>
                        appointment.appointmentStatus === 'COMPLETED'
                )

                setAppointments(eligibleAppointments)

                if (eligibleAppointments.length === 1) {
                    setOrderForm((previous) => ({
                        ...previous,
                        appointmentId: String(
                            eligibleAppointments[0].appointmentId
                        ),
                    }))
                }

            } catch (err) {
                setError(
                    getErrorMessage(
                        err,
                        'Unable to load patient appointments.'
                    )
                )
            } finally {
                setLoadingAppointments(false)
            }

            return
        }

        setOrderForm((previous) => ({
            ...previous,
            [name]: value,
        }))
    }

    const handleTestSelection = (testId) => {
        setOrderForm((previous) => {
            const alreadySelected =
                previous.testIds.includes(testId)

            return {
                ...previous,
                testIds: alreadySelected
                    ? previous.testIds.filter((id) => id !== testId)
                    : [...previous.testIds, testId],
            }
        })
    }

    const resetOrderForm = () => {
        setOrderForm({
            patientId: '',
            doctorId: '',
            appointmentId: '',
            priority: 'ROUTINE',
            clinicalNotes: '',
            testIds: [],
        })

        setAppointments([])
        setLoadingAppointments(false)
        setShowOrderForm(false)
    }

    const handleOrderSubmit = async (e) => {
        e.preventDefault()

        setError('')
        setSuccess('')

        if (!orderForm.patientId) {
            setError('Please select a patient.')
            return
        }

        if (isAdmin && !orderForm.doctorId) {
            setError('Please select a doctor.')
            return
        }

        if (isDoctor && !doctorProfile?.doctorId) {
            setError(
                'Doctor profile not found. Please login again.'
            )
            return
        }

        if (isDoctor && !orderForm.appointmentId) {
            setError('Please select a completed appointment.')
            return
        }

        if (orderForm.testIds.length === 0) {
            setError(
                'Please select at least one laboratory test.'
            )
            return
        }

        const payload = {
            patientId: Number(orderForm.patientId),

            doctorId: isDoctor
                ? Number(doctorProfile.doctorId)
                : Number(orderForm.doctorId),

            priority: orderForm.priority,
            clinicalNotes: orderForm.clinicalNotes.trim(),

            items: orderForm.testIds.map((id) => ({
                testId: Number(id),
            })),
        }

        if (orderForm.appointmentId) {
            const appointmentId = Number(orderForm.appointmentId)

            if (
                !Number.isInteger(appointmentId) ||
                appointmentId <= 0
            ) {
                setError('Please select a valid appointment.')
                return
            }

            payload.appointmentId = appointmentId
        }

        setSaving(true)

        try {
            const createdOrder = isDoctor
                ? await createLabOrderByDoctor(payload)
                : await createLabOrder(payload)

            setSuccess(
                `Lab order created successfully. Order Number: ${createdOrder.orderNumber}`
            )

            resetOrderForm()
            await loadData()

        } catch (err) {
            setError(
                getErrorMessage(err, 'Unable to create lab order.')
            )
        } finally {
            setSaving(false)
        }
    }

    const handleCollectSample = async (itemId) => {
        setError('')
        setSuccess('')
        setSaving(true)

        try {
            await collectLabSample(itemId)

            setSuccess('Sample collected successfully.')
            await loadData()
        } catch (err) {
            setError(
                getErrorMessage(err, 'Unable to collect sample.')
            )
        } finally {
            setSaving(false)
        }
    }

    const handleStartProcessing = async (itemId) => {
        setError('')
        setSuccess('')
        setSaving(true)

        try {
            await startLabProcessing(itemId)

            setSuccess(
                'Laboratory processing started successfully.'
            )

            await loadData()
        } catch (err) {
            setError(
                getErrorMessage(err, 'Unable to start processing.')
            )
        } finally {
            setSaving(false)
        }
    }

    const openResultForm = (item) => {
        setError('')
        setActiveResultItem(item.labOrderItemId)

        setResultForms((previous) => ({
            ...previous,
            [item.labOrderItemId]:
                previous[item.labOrderItemId] || {
                    resultValue: '',
                    resultUnit: item.resultUnit || '',
                    referenceRange: item.referenceRange || '',
                    resultNotes: item.resultNotes || '',
                },
        }))
    }

    const handleResultInput = (itemId, e) => {
        const { name, value } = e.target

        setResultForms((previous) => ({
            ...previous,
            [itemId]: {
                ...(previous[itemId] || {}),
                [name]: value,
            },
        }))
    }

    const handleResultSubmit = async (e, itemId) => {
        e.preventDefault()

        const result = resultForms[itemId]

        if (!result?.resultValue?.trim()) {
            setError('Result value is required.')
            return
        }

        const payload = {
            resultValue: result.resultValue.trim(),
            resultUnit: result.resultUnit.trim(),
            referenceRange: result.referenceRange.trim(),
            resultNotes: result.resultNotes.trim(),
        }

        setError('')
        setSuccess('')
        setSaving(true)

        try {
            await completeLabTest(itemId, payload)

            setSuccess('Test result submitted successfully.')
            setActiveResultItem(null)

            await loadData()
        } catch (err) {
            setError(
                getErrorMessage(err, 'Unable to submit test result.')
            )
        } finally {
            setSaving(false)
        }
    }

    const handleCancelOrder = async (order) => {
        const confirmed = window.confirm(
            `Are you sure you want to cancel order ${order.orderNumber}?`
        )

        if (!confirmed) return

        setError('')
        setSuccess('')
        setSaving(true)

        try {
            await cancelLabOrder(order.labOrderId)

            setSuccess('Lab order cancelled successfully.')
            await loadData()
        } catch (err) {
            setError(
                getErrorMessage(err, 'Unable to cancel lab order.')
            )
        } finally {
            setSaving(false)
        }
    }

    const formatDate = (date) => {
        if (!date) return '—'

        const parsedDate = new Date(date)

        if (Number.isNaN(parsedDate.getTime())) {
            return '—'
        }

        return parsedDate.toLocaleString('en-IN')
    }

    const getStatusClass = (status) => {
        return `lab-status-badge ${status?.toLowerCase() || 'unknown'}`
    }

    if (loading) {
        return (
            <section className="laboratory-page">
                <p className="lab-message">
                    Loading laboratory data...
                </p>
            </section>
        )
    }

    return (
        <section className="laboratory-page">

            <div className="laboratory-header">
                <div>
                    <h2>Laboratory Management</h2>

                    <p className="section-description">
                        Manage laboratory tests, orders, sample collection and test results.
                    </p>
                </div>

                <div className="laboratory-actions">

                    {isAdmin && activeTab === 'tests' && (
                        <button
                            className="lab-primary-btn"
                            onClick={handleAddNewTest}
                        >
                            + Add New Test
                        </button>
                    )}

                    {(isAdmin || isDoctor) && activeTab === 'orders' && (
                        <button
                            className="lab-primary-btn"
                            onClick={() => {
                                setError('')
                                setSuccess('')
                                setShowOrderForm(true)
                            }}
                        >
                            + Create Lab Order
                        </button>
                    )}

                </div>
            </div>

            {error && (
                <div className="lab-message lab-error">
                    {error}
                </div>
            )}

            {success && (
                <div className="lab-message lab-success">
                    {success}
                </div>
            )}

            <div className="lab-tabs">

                <button
                    className={activeTab === 'tests' ? 'active' : ''}
                    onClick={() => {
                        setActiveTab('tests')
                        setError('')
                        setSuccess('')
                    }}
                >
                    Lab Tests ({tests.length})
                </button>

                {canViewOrders && (
                    <button
                        className={activeTab === 'orders' ? 'active' : ''}
                        onClick={() => {
                            setActiveTab('orders')
                            setError('')
                            setSuccess('')
                        }}
                    >
                        Lab Orders ({orders.length})
                    </button>
                )}

            </div>

            {/* ==================== LAB TESTS ==================== */}

            {activeTab === 'tests' && (
                <>

                    {showTestForm && isAdmin && (
                        <div className="lab-card">

                            <h3>
                                {editingTestId
                                    ? 'Update Laboratory Test'
                                    : 'Add New Laboratory Test'}
                            </h3>

                            <form onSubmit={handleTestSubmit}>

                                <div className="lab-form-grid">

                                    <div className="lab-form-group">
                                        <label>Test Code *</label>

                                        <input
                                            name="testCode"
                                            value={testForm.testCode}
                                            onChange={handleTestInput}
                                            required
                                            maxLength={50}
                                        />
                                    </div>

                                    <div className="lab-form-group">
                                        <label>Test Name *</label>

                                        <input
                                            name="testName"
                                            value={testForm.testName}
                                            onChange={handleTestInput}
                                            maxLength={150}
                                            required
                                        />
                                    </div>

                                    <div className="lab-form-group">
                                        <label>Category *</label>

                                        <select
                                            name="category"
                                            value={testForm.category}
                                            onChange={handleTestInput}
                                            required
                                        >
                                            <option value="">
                                                Select Category
                                            </option>

                                            <option value="Hematology">
                                                Hematology
                                            </option>

                                            <option value="Biochemistry">
                                                Biochemistry
                                            </option>

                                            <option value="Microbiology">
                                                Microbiology
                                            </option>

                                            <option value="Immunology">
                                                Immunology
                                            </option>

                                            <option value="Serology">
                                                Serology
                                            </option>

                                            <option value="Clinical Pathology">
                                                Clinical Pathology
                                            </option>

                                            <option value="Histopathology">
                                                Histopathology
                                            </option>

                                            <option value="Other">
                                                Other
                                            </option>
                                        </select>
                                    </div>

                                    <div className="lab-form-group">
                                        <label>Price (₹) *</label>

                                        <input
                                            type="number"
                                            name="price"
                                            value={testForm.price}
                                            onChange={handleTestInput}
                                            min="0"
                                            step="0.01"
                                            required
                                        />
                                    </div>

                                    <div className="lab-form-group">
                                        <label>Sample Type</label>

                                        <input
                                            name="sampleType"
                                            value={testForm.sampleType}
                                            onChange={handleTestInput}
                                            maxLength={100}
                                        />
                                    </div>

                                    <div className="lab-form-group">
                                        <label>Normal Range</label>

                                        <input
                                            name="normalRange"
                                            value={testForm.normalRange}
                                            onChange={handleTestInput}
                                            maxLength={200}
                                        />
                                    </div>

                                    <div className="lab-form-group">
                                        <label>Unit</label>

                                        <input
                                            name="unit"
                                            value={testForm.unit}
                                            onChange={handleTestInput}
                                            maxLength={50}
                                        />
                                    </div>

                                    <div className="lab-form-group">
                                        <label>Description</label>

                                        <textarea
                                            name="description"
                                            value={testForm.description}
                                            onChange={handleTestInput}
                                            rows={3}
                                        />
                                    </div>

                                    <div className="lab-form-group">
                                        <label>Preparation Instructions</label>

                                        <textarea
                                            name="preparationInstructions"
                                            value={testForm.preparationInstructions}
                                            onChange={handleTestInput}
                                            rows={3}
                                        />
                                    </div>

                                </div>

                                <div className="lab-form-actions">

                                    <button
                                        type="submit"
                                        className="lab-primary-btn"
                                        disabled={saving}
                                    >
                                        {saving
                                            ? 'Saving...'
                                            : editingTestId
                                                ? 'Update Test'
                                                : 'Create Test'}
                                    </button>

                                    <button
                                        type="button"
                                        className="lab-secondary-btn"
                                        onClick={resetTestForm}
                                        disabled={saving}
                                    >
                                        Close
                                    </button>

                                </div>

                            </form>
                        </div>
                    )}

                    <div className="lab-visual-banner">

                        <div className="lab-banner-content">

                            <span className="lab-banner-tag">
                                DIAGNOSTIC CENTER
                            </span>

                            <h2>Advanced Laboratory Services</h2>

                            <p>
                                Accurate diagnostics, reliable test results and quality patient care.
                            </p>

                            <div className="lab-banner-stats">

                                <div>
                                    <strong>{tests.length}</strong>
                                    <span>Total Tests</span>
                                </div>

                                <div>
                                    <strong>
                                        {tests.filter(
                                            (test) => test.status === 'ACTIVE'
                                        ).length}
                                    </strong>

                                    <span>Active Tests</span>
                                </div>

                                <div>
                                    <strong>{orders.length}</strong>
                                    <span>Lab Orders</span>
                                </div>

                            </div>
                        </div>
                    </div>

                    <div className="lab-category-section">

                        <h3>Laboratory Test Categories</h3>

                        <div className="lab-category-grid">

                            {[
                                {
                                    name: 'Hematology',
                                    icon: '🩸',
                                    description: 'Blood analysis and cell counts',
                                },
                                {
                                    name: 'Biochemistry',
                                    icon: '🧪',
                                    description: 'Chemical analysis of body fluids',
                                },
                                {
                                    name: 'Microbiology',
                                    icon: '🔬',
                                    description: 'Microorganisms and cultures',
                                },
                                {
                                    name: 'Immunology',
                                    icon: '🧬',
                                    description: 'Immune system investigations',
                                },
                            ].map((category) => (
                                <div
                                    className="lab-category-card"
                                    key={category.name}
                                >
                                    <span className="lab-category-icon">
                                        {category.icon}
                                    </span>

                                    <div>
                                        <h4>{category.name}</h4>
                                        <p>{category.description}</p>
                                    </div>
                                </div>
                            ))}

                        </div>
                    </div>

                    <div className="lab-toolbar">

                        <input
                            className="lab-search-input"
                            placeholder="Search by test name, code or category..."
                            value={testSearch}
                            onChange={(e) => setTestSearch(e.target.value)}
                        />

                        <select
                            className="lab-filter-select"
                            value={testStatusFilter}
                            onChange={(e) => setTestStatusFilter(e.target.value)}
                        >
                            <option value="ALL">All Status</option>
                            <option value="ACTIVE">Active</option>
                            <option value="INACTIVE">Inactive</option>
                        </select>

                    </div>

                    <div className="lab-card">

                        <div className="lab-table-wrapper">

                            <table className="lab-table">

                                <thead>
                                    <tr>
                                        <th>Test Code</th>
                                        <th>Test Name</th>
                                        <th>Category</th>
                                        <th>Sample Type</th>
                                        <th>Normal Range</th>
                                        <th>Price</th>
                                        <th>Status</th>
                                        {isAdmin && <th>Actions</th>}
                                    </tr>
                                </thead>

                                <tbody>

                                    {filteredTests.map((test) => (
                                        <tr key={test.testId}>

                                            <td>{test.testCode}</td>
                                            <td>{test.testName}</td>
                                            <td>{test.category || '—'}</td>
                                            <td>{test.sampleType || '—'}</td>
                                            <td>{test.normalRange || '—'}</td>

                                            <td>
                                                ₹{Number(test.price || 0).toFixed(2)}
                                            </td>

                                            <td>
                                                <span className={getStatusClass(test.status)}>
                                                    {test.status}
                                                </span>
                                            </td>

                                            {isAdmin && (
                                                <td>
                                                    <div className="lab-form-actions">

                                                        <button
                                                            className="lab-action-btn"
                                                            onClick={() => handleEditTest(test)}
                                                            disabled={saving}
                                                        >
                                                            Edit
                                                        </button>

                                                        <button
                                                            className={
                                                                test.status === 'ACTIVE'
                                                                    ? 'lab-danger-btn'
                                                                    : 'lab-secondary-btn'
                                                            }
                                                            onClick={() => handleTestStatus(test)}
                                                            disabled={saving}
                                                        >
                                                            {test.status === 'ACTIVE'
                                                                ? 'Deactivate'
                                                                : 'Activate'}
                                                        </button>

                                                    </div>
                                                </td>
                                            )}

                                        </tr>
                                    ))}

                                    {filteredTests.length === 0 && (
                                        <tr>
                                            <td colSpan={isAdmin ? 8 : 7}>
                                                <div className="lab-empty-state">
                                                    No laboratory tests found.
                                                </div>
                                            </td>
                                        </tr>
                                    )}

                                </tbody>

                            </table>

                        </div>
                    </div>

                </>
            )}

            {/* ==================== LAB ORDERS ==================== */}

            {activeTab === 'orders' && canViewOrders && (
                <>

                    {showOrderForm && (isAdmin || isDoctor) && (
                        <div className="lab-card">

                            <h3>
                                {isDoctor
                                    ? 'Create Lab Order for Patient'
                                    : 'Create Laboratory Order'}
                            </h3>

                            <form onSubmit={handleOrderSubmit}>

                                <div className="lab-form-grid">

                                    <div className="lab-form-group">

                                        <label>Select Patient *</label>

                                        <select
                                            name="patientId"
                                            value={orderForm.patientId}
                                            onChange={handleOrderInput}
                                            required
                                        >
                                            <option value="">
                                                Select Patient
                                            </option>

                                            {patients
                                                .filter(
                                                    (patient) =>
                                                        patient.accountStatus === 'ACTIVE'
                                                )
                                                .map((patient) => (
                                                    <option
                                                        key={patient.patientId}
                                                        value={patient.patientId}
                                                    >
                                                        {patient.fullName} ({patient.patientNumber})
                                                    </option>
                                                ))}

                                        </select>
                                    </div>

                                    <div className="lab-form-group">

                                        <label>
                                            Appointment {isDoctor ? '*' : '(Optional)'}
                                        </label>

                                        {isDoctor ? (
                                            <select
                                                name="appointmentId"
                                                value={orderForm.appointmentId}
                                                onChange={handleOrderInput}
                                                required
                                                disabled={
                                                    !orderForm.patientId ||
                                                    loadingAppointments
                                                }
                                            >
                                                <option value="">
                                                    {loadingAppointments
                                                        ? 'Loading appointments...'
                                                        : !orderForm.patientId
                                                            ? 'Select patient first'
                                                            : appointments.length === 0
                                                                ? 'No completed appointments'
                                                                : 'Select Appointment'}
                                                </option>

                                                {appointments.map((appointment) => (
                                                    <option
                                                        key={appointment.appointmentId}
                                                        value={appointment.appointmentId}
                                                    >
                                                        {appointment.appointmentNumber}
                                                        {' | '}
                                                        {formatDate(appointment.appointmentDateTime)}
                                                        {' | '}
                                                        {appointment.reason || 'No reason provided'}
                                                    </option>
                                                ))}

                                            </select>
                                        ) : (
                                            <input
                                                type="number"
                                                name="appointmentId"
                                                value={orderForm.appointmentId}
                                                onChange={handleOrderInput}
                                                min="1"
                                                placeholder="Enter appointment ID (optional)"
                                            />
                                        )}

                                        {isDoctor && appointments.length > 0 && (
                                            <small>
                                                Only completed appointments are shown.
                                            </small>
                                        )}

                                    </div>

                                    {isAdmin && (
                                        <div className="lab-form-group">

                                            <label>Select Doctor *</label>

                                            <select
                                                name="doctorId"
                                                value={orderForm.doctorId}
                                                onChange={handleOrderInput}
                                                required
                                            >
                                                <option value="">
                                                    Select Doctor
                                                </option>

                                                {doctors.map((doctor) => (
                                                    <option
                                                        key={doctor.doctorId}
                                                        value={doctor.doctorId}
                                                    >
                                                        {doctor.fullName}
                                                    </option>
                                                ))}

                                            </select>
                                        </div>
                                    )}

                                    <div className="lab-form-group">

                                        <label>Priority *</label>

                                        <select
                                            name="priority"
                                            value={orderForm.priority}
                                            onChange={handleOrderInput}
                                        >
                                            <option value="ROUTINE">Routine</option>
                                            <option value="URGENT">Urgent</option>
                                            <option value="STAT">STAT</option>
                                        </select>

                                    </div>

                                    <div className="lab-form-group">

                                        <label>Clinical Notes</label>

                                        <textarea
                                            name="clinicalNotes"
                                            value={orderForm.clinicalNotes}
                                            onChange={handleOrderInput}
                                            maxLength={2000}
                                            rows={3}
                                            placeholder="Enter clinical notes..."
                                        />

                                    </div>

                                </div>

                                <div className="lab-form-group">

                                    <label>Select Laboratory Tests *</label>

                                    <div className="lab-order-items">

                                        {tests
                                            .filter(
                                                (test) => test.status === 'ACTIVE'
                                            )
                                            .map((test) => (
                                                <label
                                                    className="lab-order-item"
                                                    key={test.testId}
                                                >
                                                    <input
                                                        type="checkbox"
                                                        checked={orderForm.testIds.includes(test.testId)}
                                                        onChange={() =>
                                                            handleTestSelection(test.testId)
                                                        }
                                                    />

                                                    <span>
                                                        <strong>{test.testName}</strong>
                                                        <br />

                                                        <small>
                                                            {test.testCode}
                                                            {' · '}
                                                            ₹{Number(test.price || 0).toFixed(2)}
                                                        </small>
                                                    </span>

                                                </label>
                                            ))}

                                        {tests.filter(
                                            (test) => test.status === 'ACTIVE'
                                        ).length === 0 && (
                                            <p className="lab-empty-state">
                                                No active tests available.
                                            </p>
                                        )}

                                    </div>
                                </div>

                                <div className="lab-form-actions">

                                    <button
                                        type="submit"
                                        className="lab-primary-btn"
                                        disabled={saving || loadingAppointments}
                                    >
                                        {saving
                                            ? 'Creating...'
                                            : 'Create Lab Order'}
                                    </button>

                                    <button
                                        type="button"
                                        className="lab-secondary-btn"
                                        onClick={resetOrderForm}
                                        disabled={saving}
                                    >
                                        Close
                                    </button>

                                </div>

                            </form>
                        </div>
                    )}

                    {/* ==================== LAB ORDERS LIST ==================== */}

                    <div className="lab-toolbar">

                        <input
                            className="lab-search-input"
                            placeholder="Search order number, patient or doctor..."
                            value={orderSearch}
                            onChange={(e) => setOrderSearch(e.target.value)}
                        />

                        <select
                            className="lab-filter-select"
                            value={orderStatusFilter}
                            onChange={(e) => setOrderStatusFilter(e.target.value)}
                        >
                            <option value="ALL">All Status</option>
                            <option value="ORDERED">Ordered</option>
                            <option value="SAMPLE_COLLECTED">Sample Collected</option>
                            <option value="PROCESSING">Processing</option>
                            <option value="COMPLETED">Completed</option>
                            <option value="CANCELLED">Cancelled</option>
                        </select>

                    </div>

                    {filteredOrders.length === 0 && (
                        <div className="lab-card lab-empty-state">
                            No laboratory orders found.
                        </div>
                    )}

                    {filteredOrders.map((order) => (
                        <div className="lab-card" key={order.labOrderId}>

                            <div className="laboratory-header">

                                <div>
                                    <h3>{order.orderNumber}</h3>

                                    <p>
                                        Patient: <strong>{order.patientName}</strong>
                                    </p>

                                    <p>
                                        Doctor: <strong>{order.doctorName}</strong>
                                    </p>
                                </div>

                                <div>

                                    <span className={getStatusClass(order.status)}>
                                        {order.status}
                                    </span>

                                    {isAdmin &&
                                        !['COMPLETED', 'CANCELLED'].includes(order.status) && (
                                            <button
                                                className="lab-danger-btn"
                                                onClick={() => handleCancelOrder(order)}
                                                disabled={saving}
                                            >
                                                Cancel Order
                                            </button>
                                        )}

                                </div>
                            </div>

                            <div className="lab-form-grid">

                                <div>
                                    <strong>Order Date:</strong>
                                    <p>{formatDate(order.orderDate)}</p>
                                </div>

                                <div>
                                    <strong>Priority:</strong>
                                    <p>{order.priority}</p>
                                </div>

                                <div>
                                    <strong>Clinical Notes:</strong>
                                    <p>{order.clinicalNotes || '—'}</p>
                                </div>

                            </div>

                            <div className="lab-table-wrapper">

                                <table className="lab-table">

                                    <thead>
                                        <tr>
                                            <th>Test Name</th>
                                            <th>Price</th>
                                            <th>Status</th>
                                            <th>Sample Collected</th>
                                            <th>Result</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>

                                    <tbody>

                                        {(order.items || []).map((item) => (
                                            <tr key={item.labOrderItemId}>

                                                <td>
                                                    <strong>{item.testName}</strong>
                                                    <br />
                                                    <small>{item.testCode}</small>
                                                </td>

                                                <td>
                                                    ₹{Number(item.price || 0).toFixed(2)}
                                                </td>

                                                <td>
                                                    <span className={getStatusClass(item.status)}>
                                                        {item.status}
                                                    </span>
                                                </td>

                                                <td>
                                                    {formatDate(item.sampleCollectedAt)}
                                                </td>

                                                <td>

                                                    {item.resultValue ? (
                                                        <>
                                                            <strong>{item.resultValue}</strong>

                                                            {item.resultUnit &&
                                                                ` ${item.resultUnit}`}

                                                            {item.referenceRange && (
                                                                <small>
                                                                    <br />
                                                                    Reference: {item.referenceRange}
                                                                </small>
                                                            )}
                                                        </>
                                                    ) : '—'}

                                                </td>

                                                <td>

                                                    {isLabTechnician || isAdmin ? (
                                                        <div className="lab-form-actions">

                                                            {item.status === 'ORDERED' && (
                                                                <button
                                                                    className="lab-action-btn"
                                                                    onClick={() =>
                                                                        handleCollectSample(item.labOrderItemId)
                                                                    }
                                                                    disabled={saving}
                                                                >
                                                                    Collect Sample
                                                                </button>
                                                            )}

                                                            {item.status === 'SAMPLE_COLLECTED' && (
                                                                <button
                                                                    className="lab-action-btn"
                                                                    onClick={() =>
                                                                        handleStartProcessing(item.labOrderItemId)
                                                                    }
                                                                    disabled={saving}
                                                                >
                                                                    Start Processing
                                                                </button>
                                                            )}

                                                            {item.status === 'PROCESSING' && (
                                                                <button
                                                                    className="lab-primary-btn"
                                                                    onClick={() =>
                                                                        openResultForm(item)
                                                                    }
                                                                    disabled={saving}
                                                                >
                                                                    Enter Result
                                                                </button>
                                                            )}

                                                            {item.status === 'COMPLETED' && (
                                                                <span>Completed</span>
                                                            )}

                                                            {item.status === 'CANCELLED' && (
                                                                <span>Cancelled</span>
                                                            )}

                                                        </div>
                                                    ) : (
                                                        <span>View Only</span>
                                                    )}

                                                </td>

                                            </tr>
                                        ))}

                                        {(!order.items || order.items.length === 0) && (
                                            <tr>
                                                <td colSpan={6}>
                                                    No test items available.
                                                </td>
                                            </tr>
                                        )}

                                    </tbody>

                                </table>
                            </div>

                            {/* ==================== RESULT ENTRY ==================== */}

                            {activeResultItem &&
                                order.items?.some(
                                    (item) =>
                                        item.labOrderItemId === activeResultItem
                                ) && (
                                    <div className="lab-card">

                                        <h4>
                                            Submit Test Result - {
                                                order.items.find(
                                                    (item) =>
                                                        item.labOrderItemId === activeResultItem
                                                )?.testName
                                            }
                                        </h4>

                                        <form
                                            onSubmit={(e) =>
                                                handleResultSubmit(e, activeResultItem)
                                            }
                                        >

                                            <div className="lab-form-grid">

                                                <div className="lab-form-group">
                                                    <label>Result Value *</label>

                                                    <input
                                                        name="resultValue"
                                                        value={
                                                            resultForms[activeResultItem]?.resultValue || ''
                                                        }
                                                        onChange={(e) =>
                                                            handleResultInput(activeResultItem, e)
                                                        }
                                                        required
                                                    />
                                                </div>

                                                <div className="lab-form-group">
                                                    <label>Result Unit</label>

                                                    <input
                                                        name="resultUnit"
                                                        value={
                                                            resultForms[activeResultItem]?.resultUnit || ''
                                                        }
                                                        onChange={(e) =>
                                                            handleResultInput(activeResultItem, e)
                                                        }
                                                    />
                                                </div>

                                                <div className="lab-form-group">
                                                    <label>Reference Range</label>

                                                    <input
                                                        name="referenceRange"
                                                        value={
                                                            resultForms[activeResultItem]?.referenceRange || ''
                                                        }
                                                        onChange={(e) =>
                                                            handleResultInput(activeResultItem, e)
                                                        }
                                                    />
                                                </div>

                                                <div className="lab-form-group">
                                                    <label>Result Notes</label>

                                                    <textarea
                                                        name="resultNotes"
                                                        value={
                                                            resultForms[activeResultItem]?.resultNotes || ''
                                                        }
                                                        onChange={(e) =>
                                                            handleResultInput(activeResultItem, e)
                                                        }
                                                        rows={3}
                                                    />
                                                </div>

                                            </div>

                                            <div className="lab-form-actions">

                                                <button
                                                    type="submit"
                                                    className="lab-primary-btn"
                                                    disabled={saving}
                                                >
                                                    {saving
                                                        ? 'Submitting...'
                                                        : 'Submit Result'}
                                                </button>

                                                <button
                                                    type="button"
                                                    className="lab-secondary-btn"
                                                    onClick={() =>
                                                        setActiveResultItem(null)
                                                    }
                                                    disabled={saving}
                                                >
                                                    Close
                                                </button>

                                            </div>

                                        </form>
                                    </div>
                                )}

                        </div>
                    ))}

                </>
            )}

        </section>
    )
}

export default Laboratory

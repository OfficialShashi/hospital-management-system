
import { useEffect, useMemo, useState } from 'react'
import './Pharmacy.css'
import {
    getAllMedicines,
    createMedicine,
    updateMedicine,
    updateMedicineStatus,
} from '../services/medicineService'

const initialForm = {
    medicineCode: '',
    medicineName: '',
    genericName: '',
    manufacturer: '',
    category: '',
    dosageForm: '',
    strength: '',
    unitPrice: '',
    reorderLevel: 10,
}

const generateMedicineCode = (medicineName, medicines) => {
    const prefix = (medicineName || '')
        .replace(/[^a-zA-Z]/g, '')
        .substring(0, 4)
        .toUpperCase()

    if (!prefix) return ''

    const pattern = new RegExp(`^MED-${prefix}-(\\d+)$`)

    const existingNumbers = medicines.map(medicine => {
        const match = medicine.medicineCode?.match(pattern)
        return match ? Number(match[1]) : 0
    })

    const nextNumber = Math.max(0, ...existingNumbers) + 1

    return `MED-${prefix}-${String(nextNumber).padStart(3, '0')}`
}

function Pharmacy() {
    const [medicines, setMedicines] = useState([])
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    const [search, setSearch] = useState('')
    const [categoryFilter, setCategoryFilter] = useState('ALL')
    const [statusFilter, setStatusFilter] = useState('ALL')

    const [showForm, setShowForm] = useState(false)
    const [editingId, setEditingId] = useState(null)
    const [form, setForm] = useState(initialForm)

    const [user] = useState(() => {
        try {
            return JSON.parse(localStorage.getItem('user') || '{}')
        } catch {
            return {}
        }
    })

    const roles = Array.isArray(user.roles) ? user.roles : []

    const isAdmin = roles.some(role =>
        ['ADMIN', 'ROLE_ADMIN'].includes(
            typeof role === 'string' ? role : role?.authority
        )
    )

    const generatedMedicineCode = useMemo(() => {
        if (editingId) return form.medicineCode

        return generateMedicineCode(form.medicineName, medicines)
    }, [form.medicineName, form.medicineCode, medicines, editingId])

    const loadMedicines = async () => {
        setLoading(true)
        setError('')

        try {
            const data = await getAllMedicines()
            setMedicines(Array.isArray(data) ? data : [])
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to load medicines. Please try again.'
            )
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadMedicines()
    }, [])

    const categories = useMemo(() => {
        return [...new Set(
            medicines
                .map(medicine => medicine.category)
                .filter(Boolean)
        )].sort()
    }, [medicines])

    const filteredMedicines = useMemo(() => {
        return medicines.filter(medicine => {
            const searchText = search.trim().toLowerCase()

            const matchesSearch = !searchText ||
                medicine.medicineName?.toLowerCase().includes(searchText) ||
                medicine.medicineCode?.toLowerCase().includes(searchText) ||
                medicine.genericName?.toLowerCase().includes(searchText)

            const matchesCategory =
                categoryFilter === 'ALL' ||
                medicine.category === categoryFilter

            const matchesStatus =
                statusFilter === 'ALL' ||
                medicine.status === statusFilter

            return matchesSearch && matchesCategory && matchesStatus
        })
    }, [medicines, search, categoryFilter, statusFilter])

    const handleFormChange = (event) => {
        const { name, value } = event.target

        setForm(previous => ({
            ...previous,
            [name]: value,
        }))
    }

    const openAddForm = () => {
        setEditingId(null)
        setForm(initialForm)
        setError('')
        setSuccess('')
        setShowForm(true)
    }

    const openEditForm = (medicine) => {
        setEditingId(medicine.medicineId)

        setForm({
            medicineCode: medicine.medicineCode || '',
            medicineName: medicine.medicineName || '',
            genericName: medicine.genericName || '',
            manufacturer: medicine.manufacturer || '',
            category: medicine.category || '',
            dosageForm: medicine.dosageForm || '',
            strength: medicine.strength || '',
            unitPrice: medicine.unitPrice ?? '',
            reorderLevel: medicine.reorderLevel ?? 10,
        })

        setError('')
        setSuccess('')
        setShowForm(true)
    }

    const closeForm = () => {
        if (saving) return

        setShowForm(false)
        setEditingId(null)
        setForm(initialForm)
        setError('')
    }

    const handleSubmit = async (event) => {
        event.preventDefault()

        setSaving(true)
        setError('')
        setSuccess('')

        const medicineCode = generatedMedicineCode

        if (!medicineCode) {
            setError('Please enter a valid medicine name.')
            setSaving(false)
            return
        }

        const payload = {
            ...form,
            medicineCode,
            unitPrice: Number(form.unitPrice),
            reorderLevel: Number(form.reorderLevel),
        }

        try {
            if (editingId) {
                await updateMedicine(editingId, payload)
                setSuccess('Medicine updated successfully.')
            } else {
                await createMedicine(payload)
                setSuccess('Medicine added successfully.')
            }

            setShowForm(false)
            setEditingId(null)
            setForm(initialForm)

            await loadMedicines()
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to save medicine. Please check the entered details.'
            )
        } finally {
            setSaving(false)
        }
    }

    const handleStatusChange = async (medicine) => {
        const newStatus =
            medicine.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'

        const confirmed = window.confirm(
            `Are you sure you want to ${newStatus === 'ACTIVE' ? 'activate' : 'deactivate'} this medicine?`
        )

        if (!confirmed) return

        setError('')
        setSuccess('')

        try {
            await updateMedicineStatus(medicine.medicineId, newStatus)

            setSuccess(
                `Medicine ${newStatus.toLowerCase()} successfully.`
            )

            await loadMedicines()
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to update medicine status.'
            )
        }
    }

    const activeCount = medicines.filter(
        medicine => medicine.status === 'ACTIVE'
    ).length

    const inactiveCount = medicines.filter(
        medicine => medicine.status === 'INACTIVE'
    ).length

    const lowStockCount = medicines.filter(
        medicine =>
            medicine.status === 'ACTIVE' &&
            Number(medicine.stockQuantity ?? 0) <=
            Number(medicine.reorderLevel ?? 0)
    ).length

    const categoryVisuals = [
        {
            name: 'Tablets & Capsules',
            icon: '💊',
            description: 'Oral medicines and supplements',
        },
        {
            name: 'Syrups & Liquids',
            icon: '🧴',
            description: 'Liquid medicines and formulations',
        },
        {
            name: 'Injections',
            icon: '💉',
            description: 'Injectable medical formulations',
        },
        {
            name: 'Healthcare',
            icon: '🩺',
            description: 'Essential healthcare products',
        },
    ]

    return (
        <div className="pharmacy-page">

            {/* PREMIUM PHARMACY BANNER */}

            <div className="pharmacy-header">
                <div>
                    <span className="pharmacy-banner-tag">
                        HOSPITAL PHARMACY
                    </span>

                    <h2>Pharmacy Management</h2>

                    <p>
                        Quality medicines, organized inventory and
                        reliable patient care.
                    </p>
                </div>

                <button
                    className="pharmacy-primary-btn"
                    onClick={openAddForm}
                >
                    + Add New Medicine
                </button>
            </div>

            {/* ALERTS */}

            {error && (
                <div className="pharmacy-alert pharmacy-error">
                    {error}
                </div>
            )}

            {success && (
                <div className="pharmacy-alert pharmacy-success">
                    {success}
                </div>
            )}

            {/* CATEGORY CARDS */}

            <section className="pharmacy-category-section">
                <div className="pharmacy-section-heading">
                    <div>
                        <h3>Medicine Categories</h3>
                        <p>Explore different pharmaceutical sections</p>
                    </div>
                </div>

                <div className="pharmacy-category-grid">
                    {categoryVisuals.map(category => (
                        <div
                            className="pharmacy-category-card"
                            key={category.name}
                        >
                            <span className="pharmacy-category-icon">
                                {category.icon}
                            </span>

                            <div>
                                <h4>{category.name}</h4>
                                <p>{category.description}</p>
                            </div>
                        </div>
                    ))}
                </div>
            </section>

            {/* STATISTICS */}

            <div className="pharmacy-stats">

                <div className="pharmacy-stat-card">
                    <span>Total Medicines</span>
                    <strong>{medicines.length}</strong>
                </div>

                <div className="pharmacy-stat-card">
                    <span>Active Medicines</span>
                    <strong>{activeCount}</strong>
                </div>

                <div className="pharmacy-stat-card">
                    <span>Inactive Medicines</span>
                    <strong>{inactiveCount}</strong>
                </div>

                <div className="pharmacy-stat-card">
                    <span>Low Stock</span>
                    <strong>{lowStockCount}</strong>
                </div>

            </div>

            {/* INVENTORY SECTION */}

            <div className="pharmacy-inventory-heading">
                <div>
                    <h3>Medicine Inventory</h3>
                    <p>Search, filter and manage hospital medicines.</p>
                </div>
            </div>

            {/* SEARCH AND FILTER */}

            <div className="pharmacy-toolbar">

                <input
                    type="text"
                    placeholder="Search by medicine name, code or generic name..."
                    value={search}
                    onChange={event => setSearch(event.target.value)}
                />

                <select
                    value={categoryFilter}
                    onChange={event => setCategoryFilter(event.target.value)}
                >
                    <option value="ALL">All Categories</option>

                    {categories.map(category => (
                        <option key={category} value={category}>
                            {category}
                        </option>
                    ))}
                </select>

                <select
                    value={statusFilter}
                    onChange={event => setStatusFilter(event.target.value)}
                >
                    <option value="ALL">All Status</option>
                    <option value="ACTIVE">Active</option>
                    <option value="INACTIVE">Inactive</option>
                </select>

            </div>

            {/* MEDICINE TABLE */}

            <div className="pharmacy-table-container">

                <table className="pharmacy-table">

                    <thead>
                        <tr>
                            <th>Medicine Code</th>
                            <th>Medicine Name</th>
                            <th>Category</th>
                            <th>Strength</th>
                            <th>Dosage Form</th>
                            <th>Unit Price</th>
                            <th>Reorder Level</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>

                    <tbody>

                        {loading ? (
                            <tr>
                                <td colSpan="9" className="pharmacy-empty">
                                    Loading medicines...
                                </td>
                            </tr>
                        ) : filteredMedicines.length === 0 ? (
                            <tr>
                                <td colSpan="9" className="pharmacy-empty">
                                    No medicines found.
                                </td>
                            </tr>
                        ) : (
                            filteredMedicines.map(medicine => (
                                <tr key={medicine.medicineId}>

                                    <td>{medicine.medicineCode}</td>

                                    <td>
                                        <strong>{medicine.medicineName}</strong>

                                        <small className="pharmacy-subtext">
                                            {medicine.genericName || '-'}
                                        </small>
                                    </td>

                                    <td>{medicine.category || '-'}</td>
                                    <td>{medicine.strength || '-'}</td>
                                    <td>{medicine.dosageForm || '-'}</td>

                                    <td>
                                        ₹{Number(medicine.unitPrice || 0).toFixed(2)}
                                    </td>

                                    <td>{medicine.reorderLevel ?? 0}</td>

                                    <td>
                                        <span
                                            className={`pharmacy-status ${medicine.status === 'ACTIVE' ? 'active' : 'inactive'}`}
                                        >
                                            {medicine.status}
                                        </span>
                                    </td>

                                    <td>
                                        <div className="pharmacy-actions">

                                            <button
                                                className="pharmacy-edit-btn"
                                                onClick={() => openEditForm(medicine)}
                                            >
                                                Edit
                                            </button>

                                            {isAdmin && (
                                                <button
                                                    className="pharmacy-status-btn"
                                                    onClick={() => handleStatusChange(medicine)}
                                                >
                                                    {medicine.status === 'ACTIVE'
                                                        ? 'Deactivate'
                                                        : 'Activate'}
                                                </button>
                                            )}

                                        </div>
                                    </td>

                                </tr>
                            ))
                        )}

                    </tbody>

                </table>

            </div>

            <div className="pharmacy-result-count">
                Showing {filteredMedicines.length} of {medicines.length} medicines
            </div>

            {/* ADD / EDIT MEDICINE MODAL */}

            {showForm && (
                <div
                    className="pharmacy-modal-overlay"
                    onClick={event => {
                        if (event.target === event.currentTarget) {
                            closeForm()
                        }
                    }}
                >

                    <div className="pharmacy-modal">

                        <div className="pharmacy-modal-header">

                            <div>
                                <h3>
                                    {editingId ? 'Edit Medicine' : 'Add New Medicine'}
                                </h3>

                                <p>Enter the medicine information below.</p>
                            </div>

                            <button
                                type="button"
                                className="pharmacy-close-btn"
                                onClick={closeForm}
                            >
                                ×
                            </button>

                        </div>

                        <form onSubmit={handleSubmit}>

                            <div className="pharmacy-form-grid">

                                <div className="pharmacy-form-group">
                                    <label>Medicine Code *</label>

                                    <input
                                        name="medicineCode"
                                        value={generatedMedicineCode}
                                        readOnly
                                        placeholder="Enter medicine name first"
                                        required
                                    />

                                    {!editingId && form.medicineName.trim() && (
                                        <small className="pharmacy-subtext">
                                            Automatically generated
                                        </small>
                                    )}
                                </div>

                                <div className="pharmacy-form-group">
                                    <label>Medicine Name *</label>

                                    <input
                                        name="medicineName"
                                        value={form.medicineName}
                                        onChange={event => {
                                            const { name, value } = event.target
                                            setForm(previous => ({
                                                ...previous,
                                                [name]: value,
                                            }))
                                        }}
                                        placeholder="e.g. Paracetamol"
                                        required
                                        maxLength="150"
                                    />
                                </div>

                                <div className="pharmacy-form-group">
                                    <label>Generic Name</label>

                                    <input
                                        name="genericName"
                                        value={form.genericName}
                                        onChange={event => {
                                            const { name, value } = event.target
                                            setForm(previous => ({
                                                ...previous,
                                                [name]: value,
                                            }))
                                        }}
                                    />
                                </div>

                                <div className="pharmacy-form-group">
                                    <label>Manufacturer</label>

                                    <input
                                        name="manufacturer"
                                        value={form.manufacturer}
                                        onChange={event => {
                                            const { name, value } = event.target
                                            setForm(previous => ({
                                                ...previous,
                                                [name]: value,
                                            }))
                                        }}
                                    />
                                </div>

                                <div className="pharmacy-form-group">
                                    <label>Category</label>

                                    <input
                                        name="category"
                                        value={form.category}
                                        onChange={event => {
                                            const { name, value } = event.target
                                            setForm(previous => ({
                                                ...previous,
                                                [name]: value,
                                            }))
                                        }}
                                        placeholder="e.g. Antibiotic"
                                    />
                                </div>

                                <div className="pharmacy-form-group">
                                    <label>Dosage Form</label>

                                    <input
                                        name="dosageForm"
                                        value={form.dosageForm}
                                        onChange={event => {
                                            const { name, value } = event.target
                                            setForm(previous => ({
                                                ...previous,
                                                [name]: value,
                                            }))
                                        }}
                                        placeholder="e.g. Tablet, Syrup"
                                    />
                                </div>

                                <div className="pharmacy-form-group">
                                    <label>Strength</label>

                                    <input
                                        name="strength"
                                        value={form.strength}
                                        onChange={event => {
                                            const { name, value } = event.target
                                            setForm(previous => ({
                                                ...previous,
                                                [name]: value,
                                            }))
                                        }}
                                        placeholder="e.g. 500 mg"
                                    />
                                </div>

                                <div className="pharmacy-form-group">
                                    <label>Unit Price (₹) *</label>

                                    <input
                                        type="number"
                                        name="unitPrice"
                                        value={form.unitPrice}
                                        onChange={event => {
                                            const { name, value } = event.target
                                            setForm(previous => ({
                                                ...previous,
                                                [name]: value,
                                            }))
                                        }}
                                        min="0"
                                        step="0.01"
                                        required
                                    />
                                </div>

                                <div className="pharmacy-form-group">
                                    <label>Reorder Level *</label>

                                    <input
                                        type="number"
                                        name="reorderLevel"
                                        value={form.reorderLevel}
                                        onChange={event => {
                                            const { name, value } = event.target
                                            setForm(previous => ({
                                                ...previous,
                                                [name]: value,
                                            }))
                                        }}
                                        min="0"
                                        required
                                    />
                                </div>

                            </div>

                            <div className="pharmacy-form-actions">

                                <button
                                    type="button"
                                    className="pharmacy-cancel-btn"
                                    onClick={closeForm}
                                    disabled={saving}
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className="pharmacy-primary-btn"
                                    disabled={saving || !generatedMedicineCode}
                                >
                                    {saving
                                        ? 'Saving...'
                                        : editingId
                                            ? 'Update Medicine'
                                            : 'Save Medicine'}
                                </button>

                            </div>

                        </form>

                    </div>

                </div>
            )}

        </div>
    )
}

export default Pharmacy

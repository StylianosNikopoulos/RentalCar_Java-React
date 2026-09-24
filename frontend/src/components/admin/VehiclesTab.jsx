import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query';
import vehicleService from '../../services/vehicleService';
import toast from 'react-hot-toast';
import Swal from 'sweetalert2';
import { useLang } from '../../context/LangContext';
import { translations } from '../../i18n/translations';
import PaginationControls from './PaginationControls';
import VehicleModal from './VehicleModal';

const VehiclesTab = () => {
    const queryClient = useQueryClient();
    const { lang } = useLang();
    const t = translations[lang].admin;
    const tVehicles = translations[lang].vehicles;

    const [vehiclePage, setVehiclePage] = useState(1);
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [fuelTypeFilter, setFuelTypeFilter] = useState('');
    
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedVehicle, setSelectedVehicle] = useState(null);
    const [selectedVehicleDetails, setSelectedVehicleDetails] = useState(null);
    const itemsPerPage = 9;

    useEffect(() => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, [vehiclePage]);

    const handleSearchChange = (e) => {
        setSearchTerm(e.target.value);
        setVehiclePage(1);
    };

    const handleStatusFilterChange = (e) => {
        setStatusFilter(e.target.value);
        setVehiclePage(1);
    };

    const handleFuelFilterChange = (e) => {
        setFuelTypeFilter(e.target.value);
        setVehiclePage(1);
    };

    const { data: vehicleResponse = {}, isLoading } = useQuery({
        queryKey: ['admin-vehicles', vehiclePage, searchTerm, statusFilter, fuelTypeFilter],
        queryFn: () => vehicleService.getAllVehiclesForAdmin(
            vehiclePage - 1, 
            itemsPerPage, 
            'default', 
            searchTerm, 
            { status: statusFilter, fuelType: fuelTypeFilter }
        ),
        refetchInterval: 10000,
        staleTime: 0,
        refetchOnMount: true,
        placeholderData: keepPreviousData
    });

    const currentVehicles = vehicleResponse.content || [];
    const totalVehiclePages = vehicleResponse.page?.totalPages || 1;

    const oosMutation = useMutation({
        mutationFn: (id) => vehicleService.markVehicleOutOfService(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-vehicles'] });
            toast.success(t.toastVehOos);
        },
        onError: (error) => {
            const errorMessage = error.response?.data?.message || t.toastOpFailed;
            toast.error(errorMessage);
        }
    });

    const restoreMutation = useMutation({
        mutationFn: (id) => vehicleService.restoreVehicle ? vehicleService.restoreVehicle(id) : vehicleService.restoreVehicle(id, 'AVAILABLE'),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-vehicles'] });
            toast.success(t.toastVehRestored);
        },
        onError: () => toast.error(t.toastOpFailed)
    });

    const confirmSwal = (title, text, onConfirm) => {
        Swal.fire({
            title, text, icon: 'warning', iconColor: '#ff4d00', background: '#151515',
            showCancelButton: true, confirmButtonText: t.swalYes, cancelButtonText: t.swalNo,
            buttonsStyling: false,
            customClass: {
                container: 'swal-fix-overlay', popup: 'swal-custom-popup',
                actions: 'swal-custom-actions', confirmButton: 'swal-btn swal-btn-confirm', cancelButton: 'swal-btn swal-btn-cancel'
            }
        }).then((result) => {
            if (result.isConfirmed) onConfirm();
        });
    };

    const handleRestoreVehicle = (vehicle) => {
        const isCurrentlyOos = vehicle.status === 'OUT_OF_SERVICE';

        const title = isCurrentlyOos 
            ? t.swalRestoreVehTitle 
            : t.swalOosVehTitle;
            
        const text = isCurrentlyOos 
            ? t.swalRestoreVehText 
            : t.swalOosVehText;

        confirmSwal(title, text, () => {
            if (isCurrentlyOos) {
                restoreMutation.mutate(vehicle.id);
            } else {
                oosMutation.mutate(vehicle.id);
            }
        });
    };

    const openCreateModal = () => {
        setSelectedVehicle(null);
        setIsModalOpen(true);
    };

    const openUpdateModal = (vehicle) => {
        setSelectedVehicle(vehicle);
        setIsModalOpen(true);
    };

    const getStatusText = (status) => {
        switch (status) {
            case 'OUT_OF_SERVICE':
                return t.btnOutOfService;
            case 'RENTED':
                return t.statusRented;
            case 'AVAILABLE':
                return t.statusAvailable;
            case 'ACTIVE':
                return t.statusActive;
            case 'PENDING':
                return t.statusPending;
            case 'CONFIRMED':
                return t.statusConfirmed;
            case 'COMPLETED':
                return t.statusCompleted;
            case 'CANCELED':
                return t.statusCanceled;
            default:
                return status || t.statusUnknown;
        }
    };

    const renderStatusBadge = (status) => {
        const isOos = status === 'OUT_OF_SERVICE';
        const isRented = status === 'RENTED';
        const badgeClass = isOos ? 'status-oos' : isRented ? 'status-rented' : 'status-active';
        const iconClass = isOos ? 'fa-ban' : isRented ? 'fa-car' : 'fa-check-circle';

        return (
            <span className={`status-badge ${badgeClass}`}>
                <i className={`fas ${iconClass}`}></i>
                {getStatusText(status)}
            </span>
        );
    };

    return (
        <div className="admin-section">
            <div className="admin-actions-bar">
                <button className="add-btn" onClick={openCreateModal}>
                    <i className="fas fa-plus"></i>
                    {t.addVehicle}
                </button>

                <div className="admin-filters-group">
                    <div className="search-input-wrapper">
                        <input 
                            type="text" 
                            placeholder={tVehicles.searchPlaceholder} 
                            value={searchTerm} 
                            onChange={handleSearchChange} 
                            className="admin-filter-input"
                        />
                        <i className="fas fa-search"></i>
                    </div>
                    
                    <div className="filter-select-wrapper">
                        <i className="fas fa-filter select-lead-icon"></i>
                        <select value={statusFilter} onChange={handleStatusFilterChange} className="admin-filter-select">
                            <option value="">{t.tableStatus}</option>
                            <option value="ACTIVE">{t.statusActive}</option>
                            <option value="OUT_OF_SERVICE">{t.btnOutOfService}</option>
                            <option value="RENTED">{t.statusRented}</option>
                        </select>
                    </div>

                    <div className="filter-select-wrapper">
                        <i className="fas fa-gas-pump select-lead-icon"></i>
                        <select value={fuelTypeFilter} onChange={handleFuelFilterChange} className="admin-filter-select">
                            <option value="">{t.placeholderFuel}</option>
                            <option value="PETROL">{t.fuelGasoline}</option>
                            <option value="DIESEL">{t.fuelDiesel}</option>
                            <option value="ELECTRIC">{t.fuelElectric}</option>
                            <option value="HYBRID">{t.fuelHybrid}</option>
                        </select>
                    </div>
                </div>
            </div>
            {isLoading ? (
                <div className="loader-container" style={{ minHeight: '300px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                    <div className="loader"></div>
                    <span style={{ color: '#888', fontSize: '0.8rem', fontWeight: '800', letterSpacing: '2px', marginTop: '15px' }}>
                        {t.fetchingVehicles}
                    </span>
                </div>
            ) : (
                <>
                    <table className="admin-table">
                        <thead>
                            <tr>
                                <th>{t.tableVehicle}</th>
                                <th>{t.tableStatus}</th>
                                <th>{t.tableActions}</th>
                            </tr>
                        </thead>
                        <tbody>
                        {currentVehicles.length > 0 ? (
                            currentVehicles.map(car => {
                                const isOos = car.status === 'OUT_OF_SERVICE';
                                const isRented = car.status === 'RENTED';

                                return (
                                    <tr key={car.id} className={isOos ? 'row-out-of-service' : ''}>
                                        <td><strong>{car.brand}</strong> {car.model}</td>
                                        <td>
                                            {renderStatusBadge(car.status)}
                                        </td>
                                        <td className="actions-cell">
                                            <button className="status-btn details-btn" onClick={() => setSelectedVehicleDetails(car)}>
                                                <i className="fas fa-eye"></i> {t.btnDetails}
                                            </button>
                                            <button className="btn-update" onClick={() => openUpdateModal(car)}>
                                                <i className="fas fa-edit"></i> {t.btnUpdate}
                                            </button>
                                            <button 
                                                className={`btn-status-toggle ${isOos ? 'btn-restore' : 'btn-oos'}`} 
                                                onClick={() => handleRestoreVehicle(car)}
                                                disabled={isRented}
                                                style={isRented ? { opacity: 0.5, cursor: 'not-allowed' } : {}}
                                            >
                                                <i className={`fas ${isOos ? 'fa-undo' : 'fa-ban'}`}></i> 
                                                {isOos ? t.btnRestore : t.btnOutOfService}
                                            </button>
                                        </td>
                                    </tr>
                                );
                            })
                        ) : (
                            <tr>
                                <td colSpan="3" style={{ textAlign: 'center', padding: '20px' }}>
                                    {tVehicles.noResults}
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                    
                    <PaginationControls 
                        currentPage={vehiclePage} 
                        totalPages={totalVehiclePages} 
                        onPageChange={setVehiclePage} 
                    />
                </>
            )}

            {isModalOpen && (
                <VehicleModal 
                    vehicleToEdit={selectedVehicle} 
                    onClose={() => setIsModalOpen(false)} 
                />
            )}

            {selectedVehicleDetails && (
                <div className="modal-overlay details-modal-overlay" onClick={() => setSelectedVehicleDetails(null)}>
                    <div className="admin-modal reservation-details-modal" onClick={(event) => event.stopPropagation()}>
                        <div className="modal-heading-row">
                            <h3>{t.vehicleDetails}</h3>
                            <button className="modal-close-btn" onClick={() => setSelectedVehicleDetails(null)} aria-label={t.btnClose}>
                                <span aria-hidden="true">×</span>
                            </button>
                        </div>
                        <div className="reservation-details-grid">
                            <div><span>{t.tableVehicle}</span><strong>{selectedVehicleDetails.brand} {selectedVehicleDetails.model}</strong></div>
                            <div><span>{t.tablePlate}</span><strong>{selectedVehicleDetails.licensePlate}</strong></div>
                            <div><span>{t.tablePrice}</span><strong>€{selectedVehicleDetails.dailyPrice} / {t.daySingle}</strong></div>
                            <div><span>{t.placeholderYear}</span><strong>{selectedVehicleDetails.year}</strong></div>
                            <div><span>{t.placeholderFuel}</span><strong>{selectedVehicleDetails.fuelType}</strong></div>
                            <div><span>{t.tableStatus}</span><strong>{getStatusText(selectedVehicleDetails.status)}</strong></div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default VehiclesTab;
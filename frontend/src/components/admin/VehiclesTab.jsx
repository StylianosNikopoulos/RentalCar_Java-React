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
    const activeTranslation = translations[lang].myReservations?.active;

    const [vehiclePage, setVehiclePage] = useState(1);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedVehicle, setSelectedVehicle] = useState(null);
    const itemsPerPage = 9;

    useEffect(() => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, [vehiclePage]);

    const { data: vehicleResponse = {}, isLoading } = useQuery({
        queryKey: ['admin-vehicles', vehiclePage],
        queryFn: () => vehicleService.getAllVehiclesForAdmin(vehiclePage - 1, itemsPerPage),
        refetchInterval: 10000,
        staleTime: 0,
        refetchOnMount: true,
        placeholderData: keepPreviousData
    });

    const currentVehicles = vehicleResponse.content || [];
    const totalVehiclePages = vehicleResponse.page?.totalPages || 1;

    const oosMutation = useMutation({
        mutationFn: (id) => vehicleService.deleteVehicle(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-vehicles'] });
            toast.success(t.toastVehOos);
        },
        onError: () => toast.error(t.toastOpFailed)
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

    return (
        <div className="admin-section">
            <button className="add-btn" onClick={openCreateModal}>
                {t.addVehicle}
            </button>
            
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
                                <th>{t.tablePlate}</th>
                                <th>{t.tablePrice}</th>
                                <th>{t.tableStatus}</th>
                                <th>{t.tableActions}</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentVehicles.map(car => {
                                const isOos = car.status === 'OUT_OF_SERVICE';
                                return (
                                    <tr key={car.id} className={isOos ? 'row-out-of-service' : ''}>
                                        <td><strong>{car.brand}</strong> {car.model}</td>
                                        <td>{car.licensePlate}</td>
                                        <td>€{car.dailyPrice}</td>
                                        <td>
                                            <span className={`status-badge ${isOos ? 'status-oos' : 'status-active'}`}>
                                                <i className={`fas ${isOos ? 'fa-ban' : 'fa-check-circle'}`}></i>
                                                {isOos ? t.btnOutOfService : activeTranslation}
                                            </span>
                                        </td>
                                        <td className="actions-cell">
                                            <button className="btn-update" onClick={() => openUpdateModal(car)}>
                                                <i className="fas fa-edit"></i> {t.btnUpdate}
                                            </button>
                                            <button 
                                                className={`btn-status-toggle ${isOos ? 'btn-restore' : 'btn-oos'}`} 
                                                onClick={() => handleRestoreVehicle(car)}
                                            >
                                                <i className={`fas ${isOos ? 'fa-undo' : 'fa-ban'}`}></i> 
                                                {isOos ? t.btnRestore : t.btnOutOfService}
                                            </button>
                                        </td>
                                    </tr>
                                );
                            })}
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
        </div>
    );
};

export default VehiclesTab;
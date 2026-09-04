import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query';
import reservationService from '../../services/reservationService';
import toast from 'react-hot-toast';
import Swal from 'sweetalert2';
import { useLang } from '../../context/LangContext';
import { translations } from '../../i18n/translations';
import PaginationControls from './PaginationControls';

const ReservationsTab = () => {
    const queryClient = useQueryClient();
    const { lang } = useLang();
    const t = translations[lang].admin;

    const [reservationPage, setReservationPage] = useState(1);
    const itemsPerPage = 9;

    useEffect(() => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, [reservationPage]);

    const { data: reservationResponse = {}, isLoading } = useQuery({
        queryKey: ['admin-reservations', reservationPage],
        queryFn: () => reservationService.getAllReservations(reservationPage - 1, itemsPerPage),
        refetchInterval: 5000,
        staleTime: 0,
        refetchOnMount: true,
        refetchIntervalInBackground: true,
        placeholderData: keepPreviousData
    });

    const currentReservations = reservationResponse.content || [];
    const totalReservationPages = reservationResponse.page?.totalPages || 1;

    const statusLabel = (status) => {
        const labels = {
            PENDING: t.statusPending,
            CONFIRMED: t.statusConfirmed,
            ACTIVE: t.statusActive,
            CANCELED: t.statusCanceled,
            COMPLETED: t.statusCompleted
        };
        return labels[status] || status || t.statusUnknown;
    };

    const cancelResMutation = useMutation({
        mutationFn: (id) => reservationService.cancelReservation(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-reservations'] });
            toast.success(t.toastResCanceled);
        },
        onError: () => toast.error(t.toastResCanErr)
    });

    const returnResMutation = useMutation({
        mutationFn: (id) => reservationService.completeReservation(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-reservations'] });
            toast.success(t.toastVehReturned);
        },
        onError: () => toast.error(t.toastVehRetErr)
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

    const handleCancelReservation = (id) => {
        confirmSwal(t.swalCancelTitle, t.swalCancelText, () => {
            cancelResMutation.mutate(id);
        });
    };

    const handleCompleteReservation = (id) => {
        confirmSwal(t.swalReturnTitle, t.swalReturnText, () => {
            returnResMutation.mutate(id);
        });
    };

    return (
        <div className="admin-section">
            {isLoading ? (
                <div className="loader-container" style={{ minHeight: '300px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                    <div className="loader"></div>
                    <span style={{ color: '#888', fontSize: '0.8rem', fontWeight: '800', letterSpacing: '2px', marginTop: '15px' }}>
                        {t.fetchingReservations}
                    </span>
                </div>
            ) : (
                <>
                    <table className="admin-table">
                        <thead>
                            <tr>
                                <th>{t.tableUserEmail}</th>
                                <th>{t.tableVehicle}</th>
                                <th>{t.tableFrom}</th>
                                <th>{t.tableUntil}</th>
                                <th>{t.tableStatus}</th>
                                <th>{t.tableActions}</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentReservations.length > 0 ? currentReservations.map(res => (
                                <tr key={res.id}>
                                    <td>{res.email}</td>
                                    <td>{res.vehicleBrand} {res.vehicleName}</td>
                                    <td>{new Date(res.period.start).toLocaleDateString()}</td>
                                    <td>{new Date(res.period.end).toLocaleDateString()}</td>
                                    <td>
                                        <span className={`status-badge status-${res.status ? res.status.toLowerCase() : ''}`}>
                                            {statusLabel(res.status)}
                                        </span>
                                    </td>
                                    <td>
                                        <div className="actions-cell">
                                            {res.status === 'PENDING' && (
                                                <button className="status-btn pick-up" onClick={() => handleCancelReservation(res.id)}>
                                                    <i className="fas fa-times"></i> {t.btnCancel}
                                                </button>
                                            )}

                                            {res.status === 'ACTIVE' && (
                                                <button className="status-btn return-btn-table" onClick={() => handleCompleteReservation(res.id)}>
                                                    <i className="fas fa-undo"></i> {t.btnReturn}
                                                </button>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            )) : (
                                <tr><td colSpan="6" style={{textAlign: 'center', color: '#666', padding: '30px'}}>{t.noReservations}</td></tr>
                            )}
                        </tbody>
                    </table>
                    
                    <PaginationControls 
                        currentPage={reservationPage} 
                        totalPages={totalReservationPages} 
                        onPageChange={setReservationPage} 
                    />
                </>
            )}
        </div>
    );
};

export default ReservationsTab;
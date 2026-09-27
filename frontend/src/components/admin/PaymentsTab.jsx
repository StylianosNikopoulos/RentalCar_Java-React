import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query';
import paymentService from '../../services/paymentService';
import toast from 'react-hot-toast';
import Swal from 'sweetalert2';
import { useLang } from '../../context/LangContext';
import { translations } from '../../i18n/translations';
import PaginationControls from './PaginationControls';

const PaymentsTab = () => {
    const queryClient = useQueryClient();
    const { lang } = useLang();
    const t = translations[lang].admin;

    const [page, setPage] = useState(1);
    const [emailSearch, setEmailSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [sortBy, setSortBy] = useState('created_at,desc');
    const [selectedPaymentDate, setSelectedPaymentDate] = useState(null);

    const itemsPerPage = 10;

    useEffect(() => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, [page]);

    const handleSearchChange = (e) => {
        setEmailSearch(e.target.value);
        setPage(1);
    };

    const handleStatusChange = (e) => {
        setStatusFilter(e.target.value);
        setPage(1);
    };

    const handleSortChange = (e) => {
        setSortBy(e.target.value);
        setPage(1);
    };

    const { data: paymentResponse = {}, isLoading } = useQuery({
        queryKey: ['admin-payments', page, emailSearch, statusFilter, sortBy],
        queryFn: () => paymentService.getAllPaymentsForAdmin(
            page - 1,
            itemsPerPage,
            sortBy,
            { status: statusFilter, email: emailSearch }
        ),
        refetchInterval: 10000,
        staleTime: 0,
        placeholderData: keepPreviousData
    });

    const currentPayments = paymentResponse.content || [];
    const totalPages = paymentResponse.page?.totalPages || paymentResponse.totalPages || 1;

    // Refund Mutation
    const refundMutation = useMutation({
        mutationFn: (stripePaymentId) => paymentService.refundPayment(stripePaymentId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-payments'] });
            toast.success(t.toastRefundSuccess || 'Refund processed successfully');
        },
        onError: (error) => {
            const errorMessage = error.response?.data?.message || t.toastOpFailed || 'Refund failed';
            toast.error(errorMessage);
        }
    });

    const confirmRefund = (payment) => {
        const amountVal = payment.amount?.amount !== undefined ? payment.amount.amount : payment.amount;
        Swal.fire({
            title: t.swalRefundTitle || 'Process Refund?',
            text: `${t.swalRefundText || 'Are you sure you want to refund this payment?'} (€${amountVal})`,
            icon: 'warning',
            iconColor: '#ff4d00',
            background: '#151515',
            showCancelButton: true,
            confirmButtonText: t.swalYes || 'Yes, Refund',
            cancelButtonText: t.swalNo || 'Cancel',
            buttonsStyling: false,
            customClass: {
                container: 'swal-fix-overlay',
                popup: 'swal-custom-popup',
                actions: 'swal-custom-actions',
                confirmButton: 'swal-btn swal-btn-confirm',
                cancelButton: 'swal-btn swal-btn-cancel'
            }
        }).then((result) => {
            if (result.isConfirmed) {
                refundMutation.mutate(payment.stripePaymentId);
            }
        });
    };

    const getStatusBadge = (status) => {
        let badgeClass = 'status-pending';
        let iconClass = 'fa-clock';

        switch (status) {
            case 'COMPLETED':
                badgeClass = 'status-active';
                iconClass = 'fa-check-circle';
                break;
            case 'REFUNDED':
                badgeClass = 'status-oos';
                iconClass = 'fa-undo';
                break;
            case 'FAILED':
                badgeClass = 'status-oos';
                iconClass = 'fa-times-circle';
                break;
            case 'PENDING':
            default:
                badgeClass = 'status-pending';
                iconClass = 'fa-clock';
                break;
        }

        return (
            <span className={`status-badge ${badgeClass}`}>
                <i className={`fas ${iconClass}`}></i>
                {status}
            </span>
        );
    };

    return (
        <div className="admin-section">
            <div className="admin-actions-bar">
                <div className="admin-filters-group" style={{ marginLeft: 0 }}>
                    {/* Search Email */}
                    <div className="search-input-wrapper">
                        <input
                            type="text"
                            placeholder={t.searchEmail || 'Search by Email...'}
                            value={emailSearch}
                            onChange={handleSearchChange}
                            className="admin-filter-input"
                        />
                        <i className="fas fa-search"></i>
                    </div>

                    {/* Filter Status */}
                    <div className="filter-select-wrapper">
                        <i className="fas fa-filter select-lead-icon"></i>
                        <select value={statusFilter} onChange={handleStatusChange} className="admin-filter-select">
                            <option value="">{t.allStatuses || 'All Statuses'}</option>
                            <option value="COMPLETED">COMPLETED</option>
                            <option value="PENDING">PENDING</option>
                            <option value="REFUNDED">REFUNDED</option>
                            <option value="FAILED">FAILED</option>
                        </select>
                    </div>

                    {/* Sort By Amount / Date */}
                    <div className="filter-select-wrapper">
                        <i className="fas fa-sort select-lead-icon"></i>
                        <select value={sortBy} onChange={handleSortChange} className="admin-filter-select">
                            <option value="created_at,desc">{t.sortNewest || 'Date: Newest'}</option>
                            <option value="created_at,asc">{t.sortOldest || 'Date: Oldest'}</option>
                            <option value="amount,desc">{t.sortAmountHigh || 'Amount: High to Low'}</option>
                            <option value="amount,asc">{t.sortAmountLow || 'Amount: Low to High'}</option>
                        </select>
                    </div>
                </div>
            </div>

            {isLoading ? (
                <div className="loader-container" style={{ minHeight: '300px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                    <div className="loader"></div>
                    <span style={{ color: '#888', fontSize: '0.8rem', fontWeight: '800', letterSpacing: '2px', marginTop: '15px' }}>
                        {t.fetchingPayments || 'FETCHING PAYMENTS...'}
                    </span>
                </div>
            ) : (
                <>
                    <table className="admin-table">
                        <thead>
                            <tr>
                                <th>{t.tableEmail || 'Email'}</th>
                                <th>{t.tableAmount || 'Amount'}</th>
                                <th>{t.tableStatus || 'Status'}</th>
                                <th>{t.tableActions || 'Actions'}</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentPayments.length > 0 ? (
                                currentPayments.map(payment => {
                                    const amountVal = payment.amount?.amount !== undefined ? payment.amount.amount : payment.amount;
                                    const currencyVal = payment.amount?.currency || payment.currency || 'EUR';
                                    const formattedDate = payment.createdAt 
                                        ? new Date(payment.createdAt).toLocaleString() 
                                        : '-';
                                    const userEmail = payment.userEmail || payment.email || payment.user?.email || '-';

                                    return (
                                        <tr key={payment.id}>
                                            <td style={{ fontSize: '0.9rem', color: '#fff' }}>
                                                <i className="fas fa-envelope" style={{ marginRight: '8px', color: '#888' }}></i>
                                                {userEmail}
                                            </td>
                                            <td>
                                                <strong style={{ fontSize: '1.05rem', color: '#fff' }}>
                                                    €{amountVal}
                                                </strong>
                                                <span style={{ fontSize: '0.75rem', color: '#888', marginLeft: '5px' }}>
                                                    {currencyVal.toUpperCase()}
                                                </span>
                                            </td>
                                            <td>{getStatusBadge(payment.status)}</td>
                                            <td className="actions-cell">
                                                <button className="status-btn details-btn" onClick={() => setSelectedPaymentDate(formattedDate)}>
                                                    <i className="fas fa-eye"></i> {t.btnDetails || 'Details'}
                                                </button>

                                                {payment.receiptUrl && (
                                                    <a 
                                                        href={payment.receiptUrl} 
                                                        target="_blank" 
                                                        rel="noopener noreferrer" 
                                                        className="status-btn"
                                                        style={{ background: '#222', color: '#fff', textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '5px' }}
                                                    >
                                                        <i className="fas fa-receipt"></i> {t.btnReceipt || 'Receipt'}
                                                    </a>
                                                )}

                                                {payment.status === 'COMPLETED' && (
                                                    <button 
                                                        className="btn-status-toggle btn-oos" 
                                                        onClick={() => confirmRefund(payment)}
                                                    >
                                                        <i className="fas fa-undo"></i> {t.btnRefund || 'Refund'}
                                                    </button>
                                                )}
                                            </td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr>
                                    <td colSpan="4" style={{ textAlign: 'center', padding: '20px' }}>
                                        {t.noPaymentsFound || 'No payments found'}
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>

                    <PaginationControls 
                        currentPage={page} 
                        totalPages={totalPages} 
                        onPageChange={setPage} 
                    />
                </>
            )}

            {selectedPaymentDate && (
                <div className="modal-overlay details-modal-overlay" onClick={() => setSelectedPaymentDate(null)}>
                    <div className="admin-modal reservation-details-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-heading-row">
                            <h3>{t.paymentDetails || 'Payment Details'}</h3>
                            <button className="modal-close-btn" onClick={() => setSelectedPaymentDate(null)}>
                                ×
                            </button>
                        </div>
                        <div className="reservation-details-grid" style={{ gridTemplateColumns: '1fr', textAlign: 'center', padding: '20px 0' }}>
                            <div>
                                <span style={{ fontSize: '0.9rem', color: '#888', display: 'block', marginBottom: '8px' }}>
                                    <i className="fas fa-calendar-alt" style={{ marginRight: '6px' }}></i>
                                    {t.tableDate || 'Date & Time'}
                                </span>
                                <strong style={{ fontSize: '1.2rem', color: '#fff' }}>{selectedPaymentDate}</strong>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default PaymentsTab;
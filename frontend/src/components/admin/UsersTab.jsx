import React, { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient, keepPreviousData } from '@tanstack/react-query';
import userService from '../../services/userService';
import toast from 'react-hot-toast';
import Swal from 'sweetalert2';
import { useLang } from '../../context/LangContext';
import { translations } from '../../i18n/translations';
import PaginationControls from './PaginationControls';

const UsersTab = () => {
    const queryClient = useQueryClient();
    const { lang } = useLang();
    const t = translations[lang].admin;

    const [userPage, setUserPage] = useState(1);
    const itemsPerPage = 9;

    useEffect(() => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }, [userPage]);

    const { data: userResponse = {}, isLoading } = useQuery({
        queryKey: ['admin-users', userPage],
        queryFn: () => userService.getAllUsers(userPage - 1, itemsPerPage),
        refetchInterval: 30000,
        staleTime: 0,
        refetchOnMount: true,
        placeholderData: keepPreviousData
    });

    const currentUsers = userResponse.content || [];
    const totalUserPages = userResponse.page?.totalPages || 1;

    const deleteUserMutation = useMutation({
        mutationFn: (id) => userService.deleteUser(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-users'] });
            toast.success(t.toastUserDeleted);
            if (currentUsers.length === 1 && userPage > 1) setUserPage(prev => prev - 1);
        },
        onError: () => toast.error(t.toastUserDelErr)
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

    const handleDeleteUser = (id) => {
        confirmSwal(t.swalDeleteUserTitle, t.swalDeleteUserText, () => {
            deleteUserMutation.mutate(id);
        });
    };

    return (
        <div className="admin-section">
            {isLoading ? (
                <div className="loader-container" style={{ minHeight: '300px', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
                    <div className="loader"></div>
                    <span style={{ color: '#888', fontSize: '0.8rem', fontWeight: '800', letterSpacing: '2px', marginTop: '15px' }}>
                        {t.fetchingUsers}
                    </span>
                </div>
            ) : (
                <>
                    <table className="admin-table">
                        <thead>
                            <tr>
                                <th>{t.tableName}</th>
                                <th>{t.tableEmail}</th>
                                <th>{t.tableRole}</th>
                                <th>{t.tableActions}</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentUsers.map(user => {
                                const isDeleted = user.deleted || 
                                                user.isDeleted || 
                                                user.status === 'DELETED' || 
                                                user.active === false ||
                                                user.email?.startsWith('deleted_') ||
                                                user.firstName === 'Deleted User';

                                return (
                                    <tr key={user.id} className={isDeleted ? 'row-out-of-service' : ''}>
                                        <td>{user.firstName} {user.lastName}</td>
                                        <td>{user.email}</td>
                                        <td>
                                            <span className={`role-badge ${user.role ? user.role.toLowerCase() : ''}`}>
                                                {user.role}
                                            </span>
                                        </td>
                                        <td>
                                            <div className="actions-cell">
                                                {user.role !== 'ADMIN' && !isDeleted && (
                                                    <button className="btn-delete" onClick={() => handleDeleteUser(user.id)}>
                                                        <i className="fas fa-trash"></i> {t.btnDelete}
                                                    </button>
                                                )}
                                                
                                                {isDeleted && (
                                                    <span className="status-badge status-oos">
                                                        {t.badgeDeleted || 'DELETED'}
                                                    </span>
                                                )}
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                    
                    <PaginationControls 
                        currentPage={userPage} 
                        totalPages={totalUserPages} 
                        onPageChange={setUserPage} 
                    />
                </>
            )}
        </div>
    );
};

export default UsersTab;
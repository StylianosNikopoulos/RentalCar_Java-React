import React, { useState, useEffect } from 'react';
import VehiclesTab from '../components/admin/VehiclesTab';
import UsersTab from '../components/admin/UsersTab';
import ReservationsTab from '../components/admin/ReservationsTab';
import { useLang } from '../context/LangContext';
import { translations } from "../i18n/translations";
import '../assets/styles/admin.css';
import '../assets/styles/swal-custom.css';

const AdminPage = () => {

    useEffect(() => {
        document.title = 'RentalCar | Admin Panel';
    }, []);

    const { lang } = useLang();
    const t = translations[lang].admin;
    const [activeTab, setActiveTab] = useState('vehicles');

    return (
        <div className="admin-container">
            <aside className="admin-sidebar">
                <div className="sidebar-header">
                    <i className="fas fa-user-shield"></i>
                    <span>{t.sidebarTitle}</span>
                </div>
                <nav className="sidebar-nav">
                    <button className={activeTab === 'vehicles' ? 'active' : ''} onClick={() => setActiveTab('vehicles')}>
                        <i className="fas fa-car"></i> {t.tabVehicles}
                    </button>
                    <button className={activeTab === 'users' ? 'active' : ''} onClick={() => setActiveTab('users')}>
                        <i className="fas fa-users"></i> {t.tabUsers}
                    </button>
                    <button className={activeTab === 'reservations' ? 'active' : ''} onClick={() => setActiveTab('reservations')}>
                        <i className="fas fa-calendar-check"></i> {t.tabReservations}
                    </button>
                </nav>
            </aside>

            <main className="admin-main">
                <header className="admin-topbar">
                    <h2>
                        {activeTab === 'vehicles' ? t.tabVehicles : activeTab === 'users' ? t.tabUsers : t.tabReservations}
                    </h2>
                </header>

                <div className="admin-content">
                    {activeTab === 'vehicles' && <VehiclesTab />}
                    {activeTab === 'users' && <UsersTab />}
                    {activeTab === 'reservations' && <ReservationsTab />}
                </div>
            </main>
        </div>
    );
};

export default AdminPage;
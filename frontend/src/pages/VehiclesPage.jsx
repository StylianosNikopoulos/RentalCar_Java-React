import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom'; 
import vehicleService from '../services/vehicleService';
import { useQuery } from '@tanstack/react-query';
import { useLang } from '../context/LangContext';
import { translations } from '../i18n/translations';
import '../assets/styles/vehicles.css'; 

const VehiclesPage = () => {
   
    useEffect(() => {
        document.title = 'RentalCar | Vehicles';
    }, []);

    const { lang } = useLang();
    const t = translations[lang].vehicles;

    const navigate = useNavigate();
    const location = useLocation(); 
    
    const [searchTerm, setSearchTerm] = useState("");
    const [sortOrder, setSortOrder] = useState("default"); 
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 9;

    useEffect(() => {
        window.scrollTo({
            top: 0,
            behavior: 'smooth'
        });
    }, [currentPage]);

    const queryParams = new URLSearchParams(location.search);
    const selectedStart = queryParams.get('start');
    const selectedEnd = queryParams.get('end');

    const rentalDays = selectedStart && selectedEnd 
        ? Math.max(1, Math.ceil((new Date(selectedEnd) - new Date(selectedStart)) / (1000 * 60 * 60 * 24)))
        : 0;

    const { data: responseData = {}, isLoading, isError } = useQuery({
        queryKey: ['vehicles', selectedStart, selectedEnd, currentPage, sortOrder, searchTerm], 
        queryFn: async () => {
            if (selectedStart && selectedEnd) {
                return await vehicleService.getAvailableVehicles(selectedStart, selectedEnd, currentPage - 1, itemsPerPage, sortOrder, searchTerm);
            }
            return await vehicleService.getAllVehicles(currentPage - 1, itemsPerPage, sortOrder, searchTerm);
        },
        staleTime: 1000 * 60 * 5,
        keepPreviousData: true
    });

    const currentItems = responseData.content || [];
    const totalPages = responseData.page?.totalPages || 1;

    const handleSearch = (e) => {
        setSearchTerm(e.target.value);
        setCurrentPage(1); 
    };

    const formatDate = (dateString) => {
        if (!dateString) return "";
        const date = new Date(dateString);
        return date.toLocaleDateString(lang === 'gr' ? 'el-GR' : 'en-GB', {
            day: '2-digit',
            month: 'short',
            year: 'numeric'
        }); 
    };

    const handleCardClick = (carId) => {
        const searchPath = location.search ? location.search : "";
        navigate(`/vehicle/${carId}${searchPath}`);
    };

    if (isError) return <div className="error-message">{t.errorMsg}</div>;

    return (
        <div className="vehicles-page">
            <div className="vehicles-header">
                <h1>{t.pageTitle}</h1>

                {selectedStart && selectedEnd && (
                    <div className="availability-info-banner">
                        <div className="availability-text">
                            <i className="far fa-calendar-check"></i>
                            <span>{t.showingAvailable} </span>
                            <strong>{formatDate(selectedStart)}</strong>
                            <span className="date-separator">→</span>
                            <strong>{formatDate(selectedEnd)}</strong>
                            <span className="days-badge">({rentalDays} {rentalDays === 1 ? t.daySingle : t.daysPlural})</span>
                        </div>
                        <button className="clear-dates-btn" onClick={() => navigate('/vehicles')}>
                            <i className="fas fa-undo-alt" style={{ fontSize: '0.85rem' }}></i> {t.btnResetDates}
                        </button>
                    </div>
                )}
                
                <div className="filters-bar">
                    <div className="search-container">
                        <i className="fas fa-search search-icon"></i>
                        <input 
                            type="text" 
                            placeholder={t.searchPlaceholder} 
                            className="search-input"
                            value={searchTerm}
                            onChange={handleSearch}
                        />
                    </div>
                    <div className="sort-container">
                        <select 
                            className="sort-select" 
                            value={sortOrder}
                            onChange={(e) => {
                                setSortOrder(e.target.value);
                                setCurrentPage(1);
                            }}
                        >
                            <option value="default">{t.sortFeatured}</option>
                            <option value="low">{t.sortLowHigh}</option>
                            <option value="high">{t.sortHighLow}</option>
                        </select>
                    </div>
                </div>
            </div>

            {isLoading ? (
                <div className="loader-container">
                    <div className="loader"></div>
                    <span className="loader-text">{t.fetching}</span>
                </div>
            ) : currentItems.length === 0 ? (
                <div className="no-results">
                    <i className="fas fa-search"></i>
                    {t.noResults}
                </div>
            ) : (
                <>
                    <div className="vehicle-grid">
                        {currentItems.map(car => (
                            <div key={car.id} className="vehicle-item" onClick={() => handleCardClick(car.id)}>
                                <div className="vehicle-img-wrapper">
                                    <img 
                                        src={
                                            car.images && car.images.length > 0 
                                                ? (car.images.find(img => img.isMain)?.url || car.images[0].url) 
                                                : 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?q=80&w=2070'
                                        } 
                                        alt={`${car.brand} ${car.model}`} 
                                        className="vehicle-img" 
                                    />
                                    <span className="type-tag">{car.brand}</span>
                                </div>
                                
                                <div className="vehicle-card-body">
                                    <div className="vehicle-title-section">
                                        <h3 className="car-name">{car.brand} <span>{car.model}</span></h3>
                                        <span className="car-year">{car.year}</span>
                                    </div>

                                    <div className="card-mini-specs">
                                        <span><i className="fas fa-gas-pump"></i> {car.fuelType}</span>
                                        <span><i className="fas fa-id-card"></i> {car.licensePlate}</span>
                                    </div>

                                    <div className="card-pricing-footer">
                                        <div className="car-price-tag">
                                            <span className="price-value">€{car.dailyPrice}</span>
                                            <span className="price-label">/ {t.perDay}</span>
                                        </div>
                                        
                                        {rentalDays > 0 && (
                                            <div className="total-price-badge">
                                                <span>{t.totalFor} {rentalDays} {rentalDays === 1 ? t.daySingle : t.daysPlural}</span>
                                                <strong>€{(car.dailyPrice * rentalDays).toFixed(0)}</strong>
                                            </div>
                                        )}
                                    </div>
                                </div>
                                <button className="rent-btn-minimal">
                                    {t.btnViewDeal} <i className="fas fa-arrow-right"></i>
                                </button>
                            </div>
                        ))}
                    </div>

                    {totalPages > 1 && (
                        <div className="pagination">
                            <button 
                                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))} 
                                disabled={currentPage === 1} 
                                className="page-btn"
                            >
                                <i className="fas fa-chevron-left"></i> {t.btnPrevious}
                            </button>
                            <span className="page-info">PAGE {currentPage} OF {totalPages}</span>
                            <button 
                                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))} 
                                disabled={currentPage === totalPages} 
                                className="page-btn"
                            >
                                {t.btnNext} <i className="fas fa-chevron-right"></i>
                            </button>
                        </div>
                    )}
                </>
            )}
        </div>
    );
};

export default VehiclesPage;
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom'; 
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import vehicleService from '../services/vehicleService'; 
import toast from 'react-hot-toast'; 
import { useLang } from '../context/LangContext';
import { translations } from '../i18n/translations';
import '../assets/styles/home.css';

const Home = () => {
    const { lang } = useLang();
    const t = translations[lang].home;

    const [vehicles, setVehicles] = useState([]);
    const [loading, setLoading] = useState(true);
    
    const [startDate, setStartDate] = useState(null);
    const [endDate, setEndDate] = useState(null);
    
    const navigate = useNavigate(); 

    useEffect(() => {
        const fetchVehicles = async () => {
            try {
                const data = await vehicleService.getAllVehicles();
                const vehiclesArray = data.content || [];
                setVehicles(vehiclesArray.slice(0, 3));
            } catch (error) {
                console.error("Error fetching vehicles", error);
            } finally {
                setLoading(false);
            }
        };
        fetchVehicles();
    }, []);

    const handleSearch = () => {
        if (!startDate || !endDate) {
            toast.error(t.toastDatesError);
            return;
        }

        const sDate = startDate.toISOString().split('T')[0];
        const eDate = endDate.toISOString().split('T')[0];

        navigate(`/vehicles?start=${sDate}&end=${eDate}`);
    };

    return (
        <div className="home-page">
            <header className="hero">
                <div className="hero-content">
                    <span className="hero-badge">{t.heroBadge}</span>

                    <h1>
                        {t.heroTitle1} <br/>
                        <span className="text-outline">{t.heroTitle2}</span>
                    </h1>

                    <p>{t.heroDesc}</p>
                    
                    <div className="search-widget-v2">
                        <div className="search-field">
                            <i className="fas fa-map-marker-alt field-icon"></i>
                            <div className="field-inputs">
                                <label>{t.locationLabel}</label>
                                <input type="text" value={t.locationValue} readOnly />
                            </div>
                        </div>

                        <div className="search-field">
                            <i className="far fa-calendar-alt field-icon"></i>
                            <div className="field-inputs">
                                <label>{t.timelineLabel}</label>
                                <DatePicker
                                    selectsRange={true}
                                    startDate={startDate}
                                    endDate={endDate}
                                    onChange={(update) => {
                                        const [start, end] = update;
                                        setStartDate(start);
                                        setEndDate(end);
                                    }}
                                    minDate={new Date()}
                                    isClearable={true}
                                    placeholderText={t.placeholderDate}
                                    className="custom-home-datepicker" 
                                    dateFormat="dd MMM yyyy"
                                    popperPlacement="bottom-start"
                                />
                            </div>
                        </div>

                        <button className="search-btn-v2" onClick={handleSearch}>
                            {t.searchBtn} <i className="fas fa-arrow-right"></i>
                        </button>
                    </div>
                </div>
            </header>

            {/* FEATURED VEHICLES SECTION */}
            <section className="featured-fleet-section">
                <div className="section-header-v2">
                    <h2>{t.featuredTitle}</h2>
                    <p>{t.featuredDesc}</p>
                </div>

                {loading ? (
                    <div className="loader-container">
                        <div className="loader"></div>
                        <span className="loader-text">{t.fetching}</span>
                    </div>
                ) : (
                    <div className="home-vehicle-grid">
                        {vehicles.map(car => (
                            <div key={car.id} className="home-vehicle-card" onClick={() => navigate(`/vehicle/${car.id}`)}>
                                <div className="home-car-img-wrapper">
                                    <img 
                                        src={car.images && car.images.length > 0 ? car.images[0].url : 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?q=80&w=2070'} 
                                        alt={`${car.brand} ${car.model}`} 
                                    />
                                    {car.category && <span className="home-car-tag">{car.category}</span>}
                                </div>

                                <div className="home-car-info">
                                    <h3>{car.brand} <span>{car.model}</span></h3>

                                    <div className="home-car-specs-row">
                                        <span><i className="fas fa-gas-pump"></i> {car.fuelType}</span>
                                        <span><i className="fas fa-cog"></i> {car.licensePlate}</span>
                                    </div>

                                    <div className="home-car-price-row">
                                        <div className="home-price">
                                            <strong>€{car.dailyPrice}</strong>
                                            <span>/ {lang === 'en' ? 'day' : 'ημέρα'}</span>
                                        </div>

                                        <button className="home-view-deal-btn">
                                            {t.viewDetails}
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            {/* WHY US FEATURES SECTION */}
            <section className="features-section">
                <div className="section-header-v2">
                    <h2>{t.whyUsTitle}</h2>
                    <p>{t.whyUsDesc}</p>
                </div>

                <div className="features-grid">
                    <div className="feature-card">
                        <div className="feature-icon">
                            <i className="fas fa-road"></i>
                        </div>
                        <h3>{t.feat2Title}</h3>
                        <p>{t.feat2Desc}</p>
                    </div>

                    <div className="feature-card">
                        <div className="feature-icon">
                            <i className="fas fa-headset"></i>
                        </div>
                        <h3>{t.feat3Title}</h3>
                        <p>{t.feat3Desc}</p>
                    </div>
                </div>
            </section>
        </div>
    );
};

export default Home;
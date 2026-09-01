import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom'; 
import { useAuth } from '../hooks/useAuth';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import vehicleService from '../services/vehicleService';
import reservationService from '../services/reservationService';
import toast from 'react-hot-toast';
import { useLang } from '../context/LangContext';
import { translations } from '../i18n/translations';
import '../assets/styles/details.css';
import { Swiper, SwiperSlide } from 'swiper/react';
import { Navigation, Pagination, Mousewheel, Keyboard, Thumbs } from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/navigation';
import 'swiper/css/pagination';
import 'swiper/css/thumbs';

const VehicleDetailsPage = () => {

    useEffect(() => {
        document.title = 'RentalCar | Vehicle Details';
    }, []);

    const { lang } = useLang();
    const t = translations[lang]?.details || {};

    const { id } = useParams();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { user } = useAuth();
    
    const [startDate, setStartDate] = useState(null);
    const [endDate, setEndDate] = useState(null);
    const [acceptedTerms, setAcceptedTerms] = useState(false);
    const [thumbsSwiper, setThumbsSwiper] = useState(null);
    const [isActionPending, setIsActionPending] = useState(false);

    const { data: vehicle, isLoading: vehicleLoading } = useQuery({
        queryKey: ['vehicle', id],
        queryFn: () => vehicleService.getVehicleById(id),
        enabled: !isActionPending
    });

    const { data: bookedDates = [], isLoading: datesLoading } = useQuery({
        queryKey: ['vehicle-reservations', id],
        queryFn: async () => {
            const data = await reservationService.getVehicleReservations(id);
            const safeReservations = Array.isArray(data) ? data : [];
            return safeReservations.map(res => {
                if (res.period?.start && res.period?.end) {
                    const dStart = new Date(res.period.start);
                    const dEnd = new Date(res.period.end);
                    dStart.setHours(0, 0, 0, 0);
                    dEnd.setHours(23, 59, 59, 999); 
                    return { start: dStart, end: dEnd };
                }
                return null;
            }).filter(Boolean);
        },
        refetchInterval: 10000,
        enabled: !isActionPending
    });

    const bookingMutation = useMutation({
        mutationFn: (bookingData) => reservationService.createReservation(bookingData),
        onMutate: () => setIsActionPending(true),
        onSuccess: () => {
            toast.success(t.toastBookingSuccess || 'Reservation submitted successfully!');
            queryClient.invalidateQueries({ queryKey: ['vehicle-reservations', id] });
            setTimeout(() => navigate('/reservations'), 1200);
        },
        onError: (error) => {
            setIsActionPending(false);
            toast.error(error.response?.data?.message || t.toastBookingError || 'Failed to complete reservation');
        }
    });

    const handleBooking = async (e) => {
        e.preventDefault(); 
        if (bookingMutation.isPending || isActionPending) return;
        if (!user) {
            toast.error(t.toastLoginReq || 'Please log in to make a reservation');
            navigate('/login');
            return;
        }
        if (!acceptedTerms) {
            toast.error(t.toastTermsReq || 'You must agree to the rental terms');
            return;
        }
        if (!startDate || !endDate) {
            toast.error(t.toastDatesReq || 'Please select valid rental dates');
            return;
        }

        const formatForBackend = (date) => {
            if (!date) return null;
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            return `${year}-${month}-${day}T10:00:00`;
        };

        const bookingData = {
            vehicleId: id,
            startDate: formatForBackend(startDate),
            endDate: formatForBackend(endDate)
        };

        bookingMutation.mutate(bookingData);
    };

    if ((vehicleLoading || datesLoading) && !isActionPending) {
        return (
            <div className="loader-container">
                <div className="luxury-spinner"></div>
                <span className="loader-text">{t.fetching || 'LOADING DETAILS...'}</span>
            </div>
        );
    }

    if (!vehicle) {
        return (
            <div className="error-message">
                <h2>VEHICLE NOT FOUND</h2>
                <button onClick={() => navigate('/vehicles')} className="back-btn">
                    EXPLORE FLEET
                </button>
            </div>
        );
    }

    const allImages = vehicle.images && vehicle.images.length > 0 
        ? vehicle.images 
        : [{ url: 'https://images.unsplash.com/photo-1503376780353-7e6692767b70?q=80&w=2070' }];

    const rentalDays = startDate && endDate ? Math.max(1, Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24))) : 0;

    return (
        <div className="details-container">
            <div className="details-top-bar">
                <button onClick={() => navigate(-1)} className="back-link">
                    <i className="fas fa-arrow-left"></i> FLEET OVERVIEW
                </button>
            </div>

            <div className="details-header">
                <div className="header-main">
                    <div className="brand-meta">
                        <span className="brand-subtitle">{vehicle.brand}</span>
                        <h1>{vehicle.brand} <span className="model-accent">{vehicle.model}</span></h1>
                    </div>
                    <div className="price-tag">
                        <div className="price-wrapper">
                            <span className="currency">$</span>
                            <span className="amount">{vehicle.dailyPrice}</span>
                        </div>
                        <span className="per-day">/ {t.perDay || 'per day'}</span>
                    </div>
                </div>
            </div>

            <div className="details-grid-v2">
                <div className="vehicle-info-main">
                    <div className="vehicle-gallery-wrapper">
                        <Swiper
                            navigation={true}
                            pagination={{ clickable: true }}
                            mousewheel={true}
                            keyboard={true}
                            thumbs={{ swiper: thumbsSwiper && !thumbsSwiper.destroyed ? thumbsSwiper : null }}
                            modules={[Navigation, Pagination, Mousewheel, Keyboard, Thumbs]}
                            className="main-vehicle-swiper"
                            grabCursor={true}
                        >
                            {allImages.map((img, index) => (
                                <SwiperSlide key={index}>
                                    <img src={img.url} alt={`${vehicle.brand} ${index + 1}`} className="swiper-vehicle-img" />
                                </SwiperSlide>
                            ))}
                        </Swiper>

                        {allImages.length > 1 && (
                            <Swiper
                                onSwiper={setThumbsSwiper}
                                spaceBetween={12}
                                slidesPerView={4}
                                freeMode={true}
                                watchSlidesProgress={true}
                                modules={[Navigation, Thumbs]}
                                className="thumbs-vehicle-swiper"
                            >
                                {allImages.map((img, index) => (
                                    <SwiperSlide key={index}>
                                        <div className="thumb-img-box">
                                            <img src={img.url} alt={`Thumbnail ${index + 1}`} />
                                        </div>
                                    </SwiperSlide>
                                ))}
                            </Swiper>
                        )}
                    </div>

                    <div className="real-car-features">
                        <h2>{t.specifications || 'VEHICLE SPECIFICATIONS'}</h2>
                        <div className="features-grid">
                            <div className="feature-item">
                                <i className="fas fa-gas-pump"></i>
                                <div className="feature-meta">
                                    <span>{t.fuelType || 'Fuel System'}</span>
                                    <strong>{vehicle.fuelType}</strong>
                                </div>
                            </div>
                            <div className="feature-item">
                                <i className="fas fa-calendar-alt"></i>
                                <div className="feature-meta">
                                    <span>{t.yearModel || 'Model Year'}</span>
                                    <strong>{vehicle.year}</strong>
                                </div>
                            </div>
                            <div className="feature-item">
                                <i className="fas fa-car-side"></i>
                                <div className="feature-meta">
                                    <span>{t.modelLabel || 'Edition'}</span>
                                    <strong>{vehicle.model}</strong>
                                </div>
                            </div>
                            <div className="feature-item">
                                <i className="fas fa-id-card"></i>
                                <div className="feature-meta">
                                    <span>{t.licensePlate || 'Registration'}</span>
                                    <strong>{vehicle.licensePlate}</strong>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="booking-sidebar">
                    <div className={`booking-card ${bookingMutation.isPending || isActionPending ? 'submitting' : ''}`}>
                        {(bookingMutation.isPending || isActionPending) && (
                            <div className="booking-overlay">
                                <div className="mini-loader"></div>
                            </div>
                        )}
                        <div className="card-header">
                            <h3>{t.sidebarTitle || 'RESERVE VEHICLE'}</h3>
                            <p>{t.sidebarSubtitle || 'Select dates to configure pricing'}</p>
                        </div>
                        
                        <form onSubmit={handleBooking}>
                            <div className="form-group date-picker-group">
                                <label><i className="far fa-calendar-alt"></i> {t.datesLabel || 'Rental Interval'}</label>
                                <div className="inline-datepicker-wrapper">
                                    <DatePicker
                                        inline
                                        selectsRange={true}
                                        startDate={startDate}
                                        endDate={endDate}
                                        onChange={(update) => {
                                            const [start, end] = update;
                                            setStartDate(start);
                                            setEndDate(end);
                                        }}
                                        minDate={new Date(new Date().setDate(new Date().getDate() + 1))}
                                        excludeDateIntervals={bookedDates}
                                        monthsShown={1}
                                    />
                                </div>
                            </div>

                            <div className="invoice-breakdown">
                                <h4>{t.invoiceTitle || 'PRICE BREAKDOWN'}</h4>
                                <div className="invoice-row">
                                    <span>Daily Rate</span>
                                    <span>${vehicle.dailyPrice}</span>
                                </div>
                                <div className="invoice-row">
                                    <span>Duration</span>
                                    <span>{rentalDays} {rentalDays === 1 ? 'Day' : 'Days'}</span>
                                </div>
                                <div className="invoice-row">
                                    <span>Insurance & Fees</span>
                                    <span className="free-badge">INCLUDED</span>
                                </div>
                                <div className="invoice-total">
                                    <span>{t.totalAmount || 'Estimated Total'}</span>
                                    <strong>${(rentalDays * vehicle.dailyPrice).toFixed(2)}</strong>
                                </div>
                            </div>

                            <div className="terms-checkbox-group">
                                <label className="checkbox-container">
                                    <input 
                                        type="checkbox" 
                                        checked={acceptedTerms} 
                                        onChange={(e) => setAcceptedTerms(e.target.checked)}
                                        disabled={bookingMutation.isPending || isActionPending}
                                    />
                                    <span className="checkmark"></span>
                                    <span className="checkbox-text">
                                        {t.termsCheckboxText || 'I agree to the'} <Link to="/terms" target="_blank">{t.termsLinkText || 'Terms & Conditions'}</Link>
                                    </span>
                                </label>
                            </div>

                            <button 
                                type="submit" 
                                className="confirm-glow-btn" 
                                disabled={bookingMutation.isPending || isActionPending}
                            >
                                {bookingMutation.isPending || isActionPending 
                                    ? (t.btnProcessing || 'PROCESSING...') 
                                    : (user ? (t.btnReserve || 'CONFIRM RESERVATION') : (t.btnLoginToBook || 'LOG IN TO BOOK'))}
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default VehicleDetailsPage;
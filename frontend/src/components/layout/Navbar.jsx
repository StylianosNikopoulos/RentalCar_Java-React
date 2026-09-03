import React, { useState, useEffect, useRef } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth'; 
import toast from 'react-hot-toast';
import '../../assets/styles/navbar.css'; 
import { useLang } from "../../context/LangContext";
import { useTheme } from "../../context/ThemeContext";
import { translations } from "../../i18n/translations";

const Navbar = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate(); 
    const { lang, toggleLang } = useLang();
    const { theme, toggleTheme } = useTheme();
    const nav = translations[lang].nav;
    const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
    const [isLangOpen, setIsLangOpen] = useState(false);
    const dropdownRef = useRef(null);

    const isAdmin = user?.user?.role === 'ADMIN' || user?.role === 'ADMIN';

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsLangOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleLogout = () => {
        setIsMobileMenuOpen(false);
        logout();
        toast.success(nav.loggedOut);
        navigate('/');
    };

    const closeMobileMenu = () => {
        setIsMobileMenuOpen(false);
        setIsLangOpen(false);
    };

    return (
        <nav className="navbar">
            <Link to="/" className="nav-logo" onClick={closeMobileMenu}>
                Rental<span>Car</span>
            </Link>

            <button 
                className="mobile-menu-btn" 
                onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
                aria-label={nav.toggleNavigation}
            >
                <span className={isMobileMenuOpen ? "bar open" : "bar"}></span>
                <span className={isMobileMenuOpen ? "bar open" : "bar"}></span>
                <span className={isMobileMenuOpen ? "bar open" : "bar"}></span>
            </button>

            <div className={`nav-links ${isMobileMenuOpen ? "active" : ""}`}>

                <div className="lang-dropdown-container" ref={dropdownRef}>
                    <button className="lang-dropdown-btn" onClick={() => setIsLangOpen(!isLangOpen)}>
                        <i className="fas fa-globe"></i>
                        <span>{lang.toUpperCase()}</span>
                        <i className={`fas fa-chevron-down arrow-icon ${isLangOpen ? 'rotate' : ''}`}></i>
                    </button>
                    
                    {isLangOpen && (
                        <div className="lang-dropdown-menu">
                            <button 
                                className={`lang-dropdown-item ${lang === 'en' ? 'active' : ''}`}
                                onClick={() => { if(lang !== 'en') toggleLang(); setIsLangOpen(false); closeMobileMenu(); }}
                            >
                                EN (English)
                            </button>
                            <button 
                                className={`lang-dropdown-item ${lang === 'gr' ? 'active' : ''}`}
                                onClick={() => { if(lang !== 'gr') toggleLang(); setIsLangOpen(false); closeMobileMenu(); }}
                            >
                                GR (Ελληνικά)
                            </button>
                        </div>
                    )}
                </div>

                <button
                    className="theme-toggle-btn"
                    onClick={toggleTheme}
                    aria-pressed={theme === 'light'}
                    aria-label={theme === 'dark' ? nav.lightTheme : nav.darkTheme}
                    data-tooltip={theme === 'dark' ? nav.lightTheme : nav.darkTheme}
                >
                    <span className="theme-toggle-track" aria-hidden="true">
                        <i className="fas fa-moon theme-icon-moon"></i>
                        <i className="fas fa-sun theme-icon-sun"></i>
                        <span className="theme-toggle-thumb"></span>
                    </span>
                </button>

                {user ? (
                    <>
                        <span className="welcome-msg">
                            <i className="far fa-user-circle"></i> {user.user?.firstName || user.user?.email?.split('@')[0] || 'User'}
                        </span>

                        <NavLink to="/vehicles" onClick={closeMobileMenu}>
                            <i className="fas fa-car-side nav-icon"></i> {nav.vehicles}
                        </NavLink>
                        
                        <NavLink to="/reservations" onClick={closeMobileMenu}>
                            <i className="far fa-calendar-alt nav-icon"></i> {nav.reservations}
                        </NavLink>
                        
                        <NavLink to="/profile" onClick={closeMobileMenu}>
                            <i className="far fa-id-badge nav-icon"></i> {nav.profile}
                        </NavLink>

                        {isAdmin && (
                            <NavLink to="/admin" className="admin-link-highlight" onClick={closeMobileMenu}>
                                <i className="far fa-id-badge nav-icon"></i> {nav.adminPanel}
                            </NavLink>
                        )}
                    </>
                ) : (
                    <>
                        <NavLink to="/vehicles" onClick={closeMobileMenu}>
                            <i className="fas fa-car-side nav-icon"></i> {nav.vehicles}
                        </NavLink>
                        
                        <NavLink to="/login" onClick={closeMobileMenu}>
                            {nav.signIn}
                        </NavLink>
                        
                        <NavLink to="/register" className="register-nav-btn" onClick={closeMobileMenu}>
                            {nav.register} <i className="fas fa-user-plus"></i>
                        </NavLink>
                    </>
                )}

                {user && (
                    <button onClick={handleLogout} className="logout-btn">
                        {nav.logout}<i className="fas fa-sign-out-alt"></i>
                    </button>
                )}
            </div>
        </nav>
    );
};

export default Navbar;

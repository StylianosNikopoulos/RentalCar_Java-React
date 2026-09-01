import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import authService from '../services/authService';
import toast from 'react-hot-toast';
import { useLang } from '../context/LangContext';
import { translations } from '../i18n/translations';
import '../assets/styles/auth.css';

const ResetPassword = () => {
    
    useEffect(() => {
        document.title = 'RentalCar | Reset Password';
    }, []);

    const { lang } = useLang();
    const t = translations[lang].resetPassword;

    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const token = searchParams.get('token');

    const [passwords, setPasswords] = useState({ newPassword: '', confirmPassword: '' });
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setPasswords(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (isSubmitting) return;
        
        if (passwords.newPassword !== passwords.confirmPassword) {
            return toast.error(t.toastMismatch);
        }

        setIsSubmitting(true);
        const loadingToast = toast.loading(t.toastLoading);
        try {
            await authService.resetPassword(token, passwords.newPassword);
            toast.success(t.toastSuccess, { id: loadingToast });
            navigate('/login', { replace: true });
        } catch (error) {
            setIsSubmitting(false);
            const msg = error.response?.data?.message || t.toastError;
            toast.error(msg, { id: loadingToast });
        }
    };

    if (!token) {
        return (
            <div className="auth-container">
                <div className="auth-card" style={{ textAlign: 'center' }}>
                    <i className="fas fa-exclamation-triangle" style={{ fontSize: '2.5rem', color: '#ff4444', marginBottom: '1.5rem' }}></i>
                    <h2 style={{ fontSize: '1.8rem', marginBottom: '1rem' }}>{t.invalidTitle}</h2>
                    <p style={{ color: '#555', fontSize: '0.9rem', lineHeight: '1.6', marginBottom: '2rem' }}>
                        {t.invalidDesc}
                    </p>
                    <Link to="/login" className="back-to-login" style={{ marginTop: 0 }}>
                        <span className="arrow">←</span> {t.backToLogin}
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2>{t.title}</h2>
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>{t.newPasswordLabel}</label>
                        <input 
                            name="newPassword"
                            type="password" 
                            value={passwords.newPassword}
                            onChange={handleChange}
                            placeholder="••••••••"
                            required 
                            disabled={isSubmitting}
                        />
                    </div>
                    
                    <div className="form-group">
                        <label>{t.confirmPasswordLabel}</label>
                        <input 
                            name="confirmPassword"
                            type="password" 
                            value={passwords.confirmPassword}
                            onChange={handleChange}
                            placeholder="••••••••"
                            required 
                            disabled={isSubmitting}
                        />
                    </div>
                    
                    <button 
                        type="submit" 
                        className="auth-button" 
                        style={{ marginTop: '2rem' }}
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? '...' : <>{t.btnSubmit} <i className="fas fa-key"></i></>}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default ResetPassword;
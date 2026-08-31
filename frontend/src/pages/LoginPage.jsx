import React, { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import toast from 'react-hot-toast';
import { useNavigate, Link } from 'react-router-dom';
import { useLang } from '../context/LangContext';
import { translations } from '../i18n/translations';
import '../assets/styles/auth.css';

const LoginPage = () => {
    const { lang } = useLang();
    const t = translations[lang].login;

    const [credentials, setCredentials] = useState({ email: '', password: '' });
    const [isSubmitting, setIsSubmitting] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setCredentials(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (isSubmitting) return;

        setIsSubmitting(true);
        const loadingToast = toast.loading(t.toastLoading);
        try {
            await login(credentials);
            toast.success(t.toastSuccess, { id: loadingToast });
            navigate('/', { replace: true }); 
        } catch (error) {
            setIsSubmitting(false);
            const { code, message } = error.response?.data || {};
            if (code === "INVALID_CREDENTIALS") {
                toast.error(message, { id: loadingToast });
            } else {
                toast.error(message || t.toastError, { id: loadingToast });
            }
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2>{t.title}</h2>
                <p className="auth-subtitle">{t.subtitle || (lang === 'en' ? 'Welcome back! Please enter your details.' : 'Καλώς ήρθατε! Παρακαλώ εισάγετε τα στοιχεία σας.')}</p>
                
                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>{t.emailLabel}</label>
                        <input 
                            name="email"
                            type="email" 
                            value={credentials.email} 
                            onChange={handleChange} 
                            placeholder="name@example.com"
                            required 
                            disabled={isSubmitting}
                        />
                    </div>
                    
                    <div className="form-group">
                        <label>{t.passwordLabel}</label>
                        <input 
                            name="password"
                            type="password" 
                            value={credentials.password} 
                            onChange={handleChange} 
                            placeholder="••••••••"
                            required 
                            disabled={isSubmitting}
                        />
                    </div>

                    <div className="forgot-password-wrapper">
                        {isSubmitting ? (
                            <span className="disabled-link">{t.forgotPassword}</span>
                        ) : (
                            <Link to="/forgot-password">
                                {t.forgotPassword}
                            </Link>
                        )}
                    </div>

                    <button 
                        type="submit" 
                        className="auth-button" 
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? '...' : t.btnSubmit} <i className="fas fa-arrow-right"></i>
                    </button>
                </form>

                {isSubmitting ? (
                    <span className="auth-link disabled-link">{t.registerLink}</span>
                ) : (
                    <Link to="/register" className="auth-link">{t.registerLink}</Link>
                )}
            </div>
        </div>
    );
};

export default LoginPage;
import React, { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';
import toast from 'react-hot-toast';
import { useNavigate, Link } from 'react-router-dom';
import { useLang } from '../context/LangContext';
import { translations } from '../i18n/translations';
import '../assets/styles/auth.css';

const RegisterPage = () => {

    useEffect(() => {
        document.title = 'RentalCar | Register';
    }, []);

    const { lang } = useLang();
    const t = translations[lang].register;

    const [formData, setFormData] = useState({
        email: '',
        password: '',
        firstName: '',
        lastName: ''
    });
    
    const [isSubmitting, setIsSubmitting] = useState(false);
    const { register } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (isSubmitting) return;

        setIsSubmitting(true);
        const loadingToast = toast.loading(t.toastLoading);

        try {
            await register(formData);
            toast.success(t.toastSuccess, { id: loadingToast });
            navigate('/');
        } catch (error) {
            setIsSubmitting(false);
            toast.error(
                error.response?.data?.message || t.toastError,
                { id: loadingToast }
            );
        }
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <h2>{t.title}</h2>
                <form onSubmit={handleSubmit}>
                    <div className="auth-row-grid">
                        <div className="form-group">
                            <label>{t.firstName}</label>
                            <input 
                                name="firstName" 
                                value={formData.firstName} 
                                type="text" 
                                onChange={handleChange} 
                                placeholder={t.firstName}
                                required 
                                disabled={isSubmitting}
                            />
                        </div>
                        <div className="form-group">
                            <label>{t.lastName}</label>
                            <input 
                                name="lastName" 
                                value={formData.lastName} 
                                type="text" 
                                onChange={handleChange} 
                                placeholder={t.lastName}
                                required 
                                disabled={isSubmitting}
                            />
                        </div>
                    </div>
                    
                    <div className="form-group">
                        <label>{t.email}</label>
                        <input 
                            name="email" 
                            value={formData.email} 
                            type="email" 
                            onChange={handleChange} 
                            placeholder="name@example.com"
                            required 
                            disabled={isSubmitting}
                        />
                    </div>
                    
                    <div className="form-group">
                        <label>{t.password}</label>
                        <input 
                            name="password" 
                            value={formData.password} 
                            type="password" 
                            onChange={handleChange} 
                            placeholder={t.passwordPlaceholder}
                            required 
                            disabled={isSubmitting}
                        />
                    </div>
                    
                    <button 
                        type="submit" 
                        className="auth-button" 
                        disabled={isSubmitting}
                    >
                        {isSubmitting ? '...' : t.button} <i className="fas fa-user-plus"></i>
                    </button>
                </form>
                {isSubmitting ? (
                    <span className="auth-link disabled-link">{t.link}</span>
                ) : (
                    <Link to="/login" className="auth-link">{t.link}</Link>
                )}
            </div>
        </div>
    );
};

export default RegisterPage;
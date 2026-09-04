import React, { createContext, useState, useEffect, useContext } from 'react';

export const LangContext = createContext();

export const LangProvider = ({ children }) => {
    const [lang, setLang] = useState(() => {
        return localStorage.getItem('app_lang') || 'en';
    });

    useEffect(() => {
        localStorage.setItem('app_lang', lang);
        document.documentElement.lang = lang === 'gr' ? 'el' : 'en';
        document.documentElement.dataset.lang = lang;
    }, [lang]);

    const toggleLang = () => {
        setLang(prev => (prev === 'en' ? 'gr' : 'en'));
    };

    return (
        <LangContext.Provider value={{ lang, setLang, toggleLang }}>
            {children}
        </LangContext.Provider>
    );
};

export const useLang = () => {
    const context = useContext(LangContext);
    if (!context) {
        throw new Error('useLang must be used within a LangProvider');
    }
    return context;
};

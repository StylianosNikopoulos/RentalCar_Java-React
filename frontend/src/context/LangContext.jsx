import React, { createContext, useState, useEffect, useContext } from 'react';
import { translations } from '../i18n/translations'; 

export const LangContext = createContext();

const cleanGreek = (obj) => {
  if (typeof obj === 'string') {
    return obj
      .toUpperCase()
      .replace(/[ΆΑΑ]/g, 'A')
      .replace(/[ΈΕ]/g, 'E')
      .replace(/[ΉΗ]/g, 'H')
      .replace(/[ΊΪΙΊ]/g, 'I')
      .replace(/[ΌΟ]/g, 'O')
      .replace(/[ΎΫΥΎ]/g, 'Y')
      .replace(/[ΏΩ]/g, 'Ω');
  }
  if (typeof obj === 'object' && obj !== null) {
    const newObj = Array.isArray(obj) ? [] : {};
    for (let key in obj) {
      newObj[key] = cleanGreek(obj[key]);
    }
    return newObj;
  }
  return obj;
};

translations.gr = cleanGreek(translations.gr);

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

import React, { createContext, useContext, useEffect, useState } from 'react';

export const ThemeContext = createContext();

const getInitialTheme = () => {
    const savedTheme = localStorage.getItem('app_theme');
    if (savedTheme === 'light' || savedTheme === 'dark') return savedTheme;

    return window.matchMedia?.('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
};

export const ThemeProvider = ({ children }) => {
    const [theme, setTheme] = useState(getInitialTheme);

    useEffect(() => {
        document.documentElement.dataset.theme = theme;
        localStorage.setItem('app_theme', theme);
    }, [theme]);

    const toggleTheme = () => {
        setTheme(currentTheme => currentTheme === 'dark' ? 'light' : 'dark');
    };

    return (
        <ThemeContext.Provider value={{ theme, toggleTheme }}>
            {children}
        </ThemeContext.Provider>
    );
};

export const useTheme = () => {
    const context = useContext(ThemeContext);
    if (!context) {
        throw new Error('useTheme must be used within a ThemeProvider');
    }
    return context;
};

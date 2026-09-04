import React from 'react';
import { useLang } from '../../context/LangContext';
import { translations } from '../../i18n/translations';

const PaginationControls = ({ currentPage, totalPages, onPageChange }) => {
    const { lang } = useLang();
    const t = translations[lang].admin;

    return (
        <div className="pagination" style={{ marginTop: '20px', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '15px' }}>
            <button 
                onClick={() => onPageChange(prev => Math.max(prev - 1, 1))} 
                disabled={currentPage === 1} 
                className="page-btn"
            >
                <i className="fas fa-chevron-left"></i> {t.btnPrevious}
            </button>
            <span className="page-info" style={{ color: '#aaa', fontSize: '0.9rem' }}>
                {t.page} {currentPage} {t.of} {totalPages}
            </span>
            <button 
                onClick={() => onPageChange(prev => Math.min(prev + 1, totalPages))} 
                disabled={currentPage === totalPages} 
                className="page-btn"
            >
                {t.btnNext} <i className="fas fa-chevron-right"></i>
            </button>
        </div>
    );
};

export default PaginationControls;
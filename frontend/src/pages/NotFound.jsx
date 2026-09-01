import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import '../assets/styles/notfound.css'; 

const NotFound = () => {

    useEffect(() => {
        document.title = 'RentalCar | Page Not Found';
    }, []);

    return (
        <div className="notfound-container">
            <h1 className="notfound-code">404</h1>
            <h2 className="notfound-title">Oops! Page Not Found</h2>
            <Link to="/" className="notfound-btn">
                BACK TO HOME
            </Link>
        </div>
    );
};

export default NotFound;
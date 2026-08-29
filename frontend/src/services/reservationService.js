import api from '../api/axios';

const reservationService = {
    // Admin endpoints
    getAllReservations: async (page = 0, size = 9) => {
        const response = await api.get(`/admin/reservations?page=${page}&size=${size}`);
        return response.data;
    },

    returnVehicle: async (id) => {
        const response = await api.patch(`/admin/reservations/${id}/return`);
        return response.data;
    },

    // User endpoints
    getMyReservations: async (page = 0, size = 9) => {
        const response = await api.get(`/reservations/me?page=${page}&size=${size}`);
        return response.data;
    },

    getReservation: async (id) => {
        const response = await api.get(`/reservations/${id}`);
        return response.data;
    }, 

    createReservation: async (reservationData) => {
        const response = await api.post('/reservations',reservationData);
        return response.data;
    }, 

    cancelReservation: async (id) => {
        const response = await api.patch(`/reservations/${id}/cancel`);
        return response.data;
    },

    getVehicleReservations: async (vehicleId) => {
        const response = await api.get(`/reservations/vehicle/${vehicleId}`);
        return response.data; 
    }
};

export default reservationService;
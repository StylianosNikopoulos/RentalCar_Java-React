import api from '../api/axios';

const vehicleService = {
    // User endpoints
    getAllVehicles: async (page = 0, size = 9, sortOrder = 'default', searchTerm = '') => {
        let sortParam = 'id,desc';
        if (sortOrder === 'low') sortParam = 'dailyPrice,asc';
        if (sortOrder === 'high') sortParam = 'dailyPrice,desc';

        const response = await api.get('/vehicles', {
            params: {
                page: page,
                size: size,
                sort: sortParam,
                search: searchTerm
            }
        });
        return response.data;
    },

    getVehicleById: async (id) => {
        const response = await api.get(`/vehicles/${id}`);
        return response.data;
    },
  
    getAvailableVehicles: async (startDate, endDate, page = 0, size = 9, sortOrder = 'default', searchTerm = '') => {
        const startISO = `${startDate}T00:00:00`;
        const endISO = `${endDate}T23:59:59`;

        let sortParam = 'id,desc';
        if (sortOrder === 'low') sortParam = 'dailyPrice,asc';
        if (sortOrder === 'high') sortParam = 'dailyPrice,desc';

        const response = await api.get('/vehicles/available', {
            params: {
                start: startISO,
                end: endISO,
                page: page,
                size: size,
                sort: sortParam,
                search: searchTerm
            }
        });
        return response.data;
    },

    // Admin endpoints

    getAllVehiclesForAdmin: async (page = 0, size = 9, sortOrder = 'default', searchTerm = '') => {
        let sortParam = 'id,desc';
        if (sortOrder === 'low') sortParam = 'dailyPrice,asc';
        if (sortOrder === 'high') sortParam = 'dailyPrice,desc';

        const response = await api.get('/admin/vehicles', {
            params: { page, size, sort: sortParam, search: searchTerm }
        });
        return response.data;
    },  

    createVehicle: async (vehicleData) => {
        const response = await api.post('/admin/vehicles', vehicleData);
        return response.data;
    },

    updateVehicle: async (id, vehicleData) => {
        const response = await api.patch(`/admin/vehicles/${id}`, vehicleData);
        return response.data;
    },

    markVehicleOutOfService: async (id) => {
        await api.patch(`/admin/vehicles/${id}/out-of-service`);
    },

    restoreVehicle: async (id) => {
        const response = await api.patch(`/admin/vehicles/${id}/restore`);
        return response.data;
    }
};

export default vehicleService;
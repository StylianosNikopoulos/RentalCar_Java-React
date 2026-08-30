import api from '../api/axios';

const userService = {
    // User endpoints
    getMyProfile: async () => {
        const response = await api.get('/users/me');
        return response.data;
    },

    updateMyProfile: async (userData) => {
        const response = await api.put('/users/me', userData);
        return response.data;
    },

    // Admin endpoints
    getAllUsers: async (page = 0, size = 9) => {
        const response = await api.get(`/admin/users?page=${page}&size=${size}`); 
        return response.data;
    },
    
    getUserById: async (id) => {
        const response = await api.get(`/admin/users/${id}`);
        return response.data;
    }, 

    deleteUser: async (id) => {
        const response = await api.delete(`/admin/users/${id}`);
        return response.data;
    }
};

export default userService;
import api from '../api/axios';

const authService = {
    login: async (loginRequest) => {
            const response = await api.post('/auth/login', loginRequest);
            if (response.data && response.data.accessToken) {
                localStorage.setItem('user', JSON.stringify(response.data));
            }
            return response.data; 
        },

    register: async (registerRequest) => {
            const response = await api.post('/auth/register', registerRequest);
            if (response.data && response.data.accessToken) {
                localStorage.setItem('user', JSON.stringify(response.data));
            }
            return response.data;
    },

    forgotPassword: async (email) => {
        const response = await api.post('/auth/forgot-password', { email });
        return response.data;
    },

    resetPassword: async (token, newPassword) => {
        const response = await api.post('/auth/reset-password', { token, newPassword });
        return response.data;
    },

    logout: () => {
        localStorage.removeItem('user');
    },

    getCurrentUser: () => {
        return JSON.parse(localStorage.getItem('user'));
    },

    // Admin endpoints
    refreshUserTokenByUserId: async (userId) => {
        const response = await api.post(`/admin/auth/users/${userId}/refresh`);
        return response.data;
    }
};

export default authService;
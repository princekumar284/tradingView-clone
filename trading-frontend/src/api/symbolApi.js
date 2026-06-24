import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

export const getAllSymbols = async (accessToken) => {
    const response = await axios.get(`${BASE_URL}/symbols`, {
        headers: { Authorization: `Bearer ${accessToken}` },
        withCredentials: true
    });
    return response.data;
};

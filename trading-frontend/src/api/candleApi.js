import axios from 'axios';

const BASE_URL = 'http://localhost:8080/api';

export const getCandles = async (ticker, interval = '1D', accessToken) => {
    const response = await axios.get(`${BASE_URL}/candles/${ticker}`, {
        params: { interval },
        headers: { Authorization: `Bearer ${accessToken}` },
        withCredentials: true
    });
    return response.data;
};

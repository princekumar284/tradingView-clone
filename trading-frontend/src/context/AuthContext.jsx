import { createContext, useContext, useState, useEffect } from 'react';
import { refreshToken, logout as logoutApi } from '../api/authApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [accessToken, setAccessToken] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        refreshToken()
            .then(data => setAccessToken(data.accessToken))
            .catch(() => setAccessToken(null))
            .finally(() => setLoading(false));
    }, []);

    const login = (token) => setAccessToken(token);

    const logout = async () => {
        try {
            await logoutApi();
        } catch (e) {}
        setAccessToken(null);
    };

    return (
        <AuthContext.Provider value={{ accessToken, login, logout, loading }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}

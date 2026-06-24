import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createChart, CandlestickSeries } from 'lightweight-charts';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from '../context/AuthContext';
import { getCandles } from '../api/candleApi';
import { getAllSymbols } from '../api/symbolApi';

const CRYPTO_SYMBOLS = [
    'BTCUSDT', 'ETHUSDT', 'BNBUSDT', 'SOLUSDT', 'XRPUSDT',
    'ADAUSDT', 'DOGEUSDT', 'MATICUSDT', 'DOTUSDT', 'LINKUSDT',
    'AVAXUSDT', 'UNIUSDT', 'LTCUSDT', 'ATOMUSDT', 'XLMUSDT'
];

export default function ChartPage() {
    const { accessToken, logout } = useAuth();
    const navigate = useNavigate();

    const chartContainerRef = useRef(null);
    const chartRef = useRef(null);
    const seriesRef = useRef(null);
    const stompClientRef = useRef(null);
    const requestIdRef = useRef(0);

    const [ticker, setTicker] = useState('BTCUSDT');
    const [searchInput, setSearchInput] = useState('BTCUSDT');
    const [interval, setInterval] = useState('1D');
    const [livePrice, setLivePrice] = useState(null);
    const [priceChange, setPriceChange] = useState(null);
    const [priceChangePercent, setPriceChangePercent] = useState(null);
    const [error, setError] = useState('');
    const [symbols, setSymbols] = useState([]);
    const [showDropdown, setShowDropdown] = useState(false);

    const isCrypto = CRYPTO_SYMBOLS.includes(ticker.toUpperCase());
    const INTERVALS = isCrypto
        ? ['1H', '4H', '1D', '1W']
        : ['1D'];

    // Fetch all symbols on mount for dropdown
    useEffect(() => {
        if (!accessToken) return;
        getAllSymbols(accessToken)
            .then(data => setSymbols(data))
            .catch(() => {});
    }, [accessToken]);

    // Create chart once on mount
    useEffect(() => {
        const chart = createChart(chartContainerRef.current, {
            width: chartContainerRef.current.clientWidth,
            height: 450,
            layout: {
                background: { type: 'solid', color: '#1e222d' },
                textColor: '#d1d4dc',
            },
            grid: {
                vertLines: { color: '#2a2e39' },
                horzLines: { color: '#2a2e39' },
            },
            crosshair: { mode: 1 },
            rightPriceScale: { borderColor: '#363a45' },
            timeScale: { borderColor: '#363a45', timeVisible: true },
        });

        const series = chart.addSeries(CandlestickSeries, {
            upColor: '#26a69a',
            downColor: '#ef5350',
            borderVisible: false,
            wickUpColor: '#26a69a',
            wickDownColor: '#ef5350',
        });

        chartRef.current = chart;
        seriesRef.current = series;

        const handleResize = () => {
            chart.applyOptions({ width: chartContainerRef.current.clientWidth });
        };
        window.addEventListener('resize', handleResize);

        return () => {
            window.removeEventListener('resize', handleResize);
            chart.remove();
        };
    }, []);

    // Fetch candles when ticker or interval changes
    useEffect(() => {
        if (!seriesRef.current || !accessToken) return;

        const currentId = ++requestIdRef.current;
        setError('');
        setLivePrice(null);

        const fetchCandles = async () => {
            try {
                const candles = await getCandles(ticker, interval, accessToken);

                if (currentId !== requestIdRef.current) return;

                const formatted = candles
                    .map(c => ({
                        time: Math.floor(new Date(c.timestamp).getTime() / 1000),
                        open: parseFloat(c.open),
                        high: parseFloat(c.high),
                        low: parseFloat(c.low),
                        close: parseFloat(c.close),
                    }))
                    .sort((a, b) => a.time - b.time);

                seriesRef.current.setData(formatted);
                chartRef.current.timeScale().fitContent();
                setError('');
            } catch (err) {
                if (currentId !== requestIdRef.current) return;
                setError('Failed to load chart data. Check if symbol is valid.');
            }
        };

        fetchCandles();
    }, [ticker, interval, accessToken]);

    // WebSocket for live price (crypto only)
    useEffect(() => {
        if (!CRYPTO_SYMBOLS.includes(ticker.toUpperCase())) return;

        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
            onConnect: () => {
                client.subscribe(`/topic/price/${ticker.toUpperCase()}`, (message) => {
                    const data = JSON.parse(message.body);
                    setLivePrice(parseFloat(data.price));
                    setPriceChange(parseFloat(data.change));
                    setPriceChangePercent(parseFloat(data.changePercent));
                });
            },
            reconnectDelay: 5000,
        });

        client.activate();
        stompClientRef.current = client;

        return () => {
            client.deactivate();
        };
    }, [ticker]);

    const handleSearch = (e) => {
        e.preventDefault();
        const symbol = searchInput.trim().toUpperCase();
        if (symbol) {
            setTicker(symbol);
            setShowDropdown(false);
        }
    };

    const handleSymbolSelect = (symbol) => {
        setSearchInput(symbol);
        setTicker(symbol);
        setInterval('1D');
        setShowDropdown(false);
    };

    const filteredSymbols = symbols
        .filter(s =>
            s.ticker.toUpperCase().includes(searchInput.toUpperCase()) ||
            s.name.toUpperCase().includes(searchInput.toUpperCase())
        )
        .slice(0, 30);

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    const isPositive = priceChange >= 0;

    return (
        <div style={styles.page}>
            {/* Header */}
            <div style={styles.header}>
                <span style={styles.logo}>TradingView Clone</span>

                <form onSubmit={handleSearch} style={styles.searchForm}>
                    <div style={styles.searchWrapper}>
                        <input
                            type="text"
                            value={searchInput}
                            onChange={(e) => {
                                setSearchInput(e.target.value);
                                setShowDropdown(true);
                            }}
                            onFocus={() => setShowDropdown(true)}
                            onBlur={() => setTimeout(() => setShowDropdown(false), 150)}
                            placeholder="Search symbol... (e.g. AAPL, BTCUSDT)"
                            style={styles.searchInput}
                        />
                        {showDropdown && filteredSymbols.length > 0 && (
                            <div style={styles.dropdown}>
                                {filteredSymbols.map(s => (
                                    <div
                                        key={s.ticker}
                                        style={styles.dropdownItem}
                                        onMouseDown={() => handleSymbolSelect(s.ticker)}
                                    >
                                        <span style={styles.dropdownTicker}>{s.ticker}</span>
                                        <span style={styles.dropdownName}>{s.name}</span>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                    <button type="submit" style={styles.searchButton}>Search</button>
                </form>

                <button onClick={handleLogout} style={styles.logoutButton}>Logout</button>
            </div>

            {/* Price Info */}
            <div style={styles.priceBar}>
                <span style={styles.tickerName}>{ticker}</span>
                {livePrice ? (
                    <>
                        <span style={styles.price}>${livePrice.toLocaleString()}</span>
                        <span style={{ ...styles.change, color: isPositive ? '#26a69a' : '#ef5350' }}>
                            {isPositive ? '▲' : '▼'} {Math.abs(priceChange).toFixed(2)} ({Math.abs(priceChangePercent).toFixed(2)}%)
                        </span>
                    </>
                ) : (
                    <span style={styles.noPrice}>
                        {CRYPTO_SYMBOLS.includes(ticker) ? 'Connecting...' : 'Live price not available for stocks'}
                    </span>
                )}
            </div>

            {/* Interval Selector */}
            <div style={styles.intervalBar}>
                {INTERVALS.map(i => (
                    <button
                        key={i}
                        onClick={() => setInterval(i)}
                        style={{
                            ...styles.intervalBtn,
                            ...(interval === i ? styles.intervalBtnActive : {}),
                        }}
                    >
                        {i}
                    </button>
                ))}
            </div>

            {/* Error */}
            {error && <p style={styles.error}>{error}</p>}

            {/* Chart */}
            <div style={styles.chartWrapper}>
                <div ref={chartContainerRef} style={styles.chart} />
            </div>
        </div>
    );
}

const styles = {
    page: {
        backgroundColor: '#131722',
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
    },
    header: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0.75rem 1.5rem',
        backgroundColor: '#1e222d',
        borderBottom: '1px solid #2a2e39',
        gap: '1rem',
    },
    logo: {
        color: '#d1d4dc',
        fontWeight: 'bold',
        fontSize: '1rem',
        whiteSpace: 'nowrap',
    },
    searchForm: {
        display: 'flex',
        gap: '0.5rem',
        flex: 1,
        maxWidth: '400px',
    },
    searchWrapper: {
        position: 'relative',
        flex: 1,
    },
    dropdown: {
        position: 'absolute',
        top: '100%',
        left: 0,
        right: 0,
        backgroundColor: '#1e222d',
        border: '1px solid #363a45',
        borderRadius: '4px',
        zIndex: 100,
        maxHeight: '250px',
        overflowY: 'auto',
    },
    dropdownHeader: {
        padding: '0.4rem 0.75rem',
        fontSize: '0.7rem',
        color: '#2962ff',
        fontWeight: 'bold',
        letterSpacing: '0.08em',
        backgroundColor: '#2a2e39',
    },
    dropdownItem: {
        display: 'flex',
        justifyContent: 'space-between',
        padding: '0.6rem 0.75rem',
        cursor: 'pointer',
        borderBottom: '1px solid #2a2e39',
    },
    dropdownTicker: {
        color: '#d1d4dc',
        fontWeight: 'bold',
        fontSize: '0.85rem',
    },
    dropdownName: {
        color: '#787b86',
        fontSize: '0.8rem',
    },
    searchInput: {
        flex: 1,
        backgroundColor: '#2a2e39',
        border: '1px solid #363a45',
        borderRadius: '4px',
        padding: '0.5rem 0.75rem',
        color: '#d1d4dc',
        fontSize: '0.9rem',
        outline: 'none',
    },
    searchButton: {
        backgroundColor: '#2962ff',
        color: '#fff',
        border: 'none',
        borderRadius: '4px',
        padding: '0.5rem 1rem',
        cursor: 'pointer',
        fontSize: '0.9rem',
    },
    logoutButton: {
        backgroundColor: 'transparent',
        color: '#787b86',
        border: '1px solid #363a45',
        borderRadius: '4px',
        padding: '0.5rem 1rem',
        cursor: 'pointer',
        fontSize: '0.85rem',
    },
    priceBar: {
        display: 'flex',
        alignItems: 'center',
        gap: '1rem',
        padding: '0.75rem 1.5rem',
        backgroundColor: '#1e222d',
        borderBottom: '1px solid #2a2e39',
    },
    tickerName: {
        color: '#d1d4dc',
        fontWeight: 'bold',
        fontSize: '1.1rem',
    },
    price: {
        color: '#d1d4dc',
        fontSize: '1.1rem',
    },
    change: {
        fontSize: '0.95rem',
    },
    noPrice: {
        color: '#787b86',
        fontSize: '0.85rem',
    },
    error: {
        color: '#f7525f',
        padding: '0.75rem 1.5rem',
        fontSize: '0.85rem',
    },
    intervalBar: {
        display: 'flex',
        gap: '0.4rem',
        padding: '0.6rem 1.5rem',
        backgroundColor: '#131722',
        borderBottom: '1px solid #2a2e39',
    },
    intervalBtn: {
        backgroundColor: 'transparent',
        color: '#787b86',
        border: 'none',
        borderRadius: '4px',
        padding: '0.3rem 0.7rem',
        cursor: 'pointer',
        fontSize: '0.85rem',
    },
    intervalBtnActive: {
        backgroundColor: '#2962ff',
        color: '#ffffff',
    },
    chartWrapper: {
        flex: 1,
        padding: '1rem 1.5rem',
    },
    chart: {
        width: '100%',
    },
};

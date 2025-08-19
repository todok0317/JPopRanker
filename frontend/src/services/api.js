import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Songs API
export const songApi = {
  getAllSongs: () => api.get('/songs'),
  getSongsByChart: (chartName) => api.get(`/songs/chart/${chartName}`),
};

// Crawler API
export const crawlerApi = {
  crawlBillboardJapan: () => api.post('/crawler/billboard-japan'),
  crawlOricon: () => api.post('/crawler/oricon'),
  crawlAll: () => api.post('/crawler/all'),
  getStatus: () => api.get('/crawler/status'),
};

export default api;
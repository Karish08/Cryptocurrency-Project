import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';

export const fetchDashboardStats = createAsyncThunk('dashboard/fetchStats', async () => {
  return {
    blockchainDistribution: [],
    recentAddresses: [],
    recentTransactions: []
  };
});

const initialState = {
  stats: {},
  loading: false,
  error: null,
};

const dashboardSlice = createSlice({
  name: 'dashboard',
  initialState,
  reducers: {},
});

export default dashboardSlice.reducer;

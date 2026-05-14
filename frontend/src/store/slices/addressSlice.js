import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';
import { toast } from 'react-toastify';

const API_URL = 'http://localhost:8080/api/addresses';

// Axios interceptor for token
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const fetchAddresses = createAsyncThunk(
  'addresses/fetchAll',
  async ({ page = 0, size = 10, sortBy = 'id', sortDirection = 'desc' } = {}, { rejectWithValue }) => {
    try {
      const response = await axios.get(`${API_URL}?page=${page}&size=${size}&sortBy=${sortBy}&sortDirection=${sortDirection}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data);
    }
  }
);

export const createAddress = createAsyncThunk(
  'addresses/create',
  async (addressData, { rejectWithValue }) => {
    try {
      const response = await axios.post(API_URL, addressData);
      toast.success('Address added successfully');
      return response.data;
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to add address');
      return rejectWithValue(error.response?.data);
    }
  }
);

export const updateAddress = createAsyncThunk(
  'addresses/update',
  async ({ id, ...addressData }, { rejectWithValue }) => {
    try {
      const response = await axios.put(`${API_URL}/${id}`, addressData);
      toast.success('Address updated successfully');
      return response.data;
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to update address');
      return rejectWithValue(error.response?.data);
    }
  }
);

export const deleteAddress = createAsyncThunk(
  'addresses/delete',
  async (id, { rejectWithValue }) => {
    try {
      await axios.delete(`${API_URL}/${id}`);
      toast.success('Address deleted successfully');
      return id;
    } catch (error) {
      toast.error(error.response?.data?.message || 'Failed to delete address');
      return rejectWithValue(error.response?.data);
    }
  }
);

export const refreshBalance = createAsyncThunk(
  'addresses/refreshBalance',
  async (id, { rejectWithValue }) => {
    try {
      const response = await axios.post(`${API_URL}/${id}/refresh`);
      toast.success('Balance updated successfully');
      return response.data;
    } catch (error) {
      toast.error('Failed to refresh balance');
      return rejectWithValue(error.response?.data);
    }
  }
);

export const searchAddresses = createAsyncThunk(
  'addresses/search',
  async ({ query, page = 0, size = 10 }, { rejectWithValue }) => {
    try {
      const response = await axios.get(`${API_URL}/search?q=${query}&page=${page}&size=${size}`);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data);
    }
  }
);

const initialState = {
  addresses: [],
  currentAddress: null,
  totalElements: 0,
  totalPages: 0,
  currentPage: 0,
  isLoading: false,
  error: null,
};

const addressSlice = createSlice({
  name: 'addresses',
  initialState,
  reducers: {
    clearCurrentAddress: (state) => {
      state.currentAddress = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch addresses
      .addCase(fetchAddresses.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchAddresses.fulfilled, (state, action) => {
        state.isLoading = false;
        state.addresses = action.payload.content;
        state.totalElements = action.payload.totalElements;
        state.totalPages = action.payload.totalPages;
        state.currentPage = action.payload.number;
      })
      .addCase(fetchAddresses.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload;
      })
      // Create address
      .addCase(createAddress.fulfilled, (state, action) => {
        state.addresses.unshift(action.payload);
      })
      // Update address
      .addCase(updateAddress.fulfilled, (state, action) => {
        const index = state.addresses.findIndex(a => a.id === action.payload.id);
        if (index !== -1) {
          state.addresses[index] = action.payload;
        }
      })
      // Delete address
      .addCase(deleteAddress.fulfilled, (state, action) => {
        state.addresses = state.addresses.filter(a => a.id !== action.payload);
      })
      // Refresh balance
      .addCase(refreshBalance.fulfilled, (state, action) => {
        const index = state.addresses.findIndex(a => a.id === action.payload.id);
        if (index !== -1) {
          state.addresses[index] = action.payload;
        }
      })
      // Search addresses
      .addCase(searchAddresses.pending, (state) => {
        state.isLoading = true;
      })
      .addCase(searchAddresses.fulfilled, (state, action) => {
        state.isLoading = false;
        state.addresses = action.payload.content;
        state.totalElements = action.payload.totalElements;
        state.totalPages = action.payload.totalPages;
        state.currentPage = action.payload.number;
      });
  },
});

export const { clearCurrentAddress } = addressSlice.actions;
export default addressSlice.reducer;
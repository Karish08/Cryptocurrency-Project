import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import addressReducer from './slices/addressSlice';
import categoryReducer from './slices/categorySlice';
import transactionReducer from './slices/transactionSlice';
import dashboardReducer from './slices/dashboardSlice';
import uiReducer from './slices/uiSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    addresses: addressReducer,
    categories: categoryReducer,
    transactions: transactionReducer,
    dashboard: dashboardReducer,
    ui: uiReducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: false,
    }),
});
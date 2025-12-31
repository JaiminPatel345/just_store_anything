import { configureStore } from '@reduxjs/toolkit';
import fileReducer from './fileSlice';
import authReducer from './authSlice';

export const store = configureStore({
  reducer: {
    file: fileReducer,
    auth: authReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

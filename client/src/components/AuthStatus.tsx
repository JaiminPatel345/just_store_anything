import React, { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import type { AppDispatch, RootState } from '../store';
import { checkAuthStatus, logout, setAuthenticated } from '../store/authSlice';
import { LogIn, LogOut, Loader2, Youtube, CheckCircle, XCircle } from 'lucide-react';
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const AuthStatus: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { isAuthenticated, loading } = useSelector((state: RootState) => state.auth);
  const [loginLoading, setLoginLoading] = useState(false);

  useEffect(() => {
    // Check auth status on mount
    dispatch(checkAuthStatus());

    // Check for auth callback params in URL
    const urlParams = new URLSearchParams(window.location.search);
    const authStatus = urlParams.get('auth');
    
    if (authStatus === 'success') {
      dispatch(setAuthenticated(true));
      // Clean up URL
      window.history.replaceState({}, document.title, window.location.pathname);
    } else if (authStatus === 'error') {
      dispatch(setAuthenticated(false));
      // Clean up URL
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }, [dispatch]);

  const handleLogin = async () => {
    setLoginLoading(true);
    try {
      const response = await axios.get(`${API_URL}/auth/youtube/login`);
      const { authUrl } = response.data;
      // Redirect to Google OAuth
      window.location.href = authUrl;
    } catch (error) {
      console.error('Failed to get login URL:', error);
      setLoginLoading(false);
    }
  };

  const handleLogout = () => {
    dispatch(logout());
  };

  if (loading) {
    return (
      <div className="flex items-center gap-2 px-4 py-2 bg-gray-800 rounded-lg">
        <Loader2 className="w-4 h-4 animate-spin text-gray-400" />
        <span className="text-sm text-gray-400">Checking...</span>
      </div>
    );
  }

  return (
    <div className="flex items-center gap-3">
      {/* Status indicator */}
      <div className="flex items-center gap-2 px-3 py-1.5 bg-gray-800/50 rounded-lg border border-gray-700">
        <Youtube className="w-4 h-4 text-red-500" />
        {isAuthenticated ? (
          <>
            <CheckCircle className="w-4 h-4 text-green-500" />
            <span className="text-sm text-green-400">Connected</span>
          </>
        ) : (
          <>
            <XCircle className="w-4 h-4 text-yellow-500" />
            <span className="text-sm text-yellow-400">Not connected</span>
          </>
        )}
      </div>

      {/* Login/Logout button */}
      {isAuthenticated ? (
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 px-4 py-2 bg-gray-700 hover:bg-gray-600 
                     text-white rounded-lg transition-colors text-sm font-medium"
        >
          <LogOut className="w-4 h-4" />
          Disconnect
        </button>
      ) : (
        <button
          onClick={handleLogin}
          disabled={loginLoading}
          className="flex items-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 
                     text-white rounded-lg transition-colors text-sm font-medium
                     disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {loginLoading ? (
            <Loader2 className="w-4 h-4 animate-spin" />
          ) : (
            <LogIn className="w-4 h-4" />
          )}
          Connect YouTube
        </button>
      )}
    </div>
  );
};

export default AuthStatus;

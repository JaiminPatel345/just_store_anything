import React from 'react';
import { Outlet } from 'react-router-dom';
import TabSwitcher from './TabSwitcher';
import AuthStatus from './AuthStatus';

const Layout: React.FC = () => {
  return (
    <div className="min-h-screen bg-[#1a1a1a] text-white font-sans selection:bg-blue-500/30">
      <main className="max-w-7xl mx-auto px-4 py-12 sm:px-6 lg:px-8">
        <div className="flex flex-col items-center">
          <div className="mb-6 text-center">
            <h1 className="text-4xl font-extrabold tracking-tight text-white mb-2">JustStore</h1>
            <p className="text-gray-500">Secure Video Storage & Retrieval</p>
          </div>
          
          {/* YouTube Auth Status */}
          <div className="mb-6">
            <AuthStatus />
          </div>
          
          <TabSwitcher />
          
          <div className="w-full max-w-3xl">
            <Outlet />
          </div>
        </div>
      </main>
    </div>
  );
};

export default Layout;

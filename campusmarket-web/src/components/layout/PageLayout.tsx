import { ReactNode } from 'react';
import { Outlet } from 'react-router-dom';
import { Navbar } from './Navbar';
import { clsx } from 'clsx';

interface PageLayoutProps {
  children?: ReactNode;
  className?: string;
}

export function PageLayout({ children, className }: PageLayoutProps) {
  return (
    <div className={clsx('min-h-screen bg-gray-50 dark:bg-gray-900', className)}>
      <Navbar />
      <main className="lg:pl-64 min-h-screen">
        <div className="container-page py-6 lg:py-8">
          {children || <Outlet />}
        </div>
      </main>
    </div>
  );
}

interface AuthPageLayoutProps {
  children: ReactNode;
}

export function AuthPageLayout({ children }: AuthPageLayoutProps) {
  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="inline-flex items-center gap-2 mb-6" aria-label="CampusMarket Home">
            <div className="h-12 w-12 rounded-xl bg-primary-600 flex items-center justify-center">
              <svg className="h-7 w-7 text-white" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" fill="none"/>
              </svg>
            </div>
            <span className="text-2xl font-bold text-gray-900 dark:text-white">CampusMarket</span>
          </Link>
          <p className="text-gray-500 dark:text-gray-400">Buy and sell items on campus</p>
        </div>
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 shadow-card p-6 sm:p-8">
          {children}
        </div>
        <p className="text-center text-sm text-gray-500 dark:text-gray-400 mt-6">
          By continuing, you agree to our <a href="#" className="text-primary-600 hover:underline">Terms of Service</a> and <a href="#" className="text-primary-600 hover:underline">Privacy Policy</a>
        </p>
      </div>
    </div>
  );
}

import { Link } from 'react-router-dom';
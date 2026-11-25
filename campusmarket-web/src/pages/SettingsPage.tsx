import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '@hooks/useAuth';
import { useToast } from '@components/ui/Toast';
import { PageLayout } from '@components/layout/PageLayout';
import { Button } from '@components/ui/Button';
import { LogOut, Moon, Sun, Bell, Shield } from 'lucide-react';

export function SettingsPage() {
  const { user, signOut } = useAuth();
  const { toast } = useToast();
  const [darkMode, setDarkMode] = useState(false);
  const [notifications, setNotifications] = useState(true);

  const handleSignOut = async () => {
    await signOut();
    toast({ type: 'success', message: 'Signed out successfully' });
  };

  return (
    <PageLayout>
      <div className="max-w-2xl mx-auto space-y-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Settings</h1>
          <p className="text-gray-500 dark:text-gray-400 mt-1">Manage your account and preferences</p>
        </div>

        {/* Account */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-5 space-y-4">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Account</h2>
          <div className="flex items-center gap-4 p-4 bg-gray-50 dark:bg-gray-700 rounded-xl">
            <div className="h-12 w-12 rounded-full bg-primary-100 dark:bg-primary-900/30 flex items-center justify-center">
              <span className="text-xl font-medium text-primary-600 dark:text-primary-400">
                {user?.email?.charAt(0).toUpperCase() || 'U'}
              </span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="font-medium text-gray-900 dark:text-white truncate">{user?.email}</p>
              <p className="text-sm text-gray-500 dark:text-gray-400">Verified Student</p>
            </div>
          </div>
          <Button variant="danger" onClick={handleSignOut} className="w-full">
            <LogOut className="h-4 w-4 mr-2" />
            Sign Out
          </Button>
        </div>

        {/* Appearance */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-5 space-y-4">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Appearance</h2>
          <div className="flex items-center justify-between">
            <div>
              <p className="font-medium text-gray-900 dark:text-white">Dark Mode</p>
              <p className="text-sm text-gray-500 dark:text-gray-400">Switch between light and dark theme</p>
            </div>
            <Button
              variant={darkMode ? 'primary' : 'outline'}
              onClick={() => setDarkMode(!darkMode)}
              className="w-20"
            >
              {darkMode ? <Moon className="h-5 w-5" /> : <Sun className="h-5 w-5" />}
            </Button>
          </div>
        </div>

        {/* Notifications */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-5 space-y-4">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Notifications</h2>
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium text-gray-900 dark:text-white">Match Notifications</p>
                <p className="text-sm text-gray-500 dark:text-gray-400">Get notified when new matches are found</p>
              </div>
              <Button
                variant={notifications ? 'primary' : 'outline'}
                onClick={() => setNotifications(!notifications)}
                className="w-20"
              >
                <Bell className="h-5 w-5" />
              </Button>
            </div>
          </div>
        </div>

        {/* Privacy & Safety */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-5 space-y-4">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Privacy & Safety</h2>
          <div className="space-y-2">
            <Link to="/blocked" className="flex items-center justify-between p-3 rounded-xl hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
              <div className="flex items-center gap-3">
                <Shield className="h-5 w-5 text-gray-500" />
                <div>
                  <p className="font-medium text-gray-900 dark:text-white">Blocked Sellers</p>
                  <p className="text-sm text-gray-500 dark:text-gray-400">Manage who you've blocked</p>
                </div>
              </div>
              <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" /></svg>
            </Link>
          </div>
        </div>

        {/* About */}
        <div className="bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 p-5">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">About</h2>
          <div className="space-y-2 text-sm text-gray-500 dark:text-gray-400">
            <p>CampusMarket v1.0.0</p>
            <p>A campus marketplace for verified students</p>
            <p className="pt-2">
              <a href="#" className="text-primary-600 hover:underline">Privacy Policy</a> •
              <a href="#" className="text-primary-600 hover:underline ml-2">Terms of Service</a>
            </p>
          </div>
        </div>
      </div>
    </PageLayout>
  );
}
import { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { clsx } from 'clsx';
import type { LucideIcon } from 'lucide-react';
import {
  Home,
  Package,
  GitMerge,
  List,
  LogOut,
  Menu,
  X,
  User,
  Settings,
} from 'lucide-react';
import { useAuth } from '@hooks/useAuth';
import { useMatchBadge } from '@hooks/useMatches';
import { Badge } from '@components/ui/Badge';

interface NavItem {
  name: string;
  href: string;
  icon: LucideIcon;
  badge?: boolean;
}

const navigation: NavItem[] = [
  { name: 'Home', href: '/', icon: Home },
  { name: 'Move-out', href: '/bundles', icon: Package },
  { name: 'Matches', href: '/matches', icon: GitMerge, badge: true },
  { name: 'My Listings', href: '/my-listings', icon: List },
];

export function Navbar() {
  const { user, signOut } = useAuth();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const matchBadge = useMatchBadge();

  useEffect(() => {
    setMobileMenuOpen(false);
  }, [location.pathname]);

  const handleSignOut = async () => {
    await signOut();
  };

  return (
    <>
      {/* Mobile top bar */}
      <header className="fixed top-0 left-0 right-0 z-40 bg-white border-b border-gray-200 dark:bg-gray-900 dark:border-gray-800 lg:hidden">
        <div className="flex items-center justify-between h-16 px-4">
          <Link to="/" className="flex items-center gap-2" aria-label="CampusMarket Home">
            <div className="h-8 w-8 rounded-lg bg-primary-600 flex items-center justify-center">
              <Home className="h-5 w-5 text-white" />
            </div>
            <span className="text-xl font-bold text-gray-900 dark:text-white">CampusMarket</span>
          </Link>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-lg text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800"
              aria-label={mobileMenuOpen ? 'Close menu' : 'Open menu'}
              aria-expanded={mobileMenuOpen}
            >
              {mobileMenuOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
            </button>
          </div>
        </div>

        {mobileMenuOpen && (
          <nav className="border-t border-gray-200 dark:border-gray-800 py-4 px-4 space-y-2" role="navigation" aria-label="Main navigation">
            {navigation.map((item) => (
              <Link
                key={item.name}
                to={item.href}
                className={clsx(
                  'flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors',
                  location.pathname === item.href
                    ? 'bg-primary-50 text-primary-700 dark:bg-primary-900/30 dark:text-primary-300'
                    : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800'
                )}
                onClick={() => setMobileMenuOpen(false)}
              >
                <item.icon className="h-5 w-5 flex-shrink-0" aria-hidden="true" />
                {item.name}
                {item.badge && matchBadge > 0 && (
                  <Badge variant="primary" className="ml-auto">
                    {matchBadge}
                  </Badge>
                )}
              </Link>
            ))}

            <hr className="my-4 border-gray-200 dark:border-gray-700" />

            {user ? (
              <>
                <Link
                  to="/settings"
                  className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800"
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <Settings className="h-5 w-5" />
                  Settings
                </Link>
                <button
                  onClick={handleSignOut}
                  className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-900/20 w-full text-left"
                >
                  <LogOut className="h-5 w-5" />
                  Sign out
                </button>
              </>
            ) : (
              <Link
                to="/login"
                className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-primary-600 hover:bg-primary-50 dark:text-primary-400 dark:hover:bg-primary-900/20 w-full justify-center"
                onClick={() => setMobileMenuOpen(false)}
              >
                <LogOut className="h-5 w-5" />
                Sign in
              </Link>
            )}
          </nav>
        )}
      </header>

      {/* Desktop sidebar */}
      <aside className="hidden lg:fixed lg:inset-y-0 lg:left-0 lg:z-40 lg:w-64 lg:bg-white lg:border-r lg:border-gray-200 dark:lg:bg-gray-900 dark:lg:border-gray-800 lg:flex lg:flex-col">
        <div className="flex flex-col h-full">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-3 px-6 py-5 border-b border-gray-200 dark:border-gray-800" aria-label="CampusMarket Home">
            <div className="h-10 w-10 rounded-xl bg-primary-600 flex items-center justify-center">
              <Home className="h-6 w-6 text-white" />
            </div>
            <span className="text-xl font-bold text-gray-900 dark:text-white">CampusMarket</span>
          </Link>

          {/* Navigation */}
          <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto" role="navigation" aria-label="Main navigation">
            {navigation.map((item) => (
              <Link
                key={item.name}
                to={item.href}
                className={clsx(
                  'flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors',
                  location.pathname === item.href
                    ? 'bg-primary-50 text-primary-700 dark:bg-primary-900/30 dark:text-primary-300'
                    : 'text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800'
                )}
              >
                <item.icon className="h-5 w-5 flex-shrink-0" aria-hidden="true" />
                {item.name}
                {item.badge && matchBadge > 0 && (
                  <Badge variant="primary" className="ml-auto">
                    {matchBadge}
                  </Badge>
                )}
              </Link>
            ))}
          </nav>

          {/* User section */}
          <div className="border-t border-gray-200 dark:border-gray-800 p-4">
            {user ? (
              <div className="space-y-2">
                <div className="flex items-center gap-3 px-3 py-2">
                  <div className="h-10 w-10 rounded-full bg-primary-100 dark:bg-primary-900/30 flex items-center justify-center">
                    <User className="h-5 w-5 text-primary-600 dark:text-primary-400" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 dark:text-white truncate">
                      {user.email?.split('@')[0] || 'Student'}
                    </p>
                    <p className="text-xs text-gray-500 dark:text-gray-400 truncate">{user.email}</p>
                  </div>
                </div>
                <Link
                  to="/settings"
                  className="flex items-center gap-3 px-3 py-2 rounded-xl text-sm font-medium text-gray-600 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-gray-800 w-full"
                >
                  <Settings className="h-5 w-5" />
                  Settings
                </Link>
                <button
                  onClick={handleSignOut}
                  className="flex items-center gap-3 px-3 py-2 rounded-xl text-sm font-medium text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-900/20 w-full"
                >
                  <LogOut className="h-5 w-5" />
                  Sign out
                </button>
              </div>
            ) : (
              <Link
                to="/login"
                className="flex items-center justify-center gap-2 px-4 py-3 text-sm font-medium text-white bg-primary-600 rounded-xl hover:bg-primary-700 w-full"
              >
                <LogOut className="h-5 w-5" />
                Sign in
              </Link>
            )}
          </div>
        </div>
      </aside>
    </>
  );
}
import { Routes, Route, Navigate } from 'react-router-dom';
import { PageLayout } from '@components/layout/PageLayout';
import { AuthGate, PublicOnlyGate, OnboardingGate } from '@components/layout/AuthGate';
import { ToastProvider } from '@components/ui/Toast';

// Pages
import { HomePage } from '@pages/HomePage';
import { ListingDetailPage } from '@pages/ListingDetailPage';
import { CreateListingPage } from '@pages/CreateListingPage';
import { MyListingsPage } from '@pages/MyListingsPage';
import { MatchesPage } from '@pages/MatchesPage';
import { BundlesPage } from '@pages/BundlesPage';
import { BundleDetailPage } from '@pages/BundleDetailPage';
import { CreateBundlePage } from '@pages/CreateBundlePage';
import { BlockedUsersPage } from '@pages/BlockedUsersPage';
import { SettingsPage } from '@pages/SettingsPage';
import { AuthPage } from '@components/auth/AuthPage';
import { OnboardingPage } from '@pages/OnboardingPage';

function App() {
  return (
    <ToastProvider>
      <Routes>
        {/* Public routes */}
        <Route element={<PublicOnlyGate />}>
          <Route path="/login" element={<AuthPage />} />
        </Route>

        {/* Onboarding */}
        <Route element={<OnboardingGate />}>
          <Route path="/onboarding" element={<OnboardingPage />} />
        </Route>

        {/* Protected routes */}
        <Route element={<AuthGate />}>
          <Route element={<PageLayout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/listing/new" element={<CreateListingPage />} />
            <Route path="/listing/:id" element={<ListingDetailPage />} />
            <Route path="/my-listings" element={<MyListingsPage />} />
            <Route path="/matches" element={<MatchesPage />} />
            <Route path="/bundles" element={<BundlesPage />} />
            <Route path="/bundles/new" element={<CreateBundlePage />} />
            <Route path="/bundles/:id" element={<BundleDetailPage />} />
            <Route path="/blocked" element={<BlockedUsersPage />} />
            <Route path="/settings" element={<SettingsPage />} />
          </Route>
        </Route>

        {/* Redirect */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </ToastProvider>
  );
}

export default App;
# CampusMarket Web

A React + TypeScript Progressive Web App for the CampusMarket campus marketplace. Shares the same Firebase backend and Cloudinary storage as the Android app.

## Tech Stack

- **Framework**: React 18 + TypeScript
- **Build Tool**: Vite
- **Routing**: React Router v6
- **State Management**: TanStack Query (React Query) + Zustand
- **Styling**: Tailwind CSS + Material Tailwind
- **Forms**: React Hook Form + Zod
- **Authentication**: Firebase Auth (Email/Password + Email Verification)
- **Database**: Firebase Firestore (Real-time listeners)
- **Image Storage**: Cloudinary (Direct unsigned uploads)
- **PWA**: Workbox Service Worker
- **Testing**: Vitest + React Testing Library + Playwright

## Project Structure

```
campusmarket-web/
├── public/
│   ├── manifest.json          # PWA manifest
│   ├── sw.js                  # Service Worker
│   └── vite.svg
├── src/
│   ├── firebase/              # Firebase configuration
│   │   ├── config.ts          # Firebase app initialization
│   │   ├── auth.ts            # Auth helpers
│   │   └── firestore.ts       # Firestore helpers & converters
│   ├── cloudinary/            # Cloudinary integration
│   │   ├── config.ts          # Config & URL transforms
│   │   └── upload.ts          # Upload utilities
│   ├── models/                # TypeScript models & business logic
│   │   ├── Product.ts         # Product model + helpers
│   │   ├── MoveOutBundle.ts   # Bundle model + helpers
│   │   ├── FeedFilter.ts      # Feed filter logic
│   │   ├── BlockedUser.ts     # Blocked user model
│   │   └── MatchEngine.ts     # Matching algorithm
│   ├── services/              # Repository-pattern data access
│   │   ├── listingService.ts
│   │   ├── bundleService.ts
│   │   ├── moderationService.ts
│   ├── hooks/                 # React hooks (React Query + local state)
│   │   ├── useAuth.ts
│   │   ├── useListings.ts
│   │   ├── useBundles.ts
│   │   ├── useMatches.ts
│   │   ├── useModeration.ts
│   │   ├── useImageUpload.ts
│   │   └── useShare.ts
│   ├── components/
│   │   ├── ui/                # Design system primitives
│   │   ├── layout/            # Layout components (Navbar, PageLayout, AuthGate)
│   │   ├── product/           # Product components
│   │   ├── bundle/            # Bundle components
│   │   ├── feed/              # Feed components
│   │   ├── auth/              # Auth components
│   │   ├── onboarding/        # Onboarding components
│   │   ├── moderation/        # Moderation components
│   │   └── common/            # Shared components
│   ├── pages/                 # Route-level page components
│   ├── utils/                 # Utility functions
│   ├── styles/                # Global styles
│   └── types/                 # TypeScript type extensions
├── .env.example
├── .env.local
├── package.json
├── tsconfig.json
├── vite.config.ts
├── tailwind.config.ts
└── README.md
```

## Getting Started

### Prerequisites

- Node.js 18+
- Firebase project with Authentication and Firestore enabled
- Cloudinary account with unsigned upload preset

### Installation

```bash
cd campusmarket-web
npm install
```

### Configuration

1. Copy `.env.example` to `.env.local` and fill in your values:

```bash
cp .env.example .env.local
```

2. Required environment variables:

| Variable | Description |
|----------|-------------|
| `VITE_FIREBASE_API_KEY` | Firebase Web API Key |
| `VITE_FIREBASE_AUTH_DOMAIN` | Firebase Auth Domain |
| `VITE_FIREBASE_PROJECT_ID` | Firebase Project ID |
| `VITE_FIREBASE_STORAGE_BUCKET` | Firebase Storage Bucket |
| `VITE_FIREBASE_MESSAGING_SENDER_ID` | Firebase Messaging Sender ID |
| `VITE_FIREBASE_APP_ID` | Firebase App ID |
| `VITE_CLOUDINARY_CLOUD_NAME` | Cloudinary Cloud Name (default: urf4pcg4) |
| `VITE_CLOUDINARY_UPLOAD_PRESET` | Cloudinary Unsigned Upload Preset (default: campusMarket) |
| `VITE_CLOUDINARY_API_KEY` | Cloudinary API Key |
| `VITE_VAPID_PUBLIC_KEY` | VAPID Public Key for Web Push |

3. Firebase Console Setup:
   - Add web app to Firebase project
   - Copy config values to `.env.local`
   - Add your domain to **Authentication → Settings → Authorized domains**
   - Enable **Email/Password** provider
   - Enable **Email Verification** (required)

4. Cloudinary Setup:
   - Create unsigned upload preset named `campusMarket`
   - Enable CORS for your domain in Cloudinary Dashboard → Settings → Security → CORS

### Development

```bash
npm run dev
```

Open http://localhost:5173

### Building

```bash
npm run build
```

Output in `dist/` folder.

### Testing

```bash
npm run test        # Unit tests
npm run test:ui     # Test UI
npm run e2e         # E2E tests with Playwright
```

### Linting & Formatting

```bash
npm run lint
npm run format
```

## Features

### Core Marketplace
- ✅ Home feed with real-time updates
- ✅ Search, filter, sort listings
- ✅ Create listings (Sale + Wanted)
- ✅ Photo upload (direct to Cloudinary)
- ✅ Product detail with image gallery
- ✅ WhatsApp contact integration
- ✅ Share listings

### User Management
- ✅ Email/password authentication
- ✅ Mandatory email verification
- ✅ Student domain restriction
- ✅ Onboarding flow
- ✅ My Listings management
- ✅ Mark sold/renew/delete with undo

### Smart Matching
- ✅ Client-side MatchEngine port
- ✅ Matches page
- ✅ Match badge on nav
- ⏳ Background match checking (needs Cloud Function)

### Move-Out Bundles
- ✅ Create bundles from own items
- ✅ Bundle list & detail pages
- ✅ Atomic creation (WriteBatch)
- ✅ Close bundle
- ✅ Share bundle

### Moderation
- ✅ Report listings
- ✅ Block sellers
- ✅ Blocked users management
- ✅ Feed filtering

### PWA Features
- ✅ Service Worker (Workbox)
- ✅ Offline caching
- ✅ Install prompt
- ✅ Web Push notifications (needs VAPID keys)

## Deployment

### Vercel (Recommended)

1. Connect GitHub repository to Vercel
2. Add environment variables in Vercel dashboard
3. Deploy

### Netlify

1. Connect repository
2. Build command: `npm run build`
3. Publish directory: `dist`
3. Add environment variables
4. Deploy

### Firebase Hosting

```bash
npm install -g firebase-tools
firebase login
firebase init hosting
# Select "dist" as public directory
# Configure as SPA (rewrite all to index.html)
firebase deploy
```

## Architecture Decisions

### Direct Cloudinary Upload
Images upload directly from browser to Cloudinary using unsigned preset. No backend proxy needed. Client-side compression via canvas.

### Real-time Firestore Listeners
Uses `onSnapshot` for real-time updates. React Query manages cache and deduplication.

### MatchEngine on Client
Ported from Java to TypeScript. Runs in Web Worker for performance. Zero backend cost.

### SharedWorker for Firestore
Single listener shared across tabs to minimize Firestore reads.

## Roadmap

- [ ] Background match checking via Cloud Function + FCM
- [ ] Web Push notifications with VAPID
- [ ] Offline support with IndexedDB
- [ ] Keyboard shortcuts
- [ ] Accessibility audit (WCAG 2.1 AA)
- [ ] Cross-browser testing
- [ ] Load testing

## License

Educational project.
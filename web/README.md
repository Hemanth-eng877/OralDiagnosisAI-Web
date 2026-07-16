# OralDiagnosisAI Web (Vite + React)

This frontend connects to your existing Firebase project and Firestore database (the same project used by your Android app).

Quick start

1. Install dependencies

```bash
cd web
npm install
```

2. Create a `.env.local` file in `web/` with your Firebase config from the Firebase Console (do NOT create a new project). Values must match your existing project:

```
VITE_FIREBASE_API_KEY=your_api_key
VITE_FIREBASE_AUTH_DOMAIN=your_auth_domain
VITE_FIREBASE_PROJECT_ID=your_project_id
VITE_FIREBASE_STORAGE_BUCKET=your_storage_bucket
VITE_FIREBASE_MESSAGING_SENDER_ID=your_messaging_sender_id
VITE_FIREBASE_APP_ID=your_app_id
```

3. Run the dev server

```bash
npm run dev
```

Notes
- Ensure Firestore rules allow access from your web app or configure proper authentication.
- The `firebase.js` file uses Vite env variables; do not commit secrets.
- Collections used: `users`, `patients`, `appointments`, `reports`, `scans`.

# Frontend - Fuel Voucher

## Prerequisites
- Node.js 20+
- npm 10+

## Setup
npm install

## Run
npm run dev

## Build
npm run build

## Backend URL
By default, the app calls http://localhost:8080.
Copy .env.example to .env and update VITE_API_URL if needed.

## Auth flow in UI
- Login and Register forms are available on the home page.
- On success, JWT token is stored in localStorage.
- The app then calls GET /api/auth/me to load the signed-in profile.

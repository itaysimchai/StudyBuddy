# StudyBuddy – Android App

StudyBuddy helps students find, create, and join study sessions around them.
All data is stored in **Cloud Firestore** — nothing is hard-coded — and the app
uses the phone's **GPS** to attach real coordinates to sessions and to sort
sessions by distance from the user.

Final project for *Advanced Topics in App Innovation*, Reichman University.

## Pages

| Page | What it does |
|---|---|
| **Login** | Sign in with Google, or with email/password (bonus auth method) |
| **Home** | All sessions from Firestore, with live search by course/topic |
| **Create Session** | Form + "Use GPS" button that attaches the phone's current location |
| **Nearby Sessions** | Sessions sorted by distance from the user's current GPS position |
| **Session Details** | Full info + participants list; join/leave updates Firestore atomically |
| **Profile** | Signed-in user's name and email, plus logout |

## Firebase

- **Firestore** — single `Sessions` collection; each document holds course,
  topic, time, location name, GPS coordinates, capacity, and the participants array
- **Authentication** — Google Sign-In + Email/Password (second method, bonus)
- **Analytics** — events: `login`, `sign_up`, `sessions_loaded`, `session_created`,
  `session_opened`, `session_participation_changed`
- **Crashlytics** — connected via the Crashlytics Gradle plugin

## Phone capability

GPS via `LocationManager` (last known location):
- *Create Session* attaches the creator's coordinates to the session
- *Nearby Sessions* computes and sorts by distance (`Location.distanceBetween`)

## Running the project

1. Clone the repo and open it in Android Studio.
2. The app requires a `google-services.json` in `app/` belonging to a Firebase
   project with Google + Email/Password sign-in enabled and the debug SHA-1
   fingerprint registered (Firebase console → Project settings → Add fingerprint).
3. Run on an emulator or device with Google Play services.
   On the emulator, set a location in Extended Controls → Location so the GPS
   features have data to work with.

## Tech

Java · AndroidX · RecyclerView · Firebase (Auth, Firestore, Analytics, Crashlytics) · Google Sign-In

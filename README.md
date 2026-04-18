# 🎢 Universal Studios Guide

Universal Studios Guide is an Android mobile application designed to help visitors explore live attraction information at Universal Studios Orlando in a simple, organized, and interactive way. The app combines real-time API data, Firebase Authentication, Firebase Realtime Database, and Google Maps Compose to support park planning, navigation, and personal ride tracking.

## ✨ Features

- 🔐 User login and signup with Firebase Authentication
- 💾 Remember Me option for saved login credentials
- 🎡 Live attractions list with ride names, status, and wait times
- 🗺️ Interactive park map with live markers for rides and shows
- ⭐ Favorites system backed by Firebase Realtime Database
- 🌐 Multilingual support for English and Persian (Farsi)
- 🎛️ Filters for viewing all locations, only rides, or only shows
- 📍 Recenter and zoom controls on the map

## 📱 Application Screens

### MainActivity
- Login screen with email and password fields
- Remember Me switch
- Create account button

### SignUpActivity
- New user registration with Firebase
- Confirm password field
- Friendly validation messages

### HomeActivity
- Personalized welcome message using the user’s Firebase email
- Navigation to Attractions List, Park Map, and Favorites
- Language toggle support

### AttractionsListScreen
- Retrieves park attractions
- Displays ride cards with:
  - ride photo
  - ride name
  - live status
  - wait time
  - save to favorites button

### ParkMapScreen
- Google Map centered on Universal Orlando
- Live markers for rides and shows
- Filter options for All, Rides, and Shows
- Recenter and zoom controls

### FavoritesScreen
- Loads saved rides from Firebase Realtime Database
- Displays favorite rides in ride cards
- Remove button for deleting favorites

## ⚙️ Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Authentication:** Firebase Authentication
- **Database:** Firebase Realtime Database
- **Maps:** Google Maps Compose
- **APIs:** ThemeParks.Wiki Live Data API, Queue-Times API
- **Async Networking:** Kotlin coroutines

## 🔌 API Usage

This project uses two external APIs:

### ThemeParks.Wiki Live Data API
Used in `ParkMapScreen.kt` to:
- fetch attraction IDs
- fetch ride names and statuses
- fetch wait times
- fetch GPS coordinates for map markers

### Queue-Times API
Used in `AttractionsListScreen.kt` to:
- retrieve real-time wait times from a public source
- provide broader attraction coverage when available

The app parses nested JSON using `JSONObject`, `JSONArray`, and Kotlin coroutines for non-blocking calls.

## 🔥 Firebase Realtime Database

Each authenticated user has a unique favorites node in Firebase Realtime Database.

- Adding a favorite writes a new value under the user’s UID
- Removing a favorite deletes the value instantly
- The favorites screen listens for updates with `ValueEventListener`

## 🌍 Multilingual Support

The app includes localization for:

- **English** → `res/values/strings.xml`
- **Persian (Farsi)** → `res/values-farsi/strings.xml`

All hard-coded UI strings were moved into Android string resources to support both languages.

## 🚧 Limitations

- During testing, the first item in the Attractions and Favorites lists could appear slightly clipped when scrolling back upward.
- Ride data may vary because external APIs can sometimes return incomplete or outdated information depending on park availability.

## 🔒 Security Note

API keys and local credentials have been removed from this repository for security.

If you would like to run this project locally, you may need to provide your own Firebase configuration and API credentials.

## ▶️ Running the Project

To run the app locally:

1. Clone the repository
2. Open the project in Android Studio
3. Add your own Firebase configuration files and local credentials
4. Sync Gradle dependencies
5. Run on an emulator or Android device

## 🎥 Demo Video

[Watch the demo video here](https://drive.google.com/file/d/1tFemfa3cOQLKV9E95TTVCKHHaCES8MtH/view?usp=sharing)

## 👩‍💻 Author

**Rohina Saeydie**  
Computer Science Student at The George Washington University

## 📌 Repository Note

This project was originally created for **CSCI 4237** as Project 2 and focuses on building a practical Android application using live park data, Firebase, and Google Maps.
API keys and local credentials have been removed from this repository for security purposes, and the original API key used in development has expired.
If you would like to run this project locally, you may need to provide your own Firebase configuration and API credentials.

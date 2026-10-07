# CampusMarket

A campus marketplace Android application that allows students to buy and sell items within their campus community.

## Features

- **Create Listings**: Users can post items they want to sell with details and images
- **Browse Listings**: View available items on the home screen
- **Product Details**: Click on any listing to see detailed information
- **Contact Seller**: Direct WhatsApp integration to contact sellers instantly
- **Campus-focused**: Designed specifically for student-to-student transactions

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Architecture**: Android Jetpack (Activity KTX, AppCompat, ConstraintLayout, Material Design)
- **Build System**: Gradle with Kotlin DSL

## Project Structure

```
CampusMarket/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/smart/campusmarket/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   ├── test/
│   │   └── androidTest/
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradlew / gradlew.bat
```

## Getting Started

### Prerequisites
- Android Studio (latest version recommended)
- JDK 11 or higher
- Android SDK with API level 36

### Installation

1. Clone the repository:
   ```bash
   git clone <repository-url>
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Run the app on an emulator or physical device (API 24+)

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Recent Changes

- Added listing creation activity for users to post items for sale
- Integrated WhatsApp contact functionality for buyer-seller communication
- Implemented click handling for listings to show detailed product views
- Created prototypical home screen layout

## License

This project is for educational purposes.
# Currency Travel Helper

Currency Travel Helper is an Android utility app created for CP3406 Assignment 1. The app helps travellers convert a budget between multiple currencies using live exchange rate data.

The app is designed for quick travel planning. Users can enter an amount, choose a base currency, select one or more target currencies, and view converted values immediately. It also includes a live exchange rate trend chart so users can understand recent currency movement before making travel budget decisions.

## Core Features

- Convert an entered amount from one selected base currency to multiple target currencies.
- Choose from a range of common currencies, including AUD, USD, EUR, GBP, JPY, CNY, SGD, NZD, CAD, CHF, HKD, KRW, INR, THB, MYR, IDR, PHP, ZAR, SEK, NOK, DKK, PLN, CZK, HUF, MXN, BRL, and TRY.
- Use quick amount buttons for common travel budget checks.
- Fetch live exchange rates from the Frankfurter API.
- Display the latest available exchange rate date returned by the API.
- Show loading and error states when exchange rate data is being requested or cannot be loaded.
- View a live exchange rate trend chart for selected currency pairs.
- Switch the chart between 7-day, 30-day, and 90-day ranges.
- Display the percentage change for the selected chart range.
- Adjust decimal display settings.
- Change appearance mode between System, Light, and Dark themes.

## Screens

The app uses a simple two-screen structure:

- **Currency screen**: shows the entered amount, selected base currency, converted target currency values, latest rate date, refresh state, error messages, and exchange rate trend chart.
- **Settings screen**: lets the user change the base currency, selected target currencies, decimal places, quick amount behaviour, chart range, and app appearance.

## Technology

This project uses:

- Kotlin
- Jetpack Compose
- Material Design 3
- ViewModel for UI state management
- Repository pattern for exchange rate data handling
- Retrofit for API requests
- Kotlin serialization for JSON parsing
- Frankfurter API for live exchange rate data

## API

The app uses the Frankfurter API to retrieve current and historical exchange rate data.

- Latest rates are used for currency conversion.
- Historical rates are used to build the exchange rate trend chart.

If the API request fails, the app shows an error state instead of silently displaying incorrect data.

## How to Run

1. Open the project in Android Studio.
2. Let Gradle sync the project.
3. Run the app on an Android emulator or physical Android device.
4. Make sure the device has internet access so live exchange rates and chart data can load.

## Development Progress

Implemented features:

- Android project created and connected to GitHub.
- Project structure fixed so GitHub shows the Android project files at the repository root.
- Main currency conversion interface built with Jetpack Compose.
- Settings screen added.
- Selectable base and target currencies added.
- Live exchange rate API integration added.
- Repository and ViewModel classes added.
- Loading, refresh, and error states added.
- Live exchange rate trend chart added.
- Chart range selection added for 7D, 30D, and 90D views.
- Percentage change summary added for chart data.
- System, Light, and Dark appearance modes added.
- Self-reflection document prepared for assignment submission.

## Repository

This repository records the development progress for CP3406 Assignment 1 through regular commits, including project setup, UI development, API integration, chart functionality, theme support, and documentation updates.

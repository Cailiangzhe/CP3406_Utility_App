# Currency Travel Helper

Currency Travel Helper is a planned Android utility app for CP3406 Assignment 1. The app is designed to help users quickly convert a travel budget from one currency into several other currencies using current exchange rate data.

The goal of the app is to provide focused, at-a-glance information. Users should be able to open the app, enter or select a travel amount, and immediately see the approximate value in their chosen target currencies.

## Core Features

The planned app will include:

- A base currency and amount displayed clearly on the main screen.
- Exchange rate results for selected target currencies.
- Quick amount options for common travel budget checks, such as 50, 100, and 500.
- The latest available exchange rate date returned by the API.
- A settings screen for changing base currency, target currencies, decimal places, and quick amount options.

## Screens

Currency Travel Helper will use a simple two-screen structure:

- **Currency screen**: shows the selected amount, base currency, converted values, exchange rate date, loading state, and error message if exchange rates cannot be loaded.
- **Settings screen**: lets the user adjust preferences that affect the currency screen.

The assignment does not require settings to be persistent, so the first implementation may keep settings only while the app is running.

## Technical Plan

The app is planned to use the following Android technologies and development practices:

- Kotlin
- Jetpack Compose
- Material Design 3
- ViewModel for UI state management
- Repository pattern for exchange rate data handling
- Dependency injection for connecting the API service, repository, and ViewModel
- Retrofit for web API requests
- Frankfurter API for exchange rate data

## Development Status

Current progress:

- Blank Android project created.
- Project connected to GitHub.
- Project structure fixed so GitHub shows the Android project files at the repository root.
- Initial README added.

Pending work:

- Build the Currency screen UI.
- Build the Settings screen UI.
- Add state management for amount, selected currencies, and display options.
- Add Frankfurter API integration using Retrofit.
- Add repository and ViewModel classes.
- Refine the user interface using Material Design 3.
- Test loading, success, and error states.
- Write the self-reflection for the assignment submission.

## GitHub Progress

This repository will be updated regularly as features are implemented. Commits will be used to show continuous progress through the planning, interface design, API integration, testing, and refinement stages of the assignment.

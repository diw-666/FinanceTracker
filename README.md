# Finance Tracker

A personal finance tracker mobile application developed in Kotlin for Android. This app allows users to track their income, expenses, and savings with an intuitive interface.

## Features

### Core Features
1. **Transaction Management**
   - Add, edit, and delete income and expense transactions
   - Each transaction includes title, amount, category, and date

2. **Category-wise Spending Analysis**
   - Categorize transactions (Food, Transport, Bills, Entertainment, etc.)
   - Display summary of expenses per category

3. **Monthly Budget Setup**
   - Set monthly budget and track progress
   - Displays warnings when spending nears or exceeds budget limits

4. **Data Persistence using SharedPreferences**
   - Saves user preferences (currency type, budget settings)
   - Maintains transaction history across app restarts

### Bonus Features
1. **Data Backup using Internal Storage**
   - Export transaction data as JSON
   - Restore data from backups

2. **Push Notifications for Budget Alerts**
   - Notifications when approaching or exceeding monthly budget
   - Optional reminders to record daily expenses

## Technical Implementation

This app demonstrates the following Android development concepts:

- Kotlin language features
- XML layouts for UI design
- MVVM architecture pattern
- SharedPreferences for data persistence
- Internal Storage for backup and restore functionality
- RecyclerView for efficient list display
- Material Design components
- Date handling and formatting
- Notifications using NotificationManager

## Screenshots

(Screenshots would be added here once the app is running)

## How to Build and Run

1. Clone this repository
2. Open the project in Android Studio
3. Connect an Android device or use the emulator
4. Build and run the application

## Future Improvements

- Add charts and graphs for better visualization
- Implement search and filtering options
- Add recurring transactions
- Integrate with cloud storage for backup
- Add multi-currency support with conversion rates 
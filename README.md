# README 

# SIT305 Task 10.1D - Improved AI Learning Assistant App

## Overview
This project is my submission for **SIT305 Task 10.1D**.  
It is based on my previous **Task 6.1D** app and adds more account and user support features.

## Main Features
- Profile screen
- Learning history screen
- Upgrade account screen
- Share profile feature
- Real payment flow using **Stripe test mode**
- Original learning features from Task 6.1D are still kept

## What I Added in 10.1D
In this task, I extended the app by adding:
- a **Profile** page to show user details and learning stats
- a **History** page to show previous learning activity
- an **Upgrade Account** page with different plans
- a **Share Profile** feature using the Android share sheet
- a **Stripe test payment** flow for account upgrade

## Tech Stack
- Kotlin
- Android Studio
- Jetpack Compose
- Navigation Compose
- Room
- Retrofit
- Node.js backend
- Stripe test mode

## How to Run
### Android App
1. Open the project in Android Studio
2. Sync Gradle
3. Run the app on an emulator or Android device

### Backend
1. Open the `stripe-backend` folder
2. Run:
   ```bash
   npm install
   npm start
For Android Emulator, use:
http://10.0.2.2:3000
Payment Test

This project uses Stripe test mode, so payment should be tested with Stripe test cards only.

Example:

Card number: 4242 4242 4242 4242
Any future expiry date
Any 3-digit CVC
Notes
This project is an improved version of my Task 6.1D app
Sensitive keys are stored locally and are not uploaded to GitHub
Stripe is used in test mode for demonstration
Author

RUNQI LIU
SIT305 - Task 10.1D

# Dailya – The Personal Assistant

**An Android application for daily planning, task management, journaling, and personal wellness tracking.**

## Overview

Dailya is an Android personal assistant application developed as an academic project using **Java** and **Android Studio**.

The application brings together several everyday activities in one place, helping users organize their tasks, record daily experiences, and keep track of simple health-related habits.

Dailya is organized into four main modules:

## Features

### 1. Dashboard
Provides a daily overview of important personal information, including:
- Today's tasks
- Daily mood
- Water intake
- Sleep information
- Diary activity

### 2. Task Manager
Helps users organize and manage daily responsibilities through:
- Creating, editing, and deleting tasks
- Marking tasks as completed
- Setting dates and times
- Assigning task priorities
- Setting reminders

### 3. Diary
A personal space for recording and reviewing daily experiences:
- Adding diary entries with titles and descriptions
- Recording entry dates
- Tracking daily mood
- Connecting mood information with the daily dashboard

### 4. Health Care
Supports tracking simple personal wellness habits:
- Recording daily water intake
- Tracking sleep and wake-up times
- Viewing a summary of recorded information

## Technologies

- **Programming Language:** Java
- **Development Environment:** Android Studio
- **Platform:** Android
- **UI:** XML Layouts

## Software Design Concepts

The project applies object-oriented programming principles and several software design patterns:

- **Builder Pattern**
- **Facade Pattern**
- **Prototype Pattern**

The application is organized into separate functional modules to keep its features structured and support future development.

## Project Structure

```text
Dailya-Personal-Assistant/
│
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   ├── res/
│       │   └── AndroidManifest.xml
│       ├── test/
│       └── androidTest/
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Getting Started

To explore or run the project locally:

1. Clone this repository:

   ```bash
   git clone https://github.com/Ashstride/Dailya-Personal-Assistant.git
   ```

2. Open the project in Android Studio.
3. Allow Gradle to sync and download the required dependencies.
4. Run the application on an Android emulator or a compatible Android device.

## Project Information

- **Project Type:** Academic Project
- **Application Type:** Android Personal Assistant
- **Development Language:** Java
- **IDE:** Android Studio

## Future Improvements

Potential areas for further development include:
- Database integration for persistent data storage
- More advanced reminder and notification management
- Additional personalization options
- Enhanced habit tracking and data analysis

---

*Dailya – Bringing everyday planning and personal tracking together.*
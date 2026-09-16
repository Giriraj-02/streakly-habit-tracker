# Streakly — 75-Day Habit Tracker

Streakly is a habit-tracking web application built around a 75-day self-improvement challenge.

The application helps users manage multiple habits with different schedules, log daily completions, maintain current streaks, track their best-ever streaks, search habits, and archive habits that they have temporarily given up without permanently deleting them.

The project is designed to work for any user and any number of habits.

---

## Problem Statement

Ananya has started a 75-day self-improvement challenge involving habits such as drinking water, reading, working out, and avoiding sugar.

Some habits need to be completed every day, while others only need to be completed on weekdays.

The application should allow the user to:

- See the habits applicable for today.
- Mark today's habits as completed.
- Track the current streak of every habit.
- Track the best-ever streak of every habit.
- Search for a particular habit.
- Archive habits that are no longer active.
- Restore archived habits later.
- Maintain habit completion data persistently.
- Work for any user and any set of habits.

The implementation focuses first on reliable habit logging and streak calculation, followed by usability features.

---

## Features

### Habit Management

- Create a new habit.
- Choose between:
  - Every Day
  - Weekdays
- Archive an active habit.
- Restore an archived habit.
- Search habits by name.

### Daily Tracking

- View habits applicable to the current day.
- Mark a habit as completed.
- Toggle today's completion status.
- Prevent duplicate completion records.

### Streak Tracking

- Current streak for each habit.
- Best-ever streak for each habit.
- Different streak rules for daily and weekday habits.
- Weekends are skipped for weekday-only habits rather than breaking their streak.

### 75-Day Challenge

- Displays the current challenge day.
- Displays progress toward 75 days.
- Shows today's completion progress.

### Persistence

Habit data and completion history are stored in an H2 database.

Application restarts do not remove previously stored data.

### User Interface

- Responsive web interface.
- Today, All Habits, and Archived views.
- Habit search.
- Add Habit modal.
- Progress indicators.
- Current and best streak information.
- Archive and restore actions.
- Empty states and user feedback messages.

---

## Technology Stack

### Backend

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- H2 Database
- Maven

### Frontend

- HTML5
- CSS3
- Vanilla JavaScript

No frontend framework or external UI library is required.

---

## Project Structure


```text
streakly-habit-tracker/
│
├── pom.xml
├── README.md
├── REASONING.md
├── AI_LOGS.md
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── streakly/
        │           │
        │           ├── StreaklyApplication.java
        │           │
        │           ├── controller/
        │           │   └── HabitController.java
        │           │
        │           ├── service/
        │           │   └── HabitService.java
        │           │
        │           ├── repository/
        │           │   ├── HabitRepository.java
        │           │   └── HabitCompletionRepository.java
        │           │
        │           ├── model/
        │           │   ├── Habit.java
        │           │   ├── HabitCompletion.java
        │           │   └── Frequency.java
        │           │
        │           └── dto/
        │               └── HabitRequest.java
        │
        └── resources/
            ├── application.properties
            │
            └── static/
                ├── index.html
                ├── style.css
                └── app.js

           # Running the Project

1. Clone the repository

git clone <YOUR_PUBLIC_GITHUB_REPOSITORY_URL>
cd streakly-habit-tracker

2. Start the Spring Boot application

From the project root:

"mvn spring-boot:run"

The application starts on:

http://localhost:8080

## Running in GitHub Codespaces

Open the GitHub repository.
Open the repository in GitHub Codespaces.
Open the terminal.
Run: "mvn spring-boot:run"

Wait until Spring Boot starts successfully.
Open the PORTS tab.
Find port 8080.
Click Open in Browser.

The application will then open using the forwarded Codespaces URL.
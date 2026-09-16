# Reasoning — Streakly 75-Day Habit Tracker

## 1. Understanding the Problem

The goal of this project is to build a simple and reliable 75-day habit tracker.

A user should be able to create habits such as:

- Drink Water
- Read
- Workout
- No Sugar

Different habits can have different frequencies. For example:

- Daily — every day
- Weekdays — Monday to Friday

The main requirements are:

1. Show habits that need to be completed today.
2. Allow the user to mark a habit as completed or incomplete.
3. Track the current streak of each habit.
4. Track the best-ever streak of each habit.
5. Search for a habit.
6. Archive a habit without permanently deleting it.
7. Restore an archived habit.
8. Support different users and different types of habits.
9. Track progress through a 75-day challenge.
10. Keep logging and streak calculation reliable.

The main design priority was to make habit logging and streak calculation correct before adding unnecessary features.

---

## 2. Overall Design Approach

I divided the application into two major parts:

### Backend

The backend is responsible for:

- Habit management
- Completion logging
- Streak calculation
- Search
- Archive/restore
- 75-day progress
- Database persistence
- REST APIs

The backend follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database

Frontend

The frontend is implemented using:

HTML
CSS
Vanilla JavaScript

The frontend is responsible for displaying habits and sending requests to the backend.

Backend

The backend uses Spring Boot and exposes REST APIs for:

Creating habits
Listing habits
Searching habits
Getting today's habits
Completing/toggling habits
Archiving habits
Restoring habits
Getting dashboard information
Persistence

Spring Data JPA is used to communicate with the H2 database.

This allows habit and completion information to survive application restarts.

3. Habit Data Model

A habit contains information such as:

Habit
 ├── id
 ├── name
 ├── frequency
 ├── archived
 └── createdOn

Frequency is represented using an enum:

DAILY
WEEKDAYS

Completion records are stored separately.

Conceptually:

Habit
   |
   └── HabitCompletion
          ├── id
          ├── habit
          └── completedOn

Keeping completion records separately makes it possible to maintain a complete history instead of storing only the latest completion state.

4. Why Completion History Is Stored

The application cannot correctly calculate the best-ever streak using only a currentStreak number.

For example:

Monday     ✓
Tuesday    ✓
Wednesday  ✓
Thursday   ✓
Friday     ✓

The best streak is:

5

If the user later misses Friday of another week, the application still needs the old completion history to know that the best streak was previously 5.

Therefore, completion dates are stored as historical data.

This allows the application to calculate:

Current streak
Best-ever streak
Completion status for today
Historical progress

from the same underlying data.

5. Daily Habit Logic

A daily habit is applicable on every day.

For example:

Drink Water

Monday     applicable
Tuesday    applicable
Wednesday  applicable
Thursday   applicable
Friday     applicable
Saturday   applicable
Sunday     applicable

For a daily habit, every previous calendar day is considered when calculating a streak.

6. Weekday Habit Logic

A weekday habit is applicable only from Monday through Friday.

For example:

Workout

Monday     applicable
Tuesday    applicable
Wednesday  applicable
Thursday   applicable
Friday     applicable
Saturday   skipped
Sunday     skipped

The important rule is that Saturday and Sunday should not break a weekday habit's streak.

For example:

Friday     ✓
Saturday   -
Sunday     -
Monday     ✓

The Monday completion continues the weekday sequence rather than creating a break because Saturday and Sunday are not applicable days.

7. Current Streak Algorithm

The current streak is calculated from the most recent applicable day.

The algorithm works conceptually as follows:

1. Start with today's date.
2. Check whether today is applicable for the habit.
3. If today is completed, count it.
4. Move backwards to the previous applicable date.
5. If that date is completed, increment the streak.
6. Continue until a required date is missing.
7. Return the count.

For example, for a daily habit:

Sep 16 ✓
Sep 15 ✓
Sep 14 ✓
Sep 13 ✓
Sep 12 ✗

The current streak is:

3

because the consecutive completed sequence from the latest applicable date contains three days.

8. Best-Ever Streak Algorithm

The best-ever streak needs to consider the complete completion history.

The completion dates are processed in chronological order.

Conceptually:

previous date
      ↓
current date
      ↓
Are they consecutive applicable dates?
      ↓
Yes → increase current sequence
No  → start a new sequence
      ↓
Keep the maximum sequence

Example:

Mon ✓
Tue ✓
Wed ✓
Thu ✗
Fri ✓
Sat ✓

The sequences are:

Mon → Tue → Wed = 3
Fri → Sat        = 2

Therefore:

Best streak = 3

For weekday habits, the next applicable weekday is used instead of simply comparing calendar dates. This allows Friday and Monday to remain consecutive applicable days.

9. Why Archive Instead of Delete

The problem says that habits the user has given up should be moved out of the way but should not be gone forever.

Therefore, the application uses an archived state.

archived = false

means the habit is active.

archived = true

means the habit is archived.

This is preferable to deleting the habit because:

Completion history is preserved.
The user can restore the habit.
Accidental removal is reversible.
Archived habits can be separated from active habits in the UI.

The UI therefore uses:

Archive

instead of:

Delete

for this workflow.

10. Today's Habit Selection

The Today view should not simply show every habit.

A habit is included when:

habit is not archived
AND
habit is applicable today

Therefore:

Daily habit
    → shown every day

Weekday habit
    → shown Monday-Friday

Archived habit
    → not shown in active Today view

This directly represents the scheduling requirement in the problem statement.

11. Search Design

The application provides a search feature so that a user can quickly find a particular habit.

The search request is sent to the backend:

GET /api/habits/search?q=work

The backend searches habit names and returns matching habits.

The current implementation prioritizes simplicity and correctness. For a normal personal habit tracker with a relatively small number of habits, this approach is sufficient.

For a very large-scale system, database indexing or a dedicated search mechanism could be introduced.

12. API Design

The backend exposes REST endpoints rather than coupling the frontend directly to Java service classes.

Important endpoints include:

GET  /api/habits
GET  /api/habits/today
GET  /api/habits/archived
GET  /api/habits/search?q=...
POST /api/habits
POST /api/habits/{id}/toggle
POST /api/habits/{id}/archive
POST /api/habits/{id}/restore
GET  /api/dashboard

This provides a clear boundary between the frontend and backend.

13. Why Spring Boot

The initial prototype could be implemented using a basic Java HTTP server, but a Spring Boot implementation provides a cleaner structure for the final solution.

Spring Boot provides:

REST controller support
Dependency injection
Database integration
JPA/Hibernate support
Easier configuration
Clear separation of responsibilities
Easier future deployment

The final application therefore uses Spring Boot rather than keeping all backend and UI code inside a single Java file.

14. Why H2

H2 was selected for the initial implementation because it is lightweight and easy to run in a development environment such as GitHub Codespaces.

Advantages include:

No separate database server is required.
Minimal configuration.
Supports SQL and JPA.
Persistent file-based storage can be used.
Easy local debugging through the H2 console.

The application can later be migrated to PostgreSQL or another production database without changing the overall application architecture.

15. Why Vanilla JavaScript

The UI does not require a large frontend framework.

The application mainly needs:

API calls
Rendering habit cards
Form handling
Tabs
Search
Completion toggling
Archive/restore interactions

Vanilla JavaScript keeps the project lightweight and makes the implementation easier to understand during evaluation.

A frontend framework could be introduced if the UI becomes significantly larger.

16. UI Design Reasoning

The interface is designed around the user's main daily action:

Open application
       ↓
See today's habits
       ↓
Complete habits
       ↓
See streak progress

The main UI therefore prioritizes:

Today's habits.
Completion status.
Current streak.
Best streak.
Challenge progress.

Secondary functionality such as search and archive/restore is available without distracting from the daily tracking workflow.

17. 75-Day Challenge Progress

The application also provides challenge-level information such as:

Day X / 75

and overall daily completion progress.

The 75-day challenge acts as the product context around the habit tracker, while individual habit streaks remain independent.

For example, a user can have:

75-day challenge: Day 20

Drink Water:
Current streak = 10
Best streak = 14

Reading:
Current streak = 5
Best streak = 8

This separates challenge progress from individual habit performance.

18. Reliability Considerations

Several design decisions are intended to make logging reliable.

Persistent completion records

Completion history is stored in the database rather than only in browser memory.

Duplicate prevention

A habit should not receive multiple completion records for the same date.

Applicability validation

A weekday habit should not be marked as completed on a weekend through the normal completion flow.

Archive preservation

Archiving a habit does not remove its history.

These decisions protect the correctness of streak calculations.

19. Edge Cases Considered

The implementation considers cases such as:

No habits

The application displays an empty state instead of an empty page.

No habits applicable today

The Today view explains that there are no habits scheduled for the current day.

Newly created habit

A newly created habit starts with:

Current streak = 0
Best streak = 0

until it is completed.

Missed day

A missed applicable day breaks the current streak.

Weekend for weekday habit

Saturday and Sunday are skipped rather than breaking the weekday sequence.

Archived habit

Archived habits are hidden from active views but remain available for restoration.

Search with no results

The UI displays a suitable empty state.

Repeated completion toggle

The completion operation toggles today's state rather than creating multiple completion records for the same habit and date.

20. Alternatives Considered
In-memory storage

An in-memory Map and collection of completion dates would be simple.

However, data would be lost whenever the application restarts.

Therefore, persistent storage was selected for the final implementation.

Single Java file

The original prototype can work as a single Java file containing the HTTP server, business logic, and frontend.

This is useful for quickly validating the idea, but it becomes harder to maintain as functionality grows.

The final version separates:

Model
Controller
Service
Repository
DTO
Frontend

to improve maintainability.

MySQL

MySQL could be used instead of H2.

However, it requires additional database setup during development.

H2 keeps the project easy to run in GitHub Codespaces while preserving a database-backed architecture.

A production deployment can later use PostgreSQL or MySQL.

Frontend Framework

React or another frontend framework could be used.

For the current scope, vanilla JavaScript is sufficient and reduces project complexity.

21. Complexity

Let:

H = number of habits
C = number of completion records for a habit
Today's habits

The current implementation examines the habits to determine which are applicable.

Time: O(H)
Search

The current search checks habit names.

Time: O(H)

For a personal habit tracker this is acceptable.

Current streak

The algorithm walks backwards through applicable dates until the streak ends.

If S applicable dates are inspected:

Time: O(S)
Best streak

The completion history is processed in chronological order.

If the completion history contains C records:

Time: O(C)

The exact database sorting/iteration cost also depends on the database and query implementation.

22. Scalability Considerations

The current application is designed primarily for the challenge's expected scale: an individual user managing a reasonable number of habits.

If the application were expanded to support a large number of users, additional changes would be appropriate:

Add user authentication.
Associate every habit with a user.
Add database indexes.
Use PostgreSQL or another production database.
Optimize search using indexed queries.
Add pagination.
Add caching where appropriate.
Add automated tests.
Add monitoring and logging.

The current architecture leaves room for these changes because the frontend, service, and persistence layers are already separated.

23. Testing Strategy

The important behaviors to verify are:

Habit creation
Create daily habit
Create weekday habit
Completion
Complete habit
Undo completion
Prevent duplicate completion
Streaks
One completed day
Multiple consecutive days
Missed day
Weekday streak across weekend
Best-ever streak
Archive
Archive
View archived
Restore
Search
Matching search
Partial search
No results
Persistence
Create habit
Complete habit
Restart application
Verify data still exists
24. Final Architecture

The resulting system can be summarized as:

                    ┌─────────────────┐
                    │   Web Browser   │
                    │ HTML/CSS/JS     │
                    └────────┬────────┘
                             │
                             │ REST API
                             ▼
                    ┌─────────────────┐
                    │ HabitController │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │  HabitService   │
                    │                 │
                    │ Streak Logic    │
                    │ Schedule Logic  │
                    │ Business Rules  │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │ JPA Repositories│
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │  H2 Database    │
                    │                 │
                    │ Habits          │
                    │ Completions     │
                    └─────────────────┘
25. Final Design Goal

The solution prioritizes the core requirement:

Reliable habit logging
        +
Correct streak calculation

The remaining features are built around those two foundations:

Habit scheduling
      ↓
Today's habits
      ↓
Completion logging
      ↓
Streak calculation
      ↓
Search
      ↓
Archive / Restore
      ↓
75-day progress

The result is a small but complete habit-tracking application that directly addresses the requirements of the 75-day challenge while keeping the architecture understandable, maintainable, and suitable for further development.


### 

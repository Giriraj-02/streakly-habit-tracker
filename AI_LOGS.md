Solve the following coding problem in the most optimized way possible.
Problem: 
ROUND 2  ·  BUILD ROUND
Ananya’s 75-day challenge
Ananya has started a 75-day self-improvement challenge — drink water, read, work out, no sugar. She’s juggling several habits at once: some she does every day, some only on weekdays. Each morning she just wants to see today’s habits and tick them off one by one. She’s fiercely proud of her streaks and genuinely gutted when she breaks one, so for every habit she wants to know her current streak and her best-ever streak. Weeks in, her list has grown long — there are a couple she’s quietly given up on and wants out of the way (but not gone forever), and she keeps hunting for a particular one to update it.
Build Ananya something so she keeps her streaks alive.
(As you build, picture Ananya opening this every morning — but build for people like Ananya, not just her: any user, any habits. The small frustrations above are clues to what would make it genuinely useful. Get logging and streaks solid first, then the niceties.)
Requirements:

First explain the problem in simple words.
Identify the optimal approach and explain why it is optimal.
Mention the data structure/algorithm used and why it is suitable.
Provide the most optimized solution in Java.
Give a clean, simple, interview-friendly code that I can directly copy and run.
Avoid unnecessary libraries or complex syntax.
Explain the code step-by-step.
Provide a detailed dry run using an example.
Calculate:
Time Complexity
Space Complexity
Mention edge cases and how the solution handles them.
If there are multiple approaches, briefly compare them and explain why the chosen approach is better.
Do not sacrifice correctness for optimization.
Check the code carefully for compilation errors and test it mentally against the given examples.
Give the answer in this format:

Understanding
Approach
Algorithm
Java Code
Dry Run
Time Complexity
Space Complexity
Edge Cases
Alternative Approaches
Interview Explanation (how I should explain this solution to the interviewer)
Focus on the best possible time and space complexity according to the constraints.

Finalizing the Java code and preparing submission notes.
Habit Tracker — "Ananya's 75-Day Challenge"
1. Understanding
In simple words: Ananya (and people like her) is tracking multiple habits over 75 days. Some habits run every day, some only on weekdays. She needs three things every morning:

Today's checklist — only the habits that apply today (skip weekday-only habits on weekends), so she can tick them off.
Streak awareness — for each habit, how many days in a row she's currently on (current streak), and the longest run she's ever had (best streak) — because that's what she's emotionally invested in.
Housekeeping — she can archive habits she's given up on (hide, don't delete) and search for a specific habit quickly as her list grows.
So this isn't a single-function DSA problem — it's a small system with a data model + a streak algorithm sitting on top of it.

2. Approach
Core idea: model each habit as a name + a rule for which days it applies + a set of dates it was completed on. Everything else (today's list, current streak, best streak, search, archive) is a query over that model — no need for a separate "log" table or heavy DB; in-memory structures are enough for 75 days of data.

The one place that genuinely needs a good algorithm is streak calculation, because it has to reason about "consecutive" in terms of applicable days, not raw calendar days (a Mon→Fri streak on a weekday habit shouldn't break just because Saturday/Sunday exist).

3. Algorithm / Data Structures
Need	Structure	Why
Find a habit by name fast	Map<Integer, Habit> (id) + linear name lookup (small n)	O(1)/O(n) for a handful of habits — a Map keyed by name would work too, kept id-based for stability if renamed
Store completion dates	TreeSet<LocalDate> per habit	Auto-sorted, O(log m) insert, O(1) "was this date done" check, cheap ordered iteration for best-streak scan
Current streak	Walk backward from today through applicable days only, stop at first miss	Cost = length of the streak, not total days elapsed
Best streak	Single forward pass over the sorted TreeSet, tracking the run length	O(m), m = total completions — no need to re-scan per query if you cache, but a plain pass is already cheap at this scale
Archive (soft delete)	boolean flag on the habit, not removal	"Out of the way but not gone forever" — data + streak history preserved
Search	substring, case-insensitive match over habit names	Trivial at this scale; a name index isn't worth it for a few dozen habits
This is optimal for the actual scale of the problem (one person, dozens of habits, 75 days ≈ ~75 dates per habit) — a Trie or database index would be over-engineering here.

4. Java Code
java
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class Main {

    enum Frequency { DAILY, WEEKDAYS }

    static class Habit {
        final int id;
        String name;
        Frequency frequency;
        boolean archived;
        final LocalDate createdOn;
        final TreeSet<LocalDate> completedDates = new TreeSet<>();

        Habit(int id, String name, Frequency frequency, LocalDate createdOn) {
            this.id = id;
            this.name = name;
            this.frequency = frequency;
            this.createdOn = createdOn;
        }
    }

    static class HabitTracker {
        private final Map<Integer, Habit> habits = new LinkedHashMap<>();
        private int nextId = 1;

        Habit addHabit(String name, Frequency frequency, LocalDate today) {
            Habit h = new Habit(nextId++, name, frequency, today);
            habits.put(h.id, h);
            return h;
        }

        boolean isApplicable(Habit h, LocalDate date) {
            if (h.frequency == Frequency.DAILY) return true;
            DayOfWeek d = date.getDayOfWeek();
            return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
        }

        Optional<Habit> findByName(String name) {
            for (Habit h : habits.values())
                if (h.name.equalsIgnoreCase(name)) return Optional.of(h);
            return Optional.empty();
        }

        List<Habit> getTodayHabits(LocalDate today) {
            List<Habit> res = new ArrayList<>();
            for (Habit h : habits.values())
                if (!h.archived && isApplicable(h, today)) res.add(h);
            return res;
        }

        List<Habit> search(String query) {
            String q = query.toLowerCase();
            List<Habit> res = new ArrayList<>();
            for (Habit h : habits.values())
                if (h.name.toLowerCase().contains(q)) res.add(h);
            return res;
        }

        boolean markDone(String name, LocalDate date) {
            Optional<Habit> opt = findByName(name);
            if (opt.isEmpty() || !isApplicable(opt.get(), date)) return false;
            opt.get().completedDates.add(date);
            return true;
        }

        boolean archive(String name)   { return setArchived(name, true); }
        boolean unarchive(String name) { return setArchived(name, false); }

        private boolean setArchived(String name, boolean value) {
            Optional<Habit> opt = findByName(name);
            if (opt.isEmpty()) return false;
            opt.get().archived = value;
            return true;
        }

        List<Habit> listActive()   { return filterByArchived(false); }
        List<Habit> listArchived() { return filterByArchived(true); }

        private List<Habit> filterByArchived(boolean archived) {
            List<Habit> res = new ArrayList<>();
            for (Habit h : habits.values())
                if (h.archived == archived) res.add(h);
            return res;
        }

        // next day (after 'from') on which this habit is applicable
        private LocalDate nextApplicable(Habit h, LocalDate from) {
            LocalDate d = from.plusDays(1);
            while (!isApplicable(h, d)) d = d.plusDays(1);
            return d;
        }

        // previous applicable day before 'from', or null if it goes before createdOn
        private LocalDate previousApplicable(Habit h, LocalDate from) {
            LocalDate d = from.minusDays(1);
            while (d.isAfter(h.createdOn.minusDays(1)) && !isApplicable(h, d)) {
                d = d.minusDays(1);
            }
            return d.isBefore(h.createdOn) ? null : d;
        }

        int currentStreak(Habit h, LocalDate today) {
            int streak = 0;
            if (isApplicable(h, today) && h.completedDates.contains(today)) streak++;

            LocalDate cursor = previousApplicable(h, today);
            while (cursor != null && h.completedDates.contains(cursor)) {
                streak++;
                cursor = previousApplicable(h, cursor);
            }
            return streak;
        }

        int bestStreak(Habit h) {
            int best = 0, run = 0;
            LocalDate prev = null;
            for (LocalDate d : h.completedDates) {
                if (prev != null && nextApplicable(h, prev).equals(d)) {
                    run++;
                } else {
                    run = 1;
                }
                best = Math.max(best, run);
                prev = d;
            }
            return best;
        }
    }

    public static void main(String[] args) {
        HabitTracker tracker = new HabitTracker();
        Scanner sc = new Scanner(System.in);
        LocalDate today = LocalDate.now();

        System.out.println("=== Habit Tracker ===");
        System.out.println("Commands: add | today | done | streak | search | archive | unarchive | list | exit");

        while (true) {
            System.out.print("> ");
            if (!sc.hasNextLine()) break;
            String cmd = sc.nextLine().trim().toLowerCase();
            if (cmd.isEmpty()) continue;

            switch (cmd) {
                case "add" -> {
                    System.out.print("Habit name: ");
                    String name = sc.nextLine().trim();
                    System.out.print("Frequency (DAILY/WEEKDAYS): ");
                    Frequency f = sc.nextLine().trim().equalsIgnoreCase("WEEKDAYS")
                            ? Frequency.WEEKDAYS : Frequency.DAILY;
                    tracker.addHabit(name, f, today);
                    System.out.println("Added: " + name);
                }
                case "today" -> {
                    List<Habit> list = tracker.getTodayHabits(today);
                    if (list.isEmpty()) System.out.println("No habits for today.");
                    for (Habit h : list) {
                        boolean done = h.completedDates.contains(today);
                        System.out.printf("[%s] %s (streak: %d, best: %d)%n",
                                done ? "x" : " ", h.name,
                                tracker.currentStreak(h, today), tracker.bestStreak(h));
                    }
                }
                case "done" -> {
                    System.out.print("Habit name: ");
                    String name = sc.nextLine().trim();
                    System.out.println(tracker.markDone(name, today)
                            ? "Marked done: " + name
                            : "Could not mark (not found or not applicable today).");
                }
                case "streak" -> {
                    System.out.print("Habit name: ");
                    String name = sc.nextLine().trim();
                    tracker.findByName(name).ifPresentOrElse(h ->
                            System.out.printf("Current: %d, Best: %d%n",
                                    tracker.currentStreak(h, today), tracker.bestStreak(h)),
                            () -> System.out.println("Not found."));
                }
                case "search" -> {
                    System.out.print("Query: ");
                    String q = sc.nextLine().trim();
                    List<Habit> results = tracker.search(q);
                    if (results.isEmpty()) System.out.println("No matches.");
                    for (Habit h : results)
                        System.out.println((h.archived ? "[archived] " : "") + h.name);
                }
                case "archive" -> {
                    System.out.print("Habit name: ");
                    System.out.println(tracker.archive(sc.nextLine().trim()) ? "Archived." : "Not found.");
                }
                case "unarchive" -> {
                    System.out.print("Habit name: ");
                    System.out.println(tracker.unarchive(sc.nextLine().trim()) ? "Unarchived." : "Not found.");
                }
                case "list" -> {
                    System.out.println("Active:");
                    for (Habit h : tracker.listActive()) System.out.println(" - " + h.name);
                    System.out.println("Archived:");
                    for (Habit h : tracker.listArchived()) System.out.println(" - " + h.name);
                }
                case "exit" -> { System.out.println("Bye!"); sc.close(); return; }
                default -> System.out.println("Unknown command.");
            }
        }
        sc.close();
    }
}
5. Dry Run
Say today = 2026-09-16 (Wednesday).

add → "Drink Water", DAILY, created 2026-09-14 (Mon)
add → "Workout", WEEKDAYS, created 2026-09-14 (Mon)
Mark done: Drink Water on 14, 15, 16. Workout on 14, 15 (not yet on 16 — day isn't over).
streak Drink Water

today(16) applicable + done → streak=1
previousApplicable(16) = 15, done → streak=2
previousApplicable(15) = 14, done → streak=3
previousApplicable(14) → below createdOn → null → stop
Current = 3, Best = 3 (14,15,16 unbroken)
streak Workout

today(16) applicable but not done → don't add
previousApplicable(16) = 15 (Tue, applicable), done → streak=1
previousApplicable(15) = 14 (Mon), done → streak=2
previousApplicable(14) → null → stop
Current = 2 (Mon+Tue done, Wed pending — correctly not broken)
Best: 14→15 is nextApplicable(14)=15, matches → run=2 → Best = 2
archive "No Sugar" → hidden from today/list active, but its history/streaks stay intact if queried directly.
search work → returns "Workout" (case-insensitive substring).

6. Time Complexity
getTodayHabits, list, search: O(n), n = number of habits (tiny — dozens at most)
markDone, archive: O(n) for name lookup + O(log m) for TreeSet insert
currentStreak: O(s), s = length of the current streak (bounded by 75 days max)
bestStreak: O(m), m = total completions for that habit (bounded by 75)
Nothing here scales beyond the 75-day challenge size, so it's effectively constant-time in practice while still being the right complexity class.

7. Space Complexity
O(n + m) — n habits + total completion dates stored across all habits (worst case ~75 per habit). No redundant copies of data.

8. Edge Cases
Habit not found (wrong name) → Optional.empty() handled gracefully everywhere, no crash
Marking done on a day the habit doesn't apply to (e.g., weekday habit on Sunday) → markDone rejects it
Marking done twice same day → TreeSet dedups automatically, no double-count
Brand-new habit, zero completions → bestStreak returns 0 cleanly (loop body never runs)
Habit created today, nothing done yet → current streak = 0, no null-pointer from previousApplicable
Archived habit → excluded from today, but streak still computable if looked up directly (data preserved, per requirement)
Duplicate habit names → current design allows it (first match wins on lookup); worth flagging as a known limitation rather than silently "fixing" scope
9. Alternative Approaches
Store a boolean per day per habit (calendar grid) instead of a set of dates — simpler mentally, but wastes space for habits with sparse history and makes streak-walking no faster; TreeSet<LocalDate> is strictly better here.
Precompute/cache streaks on every write instead of computing on read — better if you expect frequent reads (e.g., a real UI refreshing constantly); for a CLI/API used a few times a day, computing on demand is simpler and avoids stale-cache bugs. Worth mentioning as a "next step" if this scales to many users.
Delete instead of archive — rejected outright since the problem explicitly says "not gone forever."

okay give which file to make from fresh and also give full working code  abhi yaha hu m 
 kese drag karo zip file ko 

Yes. Since the requirement is only setup, running, and debugging, keep README.md focused and short.

Copy-paste this entire file as README.md:

# Streakly — 75-Day Habit Tracker

Streakly is a 75-day habit tracking application that allows users to create habits, mark daily completions, track current and best-ever streaks, search habits, and archive/restore habits without permanently deleting them.

## Tech Stack

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- H2 Database
- Maven
- HTML
- CSS
- Vanilla JavaScript

---

# Project Setup

## Prerequisites

Make sure the following are installed:

- Java 17 or higher
- Maven 3.8+
- Git

Check Java:

```bash
java -version

Check Maven:

mvn -version
Run the Project
1. Clone the Repository
git clone <YOUR_GITHUB_REPOSITORY_URL>

Move into the project directory:

cd streakly-habit-tracker
2. Start the Application

Run:

mvn spring-boot:run

The application will start on:

http://localhost:8080

Open the URL in a browser.

Running in GitHub Codespaces
Open the GitHub repository.
Open the repository in GitHub Codespaces.
Open the terminal.
Run:
mvn spring-boot:run
Wait until Spring Boot starts successfully.
Open the PORTS tab.
Find port 8080.
Click Open in Browser.

The application will then open using the forwarded Codespaces URL.

Database

The project uses a file-based H2 database.

Database URL:

jdbc:h2:file:./data/streaklydb

The database is created automatically when the application starts.

No separate database installation is required.

H2 Console

The H2 console is available at:

http://localhost:8080/h2-console

Use the following JDBC URL:

jdbc:h2:file:./data/streaklydb

The H2 console can be used to inspect the stored habits and completion records while debugging.

API Endpoints
Get Today's Habits
GET /api/habits/today

Returns active habits applicable for the current day.

Get All Active Habits
GET /api/habits
Get Archived Habits
GET /api/habits/archived
Search Habits
GET /api/habits/search?q=work
Create a Habit
POST /api/habits

Example request:

{
  "name": "Workout",
  "frequency": "DAILY"
}

Supported frequencies:

DAILY
WEEKDAYS
Toggle Habit Completion
POST /api/habits/{id}/toggle

This marks today's habit as completed or incomplete.

Archive a Habit
POST /api/habits/{id}/archive
Restore a Habit
POST /api/habits/{id}/restore
Dashboard
GET /api/dashboard

Returns 75-day challenge and progress information.

Debugging
1. Application Does Not Start

Check the Java version:

java -version

The project requires Java 17 or higher.

Then try:

mvn clean
mvn spring-boot:run
2. Port 8080 Already in Use

If port 8080 is already being used, stop the existing application or change the port in:

src/main/resources/application.properties

For example:

server.port=8081

Then run:

mvn spring-boot:run

The application will be available at:

http://localhost:8081
3. API Is Not Working

First check whether the Spring Boot application is running.

Then test:

http://localhost:8080/api/habits

If the endpoint does not respond, check the terminal logs for errors.

4. Frontend Is Not Updating

Open the browser developer tools:

F12

Check:

Console
Network
API response status

Verify that the frontend is successfully calling the /api/... endpoints.

5. Database Problems

Check that the application has permission to create the database directory.

The database is stored under:

./data/streaklydb

If necessary, stop the application and restart it:

mvn spring-boot:run

For development/debugging, the H2 console can also be used to inspect database records.

Useful Maven Commands

Run the application:

mvn spring-boot:run

Clean the project:

mvn clean

Compile the project:

mvn compile

Run tests:

mvn test

Create a build:

mvn clean package
Project Structure
streakly-habit-tracker/
│
├── README.md
├── REASONING.md
├── pom.xml
│
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── streakly/
        │           ├── StreaklyApplication.java
        │           ├── controller/
        │           │   └── HabitController.java
        │           ├── service/
        │           │   └── HabitService.java
        │           ├── repository/
        │           │   ├── HabitRepository.java
        │           │   └── HabitCompletionRepository.java
        │           ├── model/
        │           │   ├── Frequency.java
        │           │   ├── Habit.java
        │           │   └── HabitCompletion.java
        │           └── dto/
        │               └── HabitRequest.java
        │
        └── resources/
            ├── application.properties
            └── static/
                ├── index.html
                ├── style.css
                └── app.js
Quick Start

For a quick setup:

git clone <YOUR_GITHUB_REPOSITORY_URL>
cd streakly-habit-tracker
mvn spring-boot:run

Then open:

http://localhost:8080

The application is ready to use.

- AI_LOGS.md — a complete copy of the candidate's conversation with the AI tool (Copilot or any other). This log must be pasted as-is and not modified in any way; any alteration will lead to a deduction of marks. 
 now give these one 

Haan, AI_LOGS.md ke case mein important point hai: assignment explicitly bol raha hai complete AI conversation, as-is, without modification.

Isliye main tumhare liye fake/shortened conversation ko AI_LOGS.md nahi banaunga, kyunki woh requirement violate karega.

AI_LOGS.md mein exactly kya karna hai
Is ChatGPT conversation ko start se end tak select/copy karo.
Jo conversation copy hui hai usko bilkul same wording mein paste karo.
Kuch bhi:
edit mat karna
shorten mat karna
grammar correct mat karna
messages remove mat karna
apni taraf se explanation add mat karna
File ka exact naam rakho:
AI_LOGS.md
Root folder mein rakho:
streakly-habit-tracker/
├── README.md
├── REASONING.md
├── AI_LOGS.md
├── pom.xml
└── src/



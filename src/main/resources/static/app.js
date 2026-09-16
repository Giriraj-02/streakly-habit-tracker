let currentTab = "today";

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("dateLabel").textContent =
        new Date().toLocaleDateString("en-IN", {
            weekday: "long",
            day: "numeric",
            month: "short"
        }).toUpperCase();

    loadDashboard();
    loadHabits();
});

async function api(url, options = {}) {
    const response = await fetch(url, {
        headers: {
            "Content-Type": "application/json"
        },
        ...options
    });

    if (!response.ok) {
        let message = "Something went wrong";
        try {
            const data = await response.json();
            message = data.detail || data.message || data.error || message;
        } catch (_) {}
        throw new Error(message);
    }

    return response.json();
}

async function loadDashboard() {
    try {
        const data = await api("/api/dashboard");

        document.getElementById("challengeDay").textContent =
            `${data.currentDay} / ${data.totalDays}`;

        document.getElementById("progressText").textContent =
            `${data.progressPercent}%`;

        document.getElementById("progressBar").style.width =
            `${data.progressPercent}%`;

        document.getElementById("todayCount").textContent =
            `${data.completedToday} / ${data.totalToday}`;
    } catch (error) {
        showToast(error.message);
    }
}

async function loadHabits() {
    try {
        let url;

        if (currentTab === "today") {
            url = "/api/habits/today";
            document.getElementById("sectionTitle").textContent = "Today's habits";
        } else if (currentTab === "archived") {
            url = "/api/habits/archived";
            document.getElementById("sectionTitle").textContent = "Archived habits";
        } else {
            url = "/api/habits";
            document.getElementById("sectionTitle").textContent = "All habits";
        }

        const habits = await api(url);
        renderHabits(habits);
    } catch (error) {
        showToast(error.message);
    }
}

function renderHabits(habits) {
    const list = document.getElementById("habitList");

    if (!habits.length) {
        list.innerHTML = `
            <div class="empty">
                <strong>No habits here yet.</strong>
                Add a habit or switch to another tab.
            </div>
        `;
        return;
    }

    list.innerHTML = habits.map(habit => `
        <article class="habit-card">
            <button
                class="check ${habit.completedToday ? "done" : ""}"
                onclick="toggleHabit(${habit.id})"
                title="Mark today's habit">
                ${habit.completedToday ? "✓" : ""}
            </button>

            <div>
                <h3 class="habit-name">${escapeHtml(habit.name)}</h3>

                <div class="habit-meta">
                    <span class="badge ${habit.frequency === "DAILY" ? "daily" : ""}">
                        ${habit.frequency === "DAILY" ? "Every day" : "Weekdays"}
                    </span>

                    ${habit.completedToday
                        ? `<span class="badge">Completed today</span>`
                        : ""}
                </div>

                <button class="archive-btn"
                    onclick="${habit.archived
                        ? `restoreHabit(${habit.id})`
                        : `archiveHabit(${habit.id})`}">
                    ${habit.archived ? "Restore habit" : "Archive habit"}
                </button>
            </div>

            <div class="stats">
                <div class="stat">
                    <strong class="fire">🔥 ${habit.currentStreak}</strong>
                    <span>Current streak</span>
                </div>

                <div class="stat">
                    <strong>🏆 ${habit.bestStreak}</strong>
                    <span>Best streak</span>
                </div>
            </div>
        </article>
    `).join("");
}

async function toggleHabit(id) {
    try {
        await api(`/api/habits/${id}/toggle`, { method: "POST" });
        await Promise.all([loadDashboard(), loadHabits()]);
        showToast("Habit updated");
    } catch (error) {
        showToast(error.message);
    }
}

async function archiveHabit(id) {
    try {
        await api(`/api/habits/${id}/archive`, { method: "POST" });
        await Promise.all([loadDashboard(), loadHabits()]);
        showToast("Habit archived — you can restore it anytime");
    } catch (error) {
        showToast(error.message);
    }
}

async function restoreHabit(id) {
    try {
        await api(`/api/habits/${id}/restore`, { method: "POST" });
        await Promise.all([loadDashboard(), loadHabits()]);
        showToast("Habit restored");
    } catch (error) {
        showToast(error.message);
    }
}

async function addHabit() {
    const nameInput = document.getElementById("habitName");
    const name = nameInput.value.trim();
    const frequency = document.querySelector(
        'input[name="frequency"]:checked'
    ).value;

    if (!name) {
        showToast("Please enter a habit name");
        return;
    }

    try {
        await api("/api/habits", {
            method: "POST",
            body: JSON.stringify({ name, frequency })
        });

        nameInput.value = "";
        closeModal();
        currentTab = "today";
        updateTabs();

        await Promise.all([loadDashboard(), loadHabits()]);
        showToast("Habit created");
    } catch (error) {
        showToast(error.message);
    }
}

async function searchHabits() {
    const query = document.getElementById("searchInput").value.trim();

    if (!query) {
        loadHabits();
        return;
    }

    try {
        const habits = await api(
            `/api/habits/search?q=${encodeURIComponent(query)}`
        );
        renderHabits(habits);
    } catch (error) {
        showToast(error.message);
    }
}

function switchTab(tab) {
    currentTab = tab;
    updateTabs();
    document.getElementById("searchInput").value = "";
    loadHabits();
}

function updateTabs() {
    document.querySelectorAll(".tab").forEach(button => {
        button.classList.toggle(
            "active",
            button.dataset.tab === currentTab
        );
    });
}

function openModal() {
    document.getElementById("modal").classList.remove("hidden");
    document.getElementById("habitName").focus();
}

function closeModal() {
    document.getElementById("modal").classList.add("hidden");
}

function showToast(message) {
    const toast = document.getElementById("toast");
    toast.textContent = message;
    toast.classList.add("show");

    setTimeout(() => toast.classList.remove("show"), 2500);
}

function escapeHtml(value) {
    return value
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

/* ========================================= */
/* ELEMENT REFERENCES                        */
/* ========================================= */

const chatBox = document.getElementById("chat-box");
const userInput = document.getElementById("userInput");
const sendBtn = document.getElementById("sendBtn");
const newChatBtn = document.getElementById("newChatBtn");
const logoutBtn = document.getElementById("logoutBtn");

let isRequestInProgress = false;
let currentSessionId = null;

/* ========================================= */
/* INITIALIZE APP                            */
/* ========================================= */

window.addEventListener("DOMContentLoaded", initApp);

async function initApp() {
    try {
        const res = await fetch("/api/chats", {
            credentials: "include"
        });

        if (!res.ok) return;

        const chats = await res.json();

        if (chats.length > 0) {
            currentSessionId = chats[0].id;
            renderSidebar(chats);
            await loadChatHistory(currentSessionId);
        } else {
            renderSidebar([]);
        }

    } catch (err) {
        console.error("App initialization failed", err);
    }
}

/* ========================================= */
/* RENDER SIDEBAR                            */
/* ========================================= */

function renderSidebar(chats) {
    const sidebar = document.getElementById("sidebarChats");
    if (!sidebar) return;

    sidebar.innerHTML = "";

    chats.forEach(chat => {
        const div = document.createElement("div");
        div.className = "sidebar-chat-item";

        if (chat.id === currentSessionId) {
            div.classList.add("active");
        }

        div.textContent = chat.title || "New Chat";

        div.addEventListener("click", async () => {
            currentSessionId = chat.id;
            await loadChatHistory(chat.id);
            renderSidebar(chats);
        });

        sidebar.appendChild(div);
    });
}

/* ========================================= */
/* CREATE NEW CHAT                           */
/* ========================================= */

async function createNewChat() {
    try {
        const res = await fetch("/api/chats", {
            method: "POST",
            credentials: "include"
        });

        if (!res.ok) throw new Error("Failed to create chat");

        const session = await res.json();

        currentSessionId = session.id;
        chatBox.innerHTML = "";

        // Reload chats
        const chatsRes = await fetch("/api/chats", {
            credentials: "include"
        });

        const chats = await chatsRes.json();
        renderSidebar(chats);

    } catch (err) {
        console.error("New chat failed", err);
        alert("Could not create new chat");
    }
}

/* ========================================= */
/* LOAD CHAT HISTORY                         */
/* ========================================= */

async function loadChatHistory(sessionId) {
    try {
        const res = await fetch(`/api/chats/${sessionId}/messages`, {
            credentials: "include"
        });

        if (!res.ok) return;

        const messages = await res.json();

        chatBox.innerHTML = "";

        messages.forEach(msg => {
            addMessage(msg.content, msg.role.toLowerCase());
        });

        chatBox.scrollTop = chatBox.scrollHeight;

    } catch (err) {
        console.error("Failed to load chat history", err);
    }
}

/* ========================================= */
/* ADD MESSAGE                               */
/* ========================================= */

function addMessage(text, sender, extraClass = "") {
    const div = document.createElement("div");
    div.className = `message ${sender} ${extraClass}`;
    div.textContent = text;
    chatBox.appendChild(div);
    chatBox.scrollTop = chatBox.scrollHeight;
    return div;
}

/* ========================================= */
/* TYPING EFFECT                             */
/* ========================================= */

function typeEffect(text) {
    const msgDiv = addMessage("", "ai");
    let i = 0;

    const typing = setInterval(() => {
        if (i < text.length) {
            msgDiv.textContent += text.charAt(i);
            i++;
            chatBox.scrollTop = chatBox.scrollHeight;
        } else {
            clearInterval(typing);
            isRequestInProgress = false;
            userInput.disabled = false;
            sendBtn.disabled = false;
            userInput.focus();
        }
    }, 20);
}

/* ========================================= */
/* SEND MESSAGE                              */
/* ========================================= */

function sendMessage() {

    if (!currentSessionId) {
        addMessage("⚠ Please create or select a chat first.", "ai", "system");
        return;
    }

    const message = userInput.value.trim();
    if (!message) return;

    if (isRequestInProgress) {
        addMessage("⏳ Please wait, AI is still responding...", "ai");
        return;
    }

    isRequestInProgress = true;
    userInput.disabled = true;
    sendBtn.disabled = true;

    addMessage(message, "user");
    userInput.value = "";

    const thinkingMsg = addMessage("🧠 Thinking...", "ai", "thinking");

    fetch("/api/ai", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({
            message: message,
            sessionId: currentSessionId
        })
    })
    .then(res => {
        if (res.status === 429) throw new Error("RATE_LIMIT");
        return res.json();
    })
    .then(data => {
        thinkingMsg.remove();
        typeEffect(data.reply);
    })
    .catch(err => {
        thinkingMsg.remove();
        isRequestInProgress = false;
        userInput.disabled = false;
        sendBtn.disabled = false;

        if (err.message === "RATE_LIMIT") {
            addMessage("⏸ Please wait before sending another request.", "ai", "system");
        } else {
            addMessage("⚠ Something went wrong. Please try again.", "ai", "system");
        }
    });
}

/* ========================================= */
/* EVENT LISTENERS                           */
/* ========================================= */

if (sendBtn) sendBtn.addEventListener("click", sendMessage);

if (userInput) {
    userInput.addEventListener("keydown", function (e) {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });
}

if (newChatBtn) newChatBtn.addEventListener("click", createNewChat);

if (logoutBtn) {
    logoutBtn.addEventListener("click", function () {
        fetch("/api/auth/logout", {
            method: "POST",
            credentials: "include"
        })
        .then(() => window.location.href = "/Account.html")
        .catch(() => alert("Logout failed"));
    });
}
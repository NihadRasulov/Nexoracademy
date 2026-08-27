(() => {
  "use strict";

  const elements = {
    form: document.getElementById("chat-form"),
    input: document.getElementById("message-input"),
    send: document.getElementById("send-button"),
    messages: document.getElementById("messages"),
    loadingIntro: document.getElementById("loading-intro"),
    connection: document.getElementById("connection-status"),
    connectionLabel: document.getElementById("connection-label"),
    newChat: document.getElementById("new-chat-button"),
    errorBanner: document.getElementById("error-banner"),
    errorMessage: document.getElementById("error-message"),
    retry: document.getElementById("retry-button"),
    messageTemplate: document.getElementById("message-template"),
    courseTemplate: document.getElementById("course-template"),
  };

  const API_BASE = resolveApiBase();
  const API_URL = `${API_BASE}/api/chat`;
  const STORAGE_KEY = "nexora-ai-session-id";
  const MAX_HISTORY = 80;

  let sessionId = getOrCreateSessionId();
  let pending = false;
  let lastRequest = null;
  let requestController = null;
  let history = [];

  function resolveApiBase() {
    const configured = document.querySelector('meta[name="api-base"]')?.content?.trim();
    if (configured) return configured.replace(/\/$/, "");
    return window.location.protocol === "file:" ? "http://127.0.0.1:8000" : "";
  }

  function createId() {
    if (window.crypto?.randomUUID) return window.crypto.randomUUID();
    return `nexora-${Date.now()}-${Math.random().toString(16).slice(2)}`;
  }

  function getOrCreateSessionId() {
    try {
      const existing = window.localStorage.getItem(STORAGE_KEY);
      if (existing) return existing;
      const created = createId();
      window.localStorage.setItem(STORAGE_KEY, created);
      return created;
    } catch (_) {
      return createId();
    }
  }

  function setConnection(state, label) {
    elements.connection.dataset.state = state;
    elements.connectionLabel.textContent = label;
  }

  function setPending(value) {
    pending = value;
    elements.input.disabled = value;
    elements.messages.setAttribute("aria-busy", String(value));
    elements.send.disabled = value || !elements.input.value.trim();
    document.querySelectorAll(".action-button").forEach((button) => {
      button.disabled = value || button.dataset.used === "true";
    });
  }

  function resizeInput() {
    elements.input.style.height = "auto";
    elements.input.style.height = `${Math.min(elements.input.scrollHeight, 132)}px`;
  }

  function scrollToBottom(behavior = "smooth") {
    window.requestAnimationFrame(() => {
      elements.messages.scrollTo({ top: elements.messages.scrollHeight, behavior });
    });
  }

  function showError(message) {
    elements.errorMessage.textContent = message;
    elements.errorBanner.hidden = false;
  }

  function hideError() {
    elements.errorBanner.hidden = true;
  }

  function normalizeText(value, fallback = "") {
    return typeof value === "string" ? value : fallback;
  }

  function applyCapture(capture) {
    const mode = normalizeText(capture, "none");
    const placeholders = {
      name: "Write your full name…",
      phone: "Write your phone number: +994XXXXXXXXX…",
      email: "Write your email address…",
      none: "Ask Nexora AI anything…",
    };
    elements.input.placeholder = placeholders[mode] || placeholders.none;
    elements.input.inputMode = mode === "phone" ? "tel" : mode === "email" ? "email" : "text";
    elements.input.autocomplete = mode === "name" ? "name" : mode === "phone" ? "tel" : mode === "email" ? "email" : "off";
  }

  function addMessage(role, text, response = {}) {
    const fragment = elements.messageTemplate.content.cloneNode(true);
    const article = fragment.querySelector(".message");
    const bubble = fragment.querySelector(".message__bubble");
    const actions = fragment.querySelector(".message__actions");
    const courses = fragment.querySelector(".message__courses");

    article.classList.add(role === "user" ? "message--user" : "message--assistant");
    bubble.textContent = normalizeText(text, "No response received.");

    if (role === "assistant") {
      renderCourses(courses, Array.isArray(response.courses) ? response.courses : []);
      renderActions(actions, Array.isArray(response.actions) ? response.actions : []);
    }

    elements.messages.appendChild(fragment);
    history.push({ role, text: normalizeText(text), response });
    if (history.length > MAX_HISTORY) history = history.slice(-MAX_HISTORY);
    scrollToBottom();
  }

  function addLocalError(text) {
    const fragment = elements.messageTemplate.content.cloneNode(true);
    const article = fragment.querySelector(".message");
    fragment.querySelector(".message__bubble").textContent = text;
    article.classList.add("message--assistant", "message--error");
    elements.messages.appendChild(fragment);
    scrollToBottom();
  }

  function renderActions(container, actions) {
    actions.forEach((action) => {
      const label = normalizeText(action?.label).trim();
      const value = normalizeText(action?.value, label).trim();
      if (!label || !value) return;
      const button = document.createElement("button");
      button.type = "button";
      button.className = "action-button";
      button.textContent = label;
      button.dataset.value = value;
      button.addEventListener("click", () => {
        if (pending) return;
        sendMessage(value, label);
      });
      container.appendChild(button);
    });
  }

  function renderCourses(container, courses) {
    courses.forEach((course) => {
      const fragment = elements.courseTemplate.content.cloneNode(true);
      const category = normalizeText(course?.category, "Course");
      const name = normalizeText(course?.name, "Nexora course");
      const level = normalizeText(course?.level);
      const instructor = normalizeText(course?.instructor);
      const scheduleDays = normalizeText(course?.schedule?.days);
      const scheduleTime = normalizeText(course?.schedule?.time);
      const tools = Array.isArray(course?.tools) ? course.tools.filter((tool) => typeof tool === "string") : [];

      fragment.querySelector(".course-card__category").textContent = category;
      fragment.querySelector(".course-card__name").textContent = name;
      fragment.querySelector(".course-card__price").textContent = Number.isFinite(course?.price) ? `${course.price} AZN` : "";

      const meta = fragment.querySelector(".course-card__meta");
      [level, instructor, [scheduleDays, scheduleTime].filter(Boolean).join(" · ")]
        .filter(Boolean)
        .forEach((item) => {
          const span = document.createElement("span");
          span.textContent = item;
          meta.appendChild(span);
        });

      const toolContainer = fragment.querySelector(".course-card__tools");
      tools.slice(0, 5).forEach((tool) => {
        const tag = document.createElement("span");
        tag.className = "tool-tag";
        tag.textContent = tool;
        toolContainer.appendChild(tag);
      });
      container.appendChild(fragment);
    });
  }

  function showTyping() {
    const wrapper = document.createElement("div");
    wrapper.className = "loading-intro";
    wrapper.id = "active-typing";
    wrapper.setAttribute("aria-label", "Nexora AI is thinking");
    wrapper.innerHTML = `<div class="assistant-avatar assistant-avatar--small" aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M12 3.25a8.75 8.75 0 1 0 0 17.5 8.75 8.75 0 0 0 0-17.5Z"/><path d="M8.5 12.3c.8 1.3 2 1.95 3.5 1.95s2.7-.65 3.5-1.95M9.25 9.4h.01M14.75 9.4h.01"/></svg></div><div class="typing" aria-hidden="true"><span></span><span></span><span></span></div>`;
    elements.messages.appendChild(wrapper);
    scrollToBottom();
  }

  function hideTyping() {
    document.getElementById("active-typing")?.remove();
  }

  async function requestChat(message) {
    requestController?.abort();
    requestController = new AbortController();
    const timeoutId = window.setTimeout(() => requestController.abort(), 35000);
    try {
      const response = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json", Accept: "application/json" },
        body: JSON.stringify({ message, sessionId, conversationId: sessionId }),
        signal: requestController.signal,
      });
      if (!response.ok) throw new Error(`Server returned ${response.status}`);
      const data = await response.json();
      if (!data || typeof data.reply !== "string") throw new Error("Invalid assistant response");
      return data;
    } finally {
      window.clearTimeout(timeoutId);
    }
  }

  async function sendMessage(value, displayValue = value, options = {}) {
    const message = normalizeText(value).trim();
    if (!message || pending) return;
    hideError();
    lastRequest = { value: message, displayValue };
    document.querySelectorAll(".action-button").forEach((button) => {
      button.dataset.used = "true";
      button.disabled = true;
    });
    if (!options.silentUser) addMessage("user", displayValue);
    setPending(true);
    showTyping();
    try {
      const data = await requestChat(message);
      hideTyping();
      addMessage("assistant", data.reply, data);
      applyCapture(data.capture);
      setConnection("online", "Online");
      lastRequest = null;
    } catch (error) {
      hideTyping();
      const aborted = error?.name === "AbortError";
      showError(aborted ? "Sorğunun vaxtı bitdi. Yenidən cəhd edin." : "AI köməkçiyə qoşulmaq mümkün olmadı.");
      setConnection("offline", "Offline");
    } finally {
      setPending(false);
      elements.input.focus({ preventScroll: true });
    }
  }

  async function initializeConversation() {
    if (!elements.loadingIntro.isConnected) elements.messages.appendChild(elements.loadingIntro);
    elements.loadingIntro.hidden = false;
    setConnection("connecting", "Qoşulur");
    setPending(true);
    hideError();
    try {
      const data = await requestChat("/start");
      elements.loadingIntro.hidden = true;
      addMessage("assistant", data.reply, data);
      applyCapture(data.capture);
      setConnection("online", "Online");
    } catch (_) {
      elements.loadingIntro.hidden = true;
      addLocalError("AI köməkçi hazırda cavab vermir.");
      showError("Serveri yoxlayın və yenidən cəhd edin.");
      setConnection("offline", "Offline");
      lastRequest = { value: "/start", displayValue: "" };
    } finally {
      setPending(false);
    }
  }

  function resetConversation() {
    requestController?.abort();
    sessionId = createId();
    try { window.localStorage.setItem(STORAGE_KEY, sessionId); } catch (_) { /* optional */ }
    history = [];
    lastRequest = null;
    elements.messages.replaceChildren();
    elements.input.value = "";
    applyCapture("none");
    resizeInput();
    initializeConversation();
  }

  elements.form.addEventListener("submit", (event) => {
    event.preventDefault();
    const value = elements.input.value.trim();
    if (!value || pending) return;
    elements.input.value = "";
    resizeInput();
    elements.send.disabled = true;
    sendMessage(value);
  });

  elements.input.addEventListener("input", () => {
    resizeInput();
    elements.send.disabled = pending || !elements.input.value.trim();
  });

  elements.input.addEventListener("keydown", (event) => {
    if (event.key === "Enter" && !event.shiftKey && !event.isComposing) {
      event.preventDefault();
      elements.form.requestSubmit();
    }
  });

  elements.newChat.addEventListener("click", resetConversation);
  elements.retry.addEventListener("click", () => {
    if (pending) return;
    hideError();
    if (lastRequest?.value === "/start") {
      elements.messages.replaceChildren();
      initializeConversation();
      return;
    }
    if (lastRequest) sendMessage(lastRequest.value, lastRequest.displayValue, { silentUser: true });
    else initializeConversation();
  });
  window.addEventListener("online", () => setConnection("connecting", "Reconnecting"));
  window.addEventListener("offline", () => setConnection("offline", "Offline"));
  resizeInput();
  initializeConversation();
})();

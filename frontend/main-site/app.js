(() => {
  "use strict";

  const $ = (selector, root = document) => root.querySelector(selector);
  const $$ = (selector, root = document) =>
    Array.from(root.querySelectorAll(selector));
  const clamp = (value, min, max) => Math.max(min, Math.min(max, value));
  const MOCK_IMAGE_FALLBACKS = Object.freeze({
    "hero-networking": {
      src: "assets/mock/mock-hero-networking.svg",
      alt: "Şəbəkə infrastrukturu üzrə nümunəvi hero vizualı",
    },
    "course-networking": {
      src: "assets/mock/mock-course-networking.svg",
      alt: "Şəbəkə texnologiyaları kursu üçün nümunəvi vizual",
    },
    "course-cybersecurity": {
      src: "assets/mock/mock-course-cybersecurity.svg",
      alt: "Kibertəhlükəsizlik kursu üçün nümunəvi vizual",
    },
    "course-cloud-devops": {
      src: "assets/mock/mock-course-cloud-devops.svg",
      alt: "Cloud və DevOps kursu üçün nümunəvi vizual",
    },
    "instructor-1": {
      src: "assets/mock/mock-instructor-networking-1.svg",
      alt: "Şəbəkə texnologiyaları müəllimi üçün nümunəvi portret",
    },
    "instructor-2": {
      src: "assets/mock/mock-instructor-networking-2.svg",
      alt: "IT sertifikasiya müəllimi üçün nümunəvi portret",
    },
    "instructor-3": {
      src: "assets/mock/mock-instructor-networking-3.svg",
      alt: "Cloud texnologiyaları müəllimi üçün nümunəvi portret",
    },
    "blog-networking": {
      src: "assets/mock/mock-blog-networking.svg",
      alt: "Şəbəkə texnologiyaları bloqu üçün nümunəvi vizual",
    },
    "blog-cybersecurity": {
      src: "assets/mock/mock-blog-cybersecurity.svg",
      alt: "Kibertəhlükəsizlik bloqu üçün nümunəvi vizual",
    },
    "blog-cloud-devops": {
      src: "assets/mock/mock-blog-cloud-devops.svg",
      alt: "Cloud və DevOps bloqu üçün nümunəvi vizual",
    },
    "scholarship-certification": {
      src: "assets/mock/mock-scholarship-certification.svg",
      alt: "IT sertifikasiyası təqaüdü üçün nümunəvi vizual",
    },
    "service-networking": {
      src: "assets/mock/mock-service-networking.svg",
      alt: "Şəbəkə laboratoriyası xidməti üçün nümunəvi ikon",
    },
    "gallery-network-lab": {
      src: "assets/mock/mock-gallery-network-lab.svg",
      alt: "Şəbəkə laboratoriyası üçün nümunəvi qalereya vizualı",
    },
    "career-network-engineer": {
      src: "assets/mock/mock-career-network-engineer.svg",
      alt: "Şəbəkə mühəndisliyi karyerası üçün nümunəvi vizual",
    },
    "faq-networking-guide": {
      src: "assets/mock/mock-faq-networking-guide.svg",
      alt: "Şəbəkə sertifikasiyası bələdçisi üçün nümunəvi vizual",
    },
    "decoration-network-nodes": {
      src: "assets/mock/mock-decoration-network-nodes.svg",
      alt: "Şəbəkə qovşaqlarını göstərən dekorativ vizual",
    },
    "project-networking": {
      src: "assets/mock/mock-project-networking.svg",
      alt: "Şəbəkə avtomatlaşdırması layihəsi üçün nümunəvi vizual",
    },
    "project-cloud-devops": {
      src: "assets/mock/mock-project-cloud-devops.svg",
      alt: "Cloud platforması layihəsi üçün nümunəvi vizual",
    },
    "project-cybersecurity": {
      src: "assets/mock/mock-project-cybersecurity.svg",
      alt: "Kibertəhlükəsizlik layihəsi üçün nümunəvi vizual",
    },
  });
  const IS_LEGACY_ROUTER = false;
  const API_BASE_URL = resolveApiBaseUrl(
    document.querySelector('meta[name="nexora-api-base"]')?.content,
  );
  const CHATBOT_API_BASE_URL = resolveChatbotApiBaseUrl(API_BASE_URL);
  let pageController = null;
  let coursesRequestId = 0;

  function resolveApiBaseUrl(value) {
    const raw = String(value || "")
      .trim()
      .replace(/\/+$/, "");
    if (location.protocol === "file:") return raw;
    try {
      const configured = new URL(raw, location.href);
      const loopback = new Set(["localhost", "127.0.0.1", "::1"]);
      if (
        loopback.has(configured.hostname) &&
        !loopback.has(location.hostname)
      ) {
        return location.origin.replace(/\/+$/, "");
      }
    } catch (_) {
      return location.origin.replace(/\/+$/, "");
    }
    return raw;
  }

  function resolveChatbotApiBaseUrl(platformBase) {
    try {
      const parsed = new URL(platformBase, location.href);
      const loopback = new Set(["localhost", "127.0.0.1", "::1"]);
      if (loopback.has(parsed.hostname) && parsed.port === "8081") {
        parsed.port = "8000";
        return parsed.origin;
      }
    } catch (_) {
      // Deployed traffic uses the same-origin reverse proxy.
    }
    return platformBase;
  }

  function applyDataImageFallbacks(root = document) {
    $$("img[data-image-fallback]", root).forEach((image) => {
      const fallback = MOCK_IMAGE_FALLBACKS[image.dataset.imageFallback];
      if (!fallback) return;
      const dataSource = safeCourseDetailUrl(image.dataset.imageSrc);
      const dataAlt = String(image.dataset.imageAlt || "").trim();
      const existingAlt = String(image.getAttribute("alt") || "").trim();

      if (dataSource) {
        image.addEventListener(
          "error",
          () => {
            image.src = fallback.src;
            image.alt = fallback.alt;
          },
          { once: true },
        );
        image.src = dataSource;
        image.alt = dataAlt || existingAlt || fallback.alt;
        return;
      }

      image.src = fallback.src;
      image.alt = fallback.alt;
    });
  }

  function announce(form, message, state = "success") {
    let node = $(".naic-form-message", form);
    if (!node) {
      node = document.createElement("p");
      node.className = "naic-form-message";
      node.setAttribute("role", "status");
      node.setAttribute("aria-live", "polite");
      form.appendChild(node);
    }
    node.dataset.state = state;
    node.textContent = message;
  }

  function clearInvalid(form) {
    $$(".naic-field-invalid", form).forEach((field) =>
      field.classList.remove("naic-field-invalid"),
    );
  }

  function markInvalid(field) {
    if (!field) return;
    field.classList.add("naic-field-invalid");
    field.addEventListener(
      "input",
      () => field.classList.remove("naic-field-invalid"),
      { once: true },
    );
  }

  function showLegacyFormError(form, error) {
    const aliases = {
      firstName: "fullName",
      lastName: "fullName",
      message: "letter",
    };
    Object.keys(error?.errors || {}).forEach((name) =>
      markInvalid(form.elements.namedItem(aliases[name] || name)),
    );
    announce(form, apiErrorMessage(error), "error");
  }

  function validEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value || "").trim());
  }

  function validPhone(value, required = false) {
    const phone = String(value || "").trim();
    if (!phone) return !required;
    const digits = phone.replace(/\D/g, "").length;
    return (
      phone.length <= 20 &&
      digits >= 7 &&
      digits <= 15 &&
      /^\+?\(?\d[\d ()-]*\d$/.test(phone)
    );
  }

  function safeStoredValue(value) {
    if (value instanceof File) {
      return { name: value.name, size: value.size, type: value.type };
    }
    return value;
  }

  function saveOffline(kind, form) {
    const key = `naic_${kind}_submissions`;
    let current = [];
    try {
      current = JSON.parse(localStorage.getItem(key) || "[]");
    } catch (_) {
      current = [];
    }
    const data = {};
    for (const [name, value] of new FormData(form).entries()) {
      data[name] = safeStoredValue(value);
    }
    current.push({ ...data, stored_at: new Date().toISOString() });
    localStorage.setItem(key, JSON.stringify(current));
    return { ok: true, offline: true };
  }

  class ApiError extends Error {
    constructor(status, message, body = null) {
      super(message);
      this.name = "ApiError";
      this.status = status;
      this.body = body;
      this.errors =
        body?.errors && typeof body.errors === "object" ? body.errors : null;
    }
  }

  function apiErrorMessage(error) {
    if (error?.status === 429)
      return "Çox sayda cəhd edildi. Bir az sonra yenidən yoxlayın.";
    if (error?.status === 0)
      return "Serverlə əlaqə yaratmaq mümkün olmadı. Server tərəfinin işlədiyini və CORS ayarlarını yoxlayın.";
    if (error?.status === 401)
      return "Sessiya etibarsızdır. Yenidən daxil olun.";
    if (error?.status === 403) return "Bu əməliyyat üçün icazəniz yoxdur.";
    return error?.message || "Sorğu zamanı xəta baş verdi.";
  }

  async function apiFetch(path, options = {}) {
    const headers = new Headers(options.headers || {});
    if (
      options.body != null &&
      !(options.body instanceof FormData) &&
      !headers.has("Content-Type")
    ) {
      headers.set("Content-Type", "application/json");
    }

    let response;
    try {
      response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });
    } catch (error) {
      if (error?.name === "AbortError") throw error;
      throw new ApiError(0, "Server tərəfi ilə əlaqə yaradılmadı.");
    }

    let body;
    if (response.status !== 204) {
      const raw = await response.text();
      if (raw) {
        try {
          body = JSON.parse(raw);
        } catch (_) {
          body = { message: raw };
        }
      }
    }

    if (!response.ok) {
      throw new ApiError(
        response.status,
        body?.message || response.statusText || "Sorğu uğursuz oldu.",
        body,
      );
    }
    return body;
  }

  async function chatbotApiFetch(path, options = {}) {
    const headers = new Headers(options.headers || {});
    if (options.body != null && !headers.has("Content-Type"))
      headers.set("Content-Type", "application/json");

    let response;
    try {
      response = await fetch(`${CHATBOT_API_BASE_URL}${path}`, {
        ...options,
        headers,
      });
    } catch (error) {
      if (error?.name === "AbortError") throw error;
      throw new ApiError(0, "Sorğu xidməti ilə əlaqə yaradılmadı.");
    }

    let body;
    const raw = await response.text();
    if (raw) {
      try {
        body = JSON.parse(raw);
      } catch (_) {
        body = { message: raw };
      }
    }
    if (!response.ok) {
      throw new ApiError(
        response.status,
        body?.message ||
          body?.reply ||
          response.statusText ||
          "Sorğu uğursuz oldu.",
        body,
      );
    }
    return body;
  }

  function setFormBusy(form, busy) {
    form.setAttribute("aria-busy", String(busy));
    $$('button[type="submit"]', form).forEach((button) => {
      button.disabled = busy;
    });
  }

  function escapeHtml(value) {
    return String(value ?? "").replace(
      /[&<>"']/g,
      (character) =>
        ({
          "&": "&amp;",
          "<": "&lt;",
          ">": "&gt;",
          '"': "&quot;",
          "'": "&#039;",
        })[character],
    );
  }

  const enumLabels = {
    GUEST: "Qonaq",
    STUDENT: "Tələbə",
    SALES_CRM: "Satış / CRM",
    CONTENT_MANAGER: "Kontent meneceri",
    ADMIN: "Administrator",
    SYSTEM_ADMIN: "Sistem administratoru",
    PENDING_VERIFICATION: "Təsdiq gözlənilir",
    ACTIVE: "Aktiv",
    SUSPENDED: "Dayandırılıb",
    DEACTIVATED: "Deaktiv edilib",
    BANNED: "Bloklanıb",
    BEGINNER: "Başlanğıc",
    INTERMEDIATE: "Orta",
    ADVANCED: "İrəli",
    ONLINE: "Onlayn",
    OFFLINE: "Əyani",
    HYBRID: "Hibrid",
    WAITLISTED: "Gözləmə siyahısı",
    HELD: "Rezerv edilib",
    PENDING_PAYMENT: "Ödəniş gözlənilir",
    CONFIRMED: "Təsdiqlənib",
    COMPLETED: "Tamamlanıb",
    CANCELLED: "Ləğv edilib",
    REFUNDED: "Geri qaytarılıb",
  };

  function enumLabel(value) {
    return (
      enumLabels[value] ||
      String(value || "")
        .replaceAll("_", " ")
        .toLocaleLowerCase("az")
    );
  }

  function formatDate(value) {
    if (!value) return "";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    const months = [
      "yanvar",
      "fevral",
      "mart",
      "aprel",
      "may",
      "iyun",
      "iyul",
      "avqust",
      "sentyabr",
      "oktyabr",
      "noyabr",
      "dekabr",
    ];
    return `${String(date.getUTCDate()).padStart(2, "0")} ${months[date.getUTCMonth()]} ${date.getUTCFullYear()}`;
  }

  function formatPrice(value, currency = "AZN") {
    if (value == null || value === "") return "Qiymət üçün müraciət et";
    const amount = Number(value);
    if (!Number.isFinite(amount)) return String(value);
    try {
      return new Intl.NumberFormat("az-AZ", {
        style: "currency",
        currency: currency || "AZN",
      }).format(amount);
    } catch (_) {
      return `${amount} ${currency || "AZN"}`;
    }
  }

  function createCourseMenuLink(course, className = "") {
    const link = document.createElement("a");
    if (className) link.className = className;
    link.href = course?.id
      ? `course-details.html?id=${encodeURIComponent(course.id)}`
      : "courses.html";
    link.textContent = course?.title || "Bütün kurslar";
    return link;
  }

  async function initCourseMenus(signal, closeMobileMenu) {
    const desktopMenus = $$(".Header_header__menu__drowpdown__KnfZg");
    const mobileCourseBodies = $$(
      '.HeaderMobile_menu__accordion__item__lNOEz[type="button"]',
    )
      .filter((button) => {
        const title = $(
          '[class*="menu__accordion__item__header__title"]',
          button,
        );
        return title?.textContent.replace(/\s+/g, " ").trim() === "Kurslar";
      })
      .map((button) => $('[class*="menu__accordion__item__body"]', button))
      .filter(Boolean);
    if (!desktopMenus.length && !mobileCourseBodies.length) return;

    try {
      const { courses } = await loadPublicCourseCatalog(signal);
      if (signal.aborted) return;
      const visibleCourses = courses.filter(
        (course) => course?.id && String(course.title || "").trim(),
      );

      desktopMenus.forEach((menu) => {
        const categoryLinks = Array.from(menu.children).filter(
          (node) =>
            node.tagName === "A" &&
            node.getAttribute("href") === "categories.html",
        );
        const links = visibleCourses.map((course) =>
          createCourseMenuLink(
            course,
            "Header_header__menu__drowpdown__item__jIbqp",
          ),
        );
        links.push(
          createCourseMenuLink(
            null,
            "Header_header__menu__drowpdown__item__jIbqp",
          ),
        );
        menu.replaceChildren(...links);
        categoryLinks.forEach((link) => menu.append(link));
      });

      mobileCourseBodies.forEach((body) => {
        const categoryLinks = Array.from(body.children).filter(
          (node) =>
            node.tagName === "A" &&
            node.getAttribute("href") === "categories.html",
        );
        const links = visibleCourses.map((course) =>
          createCourseMenuLink(course),
        );
        links.push(createCourseMenuLink(null));
        if (closeMobileMenu) {
          links.forEach((link) =>
            link.addEventListener("click", closeMobileMenu, { signal }),
          );
        }
        body.replaceChildren(...links);
        categoryLinks.forEach((link) => {
          if (closeMobileMenu)
            link.addEventListener("click", closeMobileMenu, { signal });
          body.append(link);
        });
      });
    } catch (error) {
      if (error?.name === "AbortError" || signal.aborted) return;
      // Keep the static "Bütün kurslar" link as a resilient fallback.
    }
  }

  function initHeader(signal) {
    const header = $(".Header_header__8yaFd");
    if (header) {
      const update = () =>
        header.classList.toggle("Header_fixed__CRpV_", window.scrollY > 12);
      update();
      window.addEventListener("scroll", update, { passive: true, signal });
    }
    const mobileMenu = $(".HeaderMobile_header_mobile_menu__b38W_");
    const menuButtons = $$(".header__menu__btn");
    let closeMobileMenu = null;
    if (mobileMenu && menuButtons.length) {
      const open = () => {
        mobileMenu.classList.add("HeaderMobile_show__tPAoO");
        document.documentElement.classList.add("naic-menu-open");
        document.body.classList.add("naic-menu-open");
        menuButtons[0]?.setAttribute("aria-expanded", "true");
      };
      const close = () => {
        mobileMenu.classList.remove("HeaderMobile_show__tPAoO");
        document.documentElement.classList.remove("naic-menu-open");
        document.body.classList.remove("naic-menu-open");
        menuButtons[0]?.setAttribute("aria-expanded", "false");
      };
      closeMobileMenu = close;
      menuButtons[0]?.setAttribute("aria-label", "Open menu");
      menuButtons[0]?.setAttribute("aria-expanded", "false");
      menuButtons[0]?.addEventListener("click", open, { signal });
      menuButtons[1]?.setAttribute("aria-label", "Close menu");
      menuButtons[1]?.addEventListener("click", close, { signal });
      $$("a", mobileMenu).forEach((a) =>
        a.addEventListener("click", close, { signal }),
      );
      document.addEventListener(
        "keydown",
        (event) => {
          if (event.key === "Escape") close();
        },
        { signal },
      );
    }
    $$('.HeaderMobile_menu__accordion__item__lNOEz[type="button"]').forEach(
      (button) => {
        const body = $('[class*="menu__accordion__item__body"]', button);
        if (!body) return;
        button.setAttribute("aria-expanded", "false");
        body.hidden = true;
        button.addEventListener(
          "click",
          () => {
            const expanded = button.getAttribute("aria-expanded") === "true";
            button.setAttribute("aria-expanded", String(!expanded));
            button.classList.toggle("HeaderMobile_active__Y_T4I", !expanded);
            body.hidden = expanded;
          },
          { signal },
        );
      },
    );
    void initCourseMenus(signal, closeMobileMenu);
  }

  function initHeroMedia(signal) {
    const video = $(".HeroSection_video__GVdk5");
    const playButton = $('[data-hero-control="playback"]');
    const muteButton = $('[data-hero-control="sound"]');
    const hasSource = Boolean(
      video?.querySelector("source[src]") || video?.getAttribute("src"),
    );
    if (!video || !hasSource) {
      playButton?.setAttribute("aria-disabled", "true");
      muteButton?.setAttribute("aria-disabled", "true");
      return;
    }
    const icons = {
      play: '<path d="M8 5.75v12.5L18 12 8 5.75Z"></path>',
      pause:
        '<path d="M8 5V19M16 5V19" style="fill:none" stroke="var(--neutral-1)" stroke-linecap="round" stroke-width="2"></path>',
      muted:
        '<path d="M4 9h3l4-4v14l-4-4H4V9Z"></path><path d="m15 9 5 6m0-6-5 6" style="fill:none" stroke="var(--neutral-1)" stroke-linecap="round" stroke-width="2"></path>',
      sound:
        '<path d="M4 9h3l4-4v14l-4-4H4V9Z"></path><path d="M14.5 8.5C16.4 10.4 16.4 13.6 14.5 15.5M17 6C20.3 9.3 20.3 14.7 17 18" style="fill:none" stroke="var(--neutral-1)" stroke-linecap="round" stroke-width="1.8"></path>',
    };
    const setButtonIcon = (button, icon) => {
      const svg = $("svg", button);
      if (!svg) return;
      svg.setAttribute("viewBox", "0 0 24 24");
      svg.setAttribute("aria-hidden", "true");
      svg.setAttribute("focusable", "false");
      svg.innerHTML = icons[icon];
    };
    const syncPlaybackControl = () => {
      if (!playButton) return;
      const isPaused = video.paused || video.ended;
      playButton.dataset.mediaState = isPaused ? "paused" : "playing";
      playButton.setAttribute(
        "aria-label",
        isPaused ? "Videonu oynat" : "Videonu dayandır",
      );
      setButtonIcon(playButton, isPaused ? "play" : "pause");
    };
    const syncSoundControl = () => {
      if (!muteButton) return;
      const isMuted = video.muted || video.volume === 0;
      muteButton.dataset.mediaState = isMuted ? "muted" : "unmuted";
      muteButton.setAttribute("aria-label", isMuted ? "Səsi aç" : "Səsi bağla");
      setButtonIcon(muteButton, isMuted ? "muted" : "sound");
    };
    playButton?.addEventListener(
      "click",
      async () => {
        try {
          if (video.paused) await video.play();
          else video.pause();
          syncPlaybackControl();
        } catch (_) {
          playButton.setAttribute("aria-disabled", "true");
        }
      },
      { signal },
    );
    muteButton?.addEventListener(
      "click",
      () => {
        video.muted = !video.muted;
        syncSoundControl();
      },
      { signal },
    );
    ["play", "pause", "ended"].forEach((eventName) =>
      video.addEventListener(eventName, syncPlaybackControl, { signal }),
    );
    video.addEventListener("volumechange", syncSoundControl, { signal });
    video.addEventListener(
      "loadedmetadata",
      () => {
        syncPlaybackControl();
        syncSoundControl();
      },
      { signal },
    );
    syncPlaybackControl();
    syncSoundControl();
  }

  function initHeroTypewriter(signal) {
    const TYPE_SPEED_MS = 80;
    const DELETE_SPEED_MS = 45;
    const HOLD_AFTER_TYPE_MS = 1700;
    const PAUSE_AFTER_DELETE_MS = 400;
    const START_DELAY_MS = 250;
    const title = $(".HeroSection_content__title__Wr5gI");
    const parts = title ? $$(":scope > span", title) : [];
    if (!title || parts.length !== 2) return;

    const pause = (delay) =>
      new Promise((resolve) => {
        if (signal.aborted) {
          resolve(false);
          return;
        }

        let settled = false;
        const finish = (completed) => {
          if (settled) return;
          settled = true;
          signal.removeEventListener("abort", cancel);
          resolve(completed);
        };
        const timer = window.setTimeout(() => finish(true), delay);
        const cancel = () => {
          window.clearTimeout(timer);
          finish(false);
        };
        signal.addEventListener("abort", cancel, { once: true });
      });

    const run = async () => {
      if (signal.aborted) return;

      const originalParts = parts.map((part) =>
        part.textContent.replace(/\s+/g, " ").trim(),
      );
      if (originalParts.some((part) => !part)) return;

      const exactText = originalParts.join(" ");
      title.setAttribute("aria-label", exactText);
      parts.forEach((part) => {
        part.textContent = "";
        part.setAttribute("aria-hidden", "true");
      });
      const characters = originalParts.map((part) => Array.from(part));

      if (!(await pause(START_DELAY_MS))) return;

      while (!signal.aborted) {
        for (let partIndex = 0; partIndex < characters.length; partIndex += 1) {
          for (const character of characters[partIndex]) {
            if (signal.aborted) return;
            parts[partIndex].textContent += character;
            if (!(await pause(TYPE_SPEED_MS))) return;
          }
        }

        if (!(await pause(HOLD_AFTER_TYPE_MS))) return;

        for (
          let partIndex = characters.length - 1;
          partIndex >= 0;
          partIndex -= 1
        ) {
          while (parts[partIndex].textContent.length > 0) {
            if (signal.aborted) return;
            parts[partIndex].textContent = Array.from(
              parts[partIndex].textContent,
            )
              .slice(0, -1)
              .join("");
            if (!(await pause(DELETE_SPEED_MS))) return;
          }
        }

        if (!(await pause(PAUSE_AFTER_DELETE_MS))) return;
      }
    };

    void run();
  }

  function initApplicationForm(signal) {
    const form = $("form#applicationForm");
    if (!form) return;
    form.noValidate = true;
    const steps = $$('[class*="ai-form__step_"]', form).filter((el) =>
      el.className.includes("SendApplicationSection_ai-form__step___"),
    );
    if (steps.length < 2) return;
    const activeClass = "SendApplicationSection_active__5RPzX";
    const next = $(".ai-form__step__btn-next", form);
    const back = steps[1].querySelector('button[type="button"]');

    const showStep = (index) => {
      steps.forEach((step, i) =>
        step.classList.toggle(activeClass, i === index),
      );
      steps[index]?.scrollIntoView({ behavior: "smooth", block: "center" });
    };

    next?.addEventListener(
      "click",
      () => {
        const selected = $('input[name="applicationType"]:checked', form);
        if (!selected) {
          announce(form, "Davam etmək üçün müraciət məqsədini seçin.", "error");
          return;
        }
        announce(form, "", "success");
        showStep(1);
      },
      { signal },
    );
    back?.addEventListener("click", () => showStep(0), { signal });

    const file = $('input[name="cv"]', form);
    const fileLabel = file
      ? $(`label[for="${file.id}"] [class*="label__text"]`, form)
      : null;
    const fileLabelDefault = fileLabel?.textContent || "CV əlavə et";
    file?.addEventListener(
      "change",
      () => {
        if (file.files?.[0] && fileLabel)
          fileLabel.textContent = file.files[0].name;
      },
      { signal },
    );

    form.addEventListener(
      "submit",
      async (event) => {
        event.preventDefault();
        clearInvalid(form);
        const type = $('input[name="applicationType"]:checked', form);
        const fullname = $('input[name="fullname"]', form);
        const email = $('input[name="email"]', form);
        const phone = $('input[name="phone"]', form);
        const letter = $('[name="letter"]', form);
        const cv = $('input[name="cv"]', form);
        const invalid = [];
        if (!type) invalid.push(...$$('input[name="applicationType"]', form));
        if (!fullname?.value.trim()) invalid.push(fullname);
        if (!email?.value || !validEmail(email.value)) invalid.push(email);
        if (
          !phone?.value.trim() ||
          !/^[+\d\s()-]{7,30}$/.test(phone.value.trim())
        )
          invalid.push(phone);
        if (
          !letter?.value.trim() ||
          letter.value.trim().length < 50 ||
          letter.value.length > 2000
        )
          invalid.push(letter);
        const selectedFile = cv?.files?.[0];
        if (
          !selectedFile ||
          selectedFile.size > 10 * 1024 * 1024 ||
          !/\.(pdf|doc|docx)$/i.test(selectedFile.name)
        )
          invalid.push(cv);
        invalid.filter(Boolean).forEach(markInvalid);
        if (invalid.length) {
          announce(
            form,
            "Məlumatları yoxlayın: bütün xanalar, etibarlı e-poçt, ən azı 50 simvolluq motivasiya məktubu və 10 MB-dan kiçik PDF/Word CV tələb olunur.",
            "error",
          );
          return;
        }
        setFormBusy(form, true);
        try {
          const data = {
            applicationType: Number(type.value),
            fullname: fullname.value.trim(),
            email: email.value.trim(),
            phone: phone.value.trim(),
            letter: letter.value.trim(),
          };
          const body = new FormData();
          body.append(
            "data",
            new Blob([JSON.stringify(data)], { type: "application/json" }),
          );
          body.append("cv", selectedFile, selectedFile.name);
          await apiFetch("/api/v1/applications", {
            method: "POST",
            signal,
            body,
          });
          form.reset();
          if (fileLabel) fileLabel.textContent = fileLabelDefault;
          showStep(0);
          announce(form, "Müraciətiniz uğurla göndərildi.", "success");
        } catch (error) {
          if (error?.name !== "AbortError") showLegacyFormError(form, error);
        } finally {
          setFormBusy(form, false);
        }
      },
      { signal },
    );
  }

  function initSimpleForms(signal) {
    $$("form[data-form-kind]").forEach((form) => {
      form.noValidate = true;
      form.addEventListener(
        "submit",
        async (event) => {
          event.preventDefault();
          clearInvalid(form);
          const kind = form.dataset.formKind;
          const invalid = [];
          if (kind === "subscribe") {
            const email = $('input[name="email"]', form);
            if (!email?.value || !validEmail(email.value)) invalid.push(email);
          } else {
            $$("[required]", form).forEach((field) => {
              if (!field.value?.trim()) invalid.push(field);
            });
            const email = $('input[name="email"]', form);
            if (email && !validEmail(email.value)) invalid.push(email);
            const phone = $('input[name="phone"]', form);
            if (phone && !validPhone(phone.value, true)) invalid.push(phone);
            const letter = $('[name="letter"]', form);
            if (letter && letter.value.trim().length < 10) invalid.push(letter);
          }
          invalid.filter(Boolean).forEach(markInvalid);
          if (invalid.length) {
            announce(
              form,
              kind === "subscribe"
                ? "Etibarlı e-poçt ünvanı daxil edin."
                : "Bütün xanaları düzgün doldurun.",
              "error",
            );
            return;
          }
          if (kind === "subscribe") {
            const submit = $('button[type="submit"]', form);
            submit?.setAttribute("disabled", "disabled");
            try {
              saveOffline(kind, form);
              announce(form, "E-poçt bu cihazda saxlanıldı.", "success");
              form.reset();
            } catch (_) {
              announce(
                form,
                "Məlumatı brauzer yaddaşında saxlamaq mümkün olmadı.",
                "error",
              );
            } finally {
              submit?.removeAttribute("disabled");
            }
            return;
          }
          if (kind === "contact") {
            const submit = $('button[type="submit"]', form);
            const fullName = form.elements.full_name.value.trim();
            const email = form.elements.email.value.trim();
            const phone = form.elements.phone.value.trim();
            const note = form.elements.letter.value.trim();
            submit?.setAttribute("disabled", "disabled");
            form.setAttribute("aria-busy", "true");
            try {
              const response = await chatbotApiFetch("/api/lead", {
                method: "POST",
                signal,
                body: JSON.stringify({
                  name: fullName,
                  phone,
                  email,
                  interest: "academy-contact",
                  note,
                  source: "website-contact",
                }),
              });
              if (response?.success !== true)
                throw new ApiError(0, "Müraciət qeydə alınmadı.");
              form.reset();
              announce(form, "Müraciətiniz uğurla göndərildi.", "success");
            } catch (error) {
              if (error?.name !== "AbortError")
                showLegacyFormError(form, error);
            } finally {
              form.removeAttribute("aria-busy");
              submit?.removeAttribute("disabled");
            }
          }
        },
        { signal },
      );
    });
  }

  function initVacancies(signal) {
    const input = $("#searchVacancies");
    if (!input) return;
    const tabs = $$(".Vacancies_ai-tabs__item__l5MN4");
    const activeClass = "Vacancies_ai-tabs__item--active__dqm_Y";
    const cards = $$(".Vacancies_ai-vacancies__item__MWNY2");
    const locale = "az";
    const filter = () => {
      const query = input.value.trim().toLocaleLowerCase(locale);
      const activeTab = tabs.find((tab) => tab.classList.contains(activeClass));
      const availableOnly = /mövcud|available/i.test(
        activeTab?.textContent || "",
      );
      cards.forEach((card) => {
        const matchesQuery = card.textContent
          .toLocaleLowerCase(locale)
          .includes(query);
        const matchesAvailability =
          !availableOnly || card.dataset.scholarshipAvailable !== "false";
        card.hidden = !matchesQuery || !matchesAvailability;
      });
    };
    input.addEventListener("input", filter, { signal });
    tabs.forEach((tab) =>
      tab.addEventListener(
        "click",
        () => {
          tabs.forEach((x) => x.classList.remove(activeClass));
          tab.classList.add(activeClass);
          filter();
        },
        { signal },
      ),
    );
    filter();
  }

  function setupCoverflow(
    {
      containerSelector,
      prevSelector,
      nextSelector,
      depth = 220,
      centerFromLayout = false,
      autoplayMs = 0,
    },
    signal,
  ) {
    const container = $(containerSelector);
    if (!container) return;
    const wrapper = $(".swiper-wrapper", container);
    const slides = $$(".swiper-slide", container);
    if (!wrapper || !slides.length) return;
    let active = Math.max(
      0,
      slides.findIndex((slide) =>
        slide.classList.contains("swiper-slide-active"),
      ),
    );
    let currentOffset = 0;
    const render = (animate = true) => {
      if (centerFromLayout) {
        slides.forEach((slide, index) => {
          const distance = index - active;
          slide.classList.toggle("swiper-slide-active", distance === 0);
          slide.classList.toggle("swiper-slide-prev", distance === -1);
          slide.classList.toggle("swiper-slide-next", distance === 1);
          slide.classList.toggle(
            "swiper-slide-visible",
            Math.abs(distance) <= 2,
          );
        });
      }
      const containerWidth = container.clientWidth || window.innerWidth;
      const activeSlide = slides[active];
      const slideWidth = activeSlide?.getBoundingClientRect().width || 315;
      const margin = parseFloat(getComputedStyle(activeSlide).marginRight) || 0;
      const offset = centerFromLayout
        ? containerWidth / 2 -
          ((activeSlide?.offsetLeft || 0) +
            (activeSlide?.offsetWidth || slideWidth) / 2)
        : containerWidth / 2 - slideWidth / 2 - active * (slideWidth + margin);
      currentOffset = offset;
      wrapper.style.transition = animate ? "transform 480ms ease" : "none";
      wrapper.style.transform = `translate3d(${offset}px, 0, 0)`;
      slides.forEach((slide, index) => {
        const distance = index - active;
        if (!centerFromLayout) {
          slide.classList.toggle("swiper-slide-active", distance === 0);
          slide.classList.toggle("swiper-slide-prev", distance === -1);
          slide.classList.toggle("swiper-slide-next", distance === 1);
          slide.classList.toggle(
            "swiper-slide-visible",
            Math.abs(distance) <= 2,
          );
        }
        slide.style.transition = animate
          ? "transform 480ms ease, opacity 480ms ease"
          : "none";
        slide.style.transform = `translate3d(${distance * -10}px, 0, ${-Math.abs(distance) * depth}px) scale(1)`;
        slide.style.zIndex = String(slides.length - Math.abs(distance));
        slide.style.opacity =
          Math.abs(distance) > 3 ? "0.25" : distance === 0 ? "1" : "0.55";
      });
    };
    const move = (delta) => {
      active = (active + delta + slides.length) % slides.length;
      render(true);
    };
    let autoplayTimer = null;
    let dragState = null;
    const stopAutoplay = () => {
      if (autoplayTimer === null) return;
      window.clearInterval(autoplayTimer);
      autoplayTimer = null;
    };
    const restartAutoplay = () => {
      stopAutoplay();
      autoplayTimer =
        autoplayMs > 0 && slides.length > 1
          ? window.setInterval(() => move(1), autoplayMs)
          : null;
    };
    const manualMove = (delta) => {
      dragState = null;
      move(delta);
      restartAutoplay();
    };
    $(prevSelector)?.addEventListener("click", () => manualMove(-1), {
      signal,
    });
    $(nextSelector)?.addEventListener("click", () => manualMove(1), {
      signal,
    });

    const getRenderedOffset = () => {
      const transform = window.getComputedStyle(wrapper).transform;
      if (!transform || transform === "none") return currentOffset;
      const matrix3d = transform.match(/^matrix3d\((.+)\)$/);
      if (matrix3d) {
        const values = matrix3d[1].split(",").map(Number);
        return Number.isFinite(values[12]) ? values[12] : currentOffset;
      }
      const matrix = transform.match(/^matrix\((.+)\)$/);
      if (matrix) {
        const values = matrix[1].split(",").map(Number);
        return Number.isFinite(values[4]) ? values[4] : currentOffset;
      }
      return currentOffset;
    };
    const getDragThreshold = () => {
      const activeSlide = slides[active];
      const slideWidth = activeSlide?.getBoundingClientRect().width || 315;
      return Math.min(80, Math.max(45, slideWidth * 0.12));
    };
    const startDrag = (clientX, clientY, inputType) => {
      stopAutoplay();
      const baseOffset = getRenderedOffset();
      dragState = {
        inputType,
        startX: clientX,
        startY: clientY,
        lastX: clientX,
        axis: inputType === "mouse" ? "horizontal" : null,
        baseOffset,
      };
      wrapper.style.transition = "none";
      wrapper.style.transform = `translate3d(${baseOffset}px, 0, 0)`;
    };
    const updateDrag = (clientX, clientY, event) => {
      if (!dragState) return;
      const deltaX = clientX - dragState.startX;
      const deltaY = clientY - dragState.startY;
      if (dragState.axis === null && Math.hypot(deltaX, deltaY) >= 6) {
        dragState.axis =
          Math.abs(deltaX) >= Math.abs(deltaY) ? "horizontal" : "vertical";
      }
      dragState.lastX = clientX;
      if (dragState.axis !== "horizontal") return;
      event.preventDefault();
      wrapper.style.transform = `translate3d(${dragState.baseOffset + deltaX}px, 0, 0)`;
    };
    const finishDrag = (clientX, allowSnap = true) => {
      if (!dragState) return;
      const completedDrag = dragState;
      const endX = Number.isFinite(clientX) ? clientX : completedDrag.lastX;
      const deltaX = endX - completedDrag.startX;
      dragState = null;
      if (
        allowSnap &&
        completedDrag.axis === "horizontal" &&
        Math.abs(deltaX) >= getDragThreshold()
      ) {
        move(deltaX > 0 ? -1 : 1);
      } else {
        render(true);
      }
      restartAutoplay();
    };

    container.addEventListener(
      "mousedown",
      (event) => {
        if (event.button !== 0) return;
        event.preventDefault();
        startDrag(event.clientX, event.clientY, "mouse");
      },
      { signal },
    );
    window.addEventListener(
      "mousemove",
      (event) => {
        if (dragState?.inputType !== "mouse") return;
        updateDrag(event.clientX, event.clientY, event);
      },
      { signal },
    );
    window.addEventListener(
      "mouseup",
      (event) => {
        if (dragState?.inputType !== "mouse") return;
        finishDrag(event.clientX);
      },
      { signal },
    );
    container.addEventListener("dragstart", (event) => event.preventDefault(), {
      signal,
    });
    container.addEventListener(
      "touchstart",
      (event) => {
        if (event.touches.length !== 1) return;
        const touch = event.touches[0];
        startDrag(touch.clientX, touch.clientY, "touch");
      },
      { signal, passive: true },
    );
    container.addEventListener(
      "touchmove",
      (event) => {
        if (dragState?.inputType !== "touch" || event.touches.length !== 1)
          return;
        const touch = event.touches[0];
        updateDrag(touch.clientX, touch.clientY, event);
      },
      { signal, passive: false },
    );
    container.addEventListener(
      "touchend",
      (event) => {
        if (dragState?.inputType !== "touch") return;
        finishDrag(event.changedTouches[0]?.clientX);
      },
      { signal, passive: true },
    );
    container.addEventListener(
      "touchcancel",
      () => {
        if (dragState?.inputType !== "touch") return;
        finishDrag(dragState.lastX, false);
      },
      { signal, passive: true },
    );
    window.addEventListener(
      "blur",
      () => {
        if (dragState?.inputType !== "mouse") return;
        finishDrag(dragState.lastX, false);
      },
      { signal },
    );
    window.addEventListener(
      "resize",
      () => {
        dragState = null;
        render(false);
        restartAutoplay();
      },
      { signal },
    );
    signal?.addEventListener(
      "abort",
      () => {
        stopAutoplay();
      },
      { once: true },
    );
    requestAnimationFrame(() => render(false));
    restartAutoplay();
  }

  function initSliders(signal) {
    const SCHOLARSHIPS_SLIDER_AUTOPLAY_MS = 4500;
    setupCoverflow(
      {
        containerSelector: ".SuccessStories_ai-success--stories__vv5bs .swiper",
        prevSelector:
          ".SuccessStories_section__header__controller__prev__aQ8hL",
        nextSelector:
          ".SuccessStories_section__header__controller__next__A3AXv",
        depth: 290,
        centerFromLayout: true,
        autoplayMs: SCHOLARSHIPS_SLIDER_AUTOPLAY_MS,
      },
      signal,
    );
    setupCoverflow(
      {
        containerSelector: ".ViewsFromNaic_ai-views--from--naic__Zd_6I .swiper",
        prevSelector: ".ViewsFromNaic_section__header__controller__prev__cyPxK",
        nextSelector: ".ViewsFromNaic_section__header__controller__next__wERDV",
        depth: 125,
        centerFromLayout: true,
        autoplayMs: SCHOLARSHIPS_SLIDER_AUTOPLAY_MS,
      },
      signal,
    );
  }

  function initPagination(signal) {
    const FAQ_PAGE_SIZE = 6;
    const activeClass = "Pagination_active__qQWfE";
    $$(".Pagination_ai-pagination__mtI7X").forEach((pagination) => {
      const buttons = $$(".Pagination_ai-pagination__item___y0si", pagination);
      const pageButtons = buttons.filter((button) =>
        /^\d+$/.test(button.textContent.trim()),
      );
      const section = pagination.closest(".section");
      const cards = section
        ? $$(".BlogCard_ai-blogs__item__4ILGi", section)
        : [];
      if (!pageButtons.length || !cards.length) return;
      const totalPages = Math.min(
        pageButtons.length,
        Math.ceil(cards.length / FAQ_PAGE_SIZE),
      );
      const prev = buttons.find((button) => {
        const label = button.getAttribute("aria-label") || "";
        return label.includes("Əvvəlki") || label.includes("Previous");
      });
      const next = buttons.find((button) => {
        const label = button.getAttribute("aria-label") || "";
        return label.includes("Növbəti") || label.includes("Next");
      });
      let active = clamp(
        pageButtons.findIndex((button) =>
          button.classList.contains(activeClass),
        ),
        0,
        totalPages - 1,
      );
      const setPage = (index, shouldScroll = true) => {
        active = clamp(index, 0, totalPages - 1);
        pageButtons.forEach((button, i) => {
          button.classList.toggle(activeClass, i === active);
          if (i === active) button.setAttribute("aria-current", "page");
          else button.removeAttribute("aria-current");
        });
        cards.forEach((card, i) => {
          card.hidden =
            i < active * FAQ_PAGE_SIZE || i >= (active + 1) * FAQ_PAGE_SIZE;
        });
        if (prev) prev.disabled = active === 0;
        if (next) next.disabled = active === totalPages - 1;
        if (shouldScroll) {
          section.scrollIntoView({ behavior: "smooth", block: "start" });
        }
      };
      pageButtons.forEach((button, i) =>
        button.addEventListener("click", () => setPage(i), { signal }),
      );
      prev?.addEventListener("click", () => setPage(active - 1), { signal });
      next?.addEventListener("click", () => setPage(active + 1), { signal });
      setPage(active, false);
    });
  }

  function faqSortOrder(item) {
    const direct = Number(item?.sortOrder);
    if (item?.sortOrder != null && Number.isFinite(direct)) return direct;
    const nested = Number(item?.data?.sort_order);
    return item?.data?.sort_order != null && Number.isFinite(nested)
      ? nested
      : Number.MAX_SAFE_INTEGER;
  }

  function faqCard(template, item, fallback, index) {
    const card = template.cloneNode(true);
    const key = String(item?.key || item?.id || `faq-${index + 1}`);
    const title = $(".BlogCard_ai-blogs__item__title__HICp5", card);
    const description = $(".BlogCard_ai-blogs__item__desc__OS_Ov", card);
    const fallbackTitle = $(
      ".BlogCard_ai-blogs__item__title__HICp5",
      fallback,
    )?.textContent.trim();
    const fallbackDescription = $(
      ".BlogCard_ai-blogs__item__desc__OS_Ov",
      fallback,
    )?.textContent.trim();
    const itemTitle = String(item?.title || "").trim();
    const itemBody = String(item?.body || "").trim();
    card.hidden = false;
    card.id = key;
    card.setAttribute("data-summary-only", "true");
    card.setAttribute("data-target-fragment", key);
    card.href = IS_LEGACY_ROUTER
      ? `#/nav/faq?target=${encodeURIComponent(key)}`
      : `faq.html?target=${encodeURIComponent(key)}`;
    if (title) title.textContent = itemTitle || fallbackTitle || "FAQ";
    if (description) {
      description.textContent =
        itemBody ||
        fallbackDescription ||
        "Bu sualın cavabı daha sonra əlavə ediləcək.";
    }
    return card;
  }

  function faqStateCard(template, titleText, descriptionText) {
    const card = template.cloneNode(true);
    const title = $(".BlogCard_ai-blogs__item__title__HICp5", card);
    const description = $(".BlogCard_ai-blogs__item__desc__OS_Ov", card);
    const button = $("button", card);
    card.hidden = false;
    card.removeAttribute("id");
    card.removeAttribute("href");
    card.removeAttribute("data-summary-only");
    card.removeAttribute("data-target-fragment");
    card.setAttribute("aria-disabled", "true");
    card.setAttribute("tabindex", "-1");
    if (title) title.textContent = titleText;
    if (description) description.textContent = descriptionText;
    if (button) button.disabled = true;
    return card;
  }

  function rebuildFaqPagination(signal, cardCount) {
    const current = $(".Pagination_ai-pagination__mtI7X");
    if (!current) return;
    const pagination = current.cloneNode(true);
    const buttons = $$(".Pagination_ai-pagination__item___y0si", pagination);
    const pageButtons = buttons.filter((button) =>
      /^\d+$/.test(button.textContent.trim()),
    );
    const next = buttons.find((button) => {
      const label = button.getAttribute("aria-label") || "";
      return label.includes("Növbəti") || label.includes("Next");
    });
    const template = pageButtons[0];
    if (!template || !next) return;
    pageButtons.forEach((button) => button.remove());
    const totalPages = Math.max(1, Math.ceil(cardCount / 6));
    Array.from({ length: totalPages }, (_, index) => {
      const button = template.cloneNode(true);
      button.textContent = String(index + 1);
      button.classList.toggle("Pagination_active__qQWfE", index === 0);
      if (index === 0) button.setAttribute("aria-current", "page");
      else button.removeAttribute("aria-current");
      next.before(button);
    });
    current.replaceWith(pagination);
    initPagination(signal);
  }

  async function initFaqPage(signal) {
    const grid = $(".Blogs_ai-blogs__RyLaX");
    if (!grid) return;
    const currentCards = $$(".BlogCard_ai-blogs__item__4ILGi", grid);
    const template = currentCards[0];
    if (!template) return;
    const fallbackCards = currentCards.map((card) => card.cloneNode(true));
    grid.setAttribute("aria-busy", "true");
    grid.dataset.faqSource = "loading";

    try {
      const content = await apiFetch("/api/v1/content/cms-content", {
        signal,
      });
      if (!Array.isArray(content))
        throw new ApiError(0, "FAQ məlumatının formatı düzgün deyil.");
      if (signal.aborted) return;
      const items = content
        .filter(
          (item) =>
            String(item?.type || "").toUpperCase() === "FAQ" &&
            item?.published === true,
        )
        .sort(
          (left, right) =>
            faqSortOrder(left) - faqSortOrder(right) ||
            String(left?.key || "").localeCompare(String(right?.key || "")),
        );
      if (!items.length) {
        grid.replaceChildren(
          faqStateCard(
            template,
            "FAQ tapılmadı",
            "Hazırda dərc olunmuş sual-cavab mövcud deyil.",
          ),
        );
        grid.dataset.faqSource = "api-empty";
        rebuildFaqPagination(signal, 1);
        return;
      }
      grid.replaceChildren(
        ...items.map((item, index) =>
          faqCard(
            template,
            item,
            fallbackCards[index % fallbackCards.length],
            index,
          ),
        ),
      );
      grid.dataset.faqSource = "api";
      applyDataImageFallbacks(grid);
      rebuildFaqPagination(signal, items.length);
    } catch (error) {
      if (error?.name === "AbortError" || signal.aborted) return;
      grid.replaceChildren(...fallbackCards);
      grid.dataset.faqSource = "fallback";
      rebuildFaqPagination(signal, fallbackCards.length);
    } finally {
      if (!signal.aborted) grid.removeAttribute("aria-busy");
    }
  }

  function academyMetric(value, suffix = "") {
    const text = String(value ?? "").trim();
    if (!text) return "";
    return suffix && !text.endsWith(suffix) ? `${text}${suffix}` : text;
  }

  function setAcademyInstructorCount(root, value) {
    const heading = $(".section--work .section__header__title", root);
    const count = academyMetric(value, "+");
    if (!heading || !count) return;
    const leadingText = Array.from(heading.childNodes).find(
      (node) => node.nodeType === Node.TEXT_NODE && node.textContent.trim(),
    );
    if (leadingText) leadingText.textContent = `\n         ${count}\n         `;
  }

  function setAcademyHeroImage(root, value) {
    const image = $(
      ".WhoWeAreSection_ai-who-we-are__media__image__cNM9o img",
      root,
    );
    const source = safeCourseDetailUrl(value);
    if (!image || !source) return;
    const fallback = image.currentSrc || image.getAttribute("src") || "";
    image.addEventListener(
      "error",
      () => {
        if (fallback) image.src = fallback;
      },
      { once: true },
    );
    image.src = source;
  }

  function applyAcademyContent(root, page) {
    const title = String(page?.title || "").trim();
    const body = String(page?.body || "").trim();
    const data = page?.data && typeof page.data === "object" ? page.data : {};
    const stats =
      data.stats && typeof data.stats === "object" ? data.stats : {};
    const breadcrumb = $(".NavigateSection_current__x72AF", root);
    const info = $(".WhoWeAreSection_ai-who-we-are__info__XGRRl p", root);
    const infoTitle = info ? $("strong", info) : null;
    const statItems = $$(
      ".WhoWeAreSection_ai-who-we-are__stats__item__W5tzn",
      root,
    );
    const statValues = [
      {
        count: academyMetric(stats.graduates, "+"),
        label: "Məzunlar",
      },
      {
        count: academyMetric(stats.employmentRate, "%"),
        label: "İlk 6 ayda işlə təminat",
      },
    ];

    if (breadcrumb && title) breadcrumb.textContent = title;
    if (info && body) {
      if (infoTitle && title) infoTitle.textContent = title;
      Array.from(info.childNodes)
        .filter((node) => node !== infoTitle)
        .forEach((node) => node.remove());
      info.append(document.createTextNode(` ${body}`));
    }
    statItems.forEach((item, index) => {
      const metric = statValues[index];
      if (!metric?.count) return;
      const count = $(
        ".WhoWeAreSection_ai-who-we-are__stats__item__count__j8gSP",
        item,
      );
      const label = $(
        ".WhoWeAreSection_ai-who-we-are__stats__item__title__3BeOo",
        item,
      );
      if (count) count.textContent = metric.count;
      if (label) label.textContent = metric.label;
    });
    setAcademyInstructorCount(root, stats.instructors);
    setAcademyHeroImage(root, data.heroImage);
    if (title) document.title = `${title} | Nexora Academy`;
  }

  async function initAcademyPage(signal) {
    const root = $(".main--about");
    if (!root) return;
    root.setAttribute("aria-busy", "true");
    root.dataset.academySource = "loading";
    try {
      const content = await apiFetch("/api/v1/content/cms-content", {
        signal,
      });
      if (!Array.isArray(content))
        throw new ApiError(0, "Academy məlumatının formatı düzgün deyil.");
      if (signal.aborted) return;
      const page = content.find(
        (item) =>
          String(item?.type || "").toUpperCase() === "PAGE" &&
          item?.key === "page.about" &&
          item?.published === true,
      );
      if (!page) {
        root.dataset.academySource = "fallback";
        return;
      }
      applyAcademyContent(root, page);
      root.dataset.academySource = "api";
    } catch (error) {
      if (error?.name === "AbortError" || signal.aborted) return;
      root.dataset.academySource = "fallback";
    } finally {
      if (!signal.aborted) root.removeAttribute("aria-busy");
    }
  }

  function scholarshipAvailable(item, now = Date.now()) {
    return Boolean(
      item?.active === true &&
      publicDateAllows(item.validFrom, "from", now) &&
      publicDateAllows(item.validUntil, "until", now),
    );
  }

  function scholarshipNumber(value, suffix = "") {
    if (value == null || value === "") return "";
    const number = Number(value);
    if (!Number.isFinite(number)) return "";
    const formatted = new Intl.NumberFormat("az-AZ", {
      maximumFractionDigits: 2,
    }).format(number);
    return `${formatted}${suffix}`;
  }

  function scholarshipCard(template, item, fallback, index) {
    const card = template.cloneNode(true);
    const title = $(".Vacancies_ai-vacancies__item__title__n3bnL", card);
    const description = $(".Vacancies_ai-vacancies__item__desc__A0Adm", card);
    const fallbackTitle = $(
      ".Vacancies_ai-vacancies__item__title__n3bnL",
      fallback,
    )?.textContent.trim();
    const fallbackDescription = $(
      ".Vacancies_ai-vacancies__item__desc__A0Adm",
      fallback,
    )?.textContent.trim();
    const name = String(item?.name || "").trim();
    const body = String(item?.description || "").trim();
    const details = [];
    const discount = scholarshipNumber(item?.discountPct, "%");
    const recipients = scholarshipNumber(item?.maxRecipients);
    const validFrom = item?.validFrom ? formatDate(item.validFrom) : "";
    const validUntil = item?.validUntil ? formatDate(item.validUntil) : "";
    if (discount) details.push(`${discount} dəstək`);
    if (recipients) details.push(`${recipients} yer`);
    if (validFrom && validUntil) details.push(`${validFrom} – ${validUntil}`);
    else if (validUntil) details.push(`${validUntil} tarixinədək`);
    else if (validFrom) details.push(`${validFrom} tarixindən`);
    const key = `scholarship-${String(item?.id ?? index + 1)}`;

    card.hidden = false;
    card.id = key;
    card.dataset.scholarshipAvailable = String(scholarshipAvailable(item));
    card.setAttribute("data-summary-only", "true");
    card.setAttribute("data-target-fragment", key);
    card.href = `scholarships.html?target=${encodeURIComponent(key)}`;
    if (title) title.textContent = name || fallbackTitle || "Təqaüd";
    if (description) {
      const base = body || fallbackDescription || "Təqaüd məlumatı.";
      description.textContent = details.length
        ? `${base} ${details.join(" · ")}`
        : base;
    }
    return card;
  }

  function scholarshipStateCard(template) {
    const card = template.cloneNode(true);
    const title = $(".Vacancies_ai-vacancies__item__title__n3bnL", card);
    const description = $(".Vacancies_ai-vacancies__item__desc__A0Adm", card);
    const button = $("button", card);
    card.hidden = false;
    card.removeAttribute("id");
    card.removeAttribute("href");
    card.removeAttribute("data-summary-only");
    card.removeAttribute("data-target-fragment");
    card.dataset.scholarshipAvailable = "true";
    card.setAttribute("aria-disabled", "true");
    card.setAttribute("tabindex", "-1");
    if (title) title.textContent = "Təqaüd tapılmadı";
    if (description) {
      description.textContent =
        "Hazırda göstərilə bilən təqaüd proqramı mövcud deyil.";
    }
    if (button) button.disabled = true;
    return card;
  }

  function refreshScholarshipFilters(signal) {
    const current = $(".Vacancies_section__search__FSfJg");
    if (!current) return;
    const replacement = current.cloneNode(true);
    current.replaceWith(replacement);
    initVacancies(signal);
  }

  async function initScholarshipsPage(signal) {
    const grid = $(".Vacancies_ai-vacancies__qEBqV");
    if (!grid) return;
    const currentCards = $$(".Vacancies_ai-vacancies__item__MWNY2", grid);
    const template = currentCards[0];
    if (!template) return;
    const fallbackCards = currentCards.map((card) => card.cloneNode(true));
    grid.setAttribute("aria-busy", "true");
    grid.dataset.scholarshipsSource = "loading";
    try {
      const scholarships = await apiFetch("/api/v1/scholarships", { signal });
      if (!Array.isArray(scholarships))
        throw new ApiError(0, "Təqaüd məlumatının formatı düzgün deyil.");
      if (signal.aborted) return;
      const items = [...scholarships].sort(
        (left, right) =>
          Number(left?.id ?? Number.MAX_SAFE_INTEGER) -
            Number(right?.id ?? Number.MAX_SAFE_INTEGER) ||
          String(left?.name || "").localeCompare(
            String(right?.name || ""),
            "az",
          ),
      );
      if (!items.length) {
        grid.replaceChildren(scholarshipStateCard(template));
        grid.dataset.scholarshipsSource = "api-empty";
        refreshScholarshipFilters(signal);
        return;
      }
      grid.replaceChildren(
        ...items.map((item, index) =>
          scholarshipCard(
            template,
            item,
            fallbackCards[index % fallbackCards.length],
            index,
          ),
        ),
      );
      grid.dataset.scholarshipsSource = "api";
      refreshScholarshipFilters(signal);
    } catch (error) {
      if (error?.name === "AbortError" || signal.aborted) return;
      grid.replaceChildren(...fallbackCards);
      grid.dataset.scholarshipsSource = "fallback";
      refreshScholarshipFilters(signal);
    } finally {
      if (!signal.aborted) grid.removeAttribute("aria-busy");
    }
  }

  function applyHomeBanner(root, banner) {
    const video = $(".HeroSection_video__GVdk5", root);
    const data =
      banner?.data &&
      typeof banner.data === "object" &&
      !Array.isArray(banner.data)
        ? banner.data
        : {};
    const image = safeCourseDetailUrl(data.image, "");

    if (image && video) video.poster = image;
  }

  async function initHomeBanner(signal) {
    const root = $(".HeroSection_heroSection__FTiId");
    if (!root) return;

    root.setAttribute("aria-busy", "true");
    root.dataset.bannerSource = "loading";
    try {
      const content = await apiFetch("/api/v1/content/cms-content", { signal });
      if (!Array.isArray(content))
        throw new ApiError(0, "Banner məlumatının formatı düzgün deyil.");
      if (signal.aborted) return;

      const banner = content.find(
        (item) =>
          String(item?.type || "").toUpperCase() === "BANNER" &&
          item?.key === "banner.home-hero" &&
          item?.published === true,
      );
      if (!banner) {
        root.dataset.bannerSource = "fallback";
        return;
      }

      applyHomeBanner(root, banner);
      root.dataset.bannerSource = "api";
    } catch (error) {
      if (error?.name === "AbortError" || signal.aborted) return;
      root.dataset.bannerSource = "fallback";
    } finally {
      if (!signal.aborted) root.removeAttribute("aria-busy");
    }
  }

  function applyContactContent(root, page) {
    const title = $(
      ".InfoSection_ai-contact__content__info__title__zykdV",
      root,
    );
    const items = $$(
      ".InfoSection_ai-contact__content__info__contact__item__pX_uX",
      root,
    );
    const data =
      page?.data && typeof page.data === "object" && !Array.isArray(page.data)
        ? page.data
        : {};
    const values = [data.phone, data.email, data.address];

    if (title && String(page?.title || "").trim())
      title.textContent = String(page.title).trim();
    values.forEach((value, index) => {
      const text = String(value || "").trim();
      const node = items[index]
        ? $(":scope > span:last-child", items[index])
        : null;
      if (node && text) node.textContent = text;
    });
  }

  async function initContactPage(signal) {
    const root = $(".ContactContainer_ai-contact__Fur87");
    if (!root) return;
    root.setAttribute("aria-busy", "true");
    root.dataset.contactSource = "loading";
    try {
      const content = await apiFetch("/api/v1/content/cms-content", { signal });
      if (!Array.isArray(content))
        throw new ApiError(0, "Əlaqə məlumatının formatı düzgün deyil.");
      if (signal.aborted) return;
      const page = content.find(
        (item) =>
          String(item?.type || "").toUpperCase() === "PAGE" &&
          item?.key === "page.contact" &&
          item?.published === true,
      );
      if (!page) {
        root.dataset.contactSource = "fallback";
        return;
      }
      applyContactContent(root, page);
      root.dataset.contactSource = "api";
    } catch (error) {
      if (error?.name === "AbortError" || signal.aborted) return;
      root.dataset.contactSource = "fallback";
    } finally {
      if (!signal.aborted) root.removeAttribute("aria-busy");
    }
  }

  function initPhoneInputs(signal) {
    $$('input[name="phone"]').forEach((input) => {
      input.setAttribute("inputmode", "tel");
      input.setAttribute("autocomplete", "tel");
      input.addEventListener(
        "input",
        () => {
          input.value = input.value.replace(/[^0-9+()\-\s]/g, "").slice(0, 20);
        },
        { signal },
      );
    });
  }

  function publicCategoryState(categories) {
    const byId = new Map();
    (Array.isArray(categories) ? categories : []).forEach((category) => {
      if (category?.id != null) byId.set(String(category.id), category);
    });
    const memo = new Map();
    const visiting = new Set();
    const isPublic = (id) => {
      const key = String(id);
      if (memo.has(key)) return memo.get(key);
      if (visiting.has(key)) {
        memo.set(key, false);
        return false;
      }
      const category = byId.get(key);
      if (!category || category.active !== true) {
        memo.set(key, false);
        return false;
      }
      visiting.add(key);
      const parentId = category.parentId;
      const valid =
        parentId == null || parentId === ""
          ? true
          : byId.has(String(parentId)) && isPublic(parentId);
      visiting.delete(key);
      memo.set(key, valid);
      return valid;
    };
    const visible = [...byId.values()]
      .filter((category) => isPublic(category.id))
      .sort(
        (a, b) =>
          (Number(a.sortOrder) || 0) - (Number(b.sortOrder) || 0) ||
          String(a.name || a.slug || "").localeCompare(
            String(b.name || b.slug || ""),
            "az",
          ),
      );
    return {
      byId,
      visible,
      visibleIds: new Set(visible.map((category) => String(category.id))),
    };
  }

  function publicDateAllows(value, boundary, now = Date.now()) {
    if (value == null || value === "") return true;
    const raw = String(value);
    const dateOnly = /^\d{4}-\d{2}-\d{2}$/.test(raw);
    const timestamp = Date.parse(
      dateOnly
        ? `${raw}${boundary === "until" ? "T23:59:59.999" : "T00:00:00"}`
        : raw,
    );
    if (!Number.isFinite(timestamp)) return false;
    return boundary === "until" ? timestamp >= now : timestamp <= now;
  }

  function isPublicCourse(course, visibleCategoryIds, now = Date.now()) {
    return Boolean(
      course &&
      course.published === true &&
      course.active === true &&
      course.archived === false &&
      visibleCategoryIds.has(String(course.categoryId)) &&
      publicDateAllows(course.validFrom, "from", now) &&
      publicDateAllows(course.validUntil, "until", now),
    );
  }

  function courseViewModel(course) {
    const content =
      course?.content &&
      typeof course.content === "object" &&
      !Array.isArray(course.content)
        ? course.content
        : {};
    return { ...content, ...(course || {}) };
  }

  function renderCourseCard(course, categoryNames) {
    course = courseViewModel(course);
    const category = categoryNames.get(String(course.categoryId)) || "Kurs";
    const description =
      course.shortDescription ||
      course.targetAudience ||
      "Ətraflı məlumat üçün kurs səhifəsinə keçin.";
    const duration = course.durationWeeks
      ? `${escapeHtml(course.durationWeeks)} həftə`
      : "";
    return `<article class="Nexora_courseCard">
      <div class="Nexora_courseCardTop">
        <span class="Nexora_badge">${escapeHtml(category)}</span>
        <span class="Nexora_coursePrice">${escapeHtml(formatPrice(course.basePrice, course.currency))}</span>
      </div>
      <div class="Nexora_courseCardBody">
        <h3>${escapeHtml(course.title || "Adsız kurs")}</h3>
        <p>${escapeHtml(description)}</p>
      </div>
      <div class="Nexora_courseMeta">
        <span>${escapeHtml(enumLabel(course.difficulty))}</span>
        <span>${escapeHtml(enumLabel(course.deliveryFormat))}</span>
        ${duration ? `<span>${duration}</span>` : ""}
      </div>
      <a class="ai-btn ai-btn--text" href="course-details.html?id=${encodeURIComponent(course.id || "")}">Ətraflı bax</a>
    </article>`;
  }

  function renderCoursesPagination(container, current, hasNext) {
    if (current <= 0 && !hasNext) {
      container.innerHTML = "";
      return;
    }
    container.innerHTML = `
      <button type="button" data-course-page="${current - 1}" aria-label="Əvvəlki səhifə" ${current <= 0 ? "disabled" : ""}>‹</button>
      <button type="button" class="Nexora_paginationActive" aria-current="page" disabled>${current + 1}</button>
      <button type="button" data-course-page="${current + 1}" aria-label="Növbəti səhifə" ${hasNext ? "" : "disabled"}>›</button>`;
  }

  function projectCourseStateCard(template, title, description, action = null) {
    const card = template.cloneNode(true);
    card.removeAttribute("id");
    $$("[id]", card).forEach((node) => node.removeAttribute("id"));
    const titleNode = $(".ProjectCard_ai-projects__item__title__mSuta", card);
    const descriptionNode = $(
      ".ProjectCard_ai-projects__item__desc__DQd6_",
      card,
    );
    const link = $(".ProjectCard_ai-projects__item__cta__t2MnB", card);
    const image = $("img", card);
    if (titleNode) titleNode.textContent = title;
    if (descriptionNode) descriptionNode.textContent = description;
    if (link) {
      link.removeAttribute("data-summary-only");
      link.removeAttribute("data-target-fragment");
      if (action) {
        link.href = action.href;
        link.removeAttribute("aria-disabled");
        link.removeAttribute("tabindex");
        const label = $("span", link);
        if (label) label.textContent = action.label;
      } else {
        link.removeAttribute("href");
        link.setAttribute("aria-disabled", "true");
        link.setAttribute("tabindex", "-1");
      }
    }
    if (image) {
      const fallback = MOCK_IMAGE_FALLBACKS["course-networking"];
      image.removeAttribute("data-image-src");
      image.removeAttribute("data-image-fallback");
      image.src = fallback.src;
      image.alt = fallback.alt;
    }
    return card;
  }

  function projectCourseCard(template, course, categoryNames) {
    course = courseViewModel(course);
    const card = template.cloneNode(true);
    $$("[id]", card).forEach((node) => node.removeAttribute("id"));
    card.id = String(course.slug || course.id || "");
    const title = $(".ProjectCard_ai-projects__item__title__mSuta", card);
    const description = $(".ProjectCard_ai-projects__item__desc__DQd6_", card);
    const link = $(".ProjectCard_ai-projects__item__cta__t2MnB", card);
    const image = $("img", card);
    const categoryName = categoryNames.get(String(course.categoryId)) || "";
    const detailUrl = `course-details.html?id=${encodeURIComponent(course.id || "")}`;
    if (title) title.textContent = course.title || "Adsız kurs";
    if (description) {
      description.textContent =
        course.shortDescription ||
        course.targetAudience ||
        course.fullDescription ||
        "Ətraflı məlumat üçün kurs səhifəsinə keçin.";
    }
    if (link) {
      link.href = detailUrl;
      link.removeAttribute("data-summary-only");
      link.removeAttribute("data-target-fragment");
      link.removeAttribute("aria-disabled");
      link.removeAttribute("tabindex");
      const label = $("span", link);
      if (label) label.textContent = "Kursa bax";
    }
    card.setAttribute("role", "link");
    card.setAttribute("tabIndex", "0");
    card.style.cursor = "pointer";
    card.addEventListener("click", (event) => {
      if (event.defaultPrevented || event.target.closest("a")) return;
      event.preventDefault();
      if (
        event.metaKey ||
        event.ctrlKey ||
        event.shiftKey ||
        event.altKey ||
        event.button !== 0
      ) {
        window.open(detailUrl, "_blank", "noopener");
      } else {
        window.location.assign(detailUrl);
      }
    });
    card.addEventListener("keydown", (event) => {
      if (event.key !== "Enter" && event.key !== " ") return;
      event.preventDefault();
      window.location.assign(detailUrl);
    });
    if (image) {
      const fallback = courseMockFallback({ ...course, categoryName });
      image.removeAttribute("data-image-src");
      image.removeAttribute("data-image-fallback");
      image.src = fallback.src;
      image.alt = fallback.alt;
    }
    return card;
  }

  async function loadPublicCourseCatalog(signal) {
    const categories = await apiFetch("/api/v1/categories", { signal });
    if (!Array.isArray(categories))
      throw new ApiError(0, "Kateqoriya məlumatı əlçatan deyil.");
    const categoryState = publicCategoryState(categories);
    const categoryNames = new Map(
      categoryState.visible.map((category) => [
        String(category.id),
        category.name || category.slug || String(category.id),
      ]),
    );
    const baseParams = new URLSearchParams({
      size: "100",
      sort: "title,asc",
      published: "true",
      active: "true",
    });
    const firstParams = new URLSearchParams(baseParams);
    firstParams.set("page", "0");
    const firstPage = await apiFetch(`/api/v1/courses?${firstParams}`, {
      signal,
    });
    const totalPages = Math.min(
      100,
      Math.max(1, Number.parseInt(firstPage?.totalPages, 10) || 1),
    );
    const remainingPages = await Promise.all(
      Array.from({ length: totalPages - 1 }, (_, index) => {
        const params = new URLSearchParams(baseParams);
        params.set("page", String(index + 1));
        return apiFetch(`/api/v1/courses?${params}`, { signal });
      }),
    );
    const courses = [firstPage, ...remainingPages]
      .flatMap((page) => (Array.isArray(page?.content) ? page.content : []))
      .filter((course) => isPublicCourse(course, categoryState.visibleIds));
    return { courses, categoryNames };
  }

  function featuredCourseTimestamp(course) {
    const timestamp = Date.parse(course?.updatedAt || course?.createdAt || "");
    return Number.isFinite(timestamp) ? timestamp : 0;
  }

  async function initHomeFeaturedCourses(signal) {
    const container = $(".ProjectsSection_ai-projects__NQP37");
    if (!container) return;
    const template = $(".ProjectCard_ai-projects__item__oGKFx", container);
    if (!template) return;

    container.setAttribute("aria-busy", "true");
    container.replaceChildren(
      projectCourseStateCard(
        template,
        "Kurslar yüklənir…",
        "Seçilmiş kurslar serverdən alınır.",
      ),
    );

    try {
      const { courses, categoryNames } = await loadPublicCourseCatalog(signal);
      if (signal.aborted) return;
      const featuredCourses = [...courses]
        .sort(
          (left, right) =>
            featuredCourseTimestamp(right) - featuredCourseTimestamp(left) ||
            String(left?.title || "").localeCompare(
              String(right?.title || ""),
              "az",
            ),
        )
        .slice(0, 3);
      if (!featuredCourses.length) {
        container.replaceChildren(
          projectCourseStateCard(
            template,
            "Seçilmiş kurs tapılmadı",
            "Hazırda göstərilə bilən aktiv kurs yoxdur.",
            { href: "courses.html", label: "Bütün kurslara bax" },
          ),
        );
        return;
      }
      container.replaceChildren(
        ...featuredCourses.map((course) =>
          projectCourseCard(template, course, categoryNames),
        ),
      );
    } catch (error) {
      if (error?.name === "AbortError" || signal.aborted) return;
      container.replaceChildren(
        projectCourseStateCard(
          template,
          "Kurslar hazırda əlçatan deyil",
          apiErrorMessage(error),
          { href: "courses.html", label: "Bütün kurslara bax" },
        ),
      );
    } finally {
      if (!signal.aborted) container.removeAttribute("aria-busy");
    }
  }

  function initProjectCoursesPage(signal) {
    const container = $(".ProjectsSection_ai-projects__Djj2c");
    if (!container) return;
    const template = $(".ProjectCard_ai-projects__item__oGKFx", container);
    if (!template) return;
    container.setAttribute("aria-busy", "true");
    container.replaceChildren(
      projectCourseStateCard(
        template,
        "Kurslar yüklənir…",
        "Açıq kurslar serverdən alınır.",
      ),
    );
    loadPublicCourseCatalog(signal)
      .then(({ courses, categoryNames }) => {
        if (signal.aborted) return;
        if (!courses.length) {
          container.replaceChildren(
            projectCourseStateCard(
              template,
              "Açıq kurs tapılmadı",
              "Kurs kataloqunda hazırda göstərilə bilən aktiv kurs yoxdur.",
              { href: "categories.html", label: "Kateqoriyalara bax" },
            ),
          );
          return;
        }
        container.replaceChildren(
          ...courses.map((course) =>
            projectCourseCard(template, course, categoryNames),
          ),
        );
      })
      .catch((error) => {
        if (error?.name === "AbortError" || signal.aborted) return;
        container.replaceChildren(
          projectCourseStateCard(
            template,
            "Kurslar hazırda əlçatan deyil",
            apiErrorMessage(error),
            { href: "courses.html", label: "Yenidən yoxla" },
          ),
        );
      })
      .finally(() => {
        if (!signal.aborted) container.removeAttribute("aria-busy");
      });
  }

  function initCoursesPage(signal) {
    const form = $("#courseFilters");
    const grid = $("#coursesGrid");
    const status = $("#coursesStatus");
    const pagination = $("#coursesPagination");
    if (!form || !grid || !status || !pagination) {
      initProjectCoursesPage(signal);
      return;
    }

    const categorySelect = $("#courseCategory", form);
    const categoryNames = new Map();
    let visibleCategoryIds = new Set();
    let categoriesReady = false;
    const initialParams = new URLSearchParams(location.search);
    let currentPage = Math.max(
      0,
      Number.parseInt(initialParams.get("page") || "0", 10) || 0,
    );
    let searchTimer = null;

    const applyUrlState = () => {
      ["q", "categoryId", "difficulty", "deliveryFormat"].forEach((name) => {
        const field = form.elements[name];
        if (field) field.value = initialParams.get(name) || "";
      });
    };

    const syncUrlState = () => {
      const values = new FormData(form);
      const params = new URLSearchParams();
      ["q", "categoryId", "difficulty", "deliveryFormat"].forEach((name) => {
        const value = String(values.get(name) || "").trim();
        if (value) params.set(name, value);
      });
      if (currentPage > 0) params.set("page", String(currentPage));
      history.replaceState(
        null,
        "",
        `${location.pathname}${params.size ? `?${params}` : ""}`,
      );
    };

    const loadCategories = async () => {
      const categories = await apiFetch("/api/v1/categories", { signal });
      if (!Array.isArray(categories) || signal.aborted)
        throw new ApiError(0, "Kateqoriya məlumatı əlçatan deyil.");
      const publicState = publicCategoryState(categories);
      visibleCategoryIds = publicState.visibleIds;
      publicState.visible.forEach((category) => {
        categoryNames.set(
          String(category.id),
          category.name || category.slug || String(category.id),
        );
        const option = document.createElement("option");
        option.value = String(category.id);
        option.textContent =
          category.name || category.slug || String(category.id);
        categorySelect.appendChild(option);
      });
      categoriesReady = true;
      applyUrlState();
    };

    const loadCourses = async (pageNumber = 0) => {
      const requestId = ++coursesRequestId;
      currentPage = Math.max(0, pageNumber);
      syncUrlState();
      status.textContent = "Kurslar yüklənir…";
      status.dataset.state = "loading";
      grid.setAttribute("aria-busy", "true");

      const values = new FormData(form);
      const params = new URLSearchParams({
        page: String(currentPage),
        size: "9",
        sort: "title,asc",
        published: "true",
        active: "true",
      });
      ["q", "categoryId", "difficulty", "deliveryFormat"].forEach((name) => {
        const value = String(values.get(name) || "").trim();
        if (value) params.set(name, value);
      });

      try {
        if (!categoriesReady)
          throw new ApiError(
            0,
            "Kurs görünürlüyünü yoxlamaq üçün kateqoriya məlumatı əlçatan deyil.",
          );
        const page = await apiFetch(`/api/v1/courses?${params}`, { signal });
        if (signal.aborted || requestId !== coursesRequestId) return;
        const rawCourses = Array.isArray(page?.content) ? page.content : [];
        const courses = rawCourses.filter((course) =>
          isPublicCourse(course, visibleCategoryIds),
        );
        grid.innerHTML = courses
          .map((course) => renderCourseCard(course, categoryNames))
          .join("");
        if (!courses.length) {
          grid.innerHTML =
            '<div class="Nexora_emptyState"><h3>Uyğun kurs tapılmadı</h3><p>Filtrləri dəyişərək yenidən yoxlayın.</p></div>';
        }
        status.textContent = courses.length
          ? `Bu səhifədə ${courses.length} açıq kurs göstərilir`
          : "Açıq kurs tapılmadı";
        status.dataset.state = "success";
        renderCoursesPagination(
          pagination,
          currentPage,
          page?.last === false || rawCourses.length >= 9,
        );
      } catch (error) {
        if (error?.name === "AbortError" || requestId !== coursesRequestId)
          return;
        grid.innerHTML = `<div class="Nexora_emptyState"><h3>Kursları göstərmək mümkün olmadı</h3><p>${escapeHtml(apiErrorMessage(error))}</p></div>`;
        status.textContent = "Kurs kataloqu əlçatan deyil";
        status.dataset.state = "error";
        pagination.innerHTML = "";
      } finally {
        if (requestId === coursesRequestId) grid.removeAttribute("aria-busy");
      }
    };

    form.addEventListener(
      "input",
      (event) => {
        if (event.target.name !== "q") return;
        clearTimeout(searchTimer);
        searchTimer = setTimeout(() => loadCourses(0), 300);
      },
      { signal },
    );
    form.addEventListener(
      "change",
      (event) => {
        if (event.target.name === "q") return;
        loadCourses(0);
      },
      { signal },
    );
    form.addEventListener("reset", () => setTimeout(() => loadCourses(0), 0), {
      signal,
    });
    pagination.addEventListener(
      "click",
      (event) => {
        const button = event.target.closest("button[data-course-page]");
        if (!button || button.disabled) return;
        const nextPage = Number(button.dataset.coursePage);
        if (!Number.isInteger(nextPage) || nextPage < 0) return;
        loadCourses(nextPage);
        $(".Nexora_catalogSection")?.scrollIntoView({
          behavior: "smooth",
          block: "start",
        });
      },
      { signal },
    );
    signal.addEventListener("abort", () => clearTimeout(searchTimer), {
      once: true,
    });

    loadCategories()
      .then(() => loadCourses(currentPage))
      .catch((error) => {
        if (error?.name === "AbortError") return;
        grid.innerHTML = `<div class="Nexora_emptyState"><h3>Kursları göstərmək mümkün olmadı</h3><p>${escapeHtml(apiErrorMessage(error))}</p></div>`;
        status.textContent = "Kurs kataloqu əlçatan deyil";
        status.dataset.state = "error";
        pagination.innerHTML = "";
      });
  }

  function renderCategoryCard(category, categoryState) {
    const parent = categoryState.byId.get(String(category.parentId));
    const parentText = parent
      ? `Üst kateqoriya: ${parent.name || parent.slug || parent.id}`
      : "Əsas kateqoriya";
    return `<article class="Nexora_courseCard">
      <div class="Nexora_courseCardBody">
        <h3>${escapeHtml(category.name || category.slug || "Kateqoriya")}</h3>
        <p>${escapeHtml(parentText)}</p>
      </div>
      <a class="ai-btn ai-btn--text" href="category.html?id=${encodeURIComponent(category.id)}">Kateqoriyaya bax</a>
    </article>`;
  }

  async function initCategoriesPage(signal) {
    const grid = $("#categoriesGrid");
    const status = $("#categoriesStatus");
    if (!grid || !status) return;
    try {
      const categories = await apiFetch("/api/v1/categories", { signal });
      if (signal.aborted) return;
      const categoryState = publicCategoryState(categories);
      grid.innerHTML = categoryState.visible.length
        ? categoryState.visible
            .map((category) => renderCategoryCard(category, categoryState))
            .join("")
        : '<div class="Nexora_emptyState"><h3>Açıq kateqoriya yoxdur</h3><p>Kataloqu bir az sonra yenidən yoxlayın.</p></div>';
      status.textContent = `${categoryState.visible.length} açıq kateqoriya`;
      status.dataset.state = "success";
    } catch (error) {
      if (error?.name === "AbortError") return;
      grid.innerHTML =
        '<div class="Nexora_emptyState"><h3>Kateqoriyalar əlçatan deyil</h3><p>Məlumatları hazırda yükləmək mümkün olmadı.</p></div>';
      status.textContent = apiErrorMessage(error);
      status.dataset.state = "error";
    }
  }

  async function initCategoryPage(signal) {
    const details = $("#categoryDetails");
    const childrenContainer = $("#categoryChildren");
    const childrenStatus = $("#categoryChildrenStatus");
    const coursesContainer = $("#categoryCourses");
    const coursesStatus = $("#categoryCoursesStatus");
    if (
      !details ||
      !childrenContainer ||
      !childrenStatus ||
      !coursesContainer ||
      !coursesStatus
    )
      return;
    const categoryId =
      new URLSearchParams(location.search).get("id")?.trim() || "";
    if (!/^\d+$/.test(categoryId)) {
      details.innerHTML =
        '<div class="Nexora_emptyState"><h1>Kateqoriya seçilməyib</h1><p>Kateqoriya kataloqundan seçim edin.</p></div>';
      return;
    }
    try {
      const [category, categories] = await Promise.all([
        apiFetch(`/api/v1/categories/${encodeURIComponent(categoryId)}`, {
          signal,
        }),
        apiFetch("/api/v1/categories", { signal }),
      ]);
      if (signal.aborted) return;
      const categoryState = publicCategoryState(categories);
      if (
        !category ||
        String(category.id) !== categoryId ||
        !categoryState.visibleIds.has(categoryId)
      )
        throw new ApiError(404, "Kateqoriya əlçatan deyil.");
      const name = category.name || category.slug || "Kateqoriya";
      const parent = categoryState.byId.get(String(category.parentId));
      details.innerHTML = `<div class="section__header__content">
        <p class="Nexora_eyebrow">${escapeHtml(parent?.name || "Kurs kataloqu")}</p>
        <h1 class="Nexora_pageTitle">${escapeHtml(name)}</h1>
        <p class="Nexora_pageLead">Bu kateqoriyaya aid açıq kurslar və aktiv alt kateqoriyalar.</p>
      </div>`;
      document.title = `${name} | Nexora Academy`;
      const children = categoryState.visible.filter(
        (item) => String(item.parentId) === categoryId,
      );
      childrenContainer.innerHTML = children
        .map((item) => renderCategoryCard(item, categoryState))
        .join("");
      childrenStatus.textContent = children.length
        ? `${children.length} alt kateqoriya`
        : "Aktiv alt kateqoriya yoxdur";

      const params = new URLSearchParams({
        page: "0",
        size: "24",
        sort: "title,asc",
        categoryId,
        published: "true",
        active: "true",
      });
      const page = await apiFetch(`/api/v1/courses?${params}`, { signal });
      if (signal.aborted) return;
      const categoryNames = new Map(
        categoryState.visible.map((item) => [
          String(item.id),
          item.name || item.slug || String(item.id),
        ]),
      );
      const courses = (Array.isArray(page?.content) ? page.content : []).filter(
        (course) =>
          String(course.categoryId) === categoryId &&
          isPublicCourse(course, categoryState.visibleIds),
      );
      coursesContainer.innerHTML = courses.length
        ? courses
            .map((course) => renderCourseCard(course, categoryNames))
            .join("")
        : '<div class="Nexora_emptyState"><h3>Açıq kurs tapılmadı</h3><p>Bu kateqoriyada hazırda açıq kurs yoxdur.</p></div>';
      coursesStatus.textContent = `${courses.length} açıq kurs`;
    } catch (error) {
      if (error?.name === "AbortError") return;
      details.innerHTML =
        '<div class="Nexora_emptyState"><h1>Kateqoriya hazırda əlçatan deyil</h1><p>Kateqoriya kataloquna qayıdaraq yenidən seçim edin.</p></div>';
      childrenContainer.innerHTML = "";
      coursesContainer.innerHTML = "";
      childrenStatus.textContent = "";
      coursesStatus.textContent = "";
    }
  }

  function safeCourseDetailUrl(value, fallback = "") {
    const raw = String(value || "").trim();
    if (!raw) return fallback;
    if (!/^[a-z][a-z0-9+.-]*:/i.test(raw) && !raw.startsWith("//")) return raw;
    try {
      const parsed = new URL(raw, location.href);
      return ["http:", "https:"].includes(parsed.protocol) ? raw : fallback;
    } catch (_) {
      return fallback;
    }
  }

  function courseDetailTextList(value) {
    return (Array.isArray(value) ? value : [])
      .map((item) => String(item || "").trim())
      .filter(Boolean);
  }

  function renderCourseModules(modules) {
    const items = (Array.isArray(modules) ? modules : [])
      .map((module, index) => {
        const objectModule =
          module && typeof module === "object" && !Array.isArray(module)
            ? module
            : null;
        const title = String(
          objectModule ? objectModule.title || "" : module || "",
        ).trim();
        const topics = courseDetailTextList(objectModule?.topics);
        if (!title && !topics.length) return "";
        return `<article class="Nexora_courseDetailV2__module">
          <span class="Nexora_courseDetailV2__moduleIndex">${String(index + 1).padStart(2, "0")}</span>
          <div>
            ${title ? `<h3>${escapeHtml(title)}</h3>` : ""}
            ${
              topics.length
                ? `<ul>${topics.map((topic) => `<li>${escapeHtml(topic)}</li>`).join("")}</ul>`
                : ""
            }
          </div>
        </article>`;
      })
      .filter(Boolean);
    return items.length
      ? `<section class="Nexora_courseDetailV2__contentSection">
          <div class="Nexora_courseDetailV2__sectionHeading">
            <p class="Nexora_eyebrow">Tədris planı</p>
            <h2>Kurs proqramı</h2>
          </div>
          <div class="Nexora_courseDetailV2__modules">${items.join("")}</div>
        </section>`
      : "";
  }

  function renderCourseRequirements(requirements) {
    const items = courseDetailTextList(requirements);
    return items.length
      ? `<section class="Nexora_courseDetailV2__contentSection">
          <div class="Nexora_courseDetailV2__sectionHeading">
            <p class="Nexora_eyebrow">Başlamazdan əvvəl</p>
            <h2>Tələblər</h2>
          </div>
          <ul class="Nexora_courseDetailV2__checkList">
            ${items.map((item) => `<li>${escapeHtml(item)}</li>`).join("")}
          </ul>
        </section>`
      : "";
  }

  function courseMockFallback(course) {
    const context =
      `${course.title || ""} ${course.categoryName || ""}`.toLocaleLowerCase(
        "az",
      );
    if (/cloud|bulud|devops/.test(context))
      return MOCK_IMAGE_FALLBACKS["course-cloud-devops"];
    if (/cyber|kiber|security|təhlükəsizlik/.test(context))
      return MOCK_IMAGE_FALLBACKS["course-cybersecurity"];
    return MOCK_IMAGE_FALLBACKS["course-networking"];
  }

  function renderCourseInstructor(instructor) {
    if (!instructor || typeof instructor !== "object") return "";
    const name = String(instructor.name || "").trim();
    if (!name) return "";
    const title = String(instructor.title || "").trim();
    const fallbackIndex = (name.codePointAt(0) || 0) % 3;
    const fallback = MOCK_IMAGE_FALLBACKS[`instructor-${fallbackIndex + 1}`];
    const realImageUrl = safeCourseDetailUrl(instructor.imageUrl);
    const imageUrl = realImageUrl || fallback.src;
    const imageAlt = realImageUrl ? instructor.imageAlt || name : fallback.alt;
    return `<section class="Nexora_courseDetailV2__contentSection">
      <div class="Nexora_courseDetailV2__sectionHeading">
        <p class="Nexora_eyebrow">Təlimçi</p>
        <h2>Müəllim haqqında</h2>
      </div>
      <div class="Nexora_courseDetailV2__instructor">
        <img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(imageAlt)}" loading="lazy" />
        <div>
          <h3>${escapeHtml(name)}</h3>
          ${title ? `<p>${escapeHtml(title)}</p>` : ""}
        </div>
      </div>
    </section>`;
  }

  function renderCourseDetails(course, options = {}) {
    course = courseViewModel(course);
    const title = course.title || "Kurs";
    const description =
      course.fullDescription ||
      course.description ||
      course.shortDescription ||
      "Bu kurs haqqında ətraflı məlumat hazırlanır.";
    const shortDescription =
      course.shortDescription ||
      course.description ||
      course.fullDescription ||
      "";
    const deliveryFormat = enumLabel(course.deliveryFormat);
    const duration = course.durationWeeks
      ? `${course.durationWeeks} həftə`
      : "";
    const difficulty = enumLabel(course.difficulty);
    const courseFallback = courseMockFallback(course);
    const realImageUrl = safeCourseDetailUrl(course.imageUrl);
    const imageUrl = realImageUrl || courseFallback.src;
    const imageAlt = realImageUrl
      ? course.imageAlt || `${title} kursunun əsas vizualı`
      : courseFallback.alt;
    const categoryName = options.categoryName || "Nexora Academy";
    const metaItems = [difficulty, deliveryFormat, duration].filter(Boolean);
    const courseId = String(course.id || "").trim();
    const explicitRegistrationUrl = safeCourseDetailUrl(course.registrationUrl);
    const accountLink = explicitRegistrationUrl || "contact.html";
    const accountLabel = explicitRegistrationUrl
      ? "Qeydiyyatdan keç"
      : "Qeydiyyat barədə məlumat al";
    const requirements = renderCourseRequirements(course.requirements);
    const modules = renderCourseModules(course.modules);
    const instructor = renderCourseInstructor(course.instructor);
    const certificateText = String(course.certificateText || "").trim();
    const relatedIds = Array.isArray(course.relatedCourseIds)
      ? course.relatedCourseIds.filter(
          (id) => id && String(id) !== String(course.id || ""),
        )
      : [];
    const detailRows = [
      ["Tədris formatı", deliveryFormat || "Məlumat dəqiqləşdirilir"],
      ["Müddət", duration || "Məlumat dəqiqləşdirilir"],
      ...(difficulty ? [["Səviyyə", difficulty]] : []),
      ...(course.locationText ? [["Məkan", course.locationText]] : []),
      ...(course.pricePeriod ? [["Ödəniş dövrü", course.pricePeriod]] : []),
    ];

    return `<div class="Nexora_courseDetailV2">
      <section class="Nexora_courseDetailV2__hero">
        <div class="Nexora_courseDetailV2__heroCopy">
          <p class="Nexora_eyebrow">${escapeHtml(categoryName)}</p>
          ${
            metaItems.length
              ? `<div class="Nexora_courseMeta">${metaItems.map((item) => `<span>${escapeHtml(item)}</span>`).join("")}</div>`
              : ""
          }
          <h1 class="Nexora_pageTitle">${escapeHtml(title)}</h1>
          ${shortDescription ? `<p class="Nexora_pageLead">${escapeHtml(shortDescription)}</p>` : ""}
        </div>
        <figure class="Nexora_courseDetailV2__visual">
          <img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(imageAlt)}" />
        </figure>
      </section>

      <div class="Nexora_courseDetailV2__contentLayout">
        <div class="Nexora_courseDetailV2__contentMain">
          <section class="Nexora_courseDetailV2__contentSection">
            <div class="Nexora_courseDetailV2__sectionHeading">
              <p class="Nexora_eyebrow">Ətraflı məlumat</p>
              <h2>Kurs haqqında</h2>
            </div>
            <div class="Nexora_courseDetailV2__richText"><p>${escapeHtml(description)}</p></div>
          </section>

          ${
            course.targetAudience
              ? `<section class="Nexora_courseDetailV2__contentSection">
                  <div class="Nexora_courseDetailV2__sectionHeading">
                    <p class="Nexora_eyebrow">Uyğunluq</p>
                    <h2>Kimlər üçün nəzərdə tutulub?</h2>
                  </div>
                  <p class="Nexora_courseDetailV2__bodyText">${escapeHtml(course.targetAudience)}</p>
                </section>`
              : ""
          }
          ${modules}
          ${requirements}
          ${
            certificateText
              ? `<section class="Nexora_courseDetailV2__contentSection">
                  <div class="Nexora_courseDetailV2__sectionHeading">
                    <p class="Nexora_eyebrow">Nəticə</p>
                    <h2>Sertifikat</h2>
                  </div>
                  <p class="Nexora_courseDetailV2__bodyText">${escapeHtml(certificateText)}</p>
                </section>`
              : ""
          }
          ${instructor}
        </div>

        <aside class="Nexora_panel Nexora_courseAside Nexora_courseDetailV2__aside">
          <div>
            <p class="Nexora_eyebrow">Kursun qiyməti</p>
            <strong class="Nexora_detailPrice">${escapeHtml(formatPrice(course.basePrice, course.currency))}</strong>
          </div>
          <dl class="Nexora_detailList">
            ${detailRows
              .map(
                ([label, value]) =>
                  `<div><dt>${escapeHtml(label)}</dt><dd>${escapeHtml(value)}</dd></div>`,
              )
              .join("")}
          </dl>
          <a class="ai-btn ai-btn--gradient Nexora_courseDetailV2__cta" href="${escapeHtml(accountLink)}">${escapeHtml(accountLabel)}</a>
          <p class="Nexora_muted">Qrup tarixləri, mövcud yerlər və yekun qeydiyyat məlumatları təsdiqləndikdə təqdim ediləcək.</p>
        </aside>
      </div>

      ${
        relatedIds.length
          ? `<section class="Nexora_courseDetailV2__related">
              <div class="Nexora_courseDetailV2__sectionHeading">
                <p class="Nexora_eyebrow">Davam et</p>
                <h2>Əlaqəli kurslar</h2>
              </div>
              <p class="Nexora_status" id="relatedCoursesStatus">Əlaqəli kurslar yüklənir…</p>
              <div class="Nexora_courseGrid" id="relatedCourses"></div>
            </section>`
          : ""
      }
    </div>`;
  }

  function initCourseDetailsPage(signal) {
    const container = $("#courseDetails");
    if (!container) return;

    const params = new URLSearchParams(location.search);
    const courseSlug = params.get("course")?.trim() || "";

    const reviewsContainer = $("#courseReviews");
    const reviewsStatus = $("#reviewsStatus");
    const reviewForm = $("#reviewForm");
    const reviewAccess = $("#reviewAccess");
    let relatedContainer = null;
    let relatedStatus = null;

    const courseId = params.get("id")?.trim() || "";
    if (!courseId && !courseSlug) {
      container.innerHTML =
        '<div class="Nexora_emptyState"><h1>Kurs seçilməyib</h1><p>Kataloqdan kurs seçərək yenidən yoxlayın.</p></div>';
      if (reviewsStatus) reviewsStatus.textContent = "";
      return;
    }

    if (reviewForm) reviewForm.hidden = true;
    if (reviewAccess) reviewAccess.hidden = false;
    const accessMessage = reviewAccess ? $("p", reviewAccess) : null;
    if (accessMessage)
      accessMessage.textContent =
        "Dərc olunmuş rəylər və təhlükəsiz rəy uyğunluğu üçün açıq xidmət hələ mövcud deyil.";
    if (reviewsContainer) reviewsContainer.innerHTML = "";
    if (reviewsStatus) {
      reviewsStatus.textContent = "Rəy bölməsi server dəstəyi gözləyir.";
      reviewsStatus.dataset.state = "error";
    }

    const loadCourse = async () => {
      try {
        const courseRequest = courseId
          ? apiFetch(`/api/v1/courses/${encodeURIComponent(courseId)}`, {
              signal,
            })
          : apiFetch(
              `/api/v1/courses?${new URLSearchParams({
                q: courseSlug,
                page: "0",
                size: "20",
                published: "true",
                active: "true",
              })}`,
              { signal },
            ).then((page) => {
              const course = (
                Array.isArray(page?.content) ? page.content : []
              ).find((item) => String(item?.slug || "") === courseSlug);
              if (!course) throw new ApiError(404, "Kurs tapılmadı.");
              return course;
            });
        const [course, categories] = await Promise.all([
          courseRequest,
          apiFetch("/api/v1/categories", { signal }),
        ]);
        if (signal.aborted) return;
        const categoryState = publicCategoryState(categories);
        if (!isPublicCourse(course, categoryState.visibleIds))
          throw new ApiError(404, "Kurs hazırda əlçatan deyil.");
        const category = categoryState.byId.get(String(course.categoryId));
        container.innerHTML = renderCourseDetails(course || {}, {
          categoryName: category?.name || category?.slug || "Nexora Academy",
        });
        relatedContainer = $("#relatedCourses", container);
        relatedStatus = $("#relatedCoursesStatus", container);
        if (course?.title) document.title = `${course.title} | Nexora Academy`;
        const relatedIds = [
          ...new Set(
            (Array.isArray(course.relatedCourseIds)
              ? course.relatedCourseIds
              : []
            )
              .map(String)
              .filter((id) => id && id !== String(course.id)),
          ),
        ].slice(0, 3);
        if (!relatedContainer || !relatedStatus) return;
        if (!relatedIds.length) {
          relatedStatus.textContent = "Əlaqəli açıq kurs tapılmadı.";
          relatedContainer.innerHTML = "";
          return;
        }
        relatedStatus.textContent = "Əlaqəli kurslar yüklənir…";
        const relatedResults = await Promise.allSettled(
          relatedIds.map((id) =>
            apiFetch(`/api/v1/courses/${encodeURIComponent(id)}`, { signal }),
          ),
        );
        if (signal.aborted) return;
        const relatedCourses = relatedResults
          .filter((result) => result.status === "fulfilled")
          .map((result) => result.value)
          .filter((item) => isPublicCourse(item, categoryState.visibleIds));
        const categoryNames = new Map(
          categoryState.visible.map((category) => [
            String(category.id),
            category.name || category.slug || String(category.id),
          ]),
        );
        relatedContainer.innerHTML = relatedCourses
          .map((item) => renderCourseCard(item, categoryNames))
          .join("");
        relatedStatus.textContent = relatedCourses.length
          ? `${relatedCourses.length} əlaqəli açıq kurs`
          : "Əlaqəli açıq kurs tapılmadı.";
      } catch (error) {
        if (error?.name === "AbortError") return;
        container.innerHTML =
          '<div class="Nexora_emptyState"><h1>Kurs hazırda əlçatan deyil</h1><p>Kataloqa qayıdaraq digər açıq kurslara baxın.</p></div>';
        if (relatedContainer) relatedContainer.innerHTML = "";
        if (relatedStatus) relatedStatus.textContent = "";
      }
    };

    void loadCourse();
  }

  function initApiPage(signal) {
    switch (document.body.dataset.page) {
      case "home":
        void initHomeBanner(signal);
        void initHomeFeaturedCourses(signal);
        break;
      case "courses":
        initCoursesPage(signal);
        break;
      case "categories":
        void initCategoriesPage(signal);
        break;
      case "category":
        void initCategoryPage(signal);
        break;
      case "course-details":
        initCourseDetailsPage(signal);
        break;
      case "faq":
        void initFaqPage(signal);
        break;
      case "haqqimizda":
        void initAcademyPage(signal);
        break;
      case "elaqe":
        void initContactPage(signal);
        break;
      case "scholarships":
        void initScholarshipsPage(signal);
        break;
      default:
        break;
    }
  }

  function initStandaloneTarget() {
    if (IS_LEGACY_ROUTER) return;
    const target = new URLSearchParams(location.search).get("target");
    if (!target) return;
    const node = document.getElementById(target);
    if (!node) return;
    requestAnimationFrame(() =>
      requestAnimationFrame(() => {
        node.scrollIntoView({ behavior: "smooth", block: "center" });
        node.classList.remove("naic-target-flash");
        requestAnimationFrame(() => node.classList.add("naic-target-flash"));
      }),
    );
  }

  const STAT_COUNT_UP_DURATION_MS = 1800;

  function initStatCounters(signal) {
    if (signal?.aborted) return;
    const root = $(".StatisticSection_ai-stats__vVDkK");
    if (!root) return;
    const counters = $$(".StatisticSection_ai-stats__item__count__lVNUN", root);
    if (!counters.length) return;

    const targets = counters.map((counter) => {
      const match = String(counter.textContent || "").match(/^\s*(\d+)\s*(\+)?\s*$/);
      return {
        counter,
        value: match ? parseInt(match[1], 10) : 0,
        suffix: match && match[2] ? "+" : "",
      };
    });

    const run = () => {
      if (
        window.matchMedia &&
        window.matchMedia("(prefers-reduced-motion: reduce)").matches
      ) {
        targets.forEach(({ counter, value, suffix }) => {
          counter.textContent = `${value}${suffix}`;
        });
        return;
      }
      const start = performance.now();
      targets.forEach(({ counter, value, suffix }) => {
        counter.textContent = `0${suffix}`;
      });
      const tick = (now) => {
        const progress = Math.min((now - start) / STAT_COUNT_UP_DURATION_MS, 1);
        const eased = progress >= 1 ? 1 : 1 - Math.pow(1 - progress, 3);
        targets.forEach(({ counter, value, suffix }) => {
          counter.textContent = `${Math.round(eased * value)}${suffix}`;
        });
        if (progress < 1) requestAnimationFrame(tick);
      };
      requestAnimationFrame(tick);
    };

    if (!("IntersectionObserver" in window)) {
      run();
      return;
    }
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return;
          observer.unobserve(entry.target);
          run();
        });
      },
      { threshold: 0.3 },
    );
    observer.observe(root);
  }

  function initFaqAccordion(signal) {
    const root = $(".faq-accordion");
    if (!root) return;
    const items = $$(".faq-item", root);
    if (!items.length) return;
    items.forEach((item) => {
      const question = $(".faq-question", item);
      const answer = $(".faq-answer", item);
      if (!question || !answer) return;
      question.addEventListener(
        "click",
        () => {
          const isOpen = item.classList.contains("is-open");
          items.forEach((other) => {
            const otherQuestion = $(".faq-question", other);
            const otherAnswer = $(".faq-answer", other);
            other.classList.remove("is-open");
            if (otherQuestion)
              otherQuestion.setAttribute("aria-expanded", "false");
            if (otherAnswer) otherAnswer.style.maxHeight = "0px";
          });
          if (!isOpen) {
            item.classList.add("is-open");
            question.setAttribute("aria-expanded", "true");
            answer.style.maxHeight = `${answer.scrollHeight}px`;
          }
        },
        { signal },
      );
    });
  }

  function initPage(signal) {
    applyDataImageFallbacks();
    initStandaloneTarget();
    initHeader(signal);
    initHeroMedia(signal);
    initHeroTypewriter(signal);
    initApplicationForm(signal);
    initSimpleForms(signal);
    initVacancies(signal);
    initSliders(signal);
    initPagination(signal);
    initPhoneInputs(signal);
    initStatCounters(signal);
    initFaqAccordion(signal);
    initApiPage(signal);
  }

  const boot = () => {
    pageController?.abort();
    pageController = new AbortController();
    initPage(pageController.signal);
  };
  if (document.readyState === "loading")
    document.addEventListener("DOMContentLoaded", boot, { once: true });
  else boot();
})();

/* ══════════════════════════════════════════════════════════════
   Nexora AI Chat Widget
   ══════════════════════════════════════════════════════════════ */
(() => {
  "use strict";

  const API_URL = "/api/chat";
  const STORAGE_KEY = "nexora-ai-session-id";
  const MAX_HISTORY = 80;

  let sessionId = getOrCreateSessionId();
  let pending = false;
  let history = [];
  let requestController = null;
  let lastRequest = null;
  let initialized = false;

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

  function $(sel, root) {
    return (root || document).querySelector(sel);
  }

  const elements = {
    fab: $("#chat-fab"),
    widget: $("#chat-widget"),
    close: $("#chat-close"),
    messages: $("#chat-messages"),
    form: $("#chat-form"),
    input: $("#chat-input"),
    send: $("#chat-send"),
  };

  let {
    fab,
    widget,
    close: closeBtn,
    messages: messagesEl,
    form: chatForm,
    input: chatInput,
    send: chatSend,
  } = elements;

  if (!fab || !widget) return;

  function resizeInput() {
    chatInput.style.height = "auto";
    chatInput.style.height = Math.min(chatInput.scrollHeight, 100) + "px";
  }

  function scrollToBottom() {
    requestAnimationFrame(() => {
      messagesEl.scrollTo({ top: messagesEl.scrollHeight, behavior: "smooth" });
    });
  }

  function escapeHtml(text) {
    const d = document.createElement("div");
    d.textContent = text;
    return d.innerHTML;
  }

  function normalizeText(v, fb) {
    return typeof v === "string" ? v : fb || "";
  }

  /* ── Rebind after SPA route change ── */
  function rebindChatWidget() {
    const newFab = $("#chat-fab");
    const newWidget = $("#chat-widget");
    const newClose = $("#chat-close");
    const newForm = $("#chat-form");
    const newInput = $("#chat-input");
    const newSend = $("#chat-send");
    const newMessages = $("#chat-messages");
    if (!newFab || !newWidget) return;

    Object.assign(elements, {
      fab: newFab,
      widget: newWidget,
      close: newClose,
      form: newForm,
      input: newInput,
      send: newSend,
      messages: newMessages,
    });
    fab = elements.fab;
    widget = elements.widget;
    closeBtn = elements.close;
    chatForm = elements.form;
    chatInput = elements.input;
    chatSend = elements.send;
    messagesEl = elements.messages;

    newFab.onclick = () => {
      if (isOpen) closeWidget();
      else openWidget();
    };
    newClose.onclick = closeWidget;
    newForm.onsubmit = (e) => {
      e.preventDefault();
      const v = newInput.value.trim();
      if (!v || pending) return;
      newInput.value = "";
      resizeInput();
      newSend.disabled = true;
      sendMessage(v);
    };
    newInput.oninput = () => {
      resizeInput();
      newSend.disabled = pending || !newInput.value.trim();
    };
    newInput.onkeydown = (e) => {
      if (e.key === "Enter" && !e.shiftKey && !e.isComposing) {
        e.preventDefault();
        newForm.requestSubmit();
      }
    };
  }

  /* ── Open / Close ── */
  let isOpen = false;

  function openWidget() {
    if (isOpen) {
      closeWidget();
      return;
    }
    widget.hidden = false;
    isOpen = true;
    requestAnimationFrame(() => {
      widget.classList.add("open");
      fab.classList.add("chat-fab--active");
      chatInput.focus();
    });
    if (!initialized) {
      initialized = true;
      initConversation();
    }
  }

  function closeWidget() {
    isOpen = false;
    widget.classList.remove("open");
    fab.classList.remove("chat-fab--active");
    setTimeout(() => {
      widget.hidden = true;
    }, 300);
  }

  fab.addEventListener("click", openWidget);
  closeBtn.addEventListener("click", closeWidget);
  widget.addEventListener("click", (e) => {
    if (e.target === widget) closeWidget();
  });

  /* ── Messages ── */
  function addMessage(role, text, response) {
    const wrap = document.createElement("div");
    wrap.className = "chat-msg" + (role === "user" ? " chat-msg--user" : "");

    const avatar = document.createElement("div");
    avatar.className = "chat-msg__avatar";
    avatar.textContent = "✦";

    const body = document.createElement("div");

    const bubble = document.createElement("div");
    bubble.className = "chat-msg__bubble";
    bubble.textContent = normalizeText(text, "No response received.");
    body.appendChild(bubble);

    if (role === "assistant" && response) {
      const actions = response.actions || [];
      if (actions.length) {
        const actionsWrap = document.createElement("div");
        actionsWrap.className = "chat-msg__actions";
        actions.forEach((a) => {
          const label = normalizeText(a?.label).trim();
          const value = normalizeText(a?.value, label).trim();
          if (!label || !value) return;
          const btn = document.createElement("button");
          btn.type = "button";
          btn.className = "chat-action-btn";
          btn.textContent = label;
          btn.addEventListener("click", () => {
            if (!pending) sendMessage(value, label);
          });
          actionsWrap.appendChild(btn);
        });
        body.appendChild(actionsWrap);
      }

      const courses = response.courses || [];
      if (courses.length) {
        const coursesWrap = document.createElement("div");
        coursesWrap.className = "chat-msg__courses";
        courses.forEach((c) => {
          const card = document.createElement("div");
          card.className = "chat-course";
          card.innerHTML =
            '<div class="chat-course__top"><span class="chat-course__category"></span><span class="chat-course__price"></span></div>' +
            '<div class="chat-course__name"></div>' +
            '<div class="chat-course__meta"></div>' +
            '<div class="chat-course__tools"></div>';
          card.querySelector(".chat-course__category").textContent =
            normalizeText(c?.category, "Course");
          card.querySelector(".chat-course__name").textContent = normalizeText(
            c?.name,
            "Nexora course",
          );
          card.querySelector(".chat-course__price").textContent =
            Number.isFinite(c?.price) ? c.price + " AZN" : "";
          const meta = card.querySelector(".chat-course__meta");
          [
            c?.level,
            c?.instructor,
            [c?.schedule?.days, c?.schedule?.time].filter(Boolean).join(" · "),
          ]
            .filter(Boolean)
            .forEach((m) => {
              const s = document.createElement("span");
              s.textContent = m;
              meta.appendChild(s);
            });
          const tools = card.querySelector(".chat-course__tools");
          (Array.isArray(c?.tools) ? c.tools.slice(0, 5) : []).forEach((t) => {
            const tag = document.createElement("span");
            tag.className = "chat-course__tool";
            tag.textContent = t;
            tools.appendChild(tag);
          });
          coursesWrap.appendChild(card);
        });
        body.appendChild(coursesWrap);
      }
    }

    wrap.appendChild(avatar);
    wrap.appendChild(body);
    messagesEl.appendChild(wrap);
    history.push({ role, text: normalizeText(text), response });
    if (history.length > MAX_HISTORY) history = history.slice(-MAX_HISTORY);
    scrollToBottom();
  }

  function addError(text) {
    const banner = document.createElement("div");
    banner.className = "chat-error";
    banner.innerHTML = '<span></span><button type="button">Retry</button>';
    banner.querySelector("span").textContent = text;
    banner.querySelector("button").addEventListener("click", () => {
      banner.remove();
      if (lastRequest)
        sendMessage(lastRequest.value, lastRequest.display, { silent: true });
    });
    messagesEl.appendChild(banner);
    scrollToBottom();
  }

  function showTyping() {
    const el = document.createElement("div");
    el.className = "chat-typing";
    el.id = "chat-active-typing";
    el.innerHTML =
      '<div class="chat-msg__avatar">✦</div><div class="chat-typing__dots"><span></span><span></span><span></span></div>';
    messagesEl.appendChild(el);
    scrollToBottom();
  }

  function hideTyping() {
    const el = document.getElementById("chat-active-typing");
    if (el) el.remove();
  }

  function setPending(v) {
    pending = v;
    chatInput.disabled = v;
    chatSend.disabled = v || !chatInput.value.trim();
    document.querySelectorAll(".chat-action-btn").forEach((b) => {
      b.disabled = v || b.dataset.used === "true";
    });
  }

  /* ── API ── */
  async function requestChat(message) {
    requestController?.abort();
    requestController = new AbortController();
    const tid = setTimeout(() => requestController.abort(), 35000);
    try {
      const res = await fetch(API_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify({ message, sessionId, conversationId: sessionId }),
        signal: requestController.signal,
      });
      if (!res.ok) throw new Error("Server " + res.status);
      const data = await res.json();
      if (!data || typeof data.reply !== "string")
        throw new Error("Invalid response");
      return data;
    } finally {
      clearTimeout(tid);
    }
  }

  async function sendMessage(value, display, opts) {
    const msg = normalizeText(value).trim();
    if (!msg || pending) return;
    lastRequest = { value: msg, display };
    document.querySelectorAll(".chat-action-btn").forEach((b) => {
      b.dataset.used = "true";
      b.disabled = true;
    });
    if (!opts?.silent) addMessage("user", display || msg);
    setPending(true);
    showTyping();
    try {
      const data = await requestChat(msg);
      hideTyping();
      addMessage("assistant", data.reply, data);
      setConnection(data.capture);
    } catch (err) {
      hideTyping();
      const aborted = err?.name === "AbortError";
      addError(
        aborted ? "Timeout. Try again." : "Could not reach the AI. Retry.",
      );
    } finally {
      setPending(false);
      chatInput.focus();
    }
  }

  function setConnection(capture) {
    // placeholder — no connection status in widget version
  }

  async function initConversation() {
    setPending(true);
    showTyping();
    try {
      const data = await requestChat("/start");
      hideTyping();
      addMessage("assistant", data.reply, data);
    } catch (_) {
      hideTyping();
      addError("AI is not responding. Make sure chatbot-api is running.");
    } finally {
      setPending(false);
    }
  }

  function resetChat() {
    requestController?.abort();
    sessionId = createId();
    try {
      localStorage.setItem(STORAGE_KEY, sessionId);
    } catch (_) {}
    history = [];
    lastRequest = null;
    messagesEl.innerHTML = "";
    chatInput.value = "";
    resizeInput();
    initialized = true;
    initConversation();
  }

  /* ── Events ── */
  chatForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const v = chatInput.value.trim();
    if (!v || pending) return;
    chatInput.value = "";
    resizeInput();
    chatSend.disabled = true;
    sendMessage(v);
  });

  chatInput.addEventListener("input", () => {
    resizeInput();
    chatSend.disabled = pending || !chatInput.value.trim();
  });

  chatInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey && !e.isComposing) {
      e.preventDefault();
      chatForm.requestSubmit();
    }
  });

  // New chat button — add to header if exists
  const newChatBtn = $("#chat-new-btn");
  if (newChatBtn) newChatBtn.addEventListener("click", resetChat);

  window.__rebindChatWidget = rebindChatWidget;
})();

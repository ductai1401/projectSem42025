// /client/script/categoryPage.js
(function () {
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

  /* ======================
   * 1) VIEW: Grid/List
   * ====================== */
  const VIEW_KEY = "cat_view";
  const tabGrid = $("#grid-tab");
  const tabList = $("#list-tab");
  const paneGrid = $("#grid");
  const paneList = $("#list");

  // Lấy view từ ?view= hoặc localStorage
  const url = new URL(window.location.href);
  const urlView = url.searchParams.get("view");
  const savedView = localStorage.getItem(VIEW_KEY);
  const currentView = urlView || savedView || "grid";

  function setView(view) {
    // tabs
    if (view === "list") {
      tabList?.classList.add("active");
      tabGrid?.classList.remove("active");
      paneList?.classList.add("show", "active");
      paneGrid?.classList.remove("show", "active");
    } else {
      tabGrid?.classList.add("active");
      tabList?.classList.remove("active");
      paneGrid?.classList.add("show", "active");
      paneList?.classList.remove("show", "active");
    }

    // lưu & cập nhật URL (giữ tham số khác)
    localStorage.setItem(VIEW_KEY, view);
    const u = new URL(window.location.href);
    u.searchParams.set("view", view);
    window.history.replaceState({}, "", u);
  }

  if (tabGrid && tabList) {
    // click tab -> setView
    tabGrid.addEventListener("click", () => setView("grid"));
    tabList.addEventListener("click", () => setView("list"));
    // set ban đầu
    setView(currentView);
  }

  /* ======================
   * 2) Form: Search + Sort
   * ====================== */
  const form = $('form[method="get"][action*="/category/"]') || $('form[method="get"]');
  const inputQ = form?.querySelector('input[name="q"]');
  const inputPage = form?.querySelector('input[name="page"]');
  const selectSort = form?.querySelector('select[name="sort"]');

  function resetToFirstPage() {
    if (inputPage) inputPage.value = 1;
    // đồng thời cập nhật URL hiện tại (nếu user reload/back)
    const u = new URL(window.location.href);
    u.searchParams.set("page", "1");
    window.history.replaceState({}, "", u);
  }

  // Debounce helper
  function debounce(fn, delay = 400) {
    let t;
    return (...args) => {
      clearTimeout(t);
      t = setTimeout(() => fn.apply(null, args), delay);
    };
  }

  if (inputQ && form) {
    inputQ.addEventListener(
      "input",
      debounce(() => {
        resetToFirstPage();
        form.submit();
      }, 450)
    );

    // Enter nhấn => submit nhưng vẫn về page 1
    inputQ.addEventListener("keydown", (e) => {
      if (e.key === "Enter") {
        resetToFirstPage();
      }
    });
  }

  if (selectSort && form) {
    selectSort.addEventListener("change", () => {
      resetToFirstPage();
      form.submit();
    });
  }

  /* =========================================
   * 3) Ảnh: lazy-load + fallback khi lỗi 404
   * ========================================= */
  const FALLBACK_IMG = "/images/no-image.png";

  // Gắn fallback khi ảnh lỗi
  $$("img").forEach((img) => {
    // lazy attribute (HTML5)
    img.setAttribute("loading", "lazy");

    img.addEventListener("error", () => {
      if (!img.dataset.fallback) {
        img.dataset.fallback = "1";
        img.src = FALLBACK_IMG;
        img.alt = img.alt || "no-image";
        img.style.objectFit = img.style.objectFit || "cover";
      }
    });
  });

  // Prefetch đơn giản bằng IntersectionObserver (mượt hơn khi lướt)
  if ("IntersectionObserver" in window) {
    const io = new IntersectionObserver((entries) => {
      entries.forEach((e) => {
        if (e.isIntersecting) {
          const img = e.target;
          // Nếu dùng data-src có thể chuyển qua đây; ở template hiện đã dùng src trực tiếp.
          // Ví dụ:
          // if (img.dataset.src) { img.src = img.dataset.src; img.removeAttribute('data-src'); }
          io.unobserve(img);
        }
      });
    }, { rootMargin: "200px 0px" });

    $$("img[loading='lazy']").forEach((img) => io.observe(img));
  }

  /* ======================
   * 4) Pagination UX
   * ====================== */
  const containerTop = $(".shop-page-layout") || document.body;
  function smoothToTop() {
    const offsetTop = containerTop.getBoundingClientRect().top + window.scrollY - 20;
    window.scrollTo({ top: offsetTop, behavior: "smooth" });
  }

  // Khi click các link phân trang -> cuộn mượt
  $$(".pagination a.page-link").forEach((a) => {
    a.addEventListener("click", () => {
      // giữ view hiện tại lên URL của link
      try {
        const u = new URL(a.href, window.location.origin);
        const current = new URL(window.location.href);
        const v = current.searchParams.get("view") || localStorage.getItem(VIEW_KEY) || "grid";
        u.searchParams.set("view", v);
        a.href = u.toString();
      } catch (_) {}
      smoothToTop();
    });
  });

  /* ==========================================
   * 5) Sidebar category tree (collapse/expand)
   * ========================================== */
  // HTML hiện tại tạo cây <ul><li><a>...</a> [ul con]</li></ul>
  // Ta sẽ thêm nút toggle nếu có con.
  const TREE_STATE_KEY = "cat_tree_state"; // lưu object {catId: true/false}
  const state = (() => {
    try {
      return JSON.parse(localStorage.getItem(TREE_STATE_KEY) || "{}");
    } catch {
      return {};
    }
  })();

  function buildTreeToggles(root) {
    if (!root) return;
    const lis = $$("li", root);
    lis.forEach((li) => {
      const sub = $("ul", li);
      const link = $("a", li);
      if (sub && link) {
        // Lấy categoryId từ href (/category/{id})
        let catId = null;
        try {
          const u = new URL(link.href, window.location.origin);
          // Tùy router: /category/123
          const parts = u.pathname.split("/").filter(Boolean);
          const idPart = parts[parts.length - 1];
          catId = parseInt(idPart, 10);
        } catch (_) {}

        // Tạo nút toggle
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "btn btn-sm btn-link p-0 ms-1";
        btn.setAttribute("aria-label", "Toggle children");
        btn.textContent = state[catId] === false ? "▸" : "▾"; // ▾ mở, ▸ đóng

        // Áp trạng thái ban đầu
        const expanded = state[catId] !== false; // default mở
        sub.style.display = expanded ? "" : "none";

        // Click -> toggle
        btn.addEventListener("click", (e) => {
          e.preventDefault();
          const isOpen = sub.style.display !== "none";
          sub.style.display = isOpen ? "none" : "";
          btn.textContent = isOpen ? "▸" : "▾";
          if (catId != null) {
            state[catId] = !isOpen;
            localStorage.setItem(TREE_STATE_KEY, JSON.stringify(state));
          }
        });

        // chèn nút ngay sau <a>
        link.insertAdjacentElement("afterend", btn);
      }
    });
  }

  const sidebarTreeRoot = document.getElementById("shop-dropdown");
  buildTreeToggles(sidebarTreeRoot);

  /* ===========================
   * 6) Price slider (optional)
   * ===========================
   * Chỉ khởi tạo nếu đã nạp noUiSlider (https://refreshless.com/nouislider/)
   * và có #slider-range + #amount trong DOM.
   */
  (function initPriceSlider() {
    const slider = document.getElementById("slider-range");
    const output = document.getElementById("amount");
    if (!slider || !output) return;
    if (!window.noUiSlider) return; // không có lib -> bỏ qua

    // Lấy min/max từ URL nếu có (vd ?min=100000&max=500000)
    const u = new URL(window.location.href);
    const minQ = parseInt(u.searchParams.get("min") || "0", 10);
    const maxQ = parseInt(u.searchParams.get("max") || "10000000", 10);

    noUiSlider.create(slider, {
      start: [minQ, maxQ],
      connect: true,
      range: {
        min: 0,
        max: 10000000
      },
      step: 10000
    });

    slider.noUiSlider.on("update", (values) => {
      const [v1, v2] = values.map((v) => Math.round(v));
      output.value = new Intl.NumberFormat("vi-VN").format(v1) + " ₫ - " +
                     new Intl.NumberFormat("vi-VN").format(v2) + " ₫";
    });

    // Khi thả chuột -> áp min/max vào URL & submit form:
    slider.noUiSlider.on("change", (values) => {
      const [v1, v2] = values.map((v) => Math.round(v));
      if (form) {
        // thêm hidden inputs (nếu chưa có)
        let minI = form.querySelector('input[name="min"]');
        let maxI = form.querySelector('input[name="max"]');
        if (!minI) {
          minI = document.createElement("input");
          minI.type = "hidden";
          minI.name = "min";
          form.appendChild(minI);
        }
        if (!maxI) {
          maxI = document.createElement("input");
          maxI.type = "hidden";
          maxI.name = "max";
          form.appendChild(maxI);
        }
        minI.value = String(v1);
        maxI.value = String(v2);
        resetToFirstPage();
        form.submit();
      }
    });
  })();

  /* ======================
   * 7) Tiny QoL extras
   * ====================== */
  // Khi back/forward trình duyệt, đảm bảo giữ view đúng
  window.addEventListener("popstate", () => {
    const u = new URL(window.location.href);
    const v = u.searchParams.get("view") || localStorage.getItem(VIEW_KEY) || "grid";
    setView(v);
  });
})();

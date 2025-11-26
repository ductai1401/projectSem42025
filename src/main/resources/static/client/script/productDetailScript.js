
(function() {
	const uploadsBase = '/uploads/';

	// ============ Helpers (chuẩn hoá, query) ============
	const rmAccents = s => (s || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '');
	const norm = s => rmAccents(String(s || '').trim().toLowerCase());
	const $ = (sel, root = document) => root.querySelector(sel);
	const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

	const COLOR_KEYS = ['mau', 'mau sac', 'mau săc', 'màu', 'màu sắc', 'color'];
	const SIZE_KEYS = ['kich thuoc', 'kich co', 'kích thước', 'kích cỡ', 'size'];
	const isColorKey = k => COLOR_KEYS.some(x => norm(k).includes(x));
	const isSizeKey = k => SIZE_KEYS.some(x => norm(k).includes(x));

	// ============ parseColor → tô swatch ============
	const COLOR_MAP = {
		'do': '#ff3b30', 'đỏ': '#ff3b30', 'red': '#ff3b30',
		'xanh': '#007aff', 'xanh duong': '#007aff', 'xanh dương': '#007aff', 'xanh lam': '#007aff', 'blue': '#007aff',
		'xanh la': '#34c759', 'xanh lá': '#34c759', 'green': '#34c759', 'lime': '#00ff00',
		'den': '#000', 'đen': '#000', 'black': '#000',
		'trang': '#fff', 'trắng': '#fff', 'white': '#fff',
		'vang': '#ffcc00', 'vàng': '#ffcc00', 'yellow': '#ffcc00',
		'cam': '#ff9500', 'orange': '#ff9500',
		'hong': '#ff2d55', 'hồng': '#ff2d55', 'pink': '#ff2d55',
		'tim': '#af52de', 'tím': '#af52de', 'purple': '#af52de', 'magenta': '#ff00ff',
		'nau': '#a2845e', 'nâu': '#a2845e', 'brown': '#a2845e', 'maroon': '#800000',
		'xam': '#8e8e93', 'xám': '#8e8e93', 'ghi': '#8e8e93', 'grey': '#8e8e93', 'gray': '#8e8e93',
		'bac': '#c0c0c0', 'bạc': '#c0c0c0', 'silver': '#c0c0c0',
		'gold': '#ffd700', 'be': '#f5f5dc', 'beige': '#f5f5dc', 'navy': '#001f3f', 'teal': '#008080', 'cyan': '#00ffff', 'olive': '#808000'
	};
	function parseColor(raw) {
		if (!raw) return null;
		let s = String(raw).trim();
		if (s.includes('/')) s = s.split('/')[0].trim();
		if (/^#?[0-9a-f]{3}$/i.test(s) || /^#?[0-9a-f]{6}$/i.test(s)) {
			let hex = s.replace('#', '');
			if (hex.length === 3) hex = hex.split('').map(c => c + c).join('');
			return '#' + hex;
		}
		const n = norm(s);
		if (COLOR_MAP[n]) return COLOR_MAP[n];
		const t = document.createElement('span');
		t.style.color = s;
		return t.style.color ? s : null;
	}

	// ============ Đọc variants từ DOM ẩn ============
	function parseNamePayload(raw) {
		const out = {};
		if (!raw) return out;
		const s = String(raw).trim();
		try {
			const obj = JSON.parse(s);
			if (obj && typeof obj === 'object') return obj;
		} catch (_) { }

		s.split(/\||;|,|\//).map(x => x.trim()).filter(Boolean).forEach(p => {
			const m = p.split(':');
			if (m.length >= 2) {
				const key = m[0].trim();
				const val = m.slice(1).join(':').trim();
				if (key && val) out[key] = val;
			}
		});
		return out;
	}

	const variants = $$('#variantData .v-item').map(el => {
		const namePayload = el.getAttribute('data-name');
		const attrs = parseNamePayload(namePayload);
		let color = null, size = null;
		Object.entries(attrs).forEach(([k, v]) => {
			if (isColorKey(k)) color = (v ?? '').toString().trim();
			if (isSizeKey(k)) size = (v ?? '').toString().trim();
		});
		return {
			id: el.dataset.id,
			price: el.dataset.price ? Number(el.dataset.price) : null,
			image: el.dataset.image || null,
			color, size
		};
	});

	const priceEl = $('#priceDisplay');

	// ============ Tô màu swatch ============
	$$('#colorSwatches .colorValue').forEach(el => {
		const val = el.dataset.value || el.getAttribute('title') || el.textContent || '';
		const bg = parseColor(val);
		if (bg) el.style.backgroundColor = bg;
		el.textContent = '';
		el.title = val;
	});

	// ============ State chọn biến thể ============
	let selectedColor = null;
	let selectedSize = null;
	let currentVariant = null;

	function markSelectedVariant(id) {
		$$('#variantData .v-item').forEach(el => {
			if (el.dataset.id == id) el.setAttribute('data-selected', 'true');
			else el.removeAttribute('data-selected');
		});
	}

	function pickVariant() {
		if (!variants.length) return null;
		let list = variants.slice();
		if (selectedColor) list = list.filter(v => norm(v.color) === norm(selectedColor));
		if (selectedSize) list = list.filter(v => norm(v.size) === norm(selectedSize));
		if (list.length) return list[0];

		if (selectedColor) {
			const byColor = variants.filter(v => norm(v.color) === norm(selectedColor));
			if (byColor.length) return byColor[0];
		}
		if (selectedSize) {
			const bySize = variants.filter(v => norm(v.size) === norm(selectedSize));
			if (bySize.length) return bySize[0];
		}
		return variants[0];
	}

	function setPrice(v) {
		if (!priceEl || !v) return;
		if (v.price != null) priceEl.textContent = Number(v.price).toLocaleString('vi-VN');
	}

	function setImage(v) {
		if (!v || !v.image) return;
		const url = v.image.startsWith('http') ? v.image : (uploadsBase + v.image);
		const mainImg =
			$('.modal-gallery-slider .gallery .swiper-wrapper .swiper-slide-active img') ||
			$('.modal-gallery-slider .gallery .swiper-wrapper .swiper-slide img') ||
			$('.modal-gallery-slider .gallery img');
		if (mainImg) mainImg.src = url;

		const firstThumb =
			$('.modal-gallery-slider .gallery-thumbs .swiper-wrapper .swiper-slide img') ||
			$('.modal-gallery-slider .gallery-thumbs img');
		if (firstThumb) firstThumb.src = url;
	}

	// Hidden input (tuỳ chọn)
	const variantIdInput = $('#variantIdInput');

	function applySelection() {
		const v = pickVariant();
		if (!v) return;
		currentVariant = v;
		markSelectedVariant(v.id);
		setPrice(v);
		setImage(v);
		if (variantIdInput) variantIdInput.value = v.id || '';

		// Cho các module khác lắng nghe (flash sale)
		document.dispatchEvent(new CustomEvent('variant:change', { detail: { price: v.price, id: v.id } }));
	}

	// Events chọn Color/Size
	$('#colorSwatches')?.addEventListener('click', e => {
		const el = e.target.closest('.colorValue'); if (!el) return;
		$$('#colorSwatches .colorValue').forEach(x => x.classList.remove('active'));
		el.classList.add('active');
		let val = (el.dataset.value || el.getAttribute('title') || '').trim();
		if (val.includes('/')) val = val.split('/')[0].trim();
		selectedColor = val;
		applySelection();
	});

	$('#sizeOptions')?.addEventListener('click', e => {
		const el = e.target.closest('.sizeValue'); if (!el) return;
		$$('#sizeOptions .sizeValue').forEach(x => x.classList.remove('active'));
		el.classList.add('active');
		let val = (el.dataset.value || el.textContent || '').trim();
		if (val.includes('/')) val = val.split('/')[0].trim();
		selectedSize = val;
		applySelection();
	});

	// Khởi tạo: auto-chọn swatch đầu nếu có, nếu không vẫn chọn biến thể đầu
	(function initFirstSelection() {
		const firstColor = $('#colorSwatches .colorValue');
		if (firstColor) {
			firstColor.classList.add('active');
			let val = (firstColor.dataset.value || firstColor.getAttribute('title') || '').trim();
			if (val.includes('/')) val = val.split('/')[0].trim();
			selectedColor = val;
		}
		const firstSize = $('#sizeOptions .sizeValue');
		if (firstSize) {
			firstSize.classList.add('active');
			let val = (firstSize.dataset.value || firstSize.textContent || '').trim();
			if (val.includes('/')) val = val.split('/')[0].trim();
			selectedSize = val;
		}
		applySelection();

		// Fallback nếu vì lý do nào đó giá chưa hiển thị
		if (priceEl && (!priceEl.textContent.trim() || Number(priceEl.textContent.replace(/[^\d.]/g, '')) === 0)) {
			const min = variants.map(v => v.price).filter(p => p != null && !isNaN(p))
				.reduce((a, b) => Math.min(a, b), Infinity);
			if (isFinite(min)) priceEl.textContent = Number(min).toLocaleString('vi-VN');
		}
	})();

	// ============ Add to Cart ============
	const btnAdd = $('#btnAddToCart');
	if (btnAdd) {
		btnAdd.addEventListener('click', () => {
			const v = pickVariant();
			if (!v || !v.id) { alert('Không tìm thấy biến thể phù hợp!'); return; }
			const variantId = Number(v.id);
			let quantity = parseInt($('#qtyInput')?.value || '1', 10);

			if (window.isLoggedIn) addToCartServer(variantId, quantity);
			else addToCartLocal(variantId, quantity);
		});
	}
	function getCart() {
		let cart = localStorage.getItem("cart");
		return cart ? JSON.parse(cart) : { items: [] };
	}
	function saveCart(cart) { localStorage.setItem("cart", JSON.stringify(cart)); }
	function addToCartLocal(variantId, quantity) {
		let cart = getCart();
		let item = cart.items.find(i => i.variantId === variantId);
		if (item) item.quantity += quantity; else cart.items.push({ variantId, quantity });
		saveCart(cart); alert("Thêm vào giỏ hàng thành công!");
	}
	function addToCartServer(variantId, quantity) {
		fetch("/cart/add", {
			method: "POST", headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ variantId, quantity })
		}).then(r => r.json()).then(d => {
			if (!d.success) { alert(d.message); return; }
			alert(d.message);
		}).catch(console.error);
	}

	// ============ Reviews (AJAX + Lightbox) ============
	const productId = $('#productId')?.value;
	let currentPage = 1, pageSize = 6;
	const elList = $('#rvList'), elAvg = $('#rvAvg'), elTot = $('#rvTotal'),
		elPage = $('#rvPage'), btnPrev = $('#rvPrev'), btnNext = $('#rvNext'),
		elPaging = $('#rvPaging');

	let mediaList = [], mediaIndex = 0;
	const lb = {
		root: $('#rvLightbox'), overlay: $('#rvLbOverlay'), close: $('#rvLbClose'),
		prev: $('#rvLbPrev'), next: $('#rvLbNext'),
		img: $('#rvLbImg'), vid: $('#rvLbVid'), cap: $('#rvLbCap')
	};
	function openLightbox(i) { if (!mediaList.length) return; mediaIndex = Math.max(0, Math.min(i, mediaList.length - 1)); renderLb(); lb.root.classList.remove('hidden'); document.body.classList.add('rv-no-scroll'); }
	function closeLightbox() { lb.root.classList.add('hidden'); document.body.classList.remove('rv-no-scroll'); lb.vid.pause(); }
	function renderLb() {
		const it = mediaList[mediaIndex]; if (!it) return;
		lb.img.classList.add('hidden'); lb.vid.classList.add('hidden'); lb.vid.pause();
		if (it.type === 'video') { lb.vid.src = it.src; lb.vid.classList.remove('hidden'); }
		else { lb.img.src = it.src; lb.img.classList.remove('hidden'); }
		lb.cap.textContent = it.caption || '';
	}
	function nextMedia() { if (!mediaList.length) return; mediaIndex = (mediaIndex + 1) % mediaList.length; renderLb(); }
	function prevMedia() { if (!mediaList.length) return; mediaIndex = (mediaIndex - 1 + mediaList.length) % mediaList.length; renderLb(); }
	lb.close?.addEventListener('click', closeLightbox);
	lb.overlay?.addEventListener('click', closeLightbox);
	lb.next?.addEventListener('click', nextMedia);
	lb.prev?.addEventListener('click', prevMedia);
	document.addEventListener('keydown', e => {
		if (lb.root?.classList.contains('hidden')) return;
		if (e.key === 'Escape') closeLightbox();
		else if (e.key === 'ArrowRight') nextMedia();
		else if (e.key === 'ArrowLeft') prevMedia();
	});

	const starHtml = n => Array.from({ length: Math.max(0, Math.min(5, parseInt(n || 0))) })
		.map(() => '&#9733;').join('');
	const mediaHtml = medias => {
		if (!Array.isArray(medias) || !medias.length) return '';
		return `<div class="rv-medias d-flex flex-wrap gap-2 mt-2">` + medias.map(m => {
			const url = `/uploads/reviews/${m.mediaUrl}`;
			return m.type === 1
				? `<video class="me-2 mb-2 rounded" width="96" height="96" muted playsinline src="${url}"></video>`
				: `<img class="me-2 mb-2 rounded" src="${url}" alt="media">`;
		}).join('') + `</div>`;
	};
	function rebuildMediaList() {
		mediaList = [];
		document.querySelectorAll('#rvList .rv-item').forEach(item => {
			const user = item.querySelector('strong')?.textContent?.trim() || 'Người dùng';
			const date = item.querySelector('small.text-muted')?.textContent?.trim() || '';
			const caption = [user, date].filter(Boolean).join(' • ');
			item.querySelectorAll('.rv-medias img').forEach(img => {
				const src = img.getAttribute('src'); if (src) mediaList.push({ src, type: 'image', caption });
			});
			item.querySelectorAll('.rv-medias video').forEach(v => {
				const src = v.getAttribute('src'); if (src) mediaList.push({ src, type: 'video', caption });
			});
		});
	}
	async function loadReviews(page = 1) {
		if (!productId) return;
		try {
			const res = await fetch(`/productdetails/${productId}/reviews?page=${page}&size=${pageSize}`);
			const json = await res.json();
			const list = json?.data || [], total = json?.total || 0;
			const avg = json?.averageRating != null ? Number(json.averageRating).toFixed(1) : '—';
			if (elAvg) elAvg.textContent = avg;
			if (elTot) elTot.textContent = String(total);
			elList.innerHTML = list.length ? list.map(rv => {
				const name = rv.userName?.trim() || 'Người dùng';
				const date = rv.createdAt ? String(rv.createdAt).replace('T', ' ').substring(0, 16) : '';
				const stars = starHtml(rv.rating);
				const text = rv.reviewText || '';
				return `
          <div class="rv-item pt-3 mt-3">
            <div class="d-flex justify-content-between">
              <strong>${name}</strong>
              <small class="text-muted">${date}</small>
            </div>
            <div class="my-1 rv-stars">${stars}</div>
            <div>${text}</div>
            ${mediaHtml(rv.medias)}
          </div>`;
			}).join('') : `<div class="rv-empty">Chưa có đánh giá cho sản phẩm này.</div>`;
			const totalPages = Math.max(1, Math.ceil(total / pageSize));
			currentPage = Math.min(Math.max(1, page), totalPages);
			if (elPage) elPage.textContent = String(currentPage);
			if (elPaging) {
				if (totalPages > 1) { elPaging.style.display = ''; btnPrev.disabled = currentPage <= 1; btnNext.disabled = currentPage >= totalPages; }
				else elPaging.style.display = 'none';
			}
			rebuildMediaList();
		} catch (e) { console.error('Load reviews error:', e); }
	}
	btnPrev?.addEventListener('click', () => loadReviews(currentPage - 1));
	btnNext?.addEventListener('click', () => loadReviews(currentPage + 1));
	$('#rvList')?.addEventListener('click', e => {
		const t = e.target; if (!(t instanceof Element)) return;
		if (t.matches('.rv-medias img, .rv-medias video')) {
			rebuildMediaList();
			const src = t.getAttribute('src');
			const type = t.tagName.toLowerCase() === 'video' ? 'video' : 'image';
			const idx = mediaList.findIndex(m => m.src === src && m.type === type);
			openLightbox(idx >= 0 ? idx : 0);
		}
	});
	document.addEventListener('DOMContentLoaded', rebuildMediaList);

	// ============ Product Option (JSON) ============
	document.addEventListener('DOMContentLoaded', () => {
		const field = $('#productOptionJson'); if (!field) return;
		try {
			const txt = field.value; if (!txt) return;
			const opts = JSON.parse(txt);
			const ul = $('#productOptionList'); if (!ul) return;
			ul.innerHTML = '';
			for (const [k, v] of Object.entries(opts)) {
				const li = document.createElement('li');
				li.innerHTML = `<span class="key">${k}:</span> <span class="value">${v}</span>`;
				ul.appendChild(li);
			}
		} catch (e) { console.error('Lỗi parse productOption:', e); }
	});
	(function targetFirstVariantOnLoad() {
		if (!variants.length) return;

		// 1) Ưu tiên biến thể có price > 0, nếu không có thì lấy biến thể đầu
		let v = variants.find(x => x.price != null && !isNaN(x.price) && x.price > 0) || variants[0];
		if (!v) return;

		// 2) Nếu UI chưa có active color/size thì sync theo variant đã chọn
		const setActive = (containerSel, itemSel, matchVal) => {
			const container = document.querySelector(containerSel);
			if (!container) return;
			const anyActive = container.querySelector(`${itemSel}.active`);
			if (anyActive) return; // đã có active thì giữ nguyên (tránh “cướp” chọn của server)

			// tìm item có data-value (hoặc text) khớp với matchVal
			const items = Array.from(container.querySelectorAll(itemSel));
			let matched = null;
			if (matchVal) {
				const mv = String(matchVal).trim().toLowerCase();
				matched = items.find(el => {
					const val = (el.dataset.value || el.getAttribute('title') || el.textContent || '').trim().toLowerCase();
					return val === mv || val.split('/')[0].trim() === mv;
				});
			}
			(matched || items[0])?.classList.add('active');
		};

		// 3) Nếu variant có color/size, bật đúng swatch
		if (v.color) setActive('#colorSwatches', '.colorValue', v.color);
		else setActive('#colorSwatches', '.colorValue', null);

		if (v.size) setActive('#sizeOptions', '.sizeValue', v.size);
		else setActive('#sizeOptions', '.sizeValue', null);

		// 4) Cập nhật state chọn để pickVariant() hiểu đúng
		//    (đọc lại active trong UI vì có thể không trùng exactly với v.color/v.size)
		const activeColorEl = document.querySelector('#colorSwatches .colorValue.active');
		const activeSizeEl = document.querySelector('#sizeOptions  .sizeValue.active');

		window.selectedColor = activeColorEl
			? (activeColorEl.dataset.value || activeColorEl.getAttribute('title') || '').split('/')[0].trim()
			: (v.color || null);

		window.selectedSize = activeSizeEl
			? (activeSizeEl.dataset.value || activeSizeEl.textContent || '').split('/')[0].trim()
			: (v.size || null);

		// 5) Gắn data-selected cho đúng .v-item
		markSelectedVariant(v.id);

		// 6) Áp vào UI (giá, ảnh) + bắn sự kiện cho FlashSale
		applySelection();
	})();
	// ============ FLASH SALE (đặt sau khi đã có currentVariant) ============
	const priceDisplay = document.getElementById('priceDisplay');
	const originalEl = document.getElementById('priceOriginal');
	const fsPerc = Number(priceDisplay?.getAttribute('data-fs-percern') || 0);

	// Không có FS → ẩn giá gốc & bỏ gạch
	if (!fsPerc || fsPerc <= 0) {
		if (originalEl) {
			originalEl.style.display = 'none';
			originalEl.style.textDecoration = '';
			originalEl.textContent = '';
		}
		if (priceDisplay) priceDisplay.style.textDecoration = 'none';
		// Kết thúc module FS
		return;
	}

	// Có FS
	const fmt = (n) => {
		try { return new Intl.NumberFormat('vi-VN').format(Math.round(n || 0)); }
		catch { return (Math.round(n || 0)).toString(); }
	};
	const applySale = (base) => {
		const sale = Math.max(0, base * (100 - fsPerc) / 100.0);
		priceDisplay.textContent = fmt(sale);
		priceDisplay.style.textDecoration = 'none';
		if (originalEl) {
			originalEl.textContent = fmt(base);
			originalEl.style.display = '';
			originalEl.style.textDecoration = 'line-through';
			originalEl.style.opacity = '0.7';
		}
	};

	// Áp ngay theo biến thể đang có
	if (currentVariant && currentVariant.price != null) {
		applySale(Number(currentVariant.price));
	} else if (priceDisplay) {
		const base = Number(priceDisplay.textContent.trim().replace(/[^\d.]/g, '')) || 0;
		if (base > 0) applySale(base);
	}

	// Cập nhật khi đổi variant
	document.addEventListener('variant:change', (e) => {
		const vPrice = e?.detail?.price;
		if (vPrice && vPrice > 0) applySale(Number(vPrice));
	});



})();
// === Target 1 variant ngay khi vào trang ===


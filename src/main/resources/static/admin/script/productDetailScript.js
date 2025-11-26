document.addEventListener('DOMContentLoaded', function() {
	/* ========================= BOOTSTRAP MODAL HELPERS ========================= */
	function bsHas5() { return !!(window.bootstrap && window.bootstrap.Modal); }
	function jqHas() { return !!(window.jQuery && jQuery.fn && jQuery.fn.modal); }
	function bsGetInstance(el) {
		try { if (bsHas5() && typeof bootstrap.Modal.getInstance === 'function') return bootstrap.Modal.getInstance(el); } catch (_) { }
		return el.__bsModalInstance || null;
	}
	function bsGetOrCreateInstance(el, opts) {
		try {
			if (bsHas5()) {
				if (typeof bootstrap.Modal.getOrCreateInstance === 'function') return bootstrap.Modal.getOrCreateInstance(el, opts);
				const inst = new bootstrap.Modal(el, opts);
				el.__bsModalInstance = inst;
				return inst;
			}
		} catch (_) { }
		return null;
	}
	function modalShow(el) {
		if (!el) return;
		const inst = bsGetOrCreateInstance(el, {});
		if (inst && typeof inst.show === 'function') { inst.show(); return; }
		if (jqHas()) { jQuery(el).modal('show'); return; }
		console.warn('[modalShow] Không tìm thấy Bootstrap Modal.');
	}
	function modalHide(el) {
		if (!el) return;
		const inst = bsGetInstance(el) || bsGetOrCreateInstance(el, {});
		if (inst && typeof inst.hide === 'function') { inst.hide(); return; }
		if (jqHas()) { jQuery(el).modal('hide'); return; }
		console.warn('[modalHide] Không tìm thấy Bootstrap Modal.');
	}

	/* ================================ COMMON =================================== */
	const CURRENT_URL = window.location.href;
	const PRODUCT_ID = document.getElementById('productIdHidden')?.value?.trim();

	function getCsrfHeaders() {
		const meta = document.querySelector('meta[name="_csrf"]');
		const head = document.querySelector('meta[name="_csrf_header"]');
		if (meta && head) return { [head.content]: meta.content };
		return {};
	}

	/* ============================ GALLERY (UPLOAD) ============================= */
	const gallery = document.getElementById('productGallery');
	const btnAdd = document.getElementById('btnAddPhotos');
	const addPhotosModalEl = document.getElementById('addPhotosModal');

	const modalFileInput = document.getElementById('modalPhotoInput');
	const previewWrap = document.getElementById('previewWrap');
	const btnConfirm = document.getElementById('btnConfirmUpload');
	const uploadStatus = document.getElementById('uploadStatus');

	const MAX_BYTES = 5 * 1024 * 1024; // 5MB
	const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
	const MAX_GALLERY = 6;

	let selectedFiles = [];
	const selectedSigs = new Set();
	const fileSig = (f) => `${f.name}__${f.size}__${f.lastModified}`;

	function currentGalleryCount() {
		return gallery ? gallery.querySelectorAll('img').length : 0;
	}

	function updateAddBtnState() {
		if (!btnAdd) return;
		const remain = MAX_GALLERY - currentGalleryCount();
		const shouldDisable = remain <= 0;
		btnAdd.disabled = shouldDisable;
		btnAdd.classList.toggle('disabled', shouldDisable);
		btnAdd.title = shouldDisable ? 'Thư viện đã đủ 6 ảnh' : '';
	}

	function addFilesDedup(files) {
		const added = [], skipped = [];
		for (const f of files) {
			if (!ALLOWED_TYPES.includes(f.type)) { skipped.push({ file: f, reason: 'Định dạng không hỗ trợ' }); continue; }
			if (f.size > MAX_BYTES) { skipped.push({ file: f, reason: `Vượt quá ${Math.round(MAX_BYTES / 1024 / 1024)}MB` }); continue; }
			const sig = fileSig(f);
			if (selectedSigs.has(sig)) { skipped.push({ file: f, reason: 'Trùng ảnh đã chọn' }); continue; }
			selectedSigs.add(sig); selectedFiles.push(f); added.push(f);
		}
		return { added, skipped };
	}

	function renderPreviews() {
		if (!previewWrap) return;
		previewWrap.innerHTML = '';
		if (!selectedFiles.length) {
			previewWrap.innerHTML = `<div class="col-12 text-muted small">Chưa chọn ảnh nào.</div>`;
			return;
		}
		selectedFiles.forEach((file, idx) => {
			const url = URL.createObjectURL(file);
			const col = document.createElement('div');
			col.className = 'col-4 col-sm-3 col-md-3';
			col.innerHTML = `
        <div class="position-relative border rounded overflow-hidden">
          <img src="${url}" class="w-100" style="height:110px;object-fit:cover;" alt="preview">
          <button type="button" class="btn btn-sm btn-danger position-absolute top-0 end-0 m-1 btn-remove-preview" data-idx="${idx}" title="Xoá ảnh">&times;</button>
          <div class="position-absolute bottom-0 start-0 w-100 bg-dark bg-opacity-50 text-white text-truncate px-1 small">${file.name}</div>
        </div>`;
			previewWrap.appendChild(col);
		});
	}

	// mở modal (nếu còn slot)
	btnAdd?.addEventListener('click', () => {
		const remain = MAX_GALLERY - currentGalleryCount();
		if (remain <= 0) {
			alert('Thư viện đã tối đa 6 ảnh.');
			return;
		}
		selectedFiles = [];
		selectedSigs.clear();
		if (previewWrap) previewWrap.innerHTML = '';
		if (modalFileInput) modalFileInput.value = '';
		if (btnConfirm) btnConfirm.disabled = true;
		if (uploadStatus) uploadStatus.textContent = '';
		modalShow(addPhotosModalEl);
	});

	// chọn file (cắt bớt để không vượt quá 6)
	modalFileInput?.addEventListener('change', () => {
		const remain = MAX_GALLERY - currentGalleryCount() - selectedFiles.length;
		const raw = Array.from(modalFileInput.files || []);
		const pick = remain > 0 ? raw.slice(0, remain) : [];
		const { skipped } = addFilesDedup(pick);

		if (raw.length > pick.length) {
			skipped.push({ file: { name: `+${raw.length - pick.length} ảnh` }, reason: 'Vượt quá giới hạn 6 ảnh' });
		}

		if (skipped.length && uploadStatus) {
			const msg = skipped.slice(0, 3).map(s => `${s.file.name} (${s.reason})`).join(', ');
			uploadStatus.textContent = `Đã bỏ qua: ${msg}${skipped.length > 3 ? '…' : ''}`;
		} else if (uploadStatus) {
			uploadStatus.textContent = '';
		}

		renderPreviews();
		if (btnConfirm) btnConfirm.disabled = selectedFiles.length === 0;
		modalFileInput.value = ''; // cho phép chọn lại
	});

	// bỏ ảnh trước khi upload
	previewWrap?.addEventListener('click', (e) => {
		const btn = e.target.closest('.btn-remove-preview');
		if (!btn) return;
		const idx = Number(btn.getAttribute('data-idx'));
		if (Number.isInteger(idx) && selectedFiles[idx]) {
			const removed = selectedFiles.splice(idx, 1)[0];
			if (removed) selectedSigs.delete(fileSig(removed));
			renderPreviews();
			if (btnConfirm) btnConfirm.disabled = selectedFiles.length === 0;
		}
	});

	// upload ảnh
	btnConfirm?.addEventListener('click', async () => {
		if (!PRODUCT_ID || !selectedFiles.length) return;
		btnConfirm.disabled = true;
		if (uploadStatus) uploadStatus.textContent = `Đang tải ${selectedFiles.length} ảnh...`;

		let okCount = 0;
		for (const file of selectedFiles) {
			const fd = new FormData();
			fd.append('file', file);
			try {
				const res = await fetch(`/admin/product/${encodeURIComponent(PRODUCT_ID)}/gallery`, {
					method: 'POST',
					headers: { ...getCsrfHeaders() }, // cần CSRF nếu bật Spring Security
					body: fd,
					credentials: 'same-origin'
				});
				if (!res.ok) throw new Error(`HTTP ${res.status}`);
				const json = await res.json();
				if (json.ok) {
					appendPhoto(json.imageId, json.url);
					okCount++;
					if (uploadStatus) uploadStatus.textContent = `Đã tải ${okCount}/${selectedFiles.length} ảnh...`;
					updateAddBtnState(); // cập nhật trạng thái nút ngay khi thêm
				} else {
					console.error('Upload failed payload:', json);
				}
			} catch (err) {
				console.error('Upload error:', err);
			}
		}

		modalHide(addPhotosModalEl);
		selectedFiles = []; selectedSigs.clear();
		if (previewWrap) previewWrap.innerHTML = '';
		if (modalFileInput) modalFileInput.value = '';
		if (uploadStatus) uploadStatus.textContent = '';
		updateAddBtnState(); // cập nhật lần nữa
	});

	function appendPhoto(imageId, url) {
		if (!gallery) return;
		const emptyRow = gallery.querySelector('.text-muted');
		if (emptyRow) emptyRow.closest('.col-12')?.remove();

		const col = document.createElement('div');
		col.className = 'col-4 col-sm-3 col-md-4';
		const src = url?.startsWith?.('/uploads') ? url : `/uploads/${url ?? ''}`;
		col.innerHTML = `
      <div class="position-relative border rounded overflow-hidden">
        <img src="${src}" class="w-100" style="height:100px;object-fit:cover;" alt="photo">
        <button type="button" class="btn btn-sm btn-danger position-absolute top-0 end-0 m-1 btn-del-photo" data-id="${imageId}" title="Xoá ảnh">&times;</button>
      </div>`;
		gallery.appendChild(col);

		updateAddBtnState();
	}

	// xoá ảnh (kèm CSRF)
	gallery?.addEventListener('click', async function(e) {
		const btn = e.target.closest('.btn-del-photo');
		if (!btn || !PRODUCT_ID) return;
		const imageId = btn.getAttribute('data-id');
		if (!imageId) return;
		if (!confirm('Xoá ảnh này?')) return;

		try {
			const res = await fetch(`/admin/product/${encodeURIComponent(PRODUCT_ID)}/gallery/${encodeURIComponent(imageId)}`, {
				method: 'DELETE',
				headers: { ...getCsrfHeaders() },
				credentials: 'same-origin'
			});
			if (!res.ok) throw new Error(`HTTP ${res.status}`);
			const json = await res.json();
			if (json.ok) {
				const card = btn.closest('.col-4, .col-sm-3, .col-md-4');
				if (card) card.remove();
				if (!gallery.querySelector('img')) {
					const empty = document.createElement('div');
					empty.className = 'col-12';
					empty.innerHTML = `<div class="text-muted small">Chưa có ảnh</div>`;
					gallery.appendChild(empty);
				}
				updateAddBtnState(); // mở lại nút nếu < 6 ảnh
			} else {
				alert(json.message || 'Xoá ảnh thất bại');
			}
		} catch (err) {
			console.error(err);
			alert('Không thể xoá ảnh.');
		}
	});

	// cập nhật trạng thái nút ngay khi load trang
	updateAddBtnState();

	/* ============================ FLASH SALE (HỦY) ============================= */
	async function cancelFlashSale(productId, returnUrl) {
		if (!productId) return;
		if (!confirm('Bạn chắc chắn muốn gỡ sản phẩm khỏi Flash Sale?')) return;

		const body = new URLSearchParams();
		body.append('_method', 'delete');
		body.append('onlyActive', 'false');

		try {
			const res = await fetch(`/admin/product/${encodeURIComponent(productId)}/flashsale`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8', ...getCsrfHeaders() },
				body: body.toString(),
				credentials: 'same-origin'
			});
			if (res.redirected) { window.location.href = res.url; return; }
			window.location.href = returnUrl || CURRENT_URL;
		} catch (err) {
			alert('Hủy Flash Sale thất bại: ' + (err?.message || err));
		}
	}

	const btnCancelFs = document.getElementById('btnCancelFs');
	if (btnCancelFs && PRODUCT_ID) {
		btnCancelFs.addEventListener('click', () => cancelFlashSale(PRODUCT_ID, CURRENT_URL));
	}
	document.addEventListener('click', function(e) {
		const form = e.target.closest('form.js-cancel-fs-form');
		if (!form) return;
		e.preventDefault();
		const action = form.getAttribute('action') || '';
		const productId = action.split('/').filter(Boolean).pop();
		cancelFlashSale(productId, form.getAttribute('data-return-url') || '/admin/product/');
	});

	/* ============================= FLASH SALE (GẮN) ============================ */
	(function() {
		const fsModalEl = document.getElementById('attachFsModal') || document.getElementById('flashSalePickModal');
		if (!fsModalEl) { console.warn('[FS] Không tìm thấy modal #attachFsModal / #flashSalePickModal'); return; }

		const PRODUCT_ID_LOCAL = document.getElementById('productIdHidden')?.value?.trim() || '';
		const SHOP_ID_FROM_PRODUCT = /*[[${product.shopId}]]*/ null;
		const SHOP_ID_FROM_SESSION = /*[[${session.shopId}]]*/ null;

		let openTick = 0;
		let lastTrigger = null;

		function resolveProductId(triggerEl) {
			const fromBtn = triggerEl?.getAttribute?.('data-product-id');
			const fromHidden = document.getElementById('fsm-productId')?.value;
			const pid = fromBtn || fromHidden || PRODUCT_ID_LOCAL || '';
			if (!pid) console.warn('[FS] Không xác định được productId');
			return pid;
		}

		function resolveShopId(triggerEl) {
			const fromBtn = triggerEl?.getAttribute?.('data-shop-id');
			const fromHidden = document.getElementById('fsm-shopId')?.value;
			const id = Number(fromBtn || fromHidden || SHOP_ID_FROM_PRODUCT || SHOP_ID_FROM_SESSION || 0);
			if (!id) console.warn('[FS] Không xác định được shopId.');
			return Number.isFinite(id) ? id : 0;
		}

		function extractFsArray(payload) {
			if (!payload) return [];
			if (Array.isArray(payload)) return payload;
			if (Array.isArray(payload.data)) return payload.data;
			if (payload.data && Array.isArray(payload.data.data)) return payload.data.data;
			if (Array.isArray(payload.items)) return payload.items;
			if (payload.data && Array.isArray(payload.data.items)) return payload.data.items;
			if (Array.isArray(payload.content)) return payload.content;
			if (payload.data && Array.isArray(payload.data.content)) return payload.data.content;
			if (Array.isArray(payload.list)) return payload.list;
			return [];
		}

		async function fetchFlashSales(shopId) {
			const candidates = [
				`/seller/flashsale/search?shopId=${encodeURIComponent(shopId)}&page=1&size=200`,
				`/admin/flashsale/search?shopId=${encodeURIComponent(shopId)}&page=1&size=200`,
				`/flashsale/search?shopId=${encodeURIComponent(shopId)}&page=1&size=200`,
			];
			for (const url of candidates) {
				try {
					const res = await fetch(url, { credentials: 'same-origin' });
					if (!res.ok) { console.debug('[FS] status', res.status, 'for', url); continue; }
					const json = await res.json();
					const list = extractFsArray(json);
					if (list.length) { return list; }
				} catch (e) {
					console.debug('[FS] fetch error on', url, e);
				}
			}
			return [];
		}

		async function onFsModalShow(triggerEl) {
			const myTick = ++openTick;

			const pid = resolveProductId(triggerEl);
			const shopId = resolveShopId(triggerEl);

			const pidHidden = document.getElementById('fsm-productId');
			const sidHidden = document.getElementById('fsm-shopId');
			const alertEl = document.getElementById('fsm-alert');
			const sel = document.getElementById('fsm-flashSaleId');
			const rangeEl = document.getElementById('fsm-range') || document.getElementById('fsm-fsRange');
			const rangeTop = document.getElementById('fsm-fsRangeTop');
			const qtyEl = document.getElementById('fsm-quantity');
			const pctEl = document.getElementById('fsm-percent');

			if (pidHidden) pidHidden.value = pid || '';
			if (sidHidden) sidHidden.value = shopId || '';

			if (alertEl) { alertEl.classList.add('d-none'); alertEl.textContent = ''; }
			if (sel) sel.innerHTML = '<option value="">-- chọn flash sale --</option>';
			if (rangeEl) rangeEl.textContent = '';
			if (rangeTop) rangeTop.textContent = '';
			if (qtyEl) qtyEl.value = '';
			if (pctEl) pctEl.value = '';

			if (!shopId) {
				if (alertEl) { alertEl.classList.remove('d-none'); alertEl.textContent = 'Không xác định được ShopID.'; }
				return;
			}

			const list = await fetchFlashSales(shopId);
			if (myTick !== openTick) return;

			if (!list.length) {
				if (rangeEl) rangeEl.textContent = 'Shop chưa có Flash Sale nào.';
				return;
			}

			const seen = new Set();
			list.forEach(fs => {
				const id = fs.flashSaleId;
				if (seen.has(id)) return;
				seen.add(id);

				const start = fs.startDate ? String(fs.startDate).replace('T', ' ') : '';
				const end = fs.endDate ? String(fs.endDate).replace('T', ' ') : '';
				const opt = document.createElement('option');
				opt.value = id;
				opt.text = (fs.name || ('FlashSale #' + id)) + ' — ' + start + ' → ' + end;
				opt.dataset.range = start + ' → ' + end;
				sel.appendChild(opt);
			});

			if (sel.options.length > 1) {
				sel.selectedIndex = 1;
				const r = sel.options[sel.selectedIndex]?.dataset?.range || '';
				if (rangeEl) rangeEl.textContent = r;
				if (rangeTop) rangeTop.textContent = r;
			}
		}

		document.querySelectorAll(
			'[data-bs-target="#attachFsModal"],[data-target="#attachFsModal"],' +
			'[data-bs-target="#flashSalePickModal"],[data-target="#flashSalePickModal"]'
		).forEach(btn => {
			btn.addEventListener('click', () => {
				lastTrigger = btn;
				onFsModalShow(lastTrigger);
				setTimeout(() => {
					if (!fsModalEl.classList.contains('show')) modalShow(fsModalEl);
				}, 0);
			});
		});

		if (bsHas5()) {
			fsModalEl.addEventListener('show.bs.modal', (ev) => {
				if (ev.relatedTarget) lastTrigger = ev.relatedTarget;
				onFsModalShow(lastTrigger);
			});
		} else if (jqHas()) {
			jQuery(fsModalEl).on('show.bs.modal', (ev) => {
				if (ev.relatedTarget) lastTrigger = ev.relatedTarget;
				onFsModalShow(lastTrigger);
			});
		}

		const selFs = document.getElementById('fsm-flashSaleId');
		selFs?.addEventListener('change', function() {
			const r = this.options[this.selectedIndex]?.dataset?.range || '';
			const rangeEl = document.getElementById('fsm-range') || document.getElementById('fsm-fsRange');
			const rangeTop = document.getElementById('fsm-fsRangeTop');
			if (rangeEl) rangeEl.textContent = r;
			if (rangeTop) rangeTop.textContent = r;
		});

		const btnAttach = document.getElementById('fsm-attach-btn');
		btnAttach?.addEventListener('click', async function() {
			const flashSaleId = document.getElementById('fsm-flashSaleId')?.value;
			const productId = parseInt(document.getElementById('fsm-productId')?.value, 10);
			const qtyRaw = document.getElementById('fsm-quantity')?.value;
			const pctRaw = document.getElementById('fsm-percent')?.value;
			const alertEl = document.getElementById('fsm-alert');

			const quantity = (qtyRaw === '' || qtyRaw === null) ? 0 : parseInt(qtyRaw, 10);
			const percent = (pctRaw === '' || pctRaw === null) ? 0 : parseInt(pctRaw, 10);

			if (!flashSaleId) { alertEl?.classList.remove('d-none'); if (alertEl) alertEl.textContent = 'Vui lòng chọn Flash Sale.'; return; }
			if (isNaN(quantity) || quantity < 0) { alertEl?.classList.remove('d-none'); if (alertEl) alertEl.textContent = 'Số lượng phải ≥ 0.'; return; }
			if (isNaN(percent) || percent < 0 || percent > 99) { alertEl?.classList.remove('d-none'); if (alertEl) alertEl.textContent = 'Phần trăm giảm phải trong khoảng 0–99.'; return; }

			try {
				const res = await fetch(`/seller/flashsale/${encodeURIComponent(flashSaleId)}/items`, {
					method: 'POST',
					headers: { 'Content-Type': 'application/json', ...getCsrfHeaders() },
					credentials: 'same-origin',
					body: JSON.stringify({ productId, quantity, percern: percent }) // backend dùng "percern"
				});

				let payload = null, raw = '';
				try { payload = await res.clone().json(); } catch { raw = await res.text(); }

				if (!res.ok) {
					const msg = payload?.message || raw || '';
					throw new Error(`HTTP ${res.status} ${res.statusText}${msg ? ' - ' + msg : ''}`);
				}
				if (!payload || payload.ok !== true) {
					const msg = payload?.message || 'Không thể gắn sản phẩm vào Flash Sale.';
					throw new Error(msg);
				}

				modalHide(fsModalEl);
				setTimeout(() => { window.location.reload(); }, 150);
			} catch (err) {
				alertEl?.classList.remove('d-none');
				if (alertEl) alertEl.textContent = String(err?.message || err);
				console.error('Attach FS error:', err);
			}
		});

		window.fsDebug = {
			forceLoad() { onFsModalShow(lastTrigger || null); },
			setTrigger(el) { lastTrigger = el; },
		};
	})();
});

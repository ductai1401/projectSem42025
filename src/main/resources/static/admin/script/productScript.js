(function() {
	// === CSRF helpers ===
	function getCsrf() {
		const inp = document.querySelector('input[name="_csrf"]')
			|| document.querySelector('input[name="__RequestVerificationToken"]');
		return inp ? { name: inp.getAttribute('name'), value: inp.value } : null;
	}

	// ======== Category Picker ========
	const API_ROOTS = '/api/category/roots';
	const API_CHILDREN = '/api/category/children?parentId=';
	const API_SEARCH = '/api/category/search?keyword=';

	const levelsStrip = document.getElementById('levelsStrip');
	const listL1 = document.getElementById('list-L1');
	const breadcrumb = document.getElementById('catBreadcrumb');
	const parentIDInput = document.getElementById('parentID');
	const searchInput = document.getElementById('searchInput');
	const btnSearch = document.getElementById('btnSearch');
	const btnConfirm = document.getElementById('btnConfirm');

	let path = [];

	const fetchJSON = async (url) => {
		const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
		if (!res.ok) throw new Error('HTTP ' + res.status);
		return res.json();
	};
	const setLoading = (ul, on = true) => { ul.innerHTML = on ? '<div class="loading px-2 py-1">Loading…</div>' : ''; };
	const createLi = (it) => {
		const li = document.createElement('li');
		li.className = 'item';
		li.dataset.id = it.categoryId;
		li.dataset.name = it.categoryName;
		if (it.categoryOption) li.dataset.option = it.categoryOption;
		const s = document.createElement('span');
		s.textContent = it.categoryName;
		const c = document.createElement('span');
		c.className = 'chev';
		c.innerHTML = '&rsaquo;';
		li.append(s, c);
		return li;
	};

	const renderList = (ul, items) => {
		ul.innerHTML = '';
		if (!items || !items.length) { ul.innerHTML = '<div class="text-muted small px-2 py-1">No data</div>'; return; }
		items.forEach(it => ul.appendChild(createLi(it)));
	};
	const updateBreadcrumb = () => {
		breadcrumb.innerHTML = '';
		if (!path.length) {
			breadcrumb.innerHTML = '<span class="badge bg-light text-muted border">No parent (top-level)</span>';
			parentIDInput.value = ''; return;
		}
		path.forEach(p => {
			const chip = document.createElement('span');
			chip.className = 'badge bg-primary-subtle text-primary-emphasis border';
			chip.textContent = p.name; breadcrumb.appendChild(chip);
		});
		parentIDInput.value = path[path.length - 1].id;
	};
	const attachClickHandler = (levelIndex) => {
		const ul = document.getElementById(`list-L${levelIndex}`);
		ul.addEventListener('click', async (e) => {
			const li = e.target.closest('.item'); if (!li) return;
			const id = li.dataset.id, name = li.dataset.name;
			path = path.slice(0, levelIndex - 1); path.push({ id, name, level: levelIndex });
			updateBreadcrumb();
			let i = levelIndex + 1, col;
			while ((col = document.getElementById(`col-L${i}`))) { col.remove(); i++; }
			try {
				const children = await fetchJSON(API_CHILDREN + encodeURIComponent(id));
				if (children && children.length) {
					const colId = `col-L${levelIndex + 1}`; let col2 = document.getElementById(colId);
					if (!col2) {
						col2 = document.createElement('div'); col2.className = 'level-col'; col2.id = colId;
						col2.innerHTML = `<div class="picker-panel"><div class="picker-title">Level ${levelIndex + 1}</div><ul class="picker-list" id="list-L${levelIndex + 1}"></ul></div>`;
						levelsStrip.appendChild(col2); attachClickHandler(levelIndex + 1);
					}
					const nextUl = col2.querySelector('ul');
					setLoading(nextUl, true); renderList(nextUl, children);
					levelsStrip.scrollTo({ left: levelsStrip.scrollWidth, behavior: 'smooth' });
					btnConfirm.style.display = 'none';
				} else {
					btnConfirm.style.display = 'inline-block';
				}
			} catch (err) { console.error(err); }
		});
	};
	attachClickHandler(1);
	(async function init() {
		try { setLoading(listL1, true); renderList(listL1, await fetchJSON(API_ROOTS)); }
		catch (e) { listL1.innerHTML = '<div class="text-danger small px-2 py-1">Load failed</div>'; }
	})();
	async function doSearch() {
		const kw = (searchInput.value || '').trim();
		path = []; updateBreadcrumb();
		let i = 2, col; while ((col = document.getElementById(`col-L${i}`))) { col.remove(); i++; }
		try { setLoading(listL1, true); renderList(listL1, kw ? await fetchJSON(API_SEARCH + encodeURIComponent(kw)) : await fetchJSON(API_ROOTS)); }
		catch (e) { console.error(e); listL1.innerHTML = '<div class="text-danger small px-2 py-1">Search failed</div>'; }
	}
	btnSearch.addEventListener('click', doSearch);
	searchInput.addEventListener('keydown', (e) => { if (e.key === 'Enter') { e.preventDefault(); doSearch(); } });

	// ======== Product Option ========
	const productOptionFloating = document.getElementById('productOptionFloating');
	const optionQuad = document.getElementById('optionQuad');
	const productOptionTextarea = document.getElementById('productOption');

	function showOptionQuadFromData(parsedCat) {
		if (!optionQuad) return;
		optionQuad.innerHTML = '';
		optionQuad.style.display = 'flex';
		productOptionFloating.style.display = 'none';

		let oldValues = {};
		try {
			const oldRaw = productOptionTextarea?.value?.trim();
			if (oldRaw) oldValues = JSON.parse(oldRaw);
		} catch (_) { }

		Object.keys(parsedCat).forEach((k, idx) => {
			const label = parsedCat[k];
			const inputId = `opt_dynamic_${idx + 1}`;
			const wrapper = document.createElement('div');
			wrapper.className = 'col-md-6';
			wrapper.innerHTML = `
	            <div class="form-floating">
	                <input type="text" class="form-control" id="${inputId}" 
	                       placeholder="${label}" value="${oldValues[label] ?? ''}">
	                <label for="${inputId}">${label}</label>
	            </div>
	        `;
			optionQuad.appendChild(wrapper);
		});
	}

	btnConfirm.addEventListener('click', () => {
		if (!path.length) return;
		updateBreadcrumb();

		const lastLi = document.querySelector(`#list-L${path[path.length - 1].level} .item[data-id="${path[path.length - 1].id}"]`);
		const rawOpt = lastLi?.dataset.option || null;
		if (rawOpt) {
			try {
				const parsedCat = JSON.parse(rawOpt);
				showOptionQuadFromData(parsedCat);

				let hiddenOpt = document.getElementById('categoryOption');
				if (!hiddenOpt) {
					hiddenOpt = document.createElement('input');
					hiddenOpt.type = 'hidden';
					hiddenOpt.id = 'categoryOption';
					hiddenOpt.name = 'categoryOption';
					document.getElementById('createProductForm').appendChild(hiddenOpt);
				}
				hiddenOpt.value = rawOpt;
			} catch (err) {
				console.error("Parse categoryOption failed", err);
			}
		}
	});

	// ======== QUILL EDITOR ========
	const toolbarOptions = [
		[{ 'header': [1, 2, 3, false] }],
		['bold', 'italic', 'underline', 'strike'],
		[{ 'color': [] }, { 'background': [] }],
		[{ 'list': 'ordered' }, { 'list': 'bullet' }],
		[{ 'align': [] }],
		['link', 'image', 'blockquote', 'code-block'],
		['clean']
	];

	const quill = new Quill('#quillEditor', {
		theme: 'snow',
		placeholder: 'Describe the product...',
		modules: { toolbar: toolbarOptions }
	});

	const hidden = document.getElementById('descriptionInput');
	if (hidden && hidden.value) {
		quill.root.innerHTML = hidden.value;
	}
	function getQuillHtmlOrNull() {
		const plain = quill.getText().trim();
		if (!plain) return null;
		return quill.root.innerHTML;
	}

	// ======== Variants UI & CONFIG ========
	const variantsWrap = document.getElementById('additionalFields');
	const addBtn = document.getElementById('addInputBtn');
	let variantIndex = 0;

	const variantConfigTextarea = document.getElementById('variantConfig');

	let optionBuilderEl = null;
	function ensureOptionBuilder() {
		if (optionBuilderEl) return optionBuilderEl;

		optionBuilderEl = document.createElement('div');
		optionBuilderEl.id = 'optionBuilder';
		optionBuilderEl.className = 'border rounded p-3 mb-3';
		optionBuilderEl.innerHTML = `
      <div class="d-flex align-items-center justify-content-between mb-2">
        <strong class="fs-5">Variant Builder (2 cấp)</strong>
        <div class="d-flex gap-2">
          <button type="button" id="btnClearVariants" class="btn btn-sm btn-outline-danger">Xoá danh sách biến thể đã sinh</button>
          <button type="button" id="btnCloseBuilder" class="btn btn-sm btn-outline-secondary">Đóng</button>
        </div>
      </div>

      <div class="row g-3">
        <div class="col-md-6">
          <div class="picker-panel" style="height:auto;min-height:240px;">
            <div class="picker-title">Variant 1</div>
            <div class="mb-2">
              <label class="form-label small mb-1">Tên biến thể (ví dụ: Màu sắc)</label>
              <input type="text" class="form-control" id="v1_name" placeholder="Tên biến thể cấp 1">
            </div>
            <div class="mb-2 d-flex gap-2">
              <input type="text" class="form-control" id="v1_value_input" placeholder="Giá trị (ví dụ: Hồng)">
              <button type="button" id="v1_add_value" class="btn btn-outline-primary">+</button>
            </div>
            <div id="v1_values" class="d-flex flex-wrap gap-2"></div>
          </div>
        </div>

        <div class="col-md-6">
          <div class="picker-panel" style="height:auto;min-height:240px;">
            <div class="picker-title">Variant 2 (tuỳ chọn)</div>
            <div class="mb-2">
              <label class="form-label small mb-1">Tên biến thể (ví dụ: Size)</label>
              <input type="text" class="form-control" id="v2_name" placeholder="Tên biến thể cấp 2 (có thể để trống)">
            </div>
            <div class="mb-2 d-flex gap-2">
              <input type="text" class="form-control" id="v2_value_input" placeholder="Giá trị (ví dụ: L)">
              <button type="button" id="v2_add_value" class="btn btn-outline-primary">+</button>
            </div>
            <div id="v2_values" class="d-flex flex-wrap gap-2"></div>
          </div>
        </div>
      </div>

      <div class="mt-3 d-flex gap-2">
        <button type="button" id="btnGenerateVariant" class="btn btn-primary">Generate variants</button>
      </div>
      <div class="small text-muted mt-2">
        Tip: Bạn có thể chỉ dùng Variant 1. Nếu không nhập Variant 2, hệ thống sẽ sinh theo 1 chiều.
      </div>
    `;

		variantsWrap.parentNode.insertBefore(optionBuilderEl, variantsWrap);

		const v1Values = optionBuilderEl.querySelector('#v1_values');
		const v2Values = optionBuilderEl.querySelector('#v2_values');
		const v1Input = optionBuilderEl.querySelector('#v1_value_input');
		const v2Input = optionBuilderEl.querySelector('#v2_value_input');

		function addChip(container, text) {
			const val = (text || '').trim();
			if (!val) return;
			const chip = document.createElement('span');
			chip.className = 'badge bg-primary-subtle text-primary-emphasis border';
			chip.style.cursor = 'pointer';
			chip.title = 'Bấm để xoá giá trị';
			chip.textContent = val;
			chip.addEventListener('click', () => chip.remove());
			container.appendChild(chip);
		}

		optionBuilderEl.querySelector('#v1_add_value').addEventListener('click', () => {
			addChip(v1Values, v1Input.value); v1Input.value = '';
		});
		optionBuilderEl.querySelector('#v2_add_value').addEventListener('click', () => {
			addChip(v2Values, v2Input.value); v2Input.value = '';
		});

		optionBuilderEl.querySelector('#btnCloseBuilder').addEventListener('click', () => {
			optionBuilderEl.style.display = 'none';
		});

		optionBuilderEl.querySelector('#btnClearVariants').addEventListener('click', () => {
			document.querySelectorAll('#additionalFields .input-container').forEach(x => x.remove());
		});

		optionBuilderEl.querySelector('#btnGenerateVariant').addEventListener('click', () => {
			const v1Name = (optionBuilderEl.querySelector('#v1_name').value || '').trim();
			const v2Name = (optionBuilderEl.querySelector('#v2_name').value || '').trim();
			const v1List = Array.from(v1Values.querySelectorAll('span')).map(s => s.textContent.trim()).filter(Boolean);
			const v2List = Array.from(v2Values.querySelectorAll('span')).map(s => s.textContent.trim()).filter(Boolean);

			if (!v1Name) { alert('Vui lòng nhập tên Variant 1'); return; }
			if (v1List.length === 0) { alert('Vui lòng thêm ít nhất 1 giá trị cho Variant 1'); return; }

			const variantCfg = { opt1: v1Name, values1: v1List };
			if (v2Name || v2List.length) {
				variantCfg.opt2 = v2Name || '';
				variantCfg.values2 = v2List;
			}
			try { variantConfigTextarea.value = JSON.stringify(variantCfg); } catch (_) { }

			const pairs = [];
			if (v2List.length > 0) {
				v1List.forEach(a => v2List.forEach(b => pairs.push([a, b])));
			} else {
				v1List.forEach(a => pairs.push([a]));
			}

			document.querySelectorAll('#additionalFields .input-container').forEach(x => x.remove());

			pairs.forEach(tuple => {
				const label = (tuple.length === 1)
					? (v1Name ? (v1Name + ': ') : '') + tuple[0]
					: ((v1Name ? (v1Name + ': ') : '') + tuple[0] + ' / ' + (v2Name ? (v2Name + ': ') : '') + tuple[1]);
				createVariantBox(label);
			});
		});

		return optionBuilderEl;
	}

	function createVariantBox(variantLabel) {
		const idx = variantIndex++;
		const box = document.createElement('div');
		box.className = 'input-container mb-3 p-2 border rounded';

		const header = document.createElement('div');
		header.className = 'd-flex justify-content-between align-items-center mb-2';
		header.innerHTML = `<strong>${variantLabel}</strong>`;
		const removeBtn = document.createElement('button');
		removeBtn.type = 'button'; removeBtn.className = 'btn btn-sm btn-outline-danger'; removeBtn.textContent = 'Remove';
		removeBtn.addEventListener('click', () => box.remove());
		header.appendChild(removeBtn);
		box.appendChild(header);

		const row = document.createElement('div'); row.className = 'row g-3';
		const fields = [
			{ key: 'variantName', label: 'Variant Name', placeholder: 'Variant name', value: variantLabel, col: 6 },
			{ key: 'sku', label: 'SKU', placeholder: 'SKU', col: 6 },
			{ key: 'price', label: 'Price', placeholder: 'Price', col: 4 },
			{ key: 'quantity', label: 'Quantity', placeholder: 'Quantity', col: 4 },
			{ key: 'unitCost', label: 'Unit Cost', placeholder: 'Unit cost', col: 4 }
		];
		fields.forEach(f => {
			const col = document.createElement('div'); col.className = `col-md-${f.col || 6}`;
			const inputId = `${f.key}-${idx}`;
			const inputType = (f.key === 'price' || f.key === 'quantity' || f.key === 'unitCost') ? 'number' : 'text';
			const stepAttr = (f.key === 'price' || f.key === 'unitCost') ? ' step="0.01"' : (f.key === 'quantity' ? ' step="1"' : '');
			const minAttr = (f.key === 'quantity' || f.key === 'unitCost' || f.key === 'price') ? ' min="0"' : '';
			col.innerHTML = `
        <div class="form-floating">
          <input type="${inputType}"${stepAttr}${minAttr} class="form-control" name="${f.key}[]" id="${inputId}" placeholder="${f.placeholder}" ${f.value ? `value="${f.value.replace(/"/g, '&quot;')}"` : ''}>
          <label for="${inputId}">${f.label}</label>
        </div>`;
			row.appendChild(col);
		});

		const fileCol = document.createElement('div'); fileCol.className = 'col-md-12';
		const fileId = `variantImage-${idx}`; const previewId = `variantPreview-${idx}`;
		fileCol.innerHTML = `
      <div class="form-floating">
        <input type="file" class="form-control" name="variantImages[]" id="${fileId}" accept="image/*">
        <label for="${fileId}">Choose variant image</label>
      </div>
      <img id="${previewId}" src="#" alt="Image preview"
           style="display:none;max-height:120px;margin-top:8px;object-fit:cover;border:1px solid #eee;border-radius:8px;" />`;
		row.appendChild(fileCol);

		box.appendChild(row);
		variantsWrap.appendChild(box);

		const imageInput = document.getElementById(fileId);
		const preview = document.getElementById(previewId);
		imageInput.addEventListener('change', (e) => {
			const file = e.target.files && e.target.files[0]; if (!file) return;
			const rd = new FileReader(); rd.onload = ev => { preview.src = ev.target.result; preview.style.display = 'block'; };
			rd.readAsDataURL(file);
		});
	}

	addBtn.addEventListener('click', function() {
		const el = ensureOptionBuilder();
		el.style.display = 'block';
		el.scrollIntoView({ behavior: 'smooth', block: 'center' });
	});

	// ======== Progress & Submit ========
	const formEl = document.getElementById('createProductForm');
	const progressBox = document.getElementById('progressBox');
	const progressBar = document.getElementById('progressBar');
	const progressText = document.getElementById('progressText');
	const submitBtn = document.getElementById('bottomSubmit');

	function showProgress(txt, p) {
		if (!progressBox || !progressBar || !progressText) return;
		progressBox.style.display = 'block';
		progressText.textContent = txt || '';
		progressBar.style.width = (p || 0) + '%';
	}
	function hideProgress() {
		if (progressBox) progressBox.style.display = 'none';
		if (progressBar) progressBar.style.width = '0%';
	}

	function collectVariants() {
		const blocks = Array.from(document.querySelectorAll('#additionalFields .input-container'));
		return blocks.map(b => {
			const getNum = (sel) => {
				const v = (b.querySelector(sel)?.value || '').trim();
				if (v === '') return null;
				const n = Number(v);
				return Number.isFinite(n) ? n : null;
			};
			return {
				name: (b.querySelector('[name="variantName[]"]')?.value || '').trim(),
				sku: (b.querySelector('[name="sku[]"]')?.value || '').trim(),
				price: getNum('[name="price[]"]'),
				qty: getNum('[name="quantity[]"]'),
				unitCost: getNum('[name="unitCost[]"]'),
				fileInput: b.querySelector('input[type="file"][name="variantImages[]"]') || null
			};
		});
	}

	async function uploadSingle(url, file, fieldsObj) {
		const fd = new FormData();
		if (file) fd.append('file', file);
		if (fieldsObj) Object.entries(fieldsObj).forEach(([k, v]) => {
			if (v !== undefined && v !== null) fd.append(k, String(v));
		});
		const csrf = getCsrf();
		if (csrf) fd.append(csrf.name, csrf.value);
		const res = await fetch(url, { method: 'POST', body: fd, headers: { 'Accept': 'application/json' } });
		const ct = res.headers.get('content-type') || '';
		if (!ct.includes('application/json')) {
			const txt = await res.text();
			throw new Error(`Unexpected response (status ${res.status}): ${txt.substring(0, 200)}`);
		}
		const js = await res.json();
		if (!res.ok || !js.ok) {
			throw new Error(js.message || `Upload failed (status ${res.status})`);
		}
		return js;
	}

	formEl.addEventListener('submit', async function(ev) {
		ev.preventDefault();
		submitBtn.disabled = true;

		const variants = collectVariants();

		if (optionQuad && optionQuad.style.display !== 'none') {
			try {
				const catOptRaw = document.getElementById('categoryOption')?.value || null;
				let keyMap = {};
				if (catOptRaw) keyMap = JSON.parse(catOptRaw);

				const optObj = {};
				const keys = Object.keys(keyMap);
				keys.forEach((k, idx) => {
					const name = keyMap[k];
					const inputEl = document.getElementById(`opt_dynamic_${idx + 1}`);
					const inputVal = inputEl?.value?.trim() || '';
					if (inputVal) optObj[name] = inputVal;
				});

				productOptionTextarea.value = JSON.stringify(optObj);
			} catch (err) {
				console.error("Build productOption failed", err);
			}
		}

		// ... trước khi tạo payload
		const shopIdEl = document.querySelector('input[name="shopId"]');
		const shopIdVal = shopIdEl && shopIdEl.value ? parseInt(shopIdEl.value, 10) : null;

		// Tạo payload
		const payload = {
			categoryId: document.getElementById('parentID').value
				? parseInt(document.getElementById('parentID').value, 10) : null,
			shopId: Number.isFinite(shopIdVal) ? shopIdVal : null, // để null nếu không hợp lệ → controller fallback
			productName: document.getElementById('productName').value.trim(),
			description: getQuillHtmlOrNull(),
			productOption: document.getElementById('productOption').value || null,
			variantConfig: document.getElementById('variantConfig').value || null,
			status: parseInt(document.getElementById('status').value, 10)
		};


		try {
			showProgress('Đang tạo sản phẩm…', 10);
			const csrf = getCsrf();
			const res = await fetch('/admin/product/create-json', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json',
					'Accept': 'application/json',
					...(csrf ? { 'X-CSRF-TOKEN': csrf.value } : {})
				},
				body: JSON.stringify(payload)
			});
			const js = await res.json();
			if (!js.ok) { hideProgress(); alert(js.message || 'Tạo sản phẩm thất bại'); submitBtn.disabled = false; return; }

			const productId = js.productId;
			if (!productId) { hideProgress(); alert('Không nhận được productId'); submitBtn.disabled = false; return; }

			const cover = document.getElementById('imageFile');
			if (cover && cover.files && cover.files.length > 0) {
				showProgress('Đang upload ảnh sản phẩm…', 25);
				await uploadSingle(`/admin/product/${productId}/image`, cover.files[0]);
			}

			const validVariants = variants.filter(v =>
				v.name || v.sku || v.price !== null || v.qty !== null || v.unitCost !== null || (v.fileInput && v.fileInput.files.length > 0)
			);
			const total = validVariants.length;
			for (let i = 0; i < validVariants.length; i++) {
				const v = validVariants[i];
				const fields = {
					variantName: v.name || '',
					sku: v.sku || '',
					price: v.price != null ? v.price : '',
					quantity: v.qty != null ? v.qty : '',
					logType: 1,
					unitCost: v.unitCost != null ? v.unitCost : ''
				};
				const progress = 25 + Math.floor(((i) / Math.max(total, 1)) * 70);
				showProgress(`Đang tạo biến thể ${i + 1}/${total}…`, progress);
				const file = (v.fileInput && v.fileInput.files && v.fileInput.files[0]) ? v.fileInput.files[0] : null;
				await uploadSingle(`/admin/product/${productId}/variant`, file, fields);
			}

			showProgress('Hoàn tất! Điều hướng về danh sách…', 100);
			window.location.href = '/admin/product/';

		} catch (e) {
			hideProgress();
			alert('Lỗi: ' + e.message);
			submitBtn.disabled = false;
		}
	});

	document.getElementById('btnClearParent').addEventListener('click', async () => {
		path = []; updateBreadcrumb();
		let i = 2, col; while ((col = document.getElementById(`col-L${i}`))) { col.remove(); i++; }
		listL1.innerHTML = '';
		try {
			setLoading(listL1, true);
			const roots = await fetchJSON(API_ROOTS);
			renderList(listL1, roots);
		} catch (err) {
			listL1.innerHTML = '<div class="text-danger small px-2 py-1">Load failed</div>';
		}
		levelsStrip.scrollTo({ left: 0, behavior: 'smooth' });
	});

	const bottomSubmitBtn = document.getElementById('bottomSubmit');
	if (bottomSubmitBtn) {
		bottomSubmitBtn.disabled = false;
		bottomSubmitBtn.addEventListener('click', (ev) => {
			ev.preventDefault();
			formEl.requestSubmit();
		});
	}
})();

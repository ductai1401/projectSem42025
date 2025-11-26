(function() {
  // ======== DOM refs ========
  const formEl = document.getElementById('addVariantsForm');
  const addBtn = document.getElementById('addInputBtn');
  const variantsWrap = document.getElementById('additionalFields');
  const productId = document.getElementById('pv_productId')?.value;
  const variantCfgEl = document.getElementById('variantConfig');
  const errorBox = document.getElementById('pv_errorBox');

  // progress
  const progressBox = document.getElementById('progressBox');
  const progressBar = document.getElementById('progressBar');
  const progressTxt = document.getElementById('progressText');

  function showProgress(txt, p){
    if(!progressBox) return;
    progressBox.style.display = 'block';
    if(progressTxt) progressTxt.textContent = txt || '';
    if(progressBar) progressBar.style.width = (p || 0) + '%';
  }
  function hideProgress(){
    if(progressBox) progressBox.style.display = 'none';
    if(progressBar) progressBar.style.width = '0%';
  }
  function showError(msg){
    if(!errorBox) return;
    errorBox.textContent = msg || 'Đã có lỗi xảy ra.';
    errorBox.classList.remove('d-none');
    setTimeout(()=> errorBox.classList.add('d-none'), 4000);
  }

  // ======== Lock config (SUY RA TỪ BIẾN THỂ HIỆN CÓ – không dùng productOptionPairs) ========
  let LOCK_MODE = false;                 // có biến thể sẵn -> true
  let allowedOpt1Name = null;            // tên V1 (VD: 'Màu sắc')
  let allowedOpt2Name = null;            // tên V2 (VD: 'Size') nếu tồn tại
  const allowedValues1 = new Set();      // giá trị V1 đã có (giữ nguyên nguyên bản)
  const allowedValues2 = new Set();      // giá trị V2 đã có
  const allowedValues1LC = new Set();    // lowercased để so trùng
  const allowedValues2LC = new Set();
  const existingVariantLabels = new Set();// nhãn biến thể đã có (để loại trừ tổ hợp trùng)

  const normVal = (s)=> (s||'').trim().replace(/\s+/g,' ').toLowerCase();
  const normLbl = normVal;

  function parseVariantNameToPairs(label){
    // "Color: Red / Size: L" -> [{key:'Color',value:'Red'},{key:'Size',value:'L'}]
    const out = [];
    if(!label) return out;
    const parts = String(label).split('/').map(p=>p.trim()).filter(Boolean);
    parts.forEach(p=>{
      const idx = p.indexOf(':');
      if(idx >= 0){
        const k = p.slice(0, idx).trim();
        const v = p.slice(idx+1).trim();
        if(k || v) out.push({key:k, value:v});
      }else{
        out.push({key:'', value:p});
      }
    });
    return out;
  }

  function extractFromExistingVariants(variants){
    const keyCount = new Map();     // tên key -> tần suất
    const valSetByKey = new Map();  // key -> Set(values)

    variants.forEach(v=>{
      const name = v?.varianName || v?.name || '';
      existingVariantLabels.add(normLbl(name));
      const pairs = parseVariantNameToPairs(name);
      pairs.forEach(({key,value})=>{
        const k = (key||'').trim();
        const val = (value||'').trim();
        keyCount.set(k, (keyCount.get(k)||0)+1);
        if(!valSetByKey.has(k)) valSetByKey.set(k, new Set());
        if(val) valSetByKey.get(k).add(val);
      });
    });

    const sortedKeys = Array.from(keyCount.entries())
      .sort((a,b)=> b[1]-a[1])
      .map(e=>e[0]);

    if(sortedKeys.length > 0){
      allowedOpt1Name = sortedKeys[0] || allowedOpt1Name;
      (valSetByKey.get(allowedOpt1Name) || new Set())
        .forEach(v => { allowedValues1.add(v); allowedValues1LC.add(normVal(v)); });
    }
    if(sortedKeys.length > 1){
      allowedOpt2Name = sortedKeys[1] || allowedOpt2Name;
      (valSetByKey.get(allowedOpt2Name) || new Set())
        .forEach(v => { allowedValues2.add(v); allowedValues2LC.add(normVal(v)); });
    }
  }

  async function loadExistingAndLockIfNeeded(){
    if(!productId) return;
    try{
      const res = await fetch(`/admin/product/${encodeURIComponent(productId)}`, { headers:{'Accept':'application/json'}});
      if(!res.ok) return;
      const js = await res.json();
      if(!js || js.ok === false) return;

      const variants = Array.isArray(js.variants) ? js.variants : [];
      if(variants.length === 0) return; // không bật lock

      LOCK_MODE = true;                  // có biến thể -> khóa tên option
      extractFromExistingVariants(variants);

      // Nếu không suy ra được opt1 -> bỏ lock để không làm khó người dùng
      if(!allowedOpt1Name){
        LOCK_MODE = false;
      }
    }catch(_){}
  }

  // ======== Variant Builder ========
  let variantIndex = 0;
  let optionBuilderEl = null;

  function makeChoicePills(container, valuesSet){
    container.innerHTML = '';
    const values = Array.from(valuesSet);
    if(values.length === 0){
      container.innerHTML = '<div class="text-muted small">Chưa có giá trị nào.</div>';
      return;
    }
    values.forEach(val=>{
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'btn btn-sm btn-outline-primary me-2 mb-2 selectable';
      btn.dataset.val = val;
      btn.textContent = val;
      btn.addEventListener('click', ()=> btn.classList.toggle('active'));
      container.appendChild(btn);
    });
  }
  function getSelectedPills(container){
    return Array.from(container.querySelectorAll('.selectable.active'))
                .map(b=>b.dataset.val).filter(Boolean);
  }
  function getChipValues(container){
    return Array.from(container.querySelectorAll('[data-chip]'))
                .map(ch=> (ch.textContent||'').trim())
                .filter(Boolean);
  }
  function existsChip(container, valLC){
    return Array.from(container.querySelectorAll('[data-chip]'))
                .some(ch => normVal(ch.textContent) === valLC);
  }
  function addChipUnique(container, text, oldValuesLCSet){
    const raw = (text||'').trim();
    if(!raw) return {ok:false, reason:'empty'};
    const lc = normVal(raw);

    // chặn trùng với dữ liệu cũ trong DB
    if(oldValuesLCSet.has(lc)) return {ok:false, reason:'dupOld'};

    // chặn trùng với chip vừa thêm trong phiên
    if(existsChip(container, lc)) return {ok:false, reason:'dupNew'};

    const chip = document.createElement('span');
    chip.className = 'badge bg-success-subtle text-success-emphasis border';
    chip.style.cursor = 'pointer';
    chip.dataset.chip = '1';
    chip.title = 'Bấm để xoá';
    chip.textContent = raw;
    chip.addEventListener('click', ()=> chip.remove());
    container.appendChild(chip);
    return {ok:true};
  }

  function ensureOptionBuilder(){
    if(optionBuilderEl) return optionBuilderEl;

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
              <label class="form-label small mb-1">Tên biến thể</label>
              <input type="text" class="form-control" id="v1_name" placeholder="VD: Màu sắc">
            </div>
            <div class="mb-2 d-flex gap-2" id="v1_addrow">
              <input type="text" class="form-control" id="v1_value_input" placeholder="Giá trị mới (VD: Hồng)">
              <button type="button" id="v1_add_value" class="btn btn-outline-primary">Thêm</button>
            </div>
            <div class="small text-muted mb-1">Giá trị đã có (chọn để dùng):</div>
            <div id="v1_pills" class="mb-2 d-flex flex-wrap"></div>
            <div class="small text-muted mb-1">Giá trị mới (bạn thêm):</div>
            <div id="v1_values" class="d-flex flex-wrap gap-2"></div>
          </div>
        </div>

        <div class="col-md-6">
          <div class="picker-panel" style="height:auto;min-height:240px;">
            <div class="picker-title">Variant 2 (tuỳ chọn)</div>
            <div class="mb-2">
              <label class="form-label small mb-1">Tên biến thể</label>
              <input type="text" class="form-control" id="v2_name" placeholder="VD: Size">
            </div>
            <div class="mb-2 d-flex gap-2" id="v2_addrow">
              <input type="text" class="form-control" id="v2_value_input" placeholder="Giá trị mới (VD: L)">
              <button type="button" id="v2_add_value" class="btn btn-outline-primary">Thêm</button>
            </div>
            <div class="small text-muted mb-1">Giá trị đã có (chọn để dùng):</div>
            <div id="v2_pills" class="mb-2 d-flex flex-wrap"></div>
            <div class="small text-muted mb-1">Giá trị mới (bạn thêm):</div>
            <div id="v2_values" class="d-flex flex-wrap gap-2"></div>
          </div>
        </div>
      </div>

      <div class="mt-3 d-flex gap-2">
        <button type="button" id="btnGenerateVariant" class="btn btn-primary">Generate variants</button>
      </div>
      <div class="small text-muted mt-2">
        Tip: Có thể chỉ dùng Variant 1. Nếu sản phẩm đang có Variant 2, bạn phải chọn/thêm ít nhất 1 giá trị Variant 2.
      </div>
    `;

    variantsWrap.parentNode.insertBefore(optionBuilderEl, variantsWrap);

    // refs
    const v1NameEl = optionBuilderEl.querySelector('#v1_name');
    const v2NameEl = optionBuilderEl.querySelector('#v2_name');
    const v1AddRow = optionBuilderEl.querySelector('#v1_addrow');
    const v2AddRow = optionBuilderEl.querySelector('#v2_addrow');
    const v1Input  = optionBuilderEl.querySelector('#v1_value_input');
    const v2Input  = optionBuilderEl.querySelector('#v2_value_input');
    const v1Pills  = optionBuilderEl.querySelector('#v1_pills');
    const v2Pills  = optionBuilderEl.querySelector('#v2_pills');
    const v1Values = optionBuilderEl.querySelector('#v1_values');
    const v2Values = optionBuilderEl.querySelector('#v2_values');

    // lock tên option nếu có dữ liệu cũ
    if(LOCK_MODE){
      if(allowedOpt1Name){
        v1NameEl.value = allowedOpt1Name;
        v1NameEl.readOnly = true; v1NameEl.classList.add('bg-light');
      }
      if(allowedOpt2Name){
        v2NameEl.value = allowedOpt2Name;
        v2NameEl.readOnly = true; v2NameEl.classList.add('bg-light');
      }
      // Hiện cả pills (cũ) lẫn input thêm mới (mới),
      // nhưng chặn thêm giá trị trùng với cũ khi bấm 'Thêm'
      makeChoicePills(v1Pills, allowedValues1);
      makeChoicePills(v2Pills, allowedValues2);
    }

    // sự kiện thêm chip (v1/v2) — KHÔNG cho trùng với DB & không trùng với chip hiện có
    optionBuilderEl.querySelector('#v1_add_value').addEventListener('click', ()=>{
      const r = addChipUnique(v1Values, v1Input.value, allowedValues1LC);
      if(!r.ok){
        if(r.reason==='dupOld') alert('Giá trị đã tồn tại, hãy chọn từ phần "Giá trị đã có".');
        else if(r.reason==='dupNew') alert('Giá trị vừa thêm đã có trong danh sách mới.');
      } else {
        v1Input.value = '';
      }
    });
    optionBuilderEl.querySelector('#v2_add_value').addEventListener('click', ()=>{
      const r = addChipUnique(v2Values, v2Input.value, allowedValues2LC);
      if(!r.ok){
        if(r.reason==='dupOld') alert('Giá trị đã tồn tại, hãy chọn từ phần "Giá trị đã có".');
        else if(r.reason==='dupNew') alert('Giá trị vừa thêm đã có trong danh sách mới.');
      } else {
        v2Input.value = '';
      }
    });

    // đóng/xoá
    optionBuilderEl.querySelector('#btnCloseBuilder').addEventListener('click', ()=> optionBuilderEl.style.display='none');
    optionBuilderEl.querySelector('#btnClearVariants').addEventListener('click', ()=>{
      document.querySelectorAll('#additionalFields .input-container').forEach(x=>x.remove());
    });

    // Generate biến thể
    optionBuilderEl.querySelector('#btnGenerateVariant').addEventListener('click', ()=>{
      const v1Name = (v1NameEl.value||'').trim();
      const v2Name = (v2NameEl.value||'').trim();
      if(!v1Name){ alert('Vui lòng nhập tên Variant 1'); return; }

      // tập giá trị chọn dùng: pills (cũ) + chips (mới)
      let list1 = getSelectedPills(v1Pills).concat(getChipValues(v1Values));
      let list2 = getSelectedPills(v2Pills).concat(getChipValues(v2Values));

      // loại trống/dup trong mảng (dup do vừa chọn pill + chip trùng)
      list1 = Array.from(new Set(list1.map(x=>x.trim()).filter(Boolean)));
      list2 = Array.from(new Set(list2.map(x=>x.trim()).filter(Boolean)));

      if(list1.length === 0){
        alert('Vui lòng chọn/thêm ít nhất 1 giá trị cho Variant 1');
        return;
      }
      if(LOCK_MODE && allowedOpt2Name && list2.length === 0){
        alert('Sản phẩm đang có Variant 2, vui lòng chọn/thêm ít nhất 1 giá trị cho Variant 2');
        return;
      }

      // lưu config (tham khảo server)
      const variantCfg = { opt1: v1Name, values1: list1 };
      if(v2Name || list2.length){ variantCfg.opt2 = v2Name || ''; variantCfg.values2 = list2; }
      if(variantCfgEl){ try{ variantCfgEl.value = JSON.stringify(variantCfg) }catch(_){} }

      // tạo tổ hợp
      const pairs = [];
      if(list2.length > 0){
        list1.forEach(a => list2.forEach(b => pairs.push([a,b])));
      }else{
        list1.forEach(a => pairs.push([a]));
      }

      // dọn cũ -> render mới; bỏ qua tổ hợp đã tồn tại
      document.querySelectorAll('#additionalFields .input-container').forEach(x=>x.remove());

      const emitted = new Set(); // tránh dup trong phiên
      pairs.forEach(tuple=>{
        const label = (tuple.length===1)
          ? (v1Name ? (v1Name + ': ') : '') + tuple[0]
          : ((v1Name ? (v1Name + ': ') : '') + tuple[0] + ' / ' + (v2Name ? (v2Name + ': ') : '') + tuple[1]);

        const key = normLbl(label);
        if(existingVariantLabels.has(key)) return; // trùng biến thể cũ -> bỏ
        if(emitted.has(key)) return;               // trùng biến thể vừa sinh -> bỏ
        emitted.add(key);

        createVariantBox(label, /*lockName*/ LOCK_MODE);
      });
    });

    return optionBuilderEl;
  }

  function createVariantBox(variantLabel, lockName){
    const idx = variantIndex++;
    const box = document.createElement('div');
    box.className = 'input-container mb-3 p-2 border rounded';

    const header = document.createElement('div');
    header.className = 'd-flex justify-content-between align-items-center mb-2';
    header.innerHTML = `<strong>${variantLabel}</strong>`;
    const removeBtn = document.createElement('button');
    removeBtn.type='button';
    removeBtn.className='btn btn-sm btn-outline-danger';
    removeBtn.textContent='Remove';
    removeBtn.addEventListener('click', ()=> box.remove());
    header.appendChild(removeBtn);
    box.appendChild(header);

    const row = document.createElement('div'); row.className='row g-3';
    const fields = [
      { key:'variantName', label:'Variant Name', placeholder:'Variant name', value: variantLabel, readOnly: !!lockName },
      { key:'sku',         label:'SKU',          placeholder:'SKU' },
      { key:'price',       label:'Price',        placeholder:'Price' },
      { key:'quantity',    label:'Quantity',     placeholder:'Quantity' }
    ];
    fields.forEach(f=>{
      const col = document.createElement('div'); col.className='col-md-6';
      const inputId = `${f.key}-${idx}`;
      col.innerHTML = `
        <div class="form-floating">
          <input type="text" class="form-control" name="${f.key}[]" id="${inputId}"
                 placeholder="${f.placeholder}" ${f.value?`value="${f.value.replace(/"/g,'&quot;')}"`:''} ${f.readOnly?'readonly':''}>
          <label for="${inputId}">${f.label}</label>
        </div>`;
      row.appendChild(col);
    });

    const fileCol = document.createElement('div'); fileCol.className='col-md-12';
    const fileId = `variantImage-${idx}`; const previewId = `variantPreview-${idx}`;
    fileCol.innerHTML = `
      <div class="form-floating">
        <input type="file" class="form-control" name="variantImages[]" id="${fileId}" accept="image/*">
        <label for="${fileId}">Choose variant image</label>
      </div>
      <img id="${previewId}" alt="Image preview"
           style="display:none;max-height:120px;margin-top:8px;object-fit:cover;border:1px solid #eee;border-radius:8px;" />`;
    row.appendChild(fileCol);

    box.appendChild(row);
    variantsWrap.appendChild(box);

    const imageInput = document.getElementById(fileId);
    const preview = document.getElementById(previewId);
    imageInput.addEventListener('change', (e)=>{
      const file = e.target.files && e.target.files[0];
      if(!file) return;
      const rd = new FileReader();
      rd.onload = ev => { preview.src = ev.target.result; preview.style.display='block'; };
      rd.readAsDataURL(file);
    });
  }

  // mở builder khi bấm nút
  addBtn.addEventListener('click', function(){
    const el = ensureOptionBuilder();
    el.style.display = 'block';
    el.scrollIntoView({ behavior:'smooth', block:'center' });
  });

  // ======== Submit logic (batch upload) ========
  function collectVariants(){
    const blocks = Array.from(document.querySelectorAll('#additionalFields .input-container'));
    return blocks.map(b => ({
      name: (b.querySelector('[name="variantName[]"]')?.value || '').trim(),
      sku:  (b.querySelector('[name="sku[]"]')?.value || '').trim(),
      price: (()=> {
        const raw = (b.querySelector('[name="price[]"]')?.value || '').trim();
        if(raw==='') return null;
        const n = Number(raw); return Number.isFinite(n) ? n : null;
      })(),
      qty: (()=> {
        const raw = (b.querySelector('[name="quantity[]"]')?.value || '').trim();
        if(raw==='') return null;
        const n = Number(raw); return Number.isFinite(n) ? n : null;
      })(),
      fileInput: b.querySelector('input[type="file"][name="variantImages[]"]') || null
    }));
  }

  function getCsrf(){
    const cs = document.querySelector('input[name="_csrf"]');
    return cs ? cs.value : null;
  }

  async function uploadSingleVariant(productId, file, fieldsObj){
    const fd = new FormData();
    if(file) fd.append('file', file);
    if(fieldsObj){
      Object.entries(fieldsObj).forEach(([k,v])=>{
        if(v !== undefined && v !== null) fd.append(k, String(v));
      });
    }
    const headers = {};
    const token = getCsrf();
    if(token) headers['X-CSRF-TOKEN'] = token;

    const res = await fetch(`/admin/product/${encodeURIComponent(productId)}/variant`, {
      method:'POST',
      headers,
      body: fd
    });
    let js;
    try{ js = await res.json(); }catch(_){ throw new Error('Bad JSON response'); }
    if(!js.ok) throw new Error(js.message || 'Upload failed');
    return js;
  }

  // LOCK_MODE chỉ khóa tên option; giá trị có thể là mới/cũ (đã xử lý ở builder).
  // Nhãn variantName input đã readonly khi LOCK_MODE, nên không cần validate bổ sung.

  formEl.addEventListener('submit', async function(ev){
    ev.preventDefault();
    const btn = document.getElementById('btnSaveVariants');
    btn.disabled = true;

    if(!productId){
      showError('Thiếu productId.');
      btn.disabled = false; return;
    }

    const variants = collectVariants().filter(v =>
      v.name || v.sku || v.price !== null || v.qty !== null || (v.fileInput && v.fileInput.files.length > 0)
    );
    if(variants.length === 0){
      showError('Chưa có biến thể nào để lưu.');
      btn.disabled = false; return;
    }

    try{
      const total = variants.length;
      for(let i=0;i<variants.length;i++){
        const v = variants[i];
        const fields = {
          variantName: v.name || '',
          sku: v.sku || '',
          price: v.price != null ? v.price : '',  // server quyết định null/0
          quantity: v.qty != null ? v.qty : 0     // tránh NOT NULL ở DB
        };
        const file = (v.fileInput && v.fileInput.files && v.fileInput.files[0]) ? v.fileInput.files[0] : null;

        const progress = Math.min(5 + Math.floor(((i)/Math.max(total,1))*90), 95);
        showProgress(`Đang tạo biến thể ${i+1}/${total}…`, progress);
        await uploadSingleVariant(productId, file, fields);
      }
      showProgress('Hoàn tất! Điều hướng về chi tiết…', 100);
      window.location.href = `/admin/product/detail/${encodeURIComponent(productId)}`;
    }catch(e){
      hideProgress();
      showError('Lỗi khi lưu biến thể: ' + e.message);
      btn.disabled = false;
    }
  });

  // ===== Init =====
  (async function init(){
    await loadExistingAndLockIfNeeded();
    // mở builder theo yêu cầu người dùng
  })();

})();

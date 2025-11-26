(function(){
  // Page guard: chỉ chạy trên trang có form này
  const form = document.getElementById('createCategoryForm');
  if (!form) return;

  // ===========================
  // API endpoints
  // ===========================
  const API_ROOTS    = '/api/category/roots';
  const API_CHILDREN = '/api/categorychildren?parentId=';
  const API_SEARCH   = '/api/categorysearch?keyword=';

  // ===========================
  // DOM
  // ===========================
  const levelsStrip   = document.getElementById('levelsStrip');
  const listL1        = document.getElementById('list-L1');
  const breadcrumb    = document.getElementById('catBreadcrumb');

  // ĐÃ ĐỔI ID: hidden parent, fields
  const parentIDInput = document.getElementById('catParentID');
  const elName        = document.getElementById('catName');
  const elDesc        = document.getElementById('catDescription');
  const elSort        = document.getElementById('catSortOrder');
  const elStat        = document.getElementById('catStatus');

  // Search: ưu tiên ID mới; fallback nếu HTML chưa đổi
  const searchInput   = document.getElementById('catSearchInput') || document.getElementById('searchInput');
  const btnSearch     = document.getElementById('catBtnSearch')   || document.getElementById('btnSearch');

  // Optional client-side error spans (nếu có trong view)
  const errName = document.getElementById('err-categoryName');
  const errDesc = document.getElementById('err-description');
  const errSort = document.getElementById('err-sortOrder');
  const errStat = document.getElementById('err-status');

  // ===========================
  // State
  // ===========================
  let path = []; // [{id,name,level}]

  // ===========================
  // Helpers (fetch & render)
  // ===========================
  const fetchJSON = async (url) => {
    const res = await fetch(url, { headers: { 'Accept':'application/json' }});
    if (!res.ok) throw new Error('HTTP ' + res.status);
    return res.json();
  };

  const setLoading = (ul, on=true) => {
    ul.innerHTML = on ? '<div class="loading px-2 py-1">Loading…</div>' : '';
  };

  const createLi = (item) => {
    const li = document.createElement('li');
    li.className = 'item';
    li.dataset.id = item.categoryId;
    li.dataset.name = item.categoryName;
    const name = document.createElement('span'); name.textContent = item.categoryName;
    const chev = document.createElement('span'); chev.className='chev'; chev.innerHTML='&rsaquo;';
    li.appendChild(name); li.appendChild(chev);
    return li;
  };

  const renderList = (ul, items) => {
    ul.innerHTML = '';
    if (!items || !items.length){
      const empty = document.createElement('div');
      empty.className='text-muted small px-2 py-1 placeholder';
      empty.textContent='No data';
      ul.appendChild(empty);
      return;
    }
    items.forEach(it => ul.appendChild(createLi(it)));
  };

  const updateBreadcrumb = () => {
    breadcrumb.innerHTML = '';
    if (!path.length){
      const chip = document.createElement('span');
      chip.className = 'badge bg-light text-muted border';
      chip.textContent = 'No parent (top-level)';
      breadcrumb.appendChild(chip);
      if (parentIDInput) parentIDInput.value = '';
      return;
    }
    path.forEach(p => {
      const chip = document.createElement('span');
      chip.className = 'badge bg-primary-subtle text-primary-emphasis border';
      chip.textContent = p.name;
      breadcrumb.appendChild(chip);
    });
    if (parentIDInput) parentIDInput.value = path[path.length - 1].id;
  };

  const markActive = (ul, id) => {
    ul.querySelectorAll('.item').forEach(li => {
      li.classList.toggle('active', li.dataset.id === String(id));
    });
  };

  const scrollStripRight = () => {
    if (!levelsStrip) return;
    levelsStrip.scrollTo({ left: levelsStrip.scrollWidth, behavior: 'smooth' });
  };

  // tạo (hoặc lấy) panel theo levelIndex (1-based); gắn handler click cho panel mới
  const ensureLevelPanel = (levelIndex) => {
    const colId = `col-L${levelIndex}`;
    let col = document.getElementById(colId);
    if (!col){
      col = document.createElement('div');
      col.className = 'level-col';
      col.id = colId;
      col.innerHTML = `
        <div class="picker-panel">
          <div class="picker-title">Level ${levelIndex}</div>
          <ul class="picker-list" id="list-L${levelIndex}"></ul>
        </div>
      `;
      levelsStrip.appendChild(col);
      attachClickHandler(levelIndex); // handler cho UL mới
    }
    col.classList.remove('d-none');
    return col.querySelector('ul');
  };

  // xoá tất cả panel sau levelIndex
  const removePanelsAfter = (levelIndex) => {
    let i = levelIndex + 1, col;
    while ((col = document.getElementById(`col-L${i}`))){
      col.remove();
      i++;
    }
  };

  // gán sự kiện click cho 1 UL level
  const attachClickHandler = (levelIndex) => {
    const ul = document.getElementById(`list-L${levelIndex}`);
    ul.addEventListener('click', async (e) => {
      const li = e.target.closest('.item'); if(!li) return;
      const id = li.dataset.id, name = li.dataset.name;

      // cập nhật path ở đúng level (cắt bỏ các cấp sau)
      path = path.slice(0, levelIndex - 1);
      path.push({ id, name, level: levelIndex });
      updateBreadcrumb();
      markActive(ul, id);

      // xoá panel sau N và load children cho N+1
      removePanelsAfter(levelIndex);
      try{
        const children = await fetchJSON(API_CHILDREN + encodeURIComponent(id));
        if (children && children.length){
          const nextUl = ensureLevelPanel(levelIndex + 1);
          setLoading(nextUl, true);
          renderList(nextUl, children);
          scrollStripRight();
        }
      }catch(err){
        console.error(err);
      }
    });
  };

  // --------- INIT: gắn handler L1 + load roots ---------
  if (listL1) attachClickHandler(1);
  (async function init(){
    if (!listL1) return;
    try{
      setLoading(listL1, true);
      const roots = await fetchJSON(API_ROOTS);
      renderList(listL1, roots);
    }catch(err){
      listL1.innerHTML = '<div class="text-danger small px-2 py-1">Load failed</div>';
      console.error(err);
    }
  })();

  // Clear parent
  const btnClearParent = document.getElementById('btnClearParent');
  if (btnClearParent && listL1){
    btnClearParent.addEventListener('click', async ()=>{
      path = [];
      updateBreadcrumb();
      removePanelsAfter(1);
      listL1.innerHTML = '';
      try{
        setLoading(listL1, true);
        const roots = await fetchJSON(API_ROOTS);
        renderList(listL1, roots);
      }catch(err){
        listL1.innerHTML = '<div class="text-danger small px-2 py-1">Load failed</div>';
      }
      levelsStrip && levelsStrip.scrollTo({ left: 0, behavior: 'smooth' });
    });
  }

  // Search
  async function doSearch(){
    if (!listL1) return;
    const kw = (searchInput?.value || '').trim();
    path = []; updateBreadcrumb(); removePanelsAfter(1);
    try{
      setLoading(listL1, true);
      const data = kw ? await fetchJSON(API_SEARCH + encodeURIComponent(kw))
                      : await fetchJSON(API_ROOTS);
      renderList(listL1, data);
      levelsStrip && levelsStrip.scrollTo({ left: 0, behavior: 'smooth' });
    }catch(err){
      console.error(err);
      listL1.innerHTML = '<div class="text-danger small px-2 py-1">Search failed</div>';
    }
  }
  btnSearch && btnSearch.addEventListener('click', doSearch);
  searchInput && searchInput.addEventListener('keydown', (e)=>{ if(e.key==='Enter'){ e.preventDefault(); doSearch(); }});

  // ===========================
  // Validate form & show inline errors
  // ===========================
  const MAX_NAME = 255;
  const MAX_DESC = 2000;

  const normSpace = (s) => (s||'').trim().replace(/\s+/g,' ');

  function showError(inputEl, errEl, msg){
    if(!inputEl) return;
    inputEl.classList.add('is-invalid');
    if (errEl) errEl.textContent = msg || '';
  }
  function clearError(inputEl, errEl){
    if(!inputEl) return;
    inputEl.classList.remove('is-invalid');
    if (errEl) errEl.textContent = '';
  }

  function validateName(){
    if (!elName) return true;
    const v = normSpace(elName.value);
    if(!v){ showError(elName, errName, 'Tên danh mục là bắt buộc.'); return false; }
    if(v.length > MAX_NAME){ showError(elName, errName, `Tên danh mục tối đa ${MAX_NAME} ký tự.`); return false; }
    clearError(elName, errName); return true;
  }
  function validateDesc(){
    if (!elDesc) return true;
    const v = (elDesc.value || '').trim();
    if(v.length > MAX_DESC){ showError(elDesc, errDesc, `Mô tả tối đa ${MAX_DESC} ký tự.`); return false; }
    clearError(elDesc, errDesc); return true;
  }
  function validateSort(){
    if (!elSort) return true;
    const n = Number(elSort.value);
    if (Number.isNaN(n) || n < 0 || !Number.isInteger(n)){
      showError(elSort, errSort, 'Sort Order phải là số nguyên ≥ 0.'); return false;
    }
    clearError(elSort, errSort); return true;
  }
  function validateStatus(){
    if (!elStat) return true;
    const v = String(elStat.value);
    if(v !== '0' && v !== '1'){ showError(elStat, errStat, 'Trạng thái không hợp lệ.'); return false; }
    clearError(elStat, errStat); return true;
  }

  elName && elName.addEventListener('input', validateName);
  elDesc && elDesc.addEventListener('input', validateDesc);
  elSort && elSort.addEventListener('input', validateSort);
  elStat && elStat.addEventListener('change', validateStatus);

  form.addEventListener('submit', function(e){
    const okName = validateName();
    const okDesc = validateDesc();
    const okSort = validateSort();
    const okStat = validateStatus();

    const allOk = okName && okDesc && okSort && okStat;
    if(!allOk){
      e.preventDefault();
      const firstInvalid = document.querySelector('.is-invalid');
      if(firstInvalid){
        firstInvalid.scrollIntoView({behavior:'smooth', block:'center'});
        firstInvalid.focus({preventScroll:true});
      }
    }
  });
})();

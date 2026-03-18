/**
 * 주문 관리 시스템 - 번호 기반 페이지네이션
 * REST API를 호출하여 DOM을 직접 조작
 */

const API_BASE = '/api/orders';

/* ──────────────────────────────────────────────
   상태
   ────────────────────────────────────────────── */
let currentPage = 1;

/* ──────────────────────────────────────────────
   초기화
   ────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
    loadCount();
    loadVersion();
    loadOrders();

    document.getElementById('searchBtn').addEventListener('click', () => {
        currentPage = 1;
        loadOrders();
    });

    document.getElementById('resetBtn').addEventListener('click', () => {
        document.getElementById('customerName').value = '';
        document.getElementById('status').value = '';
        document.getElementById('size').value = '50';
        currentPage = 1;
        loadOrders();
    });

    document.getElementById('prevBlockBtn').addEventListener('click', goPrevBlock);
    document.getElementById('nextBlockBtn').addEventListener('click', goNextBlock);
});

/* ──────────────────────────────────────────────
   API 호출
   ────────────────────────────────────────────── */
async function loadCount() {
    try {
        const res = await fetch(`${API_BASE}/count`);
        const body = await res.json();
        document.getElementById('totalCount').textContent = body.data.count.toLocaleString();
    } catch (e) {
        console.error('count 조회 실패', e);
    }
}

async function loadVersion() {
    try {
        const res = await fetch('/api/version');
        const body = await res.json();
        document.getElementById('appVersion').textContent = body.data.version;
    } catch (e) {
        console.error('버전 조회 실패', e);
    }
}

async function loadOrders() {
    const tbody = document.getElementById('orderBody');
    tbody.innerHTML = '<tr><td colspan="8" class="loading">불러오는 중...</td></tr>';

    const params = buildParams();

    try {
        const res = await fetch(`${API_BASE}?${params}`);
        const body = await res.json();
        if (!body.success) throw new Error(body.message);
        renderTable(body.data);
        renderPaging(body.data);
    } catch (e) {
        tbody.innerHTML = '<tr><td colspan="8" class="loading">데이터를 불러올 수 없습니다.</td></tr>';
        console.error('주문 조회 실패', e);
    }
}

function buildParams() {
    const size = document.getElementById('size').value || '50';
    const customerName = document.getElementById('customerName').value.trim();
    const status = document.getElementById('status').value;

    const p = new URLSearchParams();
    p.set('page', currentPage);
    p.set('size', size);
    if (customerName) p.set('customerName', customerName);
    if (status) p.set('status', status);
    return p.toString();
}

/* ──────────────────────────────────────────────
   테이블 렌더링
   ────────────────────────────────────────────── */
function renderTable(page) {
    const tbody = document.getElementById('orderBody');
    const rows = page.content;

    if (!rows || rows.length === 0) {
        tbody.innerHTML = '<tr class="empty-row"><td colspan="8">조회된 주문이 없습니다.</td></tr>';
        return;
    }

    tbody.innerHTML = rows.map(o => `
        <tr>
            <td><a href="/detail.html?id=${o.id}">${o.id}</a></td>
            <td>${esc(o.customerName)}</td>
            <td>${esc(o.productName)}</td>
            <td>${o.quantity}</td>
            <td class="amount">${num(o.price)}원</td>
            <td class="amount">${num(o.totalAmount)}원</td>
            <td><span class="status status-${o.status}">${o.status}</span></td>
            <td>${fmtDate(o.createdAt)}</td>
        </tr>
    `).join('');
}

/* ──────────────────────────────────────────────
   페이지 번호 네비게이션 렌더링
   ────────────────────────────────────────────── */
function renderPaging(page) {
    const container = document.getElementById('pageNumbers');
    const info = document.getElementById('pageInfo');
    const prevBtn = document.getElementById('prevBlockBtn');
    const nextBtn = document.getElementById('nextBlockBtn');

    container.innerHTML = '';

    if (page.totalPages <= 0) {
        info.textContent = '';
        prevBtn.style.display = 'none';
        nextBtn.style.display = 'none';
        return;
    }

    // 페이지 번호 버튼 생성 (startPage ~ endPage)
    for (let i = page.startPage; i <= page.endPage; i++) {
        const btn = document.createElement('button');
        btn.textContent = i;
        btn.className = 'page-btn' + (i === page.page ? ' active' : '');
        btn.addEventListener('click', () => goToPage(i));
        container.appendChild(btn);
    }

    // 이전/다음 블록 버튼
    prevBtn.style.display = page.hasPrevious ? '' : 'none';
    nextBtn.style.display = page.hasNext ? '' : 'none';

    // 페이지 정보
    info.textContent = `${page.page} / ${page.totalPages} 페이지 (총 ${page.totalElements.toLocaleString()}건)`;
}

/* ──────────────────────────────────────────────
   페이지 이동
   ────────────────────────────────────────────── */
function goToPage(page) {
    currentPage = page;
    loadOrders();
}

function goPrevBlock() {
    const pageNums = document.getElementById('pageNumbers');
    const firstBtn = pageNums.querySelector('.page-btn');
    if (firstBtn) {
        currentPage = parseInt(firstBtn.textContent) - 1;
        if (currentPage < 1) currentPage = 1;
        loadOrders();
    }
}

function goNextBlock() {
    const pageNums = document.getElementById('pageNumbers');
    const buttons = pageNums.querySelectorAll('.page-btn');
    if (buttons.length > 0) {
        currentPage = parseInt(buttons[buttons.length - 1].textContent) + 1;
        loadOrders();
    }
}

/* ──────────────────────────────────────────────
   유틸
   ────────────────────────────────────────────── */
function num(v) {
    return v == null ? '0' : Number(v).toLocaleString();
}

function fmtDate(arr) {
    if (!arr) return '-';
    if (Array.isArray(arr)) {
        const [y, mo, d, h = 0, mi = 0] = arr;
        return `${y}-${pad(mo)}-${pad(d)} ${pad(h)}:${pad(mi)}`;
    }
    const dt = new Date(arr);
    if (isNaN(dt)) return String(arr);
    return `${dt.getFullYear()}-${pad(dt.getMonth()+1)}-${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}`;
}

function pad(n) { return String(n).padStart(2, '0'); }

function esc(s) {
    if (!s) return '';
    const d = document.createElement('div');
    d.textContent = s;
    return d.innerHTML;
}

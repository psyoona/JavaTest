/**
 * 주문 관리 시스템 - 클라이언트 렌더링
 * REST API를 호출하여 DOM을 직접 조작
 */

const API_BASE = '/api/orders';

/* ──────────────────────────────────────────────
   상태
   ────────────────────────────────────────────── */
let currentCursor = null;
let cursorHistory = []; // 이전 페이지 커서 기록

/* ──────────────────────────────────────────────
   초기화
   ────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', () => {
    loadCount();
    loadOrders();

    document.getElementById('searchBtn').addEventListener('click', () => {
        currentCursor = null;
        cursorHistory = [];
        loadOrders();
    });

    document.getElementById('resetBtn').addEventListener('click', () => {
        document.getElementById('customerName').value = '';
        document.getElementById('status').value = '';
        document.getElementById('size').value = '50';
        currentCursor = null;
        cursorHistory = [];
        loadOrders();
    });

    document.getElementById('nextBtn').addEventListener('click', goNext);
    document.getElementById('firstBtn').addEventListener('click', goFirst);
});

/* ──────────────────────────────────────────────
   API 호출
   ────────────────────────────────────────────── */
async function loadCount() {
    try {
        const res = await fetch(`${API_BASE}/count`);
        const data = await res.json();
        document.getElementById('totalCount').textContent = data.count.toLocaleString();
    } catch (e) {
        console.error('count 조회 실패', e);
    }
}

async function loadOrders() {
    const tbody = document.getElementById('orderBody');
    tbody.innerHTML = '<tr><td colspan="8" class="loading">불러오는 중...</td></tr>';

    const params = buildParams();

    try {
        const res = await fetch(`${API_BASE}?${params}`);
        const page = await res.json();
        renderTable(page);
        renderPaging(page);
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
    p.set('size', size);
    if (currentCursor !== null) p.set('cursor', currentCursor);
    if (customerName) p.set('customerName', customerName);
    if (status) p.set('status', status);
    return p.toString();
}

/* ──────────────────────────────────────────────
   렌더링
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

function renderPaging(page) {
    const info = document.getElementById('rangeInfo');
    const nextBtn = document.getElementById('nextBtn');
    const rows = page.content;

    if (rows && rows.length > 0) {
        info.textContent = `현재 범위: ID ${rows[0].id} ~ ${rows[rows.length - 1].id}`;
    } else {
        info.textContent = '';
    }

    if (page.hasNext) {
        nextBtn.className = 'btn btn-primary';
        nextBtn.dataset.cursor = page.nextCursor;
    } else {
        nextBtn.className = 'btn btn-disabled';
        nextBtn.dataset.cursor = '';
    }
}

/* ──────────────────────────────────────────────
   페이징 이동
   ────────────────────────────────────────────── */
function goNext() {
    const next = document.getElementById('nextBtn').dataset.cursor;
    if (!next) return;
    if (currentCursor !== null) cursorHistory.push(currentCursor);
    currentCursor = Number(next);
    loadOrders();
}

function goFirst() {
    currentCursor = null;
    cursorHistory = [];
    loadOrders();
}

/* ──────────────────────────────────────────────
   유틸
   ────────────────────────────────────────────── */
function num(v) {
    return v == null ? '0' : Number(v).toLocaleString();
}

function fmtDate(arr) {
    if (!arr) return '-';
    // Spring의 LocalDateTime은 JSON 배열 [y,m,d,h,m,s] 또는 ISO 문자열로 올 수 있음
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

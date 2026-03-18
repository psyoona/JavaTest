/**
 * 주문 상세 페이지 JS
 */
const API_BASE = '/api/orders';

document.addEventListener('DOMContentLoaded', () => {
    const params = new URLSearchParams(window.location.search);
    const id = params.get('id');
    if (!id) {
        showError('주문 ID가 지정되지 않았습니다.');
        return;
    }
    loadOrder(id);
});

async function loadOrder(id) {
    try {
        const res = await fetch(`${API_BASE}/${encodeURIComponent(id)}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const body = await res.json();
        if (!body.success) throw new Error(body.message);
        render(body.data);
    } catch (e) {
        showError('주문을 불러올 수 없습니다.');
        console.error(e);
    }
}

function render(o) {
    document.getElementById('detailContent').innerHTML = `
        <div class="field"><div class="field-label">주문 ID</div><div class="field-value">${o.id}</div></div>
        <div class="field"><div class="field-label">고객명</div><div class="field-value">${esc(o.customerName)}</div></div>
        <div class="field"><div class="field-label">상품명</div><div class="field-value">${esc(o.productName)}</div></div>
        <div class="field"><div class="field-label">수량</div><div class="field-value">${o.quantity}</div></div>
        <div class="field"><div class="field-label">단가</div><div class="field-value">${num(o.price)}원</div></div>
        <div class="field"><div class="field-label">총액</div><div class="field-value">${num(o.totalAmount)}원</div></div>
        <div class="field"><div class="field-label">주문 상태</div><div class="field-value"><span class="status status-${o.status}">${o.status}</span></div></div>
        <div class="field"><div class="field-label">주문일시</div><div class="field-value">${fmtDate(o.createdAt)}</div></div>
    `;
}

function showError(msg) {
    document.getElementById('detailContent').innerHTML = `<p style="text-align:center;padding:40px;color:#e74c3c;">${esc(msg)}</p>`;
}

function num(v) {
    return v == null ? '0' : Number(v).toLocaleString();
}

function fmtDate(arr) {
    if (!arr) return '-';
    if (Array.isArray(arr)) {
        const [y, mo, d, h = 0, mi = 0, s = 0] = arr;
        return `${y}-${pad(mo)}-${pad(d)} ${pad(h)}:${pad(mi)}:${pad(s)}`;
    }
    const dt = new Date(arr);
    if (isNaN(dt)) return String(arr);
    return `${dt.getFullYear()}-${pad(dt.getMonth()+1)}-${pad(dt.getDate())} ${pad(dt.getHours())}:${pad(dt.getMinutes())}:${pad(dt.getSeconds())}`;
}

function pad(n) { return String(n).padStart(2, '0'); }

function esc(s) {
    if (!s) return '';
    const d = document.createElement('div');
    d.textContent = s;
    return d.innerHTML;
}
